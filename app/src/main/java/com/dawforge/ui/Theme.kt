package com.dawforge.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Scheme = darkColorScheme(
    primary = Color(0xFFBFB2FF), onPrimary = Color(0xFF2A1A6E),
    primaryContainer = Color(0xFF413491), onPrimaryContainer = Color(0xFFE5DEFF),
    secondary = Color(0xFF6FD8E6), tertiary = Color(0xFFFFB4A8),
    background = Color(0xFF0F0E17), onBackground = Color(0xFFE6E1F0),
    surface = Color(0xFF0F0E17), onSurface = Color(0xFFE6E1F0),
    surfaceVariant = Color(0xFF2A2938), onSurfaceVariant = Color(0xFFC9C3DC),
    surfaceContainer = Color(0xFF1A1927), surfaceContainerHigh = Color(0xFF242335),
    outline = Color(0xFF8A859C), outlineVariant = Color(0xFF3A384A),
    secondaryContainer = Color(0xFF3B3558), onSecondaryContainer = Color(0xFFE5DEFF)
)

val TrackColors = listOf(
    Color(0xFF9D8CFF), Color(0xFF4DD0E1), Color(0xFFFF8A80),
    Color(0xFFFFD54F), Color(0xFF81C784), Color(0xFFF48FB1)
)
fun trackColor(i: Int) = TrackColors[i % TrackColors.size]

@Composable
fun DawTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = Scheme,
    shapes = Shapes(
        small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(18.dp),
        large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(32.dp)
    ),
    content = content
)
