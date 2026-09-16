package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun VoiceWavePlayer(
    durationSeconds: Int,
    isFromMe: Boolean,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    // Waveform heights seed
    val waveHeights = remember {
        listOf(
            12f, 18f, 26f, 14f, 32f, 22f, 16f, 28f,
            36f, 20f, 14f, 26f, 30f, 18f, 24f, 34f,
            20f, 12f, 22f, 28f, 16f, 30f, 24f, 14f
        )
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val steps = durationSeconds * 10
            for (i in 1..steps) {
                if (!isPlaying) break
                delay(100)
                progress = i.toFloat() / steps
            }
            isPlaying = false
            progress = 0f
        } else {
            progress = 0f
        }
    }

    val playButtonBg = if (isFromMe) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primary
    val playButtonIconColor = if (isFromMe) Color.White else MaterialTheme.colorScheme.onPrimary
    val barColorActive = if (isFromMe) Color.White else MaterialTheme.colorScheme.primary
    val barColorInactive = if (isFromMe) Color.White.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(playButtonBg)
                .clickable { isPlaying = !isPlaying },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause voice message" else "Play voice message",
                tint = playButtonIconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Row(
                modifier = Modifier.width(150.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                waveHeights.forEachIndexed { index, height ->
                    val isPast = (index.toFloat() / waveHeights.size) <= progress
                    val heightMultiplier = if (isPlaying && isPast) 1.2f else 1.0f

                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height((height * heightMultiplier).dp.coerceIn(8.dp, 36.dp))
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isPast) barColorActive else barColorInactive)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val elapsedSec = (progress * durationSeconds).toInt()
            val minutes = elapsedSec / 60
            val seconds = elapsedSec % 60
            val formattedTime = if (isPlaying) {
                String.format("%d:%02d", minutes, seconds)
            } else {
                String.format("0:%02d", durationSeconds)
            }

            Text(
                text = formattedTime,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isFromMe) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
