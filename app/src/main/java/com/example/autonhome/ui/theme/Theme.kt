package com.example.autonhome.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1A5F7A),  // Bleu conteneur
    secondary = Color(0xFF2E8B57),  // Vert nature
    tertiary = Color(0xFFFF8C00),  // Orange solaire
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFFFFFFF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF64B5F6),  // Bleu clair
    secondary = Color(0xFF81C784),  // Vert clair
    tertiary = Color(0xFFFFB74D),  // Orange clair
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

@Composable
fun AutonHomeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


