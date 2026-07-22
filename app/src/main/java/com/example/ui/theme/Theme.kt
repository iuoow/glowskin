package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = RosePrimary,
    onPrimary = SoftSurface,
    primaryContainer = Color(0xFFFCE4EC),
    onPrimaryContainer = Color(0xFF880E4F),
    secondary = RoseSecondary,
    onSecondary = SoftSurface,
    secondaryContainer = Color(0xFFF8BBD0),
    tertiary = RoseTertiary,
    background = BlushBackground,
    onBackground = DeepText,
    surface = SoftSurface,
    onSurface = DeepText,
    surfaceVariant = Color(0xFFFFF0F3),
    onSurfaceVariant = MutedText,
    outline = RoseBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = RosePrimaryDark,
    onPrimary = DeepTextDark,
    primaryContainer = Color(0xFF880E4F),
    onPrimaryContainer = Color(0xFFFCE4EC),
    secondary = RoseSecondary,
    background = BlushBackgroundDark,
    onBackground = DeepTextDark,
    surface = SoftSurfaceDark,
    onSurface = DeepTextDark,
    surfaceVariant = Color(0xFF2C1E22),
    onSurfaceVariant = MutedTextDark,
    outline = Color(0xFF523B41)
)

@Composable
fun GlowSkinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent feminine branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
