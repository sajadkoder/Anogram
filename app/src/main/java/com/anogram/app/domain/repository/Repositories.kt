package com.anogram.app.domain.repository

import com.anogram.app.domain.model.Chat
import com.anogram.app.domain.model.Message
import com.anogram.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getAllChats(): Flow<List<Chat>>
    fun getChatById(chatId: Long): Flow<Chat?>
    suspend fun insertChat(chat: Chat): Long
    suspend fun updateChat(chat: Chat)
    suspend fun deleteChat(chatId: Long)
    suspend fun updateLastMessage(chatId: Long, message: String, timestamp: Long)
    suspend fun incrementUnreadCount(chatId: Long)
    suspend fun clearUnreadCount(chatId: Long)
}

interface MessageRepository {
    fun getMessagesForChat(chatId: Long): Flow<List<Message>>
    suspend fun insertMessage(message: Message): Long
    suspend fun updateMessageStatus(messageId: Long, status: com.anogram.app.domain.model.MessageStatus)
    suspend fun deleteMessage(messageId: Long)
    suspend fun deleteAllMessagesForChat(chatId: Long)
}

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun saveUser(user: User)
    suspend fun updateUser(user: User)
}
