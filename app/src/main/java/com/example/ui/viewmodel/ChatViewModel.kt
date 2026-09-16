package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.db.ChitChatDatabase
import com.example.data.model.ChatItem
import com.example.data.model.ChatMessage
import com.example.data.model.ContactItem
import com.example.data.model.UserProfile
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository

    init {
        val db = ChitChatDatabase.getInstance(application)
        repository = ChatRepository(db.userDao(), db.chatDao(), db.messageDao())
    }

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _activeFilter = MutableStateFlow("all") // all, unread, groups
    val activeFilter = _activeFilter.asStateFlow()

    val chats: StateFlow<List<ChatItem>> = combine(
        repository.allChats,
        _searchQuery,
        _activeFilter
    ) { chatList, query, filter ->
        var list = chatList
        if (query.isNotBlank()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.lastMessage.contains(query, ignoreCase = true) ||
                        it.handle.contains(query, ignoreCase = true)
            }
        }
        if (filter == "unread") {
            list = list.filter { it.unreadCount > 0 }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contacts: List<ContactItem> = repository.getContactList()

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId = _activeChatId.asStateFlow()

    private val _isPartnerTyping = MutableStateFlow(false)
    val isPartnerTyping = _isPartnerTyping.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<ChatMessage>> = _activeChatId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getMessages(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeChat: StateFlow<ChatItem?> = combine(repository.allChats, _activeChatId) { list, id ->
        list.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterChanged(filter: String) {
        _activeFilter.value = filter
    }

    fun completeWelcome(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveWelcomeUser(name)
        }
    }

    fun selectChat(chatId: String) {
        _activeChatId.value = chatId
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearUnread(chatId)
        }
    }

    fun clearActiveChat() {
        _activeChatId.value = null
        _isPartnerTyping.value = false
    }

    fun sendTextMessage(text: String) {
        val chatId = _activeChatId.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.sendTextMessage(chatId, text.trim())
            triggerSimulatedReplyIfNeeded(chatId, text.trim())
        }
    }

    fun sendVoiceMessage(durationSeconds: Int = 14) {
        val chatId = _activeChatId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.sendVoiceMessage(chatId, durationSeconds)
            triggerSimulatedReplyIfNeeded(chatId, "[Sent Voice Message]")
        }
    }

    fun sendImageMessage(imageRes: Int, caption: String = "") {
        val chatId = _activeChatId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.sendImageMessage(chatId, imageRes, caption)
            triggerSimulatedReplyIfNeeded(chatId, "[Sent Image]")
        }
    }

    fun toggleReaction(messageId: String, emoji: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleReaction(messageId, emoji)
        }
    }

    fun startChatWithContact(contact: ContactItem, onOpened: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val chatId = repository.createOrGetChatForContact(contact)
            launch(Dispatchers.Main) {
                selectChat(chatId)
                onOpened(chatId)
            }
        }
    }

    fun updateOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateOnlineStatus(isOnline)
        }
    }

    fun updateThemeMode(themeMode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateThemeMode(themeMode)
        }
    }

    fun updateProfile(name: String, bio: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProfile(name, bio)
        }
    }

    private fun triggerSimulatedReplyIfNeeded(chatId: String, userPrompt: String) {
        viewModelScope.launch(Dispatchers.IO) {
            delay(800)
            _isPartnerTyping.value = true
            delay(1600)
            _isPartnerTyping.value = false

            val (reply, senderName) = when (chatId) {
                "ai_spark" -> {
                    val aiResponse = generateSparkAiReply(userPrompt)
                    Pair(aiResponse, "Spark AI Assistant")
                }
                "c_elena" -> Pair("Loving this! The purple-blue accent and fluid transitions really make ChitChat feel so sleek.", "Elena Rostova")
                "c_marcus" -> Pair("Awesome! Checking it out on my device right now. Everything runs at 60fps.", "Marcus Chen")
                "c_sophia" -> Pair("That sounds super exciting! Can't wait to see more.", "Sophia Williams")
                "c_david" -> Pair("Sounds like a plan! I'll see you tomorrow around 10.", "David Miller")
                else -> Pair("Got your message! Thanks for reaching out via ChitChat 💬", "Contact")
            }

            repository.receiveReply(chatId, reply, senderName)
        }
    }

    private fun generateSparkAiReply(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hello there! ✨ Spark AI is here. Need help drafting an email, summarizing text, or chatting?"
            lower.contains("design") || lower.contains("ui") || lower.contains("theme") ->
                "The ChitChat Material Design 3 interface uses a subtle purple-blue harmony, generous padding, and soft ambient elevation for maximum readability."
            lower.contains("voice") ->
                "Voice messages feature high-precision visual waveforms and one-tap playback for smooth listening!"
            lower.contains("how are you") ->
                "I'm feeling energized and ready to assist you! What shall we tackle next?"
            lower.contains("joke") ->
                "Why do programmers prefer dark mode? Because light attracts bugs! 🐛😄"
            else ->
                "Spark AI: That's fascinating! In ChitChat, conversations stay fast, responsive, and beautifully organized. Anything else you'd like to explore?"
        }
    }
}
