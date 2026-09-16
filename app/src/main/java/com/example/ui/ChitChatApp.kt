package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.ConversationScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ChatViewModel

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    CHATS("Chats", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat),
    CONTACTS("Contacts", Icons.Default.People, Icons.Outlined.People),
    AI("Spark AI", Icons.Default.AutoAwesome, Icons.Outlined.AutoAwesome),
    PROFILE("Profile", Icons.Default.Person, Icons.Outlined.Person),
    SETTINGS("Settings", Icons.Default.Settings, Icons.Outlined.Settings)
}

@Composable
fun ChitChatApp(
    chatViewModel: ChatViewModel = viewModel()
) {
    val userProfile by chatViewModel.userProfile.collectAsStateWithLifecycle()
    val chats by chatViewModel.chats.collectAsStateWithLifecycle()
    val activeChatId by chatViewModel.activeChatId.collectAsStateWithLifecycle()
    val activeChat by chatViewModel.activeChat.collectAsStateWithLifecycle()
    val activeMessages by chatViewModel.activeMessages.collectAsStateWithLifecycle()
    val isPartnerTyping by chatViewModel.isPartnerTyping.collectAsStateWithLifecycle()
    val searchQuery by chatViewModel.searchQuery.collectAsStateWithLifecycle()
    val activeFilter by chatViewModel.activeFilter.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(MainTab.CHATS) }

    val systemDark = isSystemInDarkTheme()
    val isDarkTheme = when (userProfile?.themeMode) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }

    MyApplicationTheme(darkTheme = isDarkTheme) {
        if (userProfile == null || !userProfile!!.hasCompletedWelcome) {
            WelcomeScreen(
                onContinue = { enteredName ->
                    chatViewModel.completeWelcome(enteredName)
                }
            )
        } else {
            if (activeChatId != null) {
                BackHandler {
                    chatViewModel.clearActiveChat()
                }

                ConversationScreen(
                    chat = activeChat,
                    messages = activeMessages,
                    isTyping = isPartnerTyping,
                    onBack = { chatViewModel.clearActiveChat() },
                    onSendMessage = { text -> chatViewModel.sendTextMessage(text) },
                    onSendVoiceMessage = { duration -> chatViewModel.sendVoiceMessage(duration) },
                    onSendImageMessage = { res, cap -> chatViewModel.sendImageMessage(res, cap) },
                    onToggleReaction = { id, emoji -> chatViewModel.toggleReaction(id, emoji) }
                )
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            val totalUnread = chats.sumOf { it.unreadCount }

                            MainTab.values().forEach { tab ->
                                val isSelected = selectedTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        if (tab == MainTab.AI) {
                                            chatViewModel.selectChat("ai_spark")
                                        } else {
                                            selectedTab = tab
                                        }
                                    },
                                    icon = {
                                        if (tab == MainTab.CHATS && totalUnread > 0) {
                                            BadgedBox(
                                                badge = {
                                                    Badge { Text("$totalUnread") }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = tab.title,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.title,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    },
                                    label = {
                                        Text(text = tab.title, fontSize = 11.sp)
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "MainTabsAnimation"
                        ) { targetTab ->
                            when (targetTab) {
                                MainTab.CHATS -> {
                                    ChatListScreen(
                                        chats = chats,
                                        onlineContacts = chatViewModel.contacts.filter { it.isOnline },
                                        searchQuery = searchQuery,
                                        onSearchQueryChange = { chatViewModel.onSearchQueryChanged(it) },
                                        activeFilter = activeFilter,
                                        onFilterChange = { chatViewModel.onFilterChanged(it) },
                                        onChatClick = { id -> chatViewModel.selectChat(id) },
                                        onNewChatClick = { selectedTab = MainTab.CONTACTS },
                                        onContactClick = { contact ->
                                            chatViewModel.startChatWithContact(contact) {}
                                        }
                                    )
                                }
                                MainTab.CONTACTS -> {
                                    ContactsScreen(
                                        contacts = chatViewModel.contacts,
                                        onContactSelected = { contact ->
                                            chatViewModel.startChatWithContact(contact) {}
                                        }
                                    )
                                }
                                MainTab.AI -> {
                                    // Managed by clicking AI in bottom nav (opens conversation directly)
                                }
                                MainTab.PROFILE -> {
                                    ProfileScreen(
                                        userProfile = userProfile,
                                        onUpdateOnlineStatus = { chatViewModel.updateOnlineStatus(it) },
                                        onUpdateProfile = { name, bio -> chatViewModel.updateProfile(name, bio) }
                                    )
                                }
                                MainTab.SETTINGS -> {
                                    SettingsScreen(
                                        currentThemeMode = userProfile?.themeMode ?: "system",
                                        onThemeModeChange = { mode -> chatViewModel.updateThemeMode(mode) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
