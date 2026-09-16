package com.example.data.model

enum class MessageType {
    TEXT,
    IMAGE,
    VOICE
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

data class ChatItem(
    val id: String,
    val name: String,
    val handle: String,
    val avatarRes: Int? = null,
    val avatarColor: Long = 0xFF6356E5,
    val isOnline: Boolean = false,
    val lastSeenText: String = "Offline",
    val lastMessage: String = "",
    val lastMessageTime: String = "",
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isAiAssistant: Boolean = false,
    val isTyping: Boolean = false
)

data class ChatMessage(
    val id: String,
    val chatId: String,
    val senderName: String,
    val isFromMe: Boolean,
    val text: String = "",
    val timestamp: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.READ,
    val voiceDurationSeconds: Int = 0,
    val imageRes: Int? = null,
    val reaction: String? = null
)

data class ContactItem(
    val id: String,
    val name: String,
    val handle: String,
    val statusQuote: String,
    val isOnline: Boolean,
    val avatarColor: Long
)

data class UserProfile(
    val name: String,
    val handle: String = "",
    val statusBio: String = "Available on ChitChat ✨",
    val isOnline: Boolean = true,
    val avatarColor: Long = 0xFF5B4DFB,
    val hasCompletedWelcome: Boolean = false,
    val themeMode: String = "system" // system, light, dark
)
