package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

val ChitChatDarkColorScheme = darkColorScheme(
  primary = ChitChatPrimaryDark,
  onPrimary = ChitChatOnPrimaryDark,
  primaryContainer = ChitChatPrimaryContainerDark,
  onPrimaryContainer = ChitChatOnPrimaryContainerDark,
  secondary = ChitChatSecondaryDark,
  onSecondary = ChitChatOnSecondaryDark,
  secondaryContainer = ChitChatSecondaryContainerDark,
  onSecondaryContainer = ChitChatOnSecondaryContainerDark,
  tertiary = ChitChatTertiaryDark,
  onTertiary = ChitChatOnTertiaryDark,
  tertiaryContainer = ChitChatTertiaryContainerDark,
  onTertiaryContainer = ChitChatOnTertiaryContainerDark,
  background = ChitChatBackgroundDark,
  onBackground = ChitChatOnBackgroundDark,
  surface = ChitChatSurfaceDark,
  onSurface = ChitChatOnSurfaceDark,
  surfaceVariant = ChitChatSurfaceVariantDark,
  onSurfaceVariant = ChitChatOnSurfaceVariantDark,
  outline = ChitChatOutlineDark,
  outlineVariant = ChitChatOutlineVariantDark
)

val ChitChatLightColorScheme = lightColorScheme(
  primary = ChitChatPrimaryLight,
  onPrimary = ChitChatOnPrimaryLight,
  primaryContainer = ChitChatPrimaryContainerLight,
  onPrimaryContainer = ChitChatOnPrimaryContainerLight,
  secondary = ChitChatSecondaryLight,
  onSecondary = ChitChatOnSecondaryLight,
  secondaryContainer = ChitChatSecondaryContainerLight,
  onSecondaryContainer = ChitChatOnSecondaryContainerLight,
  tertiary = ChitChatTertiaryLight,
  onTertiary = ChitChatOnTertiaryLight,
  tertiaryContainer = ChitChatTertiaryContainerLight,
  onTertiaryContainer = ChitChatOnTertiaryContainerLight,
  background = ChitChatBackgroundLight,
  onBackground = ChitChatOnBackgroundLight,
  surface = ChitChatSurfaceLight,
  onSurface = ChitChatOnSurfaceLight,
  surfaceVariant = ChitChatSurfaceVariantLight,
  onSurfaceVariant = ChitChatOnSurfaceVariantLight,
  outline = ChitChatOutlineLight,
  outlineVariant = ChitChatOutlineVariantLight
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Preserve our brand minimal purple-blue palette
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> ChitChatDarkColorScheme
    else -> ChitChatLightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

