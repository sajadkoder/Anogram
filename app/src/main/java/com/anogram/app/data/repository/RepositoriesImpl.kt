package com.anogram.app.data.repository

import com.anogram.app.data.local.dao.ChatDao
import com.anogram.app.data.local.dao.MessageDao
import com.anogram.app.data.local.dao.UserDao
import com.anogram.app.data.local.entity.ChatEntity
import com.anogram.app.data.local.entity.MessageEntity
import com.anogram.app.data.local.entity.UserEntity
import com.anogram.app.domain.model.Chat
import com.anogram.app.domain.model.Message
import com.anogram.app.domain.model.MessageStatus
import com.anogram.app.domain.model.User
import com.anogram.app.domain.repository.ChatRepository
import com.anogram.app.domain.repository.MessageRepository
import com.anogram.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao
) : ChatRepository {

    override fun getAllChats(): Flow<List<Chat>> =
        chatDao.getAllChats().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getChatById(chatId: Long): Flow<Chat?> =
        chatDao.getChatById(chatId).map { it?.toDomain() }

    override suspend fun insertChat(chat: Chat): Long =
        chatDao.insertChat(ChatEntity.fromDomain(chat))

    override suspend fun updateChat(chat: Chat) =
        chatDao.updateChat(ChatEntity.fromDomain(chat))

    override suspend fun deleteChat(chatId: Long) =
        chatDao.deleteChat(chatId)

    override suspend fun updateLastMessage(chatId: Long, message: String, timestamp: Long) =
        chatDao.updateLastMessage(chatId, message, timestamp)

    override suspend fun incrementUnreadCount(chatId: Long) =
        chatDao.incrementUnreadCount(chatId)

    override suspend fun clearUnreadCount(chatId: Long) =
        chatDao.clearUnreadCount(chatId)
}

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao
) : MessageRepository {

    override fun getMessagesForChat(chatId: Long): Flow<List<Message>> =
        messageDao.getMessagesForChat(chatId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insertMessage(message: Message): Long =
        messageDao.insertMessage(MessageEntity.fromDomain(message))

    override suspend fun updateMessageStatus(messageId: Long, status: MessageStatus) =
        messageDao.updateMessageStatus(messageId, status.name)

    override suspend fun deleteMessage(messageId: Long) =
        messageDao.deleteMessage(messageId)

    override suspend fun deleteAllMessagesForChat(chatId: Long) =
        messageDao.deleteAllMessagesForChat(chatId)
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun getCurrentUser(): Flow<User?> =
        userDao.getCurrentUser().map { it?.toDomain() }

    override suspend fun saveUser(user: User) =
        userDao.saveUser(UserEntity.fromDomain(user))

    override suspend fun updateUser(user: User) =
        userDao.updateUser(UserEntity.fromDomain(user))
}
