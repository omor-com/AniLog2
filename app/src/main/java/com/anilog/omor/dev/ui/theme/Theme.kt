package com.anilog.omor.dev.ui.theme

import android.app.Activity
import android.os.Build
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
import com.anilog.omor.dev.domain.model.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Violet80,
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    tertiary = StarGold,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

private val AmoledColorScheme = darkColorScheme(
    primary = Violet80,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF381E72),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color.Black,
    tertiary = StarGold,
    background = AmoledBackground,
    onBackground = Color(0xFFEEEEEE),
    surface = AmoledSurface,
    onSurface = Color(0xFFEEEEEE),
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = Color(0xFFB0ACC0),
    outline = AmoledOutline
)

private val LightColorScheme = lightColorScheme(
    primary = VioletPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    tertiary = StarGold,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

@Composable
fun AniLogTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        AppThemeMode.SYSTEM -> if (isSystemDark) DarkColorScheme else LightColorScheme
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.AMOLED -> AmoledColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val isLight = colorScheme.background == LightBackground
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLight
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
