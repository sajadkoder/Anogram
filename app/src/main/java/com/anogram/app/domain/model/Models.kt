package com.anogram.app.domain.model

data class Chat(
    val id: Long = 0,
    val name: String,
    val avatarUrl: String? = null,
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isGroup: Boolean = false,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val typingUsers: List<String> = emptyList()
)

data class Message(
    val id: Long = 0,
    val chatId: Long,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean = true,
    val status: MessageStatus = MessageStatus.SENT,
    val replyToId: Long? = null,
    val replyToContent: String? = null,
    val attachments: List<Attachment> = emptyList(),
    val reactions: List<Reaction> = emptyList(),
    val isDeleted: Boolean = false,
    val isEdited: Boolean = false
)

data class Attachment(
    val id: Long = 0,
    val type: AttachmentType,
    val url: String? = null,
    val localPath: String? = null,
    val fileName: String? = null,
    val fileSize: Long = 0,
    val mimeType: String? = null,
    val thumbnailUrl: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Long? = null
)

enum class AttachmentType {
    IMAGE,
    VIDEO,
    AUDIO,
    FILE,
    VOICE
}

data class Reaction(
    val emoji: String,
    val count: Int,
    val users: List<String> = emptyList(),
    val isSelected: Boolean = false
)

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

data class User(
    val id: Long = 0,
    val name: String,
    val avatarUrl: String? = null,
    val phone: String? = null,
    val bio: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: Long? = null,
    val isTyping: Boolean = false
)

data class ChatSettings(
    val chatId: Long,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val wallpaper: String? = null,
    val customNotifications: Boolean = false,
    val messagePreview: Boolean = true
)
