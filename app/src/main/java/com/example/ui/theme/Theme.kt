package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AuraPrimary,
    secondary = AuraSecondary,
    tertiary = AuraTertiary,
    background = AuraDarkBackground,
    surface = AuraDarkSurface,
    surfaceVariant = AuraDarkSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = AuraTextPrimary,
    onSurface = AuraTextPrimary,
    onSurfaceVariant = AuraTextSecondary,
    outlineVariant = Color(0x20FFFFFF)
)

private val AmoledColorScheme = darkColorScheme(
    primary = AuraPrimary,
    secondary = AuraSecondary,
    tertiary = AuraTertiary,
    background = AuraAmoledBackground,
    surface = AuraAmoledSurface,
    surfaceVariant = AuraAmoledSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = AuraTextPrimary,
    onSurface = AuraTextPrimary,
    onSurfaceVariant = AuraTextSecondary,
    outlineVariant = Color(0x20FFFFFF)
)

private val LightColorScheme = lightColorScheme(
    primary = AuraPrimary,
    secondary = AuraSecondary,
    tertiary = AuraTertiary,
    background = AuraLightBackground,
    surface = AuraLightSurface,
    surfaceVariant = AuraLightSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = AuraLightTextPrimary,
    onSurface = AuraLightTextPrimary,
    onSurfaceVariant = AuraLightTextSecondary,
    outlineVariant = Color(0x20000000)
)

@Composable
fun AuraMusicTheme(
    themeMode: String = "SYSTEM", // "SYSTEM", "DARK", "LIGHT", "AMOLED", "DYNAMIC"
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val useDarkTheme = when (themeMode) {
        "LIGHT" -> false
        "DARK", "AMOLED" -> true
        else -> systemInDark
    }

    val context = LocalContext.current
    val colorScheme = when (themeMode) {
        "AMOLED" -> AmoledColorScheme
        "LIGHT" -> LightColorScheme
        "DARK" -> DarkColorScheme
        "DYNAMIC" -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (useDarkTheme) DarkColorScheme else LightColorScheme
            }
        }
        "SYSTEM" -> {
            if (useDarkTheme) DarkColorScheme else LightColorScheme
        }
        else -> if (useDarkTheme) DarkColorScheme else LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !useDarkTheme
                insetsController.isAppearanceLightNavigationBars = !useDarkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

