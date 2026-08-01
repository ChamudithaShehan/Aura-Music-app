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

private val DarkColorScheme = darkColorScheme(
    primary = AuraPrimary,
    secondary = AuraSecondary,
    tertiary = AuraTertiary,
    background = AuraDarkBackground,
    surface = AuraDarkSurface,
    surfaceVariant = AuraDarkSurfaceVariant,
    onPrimary = AuraDarkBackground,
    onSecondary = AuraDarkBackground,
    onBackground = AuraTextPrimary,
    onSurface = AuraTextPrimary,
    onSurfaceVariant = AuraTextSecondary
)

private val AmoledColorScheme = darkColorScheme(
    primary = AuraPrimary,
    secondary = AuraSecondary,
    tertiary = AuraTertiary,
    background = AuraAmoledBackground,
    surface = AuraAmoledSurface,
    surfaceVariant = AuraAmoledSurface,
    onPrimary = AuraAmoledBackground,
    onSecondary = AuraAmoledBackground,
    onBackground = AuraTextPrimary,
    onSurface = AuraTextPrimary,
    onSurfaceVariant = AuraTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = AuraPrimary,
    secondary = AuraSecondary,
    tertiary = AuraTertiary,
    background = AuraLightBackground,
    surface = AuraLightSurface,
    surfaceVariant = AuraLightSurfaceVariant,
    onPrimary = AuraTextPrimary,
    onSecondary = AuraTextPrimary,
    onBackground = AuraLightTextPrimary,
    onSurface = AuraLightTextPrimary,
    onSurfaceVariant = AuraLightTextSecondary
)

@Composable
fun AuraMusicTheme(
    themeMode: String = "SYSTEM", // "SYSTEM", "DARK", "LIGHT", "AMOLED", "DYNAMIC"
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when (themeMode) {
        "AMOLED" -> AmoledColorScheme
        "LIGHT" -> LightColorScheme
        "DARK" -> DarkColorScheme
        "DYNAMIC" -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }
        "SYSTEM" -> {
            if (darkTheme) DarkColorScheme else LightColorScheme
        }
        else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
