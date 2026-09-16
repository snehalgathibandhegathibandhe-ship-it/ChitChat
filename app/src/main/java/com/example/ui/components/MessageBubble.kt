package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.ui.theme.ReadTickBlue

@Composable
fun MessageBubble(
    message: ChatMessage,
    onReactionSelect: (String) -> Unit,
    onImageClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showReactionMenu by remember { mutableStateOf(false) }

    val bubbleShape = if (message.isFromMe) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }

    val bubbleAlignment = if (message.isFromMe) Alignment.End else Alignment.Start

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp),
        horizontalAlignment = bubbleAlignment
    ) {
        Box(
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            val backgroundModifier = if (message.isFromMe) {
                Modifier.background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF5B4DFB),
                            Color(0xFF4361EE)
                        )
                    )
                )
            } else {
                Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = bubbleShape
                    )
            }

            Box(
                modifier = Modifier
                    .shadow(elevation = 1.dp, shape = bubbleShape)
                    .clip(bubbleShape)
                    .then(backgroundModifier)
                    .clickable { showReactionMenu = !showReactionMenu }
                    .padding(
                        horizontal = if (message.type == MessageType.IMAGE) 6.dp else 14.dp,
                        vertical = if (message.type == MessageType.IMAGE) 6.dp else 10.dp
                    )
            ) {
                Column {
                    when (message.type) {
                        MessageType.TEXT -> {
                            Text(
                                text = message.text,
                                color = if (message.isFromMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            )
                        }
                        MessageType.VOICE -> {
                            VoiceWavePlayer(
                                durationSeconds = message.voiceDurationSeconds,
                                isFromMe = message.isFromMe
                            )
                        }
                        MessageType.IMAGE -> {
                            if (message.imageRes != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { onImageClick(message.imageRes) }
                                ) {
                                    Image(
                                        painter = painterResource(id = message.imageRes),
                                        contentDescription = "Shared image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 160.dp, max = 220.dp)
                                    )
                                }
                                if (message.text.isNotEmpty() && message.text != "Photo") {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = message.text,
                                        color = if (message.isFromMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = message.timestamp,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (message.isFromMe) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )

                        if (message.isFromMe) {
                            StatusIcon(status = message.status)
                        }
                    }
                }
            }

            // Attached reaction badge
            if (message.reaction != null) {
                Box(
                    modifier = Modifier
                        .align(if (message.isFromMe) Alignment.BottomStart else Alignment.BottomEnd)
                        .offset(y = 10.dp, x = if (message.isFromMe) (-4).dp else 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .clickable { showReactionMenu = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = message.reaction, fontSize = 12.sp)
                }
            }
        }

        // Quick Reaction Picker Bar
        if (showReactionMenu) {
            Spacer(modifier = Modifier.height(6.dp))
            QuickReactionRow(
                onSelect = { emoji ->
                    onReactionSelect(emoji)
                    showReactionMenu = false
                }
            )
        }
    }
}

@Composable
fun StatusIcon(status: MessageStatus) {
    when (status) {
        MessageStatus.SENDING -> {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Sending",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(13.dp)
            )
        }
        MessageStatus.SENT -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                tint = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.size(13.dp)
            )
        }
        MessageStatus.DELIVERED -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Delivered",
                tint = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.size(14.dp)
            )
        }
        MessageStatus.READ -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Read",
                tint = ReadTickBlue,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun QuickReactionRow(
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val reactions = listOf("❤️", "👍", "😂", "🔥", "😮", "✨")

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp,
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            reactions.forEach { emoji ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onSelect(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 18.sp)
                }
            }
        }
    }
}
