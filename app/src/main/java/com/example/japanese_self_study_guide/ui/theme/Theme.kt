package com.example.japanese_self_study_guide.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary          = Color(0xFFE91E63),
    onPrimary        = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD6E4),
    onPrimaryContainer = Color(0xFF3E0021),

    secondary        = Color(0xFFC13D6C),
    onSecondary      = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD6E4),
    onSecondaryContainer = Color(0xFF3E0021),

    background       = Color(0xFFFFE0E6),
    onBackground     = Color(0xFF4A4A4A),

    surface          = Color(0xFFFFFFFF),
    onSurface        = Color(0xFF4A4A4A),
    onSurfaceVariant = Color(0xFF7A7A7A),

    error            = Color(0xFFB00020),
    outline          = Color(0xFFBDBDBD),
)

private val DarkColors = darkColorScheme(
    primary          = Color(0xFFF06292),
    onPrimary        = Color(0xFF1C1B1F),
    primaryContainer = Color(0xFF880E4F),
    onPrimaryContainer = Color(0xFFFFD6E4),

    secondary        = Color(0xFFC2185B),
    onSecondary      = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF5C0033),
    onSecondaryContainer = Color(0xFFFFD6E4),

    background       = Color(0xFF1C1B1F),
    onBackground     = Color(0xFFFFFFFF),

    surface          = Color(0xFF2A292C),
    onSurface        = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFAAAAAA),

    error            = Color(0xFFCF6679),
    outline          = Color(0xFF555555),
)

@Composable
fun JapaneseSelfStudyGuideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}