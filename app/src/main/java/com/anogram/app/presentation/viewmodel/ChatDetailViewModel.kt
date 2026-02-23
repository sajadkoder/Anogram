package com.anogram.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

data class ChatDetailState(
    val isLoading: Boolean = true,
    val messageText: String = "",
    val isSending: Boolean = false,
    val replyToMessage: Message? = null
)

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val chatId: Long = savedStateHandle.get<Long>("chatId") ?: 0L

    private val _state = MutableStateFlow(ChatDetailState())
    val state: StateFlow<ChatDetailState> = _state.asStateFlow()

    val chat: StateFlow<Chat?> = chatRepository.getChatById(chatId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val messages: StateFlow<List<Message>> = messageRepository.getMessagesForChat(chatId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            chat.collect {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
        viewModelScope.launch {
            messages.collect {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
        viewModelScope.launch {
            chatRepository.clearUnreadCount(chatId)
        }
    }

    fun onMessageTextChange(text: String) {
        _state.value = _state.value.copy(messageText = text)
    }

    fun sendMessage() {
        val text = _state.value.messageText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSending = true)

            val message = Message(
                chatId = chatId,
                content = text,
                timestamp = System.currentTimeMillis(),
                isOutgoing = true,
                status = MessageStatus.SENT,
                replyToId = _state.value.replyToMessage?.id
            )
            messageRepository.insertMessage(message)
            chatRepository.updateLastMessage(chatId, text, message.timestamp)

            _state.value = _state.value.copy(
                messageText = "",
                isSending = false,
                replyToMessage = null
            )
        }
    }

    fun setReplyTo(message: Message?) {
        _state.value = _state.value.copy(replyToMessage = message)
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            messageRepository.deleteMessage(messageId)
        }
    }

    fun simulateIncomingMessage(content: String) {
        viewModelScope.launch {
            val message = Message(
                chatId = chatId,
                content = content,
                timestamp = System.currentTimeMillis(),
                isOutgoing = false,
                status = MessageStatus.DELIVERED
            )
            messageRepository.insertMessage(message)
            chatRepository.updateLastMessage(chatId, content, message.timestamp)
            chatRepository.incrementUnreadCount(chatId)
        }
    }
}
