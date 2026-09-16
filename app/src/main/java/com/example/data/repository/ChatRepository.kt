package com.example.data.repository

import com.example.R
import com.example.data.db.ChatDao
import com.example.data.db.ChatEntity
import com.example.data.db.MessageDao
import com.example.data.db.MessageEntity
import com.example.data.db.UserDao
import com.example.data.db.UserEntity
import com.example.data.model.ChatItem
import com.example.data.model.ChatMessage
import com.example.data.model.ContactItem
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatRepository(
    private val userDao: UserDao,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    init {
        scope.launch {
            seedInitialDataIfNeeded()
        }
    }

    val userProfile: Flow<UserProfile?> = userDao.getUserProfile().map { entity ->
        entity?.let {
            UserProfile(
                name = it.name,
                handle = it.handle,
                statusBio = it.statusBio,
                isOnline = it.isOnline,
                avatarColor = it.avatarColor,
                hasCompletedWelcome = it.hasCompletedWelcome,
                themeMode = it.themeMode
            )
        }
    }

    val allChats: Flow<List<ChatItem>> = chatDao.getAllChats().map { entities ->
        entities.map { it.toChatItem() }
    }

    fun getMessages(chatId: String): Flow<List<ChatMessage>> {
        return messageDao.getMessagesForChat(chatId).map { entities ->
            entities.map { it.toChatMessage() }
        }
    }

    suspend fun saveWelcomeUser(name: String) {
        val cleanName = name.trim().ifEmpty { "ChitChatter" }
        val handle = "@" + cleanName.lowercase().replace(" ", ".")
        userDao.insertOrUpdateUserProfile(
            UserEntity(
                id = 1,
                name = cleanName,
                handle = handle,
                statusBio = "Connecting with style on ChitChat ✨",
                isOnline = true,
                avatarColor = 0xFF5B4DFB,
                hasCompletedWelcome = true,
                themeMode = "system"
            )
        )
    }

    suspend fun updateOnlineStatus(isOnline: Boolean) {
        userDao.updateOnlineStatus(isOnline)
    }

    suspend fun updateThemeMode(themeMode: String) {
        userDao.updateThemeMode(themeMode)
    }

    suspend fun updateProfile(name: String, bio: String) {
        userDao.updateProfile(name, bio)
    }

    suspend fun clearUnread(chatId: String) {
        chatDao.clearUnread(chatId)
    }

    suspend fun sendTextMessage(chatId: String, text: String) {
        val now = System.currentTimeMillis()
        val timeStr = formatTime(now)
        val messageId = UUID.randomUUID().toString()

        val message = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderName = "Me",
            isFromMe = true,
            text = text,
            timestamp = timeStr,
            timestampMillis = now,
            type = MessageType.TEXT.name,
            status = MessageStatus.SENT.name,
            voiceDurationSeconds = 0,
            imageRes = null,
            reaction = null
        )
        messageDao.insertMessage(message)
        chatDao.updateLastMessage(chatId, text, timeStr, now)
    }

    suspend fun sendVoiceMessage(chatId: String, durationSeconds: Int = 18) {
        val now = System.currentTimeMillis()
        val timeStr = formatTime(now)
        val messageId = UUID.randomUUID().toString()

        val message = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderName = "Me",
            isFromMe = true,
            text = "Voice message",
            timestamp = timeStr,
            timestampMillis = now,
            type = MessageType.VOICE.name,
            status = MessageStatus.SENT.name,
            voiceDurationSeconds = durationSeconds,
            imageRes = null,
            reaction = null
        )
        messageDao.insertMessage(message)
        chatDao.updateLastMessage(chatId, "🎤 Voice note (0:${if (durationSeconds < 10) "0$durationSeconds" else durationSeconds})", timeStr, now)
    }

    suspend fun sendImageMessage(chatId: String, imageRes: Int, caption: String = "") {
        val now = System.currentTimeMillis()
        val timeStr = formatTime(now)
        val messageId = UUID.randomUUID().toString()

        val message = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderName = "Me",
            isFromMe = true,
            text = caption.ifEmpty { "Photo" },
            timestamp = timeStr,
            timestampMillis = now,
            type = MessageType.IMAGE.name,
            status = MessageStatus.SENT.name,
            voiceDurationSeconds = 0,
            imageRes = imageRes,
            reaction = null
        )
        messageDao.insertMessage(message)
        chatDao.updateLastMessage(chatId, "📷 Photo shared", timeStr, now)
    }

    suspend fun toggleReaction(messageId: String, emoji: String) {
        messageDao.updateReaction(messageId, emoji)
    }

    suspend fun markMessageDeliveredAndRead(messageId: String) {
        messageDao.updateMessageStatus(messageId, MessageStatus.READ.name)
    }

    suspend fun receiveReply(chatId: String, replyText: String, senderName: String) {
        val now = System.currentTimeMillis()
        val timeStr = formatTime(now)
        val messageId = UUID.randomUUID().toString()

        val reply = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderName = senderName,
            isFromMe = false,
            text = replyText,
            timestamp = timeStr,
            timestampMillis = now,
            type = MessageType.TEXT.name,
            status = MessageStatus.READ.name,
            voiceDurationSeconds = 0,
            imageRes = null,
            reaction = null
        )
        messageDao.insertMessage(reply)
        chatDao.updateLastMessage(chatId, replyText, timeStr, now)
    }

    fun getContactList(): List<ContactItem> {
        return listOf(
            ContactItem("ai_spark", "Spark AI Assistant", "@spark.ai", "Always here to help and brainstorm ✨", true, 0xFF705AF8),
            ContactItem("c_elena", "Elena Rostova", "@elena.design", "Product Designer • Visual craft & typography", true, 0xFF5B4DFB),
            ContactItem("c_marcus", "Marcus Chen", "@marcus.dev", "Building scalable Android apps 🚀", true, 0xFF4361EE),
            ContactItem("c_sophia", "Sophia Williams", "@sophia.w", "Exploring mountains and cozy cafes ☕🌲", false, 0xFF8B5CF6),
            ContactItem("c_david", "David Miller", "@david.m", "Weekend cyclist & tech enthusiast", false, 0xFF0EA5E9),
            ContactItem("c_amara", "Amara Okafor", "@amara.o", "Creative Director • Studio Lumière", false, 0xFF10B981),
            ContactItem("c_liam", "Liam Vance", "@liam.v", "Jetpack Compose & Kotlin lover", true, 0xFFF59E0B),
            ContactItem("c_chloe", "Chloe Bennett", "@chloe.b", "Designing future human interfaces 💫", true, 0xFFEC4899),
            ContactItem("c_lucas", "Lucas Silva", "@lucas.s", "Coffee, code, repeat ☕💻", false, 0xFF6366F1),
            ContactItem("c_zara", "Zara Patel", "@zara.p", "Music producer & sound designer 🎧", true, 0xFF14B8A6)
        )
    }

    suspend fun createOrGetChatForContact(contact: ContactItem): String {
        val existing = chatDao.getChatById(contact.id)
        if (existing == null) {
            val now = System.currentTimeMillis()
            val timeStr = formatTime(now)
            val newChat = ChatEntity(
                id = contact.id,
                name = contact.name,
                handle = contact.handle,
                avatarRes = if (contact.id == "ai_spark") R.drawable.avatar_spark_ai else null,
                avatarColor = contact.avatarColor,
                isOnline = contact.isOnline,
                lastSeenText = if (contact.isOnline) "Online" else "Active recently",
                lastMessage = "Started a new conversation",
                lastMessageTime = timeStr,
                unreadCount = 0,
                isPinned = false,
                isAiAssistant = contact.id == "ai_spark",
                lastUpdatedMillis = now
            )
            chatDao.insertChat(newChat)
        }
        return contact.id
    }

    private suspend fun seedInitialDataIfNeeded() {
        val existingChats = chatDao.getAllChats().firstOrNull()
        if (!existingChats.isNullOrEmpty()) return

        val now = System.currentTimeMillis()

        val seedChats = listOf(
            ChatEntity(
                id = "ai_spark",
                name = "Spark AI Assistant",
                handle = "@spark.ai",
                avatarRes = R.drawable.avatar_spark_ai,
                avatarColor = 0xFF705AF8,
                isOnline = true,
                lastSeenText = "Always Online • AI Powered",
                lastMessage = "Hey! I'm Spark AI. How can I help your day today?",
                lastMessageTime = "10:42 AM",
                unreadCount = 1,
                isPinned = true,
                isAiAssistant = true,
                lastUpdatedMillis = now
            ),
            ChatEntity(
                id = "c_elena",
                name = "Elena Rostova",
                handle = "@elena.design",
                avatarRes = null,
                avatarColor = 0xFF5B4DFB,
                isOnline = true,
                lastSeenText = "Online",
                lastMessage = "The purple-blue theme looks so premium and clean!",
                lastMessageTime = "10:35 AM",
                unreadCount = 2,
                isPinned = true,
                isAiAssistant = false,
                lastUpdatedMillis = now - 1000 * 60 * 7
            ),
            ChatEntity(
                id = "c_marcus",
                name = "Marcus Chen",
                handle = "@marcus.dev",
                avatarRes = null,
                avatarColor = 0xFF4361EE,
                isOnline = true,
                lastSeenText = "Online",
                lastMessage = "🎤 Voice note (0:24)",
                lastMessageTime = "9:50 AM",
                unreadCount = 0,
                isPinned = false,
                isAiAssistant = false,
                lastUpdatedMillis = now - 1000 * 60 * 50
            ),
            ChatEntity(
                id = "c_sophia",
                name = "Sophia Williams",
                handle = "@sophia.w",
                avatarRes = null,
                avatarColor = 0xFF8B5CF6,
                isOnline = false,
                lastSeenText = "Active 20m ago",
                lastMessage = "📷 Photo shared",
                lastMessageTime = "Yesterday",
                unreadCount = 0,
                isPinned = false,
                isAiAssistant = false,
                lastUpdatedMillis = now - 1000 * 60 * 60 * 20
            ),
            ChatEntity(
                id = "c_david",
                name = "David Miller",
                handle = "@david.m",
                avatarRes = null,
                avatarColor = 0xFF0EA5E9,
                isOnline = false,
                lastSeenText = "Active 2h ago",
                lastMessage = "Let's catch up over coffee tomorrow morning!",
                lastMessageTime = "Yesterday",
                unreadCount = 0,
                isPinned = false,
                isAiAssistant = false,
                lastUpdatedMillis = now - 1000 * 60 * 60 * 26
            ),
            ChatEntity(
                id = "c_amara",
                name = "Amara Okafor",
                handle = "@amara.o",
                avatarRes = null,
                avatarColor = 0xFF10B981,
                isOnline = false,
                lastSeenText = "Active yesterday",
                lastMessage = "Sounds perfect, see you then! 🙌",
                lastMessageTime = "Sep 7",
                unreadCount = 0,
                isPinned = false,
                isAiAssistant = false,
                lastUpdatedMillis = now - 1000 * 60 * 60 * 48
            )
        )
        chatDao.insertChats(seedChats)

        // Seed initial messages for Spark AI
        val aiMessages = listOf(
            MessageEntity(
                id = "m_ai_1",
                chatId = "ai_spark",
                senderName = "Spark AI Assistant",
                isFromMe = false,
                text = "Welcome to ChitChat! 👋 I'm Spark, your built-in AI Assistant.",
                timestamp = "10:40 AM",
                timestampMillis = now - 1000 * 120,
                type = MessageType.TEXT.name,
                status = MessageStatus.READ.name,
                voiceDurationSeconds = 0,
                imageRes = null,
                reaction = "✨"
            ),
            MessageEntity(
                id = "m_ai_2",
                chatId = "ai_spark",
                senderName = "Spark AI Assistant",
                isFromMe = false,
                text = "You can ask me to summarize ideas, draft friendly responses, recommend music, or chat whenever you want. Give it a try!",
                timestamp = "10:42 AM",
                timestampMillis = now - 1000 * 60,
                type = MessageType.TEXT.name,
                status = MessageStatus.READ.name,
                voiceDurationSeconds = 0,
                imageRes = null,
                reaction = null
            )
        )
        aiMessages.forEach { messageDao.insertMessage(it) }

        // Seed messages for Elena
        val elenaMessages = listOf(
            MessageEntity(
                id = "m_el_1",
                chatId = "c_elena",
                senderName = "Me",
                isFromMe = true,
                text = "Hey Elena! Have you tested the new ChitChat interface?",
                timestamp = "10:30 AM",
                timestampMillis = now - 1000 * 60 * 10,
                type = MessageType.TEXT.name,
                status = MessageStatus.READ.name,
                voiceDurationSeconds = 0,
                imageRes = null,
                reaction = null
            ),
            MessageEntity(
                id = "m_el_2",
                chatId = "c_elena",
                senderName = "Elena Rostova",
                isFromMe = false,
                text = "Yes! The typography and soft shadows feel so smooth. 🎨",
                timestamp = "10:32 AM",
                timestampMillis = now - 1000 * 60 * 8,
                type = MessageType.TEXT.name,
                status = MessageStatus.READ.name,
                voiceDurationSeconds = 0,
                imageRes = null,
                reaction = "❤️"
            ),
            MessageEntity(
                id = "m_el_3",
                chatId = "c_elena",
                senderName = "Elena Rostova",
                isFromMe = false,
                text = "The purple-blue theme looks so premium and clean!",
                timestamp = "10:35 AM",
                timestampMillis = now - 1000 * 60 * 5,
                type = MessageType.TEXT.name,
                status = MessageStatus.READ.name,
                voiceDurationSeconds = 0,
                imageRes = null,
                reaction = null
            )
        )
        elenaMessages.forEach { messageDao.insertMessage(it) }

        // Seed messages for Marcus (with Voice Note)
        val marcusMessages = listOf(
            MessageEntity(
                id = "m_mc_1",
                chatId = "c_marcus",
                senderName = "Marcus Chen",
                isFromMe = false,
                text = "Hey, sending you a quick voice memo about the project updates:",
                timestamp = "9:48 AM",
                timestampMillis = now - 1000 * 60 * 55,
                type = MessageType.TEXT.name,
                status = MessageStatus.READ.name,
                voiceDurationSeconds = 0,
                imageRes = null,
                reaction = null
            ),
            MessageEntity(
                id = "m_mc_2",
                chatId = "c_marcus",
                senderName = "Marcus Chen",
                isFromMe = false,
                text = "Voice note",
                timestamp = "9:50 AM",
                timestampMillis = now - 1000 * 60 * 50,
                type = MessageType.VOICE.name,
                status = MessageStatus.READ.name,
                voiceDurationSeconds = 24,
                imageRes = null,
                reaction = "👍"
            )
        )
        marcusMessages.forEach { messageDao.insertMessage(it) }

        // Seed messages for Sophia (with Image Note)
        val sophiaMessages = listOf(
            MessageEntity(
                id = "m_sp_1",
                chatId = "c_sophia",
                senderName = "Sophia Williams",
                isFromMe = false,
                text = "Morning hike view from today! Totally peaceful up here 🌲⛰️",
                timestamp = "Yesterday",
                timestampMillis = now - 1000 * 60 * 60 * 20,
                type = MessageType.IMAGE.name,
                status = MessageStatus.READ.name,
                voiceDurationSeconds = 0,
                imageRes = R.drawable.sample_nature_photo,
                reaction = "🔥"
            )
        )
        sophiaMessages.forEach { messageDao.insertMessage(it) }

        // Seed messages for David (with Coffee Photo)
        val davidMessages = listOf(
            MessageEntity(
                id = "m_dv_1",
                chatId = "c_david",
                senderName = "David Miller",
                isFromMe = false,
                text = "At our favorite spot nearby. Let's catch up over coffee tomorrow morning!",
                timestamp = "Yesterday",
                timestampMillis = now - 1000 * 60 * 60 * 26,
                type = MessageType.IMAGE.name,
                status = MessageStatus.READ.name,
                voiceDurationSeconds = 0,
                imageRes = R.drawable.sample_coffee_photo,
                reaction = "☕"
            )
        )
        davidMessages.forEach { messageDao.insertMessage(it) }
    }

    private fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    private fun ChatEntity.toChatItem(): ChatItem {
        return ChatItem(
            id = id,
            name = name,
            handle = handle,
            avatarRes = avatarRes,
            avatarColor = avatarColor,
            isOnline = isOnline,
            lastSeenText = lastSeenText,
            lastMessage = lastMessage,
            lastMessageTime = lastMessageTime,
            unreadCount = unreadCount,
            isPinned = isPinned,
            isAiAssistant = isAiAssistant,
            isTyping = false
        )
    }

    private fun MessageEntity.toChatMessage(): ChatMessage {
        val msgType = try {
            MessageType.valueOf(type)
        } catch (_: Exception) {
            MessageType.TEXT
        }
        val msgStatus = try {
            MessageStatus.valueOf(status)
        } catch (_: Exception) {
            MessageStatus.READ
        }
        return ChatMessage(
            id = id,
            chatId = chatId,
            senderName = senderName,
            isFromMe = isFromMe,
            text = text,
            timestamp = timestamp,
            timestampMillis = timestampMillis,
            type = msgType,
            status = msgStatus,
            voiceDurationSeconds = voiceDurationSeconds,
            imageRes = imageRes,
            reaction = reaction
        )
    }
}
