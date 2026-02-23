package com.anogram.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anogram.app.domain.model.Chat
import com.anogram.app.domain.model.Message
import com.anogram.app.domain.model.MessageStatus
import com.anogram.app.domain.model.User

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val avatarUrl: String?,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int,
    val isOnline: Boolean,
    val isGroup: Boolean,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false
) {
    fun toDomain() = Chat(
        id = id,
        name = name,
        avatarUrl = avatarUrl,
        lastMessage = lastMessage,
        lastMessageTime = lastMessageTime,
        unreadCount = unreadCount,
        isOnline = isOnline,
        isGroup = isGroup,
        isMuted = isMuted,
        isPinned = isPinned,
        isArchived = isArchived
    )

    companion object {
        fun fromDomain(chat: Chat) = ChatEntity(
            id = chat.id,
            name = chat.name,
            avatarUrl = chat.avatarUrl,
            lastMessage = chat.lastMessage,
            lastMessageTime = chat.lastMessageTime,
            unreadCount = chat.unreadCount,
            isOnline = chat.isOnline,
            isGroup = chat.isGroup,
            isMuted = chat.isMuted,
            isPinned = chat.isPinned,
            isArchived = chat.isArchived
        )
    }
}

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chatId: Long,
    val content: String,
    val timestamp: Long,
    val isOutgoing: Boolean,
    val status: String,
    val replyToId: Long?,
    val replyToContent: String?,
    val attachmentsJson: String = "[]",
    val reactionsJson: String = "[]",
    val isDeleted: Boolean = false,
    val isEdited: Boolean = false
) {
    fun toDomain() = Message(
        id = id,
        chatId = chatId,
        content = content,
        timestamp = timestamp,
        isOutgoing = isOutgoing,
        status = MessageStatus.valueOf(status),
        replyToId = replyToId,
        replyToContent = replyToContent,
        isDeleted = isDeleted,
        isEdited = isEdited
    )

    companion object {
        fun fromDomain(message: Message) = MessageEntity(
            id = message.id,
            chatId = message.chatId,
            content = message.content,
            timestamp = message.timestamp,
            isOutgoing = message.isOutgoing,
            status = message.status.name,
            replyToId = message.replyToId,
            replyToContent = message.replyToContent,
            isDeleted = message.isDeleted,
            isEdited = message.isEdited
        )
    }
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val avatarUrl: String?,
    val phone: String?,
    val bio: String?,
    val isOnline: Boolean,
    val lastSeen: Long?
) {
    fun toDomain() = User(
        id = id,
        name = name,
        avatarUrl = avatarUrl,
        phone = phone,
        bio = bio,
        isOnline = isOnline,
        lastSeen = lastSeen
    )

    companion object {
        fun fromDomain(user: User) = UserEntity(
            id = user.id,
            name = user.name,
            avatarUrl = user.avatarUrl,
            phone = user.phone,
            bio = user.bio,
            isOnline = user.isOnline,
            lastSeen = user.lastSeen
        )
    }
}
