package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val handle: String,
    val statusBio: String,
    val isOnline: Boolean,
    val avatarColor: Long,
    val hasCompletedWelcome: Boolean,
    val themeMode: String
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val name: String,
    val handle: String,
    val avatarRes: Int? = null,
    val avatarColor: Long,
    val isOnline: Boolean,
    val lastSeenText: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int,
    val isPinned: Boolean,
    val isAiAssistant: Boolean,
    val lastUpdatedMillis: Long
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderName: String,
    val isFromMe: Boolean,
    val text: String,
    val timestamp: String,
    val timestampMillis: Long,
    val type: String, // "TEXT", "IMAGE", "VOICE"
    val status: String, // "SENDING", "SENT", "DELIVERED", "READ"
    val voiceDurationSeconds: Int,
    val imageRes: Int?,
    val reaction: String?
)
