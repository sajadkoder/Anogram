package com.anogram.app.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val TelegramBlue = Color(0xFF2481CC)
val TelegramLightBlue = Color(0xFF7AC1E3)
val TelegramDarkBlue = Color(0xFF1A6789)
val MessageSent = Color(0xFFDCF8C6)
val MessageReceived = Color(0xFFFFFFFF)
val OnlineGreen = Color(0xFF4CD964)
val UnreadBadge = Color(0xFF4CD964)

private val DarkColorScheme = darkColorScheme(
    primary = TelegramBlue,
    secondary = TelegramLightBlue,
    tertiary = TelegramDarkBlue,
    background = Color(0xFF0F0F0F),
    surface = Color(0xFF1C1C1C),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = TelegramBlue,
    secondary = TelegramLightBlue,
    tertiary = TelegramDarkBlue,
    background = Color(0xFFEFEFF0),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun AnoGramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    @Suppress("DEPRECATION")
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
