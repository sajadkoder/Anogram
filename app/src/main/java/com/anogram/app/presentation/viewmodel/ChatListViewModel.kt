package com.anogram.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anogram.app.domain.model.Chat
import com.anogram.app.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val filteredChats: List<Chat> = emptyList()
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatListState())
    val state: StateFlow<ChatListState> = _state.asStateFlow()

    val chats: StateFlow<List<Chat>> = chatRepository.getAllChats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            chats.collect { chatList ->
                if (chatList.isEmpty()) {
                    insertDemoChats()
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    filteredChats = filterChats(chatList, _state.value.searchQuery)
                )
            }
        }
    }

    private suspend fun insertDemoChats() {
        val demoChats = listOf(
            Chat(name = "Alice", lastMessage = "Hey, how are you?", lastMessageTime = System.currentTimeMillis() - 300000, unreadCount = 2, isOnline = true),
            Chat(name = "Bob", lastMessage = "See you tomorrow!", lastMessageTime = System.currentTimeMillis() - 3600000, unreadCount = 0, isOnline = false),
            Chat(name = "Work Group", lastMessage = "Meeting at 3pm", lastMessageTime = System.currentTimeMillis() - 7200000, unreadCount = 5, isGroup = true),
            Chat(name = "Charlie", lastMessage = "Thanks!", lastMessageTime = System.currentTimeMillis() - 86400000, unreadCount = 0, isOnline = true),
            Chat(name = "Family", lastMessage = "Happy Birthday!", lastMessageTime = System.currentTimeMillis() - 172800000, unreadCount = 3, isGroup = true, isPinned = true)
        )
        demoChats.forEach { chatRepository.insertChat(it) }
    }

    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(
            searchQuery = query,
            filteredChats = filterChats(chats.value, query)
        )
    }

    fun createNewChat(name: String) {
        viewModelScope.launch {
            val newChat = Chat(
                name = name,
                lastMessage = "",
                lastMessageTime = System.currentTimeMillis(),
                unreadCount = 0,
                isOnline = false,
                isGroup = false
            )
            chatRepository.insertChat(newChat)
        }
    }

    fun deleteChat(chatId: Long) {
        viewModelScope.launch {
            chatRepository.deleteChat(chatId)
        }
    }

    private fun filterChats(chats: List<Chat>, query: String): List<Chat> {
        return if (query.isBlank()) chats
        else chats.filter { it.name.contains(query, ignoreCase = true) }
    }
}
