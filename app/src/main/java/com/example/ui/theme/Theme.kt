package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PixelYellow,
    onPrimary = PixelBlack,
    primaryContainer = PixelSurfaceContainer,
    onPrimaryContainer = PixelWhite,
    secondary = PixelBlue,
    onSecondary = PixelBlack,
    tertiary = PixelOrange,
    background = PixelBlack,
    onBackground = PixelWhite,
    surface = PixelSurfaceDark,
    onSurface = PixelWhite,
    surfaceVariant = PixelSurfaceVariant,
    onSurfaceVariant = PixelTextSecondary,
    outline = PixelBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to camera dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context)
        }
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
