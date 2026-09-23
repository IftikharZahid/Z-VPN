package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ZvpnDarkColorScheme = darkColorScheme(
  primary = VibrantBlue,
  onPrimary = Color(0xFF040B16),
  primaryContainer = Color(0xFF1E3A8A),
  onPrimaryContainer = Color(0xFFBFDBFE),
  secondary = CyanAccent,
  onSecondary = Color(0xFF02171E),
  secondaryContainer = Color(0xFF0E4A5C),
  onSecondaryContainer = Color(0xFFA5F3FC),
  tertiary = OkEmerald,
  onTertiary = Color(0xFF022016),
  background = DarkBgEnd,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = TextMuted,
  outline = DarkSurfaceStroke,
  outlineVariant = TextMuted2
)

@Composable
fun ZvpnTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = ZvpnDarkColorScheme,
    typography = Typography,
    content = content
  )
}
