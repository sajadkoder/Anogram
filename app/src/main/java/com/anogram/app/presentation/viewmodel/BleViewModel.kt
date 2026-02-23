package com.anogram.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anogram.app.data.bluetooth.BleManager
import com.anogram.app.data.bluetooth.BleMessage
import com.anogram.app.data.bluetooth.BlePeer
import com.anogram.app.domain.model.Chat
import com.anogram.app.domain.model.Message
import com.anogram.app.domain.model.MessageStatus
import com.anogram.app.domain.repository.ChatRepository
import com.anogram.app.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class BleState(
    val isBluetoothEnabled: Boolean = false,
    val isScanning: Boolean = false,
    val discoveredPeers: List<BlePeer> = emptyList(),
    val connectedPeers: Set<String> = emptySet(),
    val lastReceivedMessage: BleMessage? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class BleViewModel @Inject constructor(
    private val bleManager: BleManager,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BleState())
    val state: StateFlow<BleState> = _state.asStateFlow()

    val isScanning = bleManager.isScanning.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val discoveredPeers = bleManager.discoveredPeers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val isBluetoothEnabled = bleManager.isBluetoothEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val connectedPeers = bleManager.connectedPeers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val deviceId = UUID.randomUUID().toString().take(8)

    init {
        bleManager.setDeviceInfo(deviceId, "AnoGram")
        
        viewModelScope.launch {
            bleManager.lastMessage.collect { bleMessage ->
                bleMessage?.let { handleIncomingMessage(it) }
            }
        }

        viewModelScope.launch {
            bleManager.isScanning.collect { scanning ->
                _state.value = _state.value.copy(isScanning = scanning)
            }
        }

        viewModelScope.launch {
            bleManager.isBluetoothEnabled.collect { enabled ->
                _state.value = _state.value.copy(isBluetoothEnabled = enabled)
            }
        }
    }

    fun refreshBluetoothState() {
        bleManager.updateBluetoothState()
    }

    fun startScan() {
        if (!bleManager.isBluetoothSupported()) {
            _state.value = _state.value.copy(errorMessage = "Bluetooth not supported")
            return
        }
        bleManager.startScan()
    }

    fun stopScan() {
        bleManager.stopScan()
    }

    fun sendMessage(chatId: Long, content: String) {
        viewModelScope.launch {
            val message = Message(
                chatId = chatId,
                content = content,
                timestamp = System.currentTimeMillis(),
                isOutgoing = true,
                status = MessageStatus.SENDING
            )
            messageRepository.insertMessage(message)
            
            val bleMessage = BleMessage(
                senderId = deviceId,
                senderName = "Me",
                content = content
            )
            
            chatRepository.updateLastMessage(chatId, content, message.timestamp)

            messageRepository.updateMessageStatus(message.id, MessageStatus.SENT)
        }
    }

    private suspend fun handleIncomingMessage(bleMessage: BleMessage) {
        val chats = chatRepository.getAllChats()
        chats.collect { chatList ->
            val bleChat = chatList.find { it.name == "BLE Mesh" }
            
            if (bleChat != null) {
                val message = Message(
                    chatId = bleChat.id,
                    content = "[BLE] ${bleMessage.senderName}: ${bleMessage.content}",
                    timestamp = bleMessage.timestamp,
                    isOutgoing = false,
                    status = MessageStatus.DELIVERED
                )
                messageRepository.insertMessage(message)
                chatRepository.updateLastMessage(bleChat.id, bleMessage.content, bleMessage.timestamp)
            }
            
            bleManager.markMessageDelivered(bleMessage.id)
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
