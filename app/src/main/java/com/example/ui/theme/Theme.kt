package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = DeepLavender,
    onPrimary = SoftWhite,
    primaryContainer = PalePink,
    onPrimaryContainer = MainText,
    secondary = SoftLavender,
    onSecondary = MainText,
    secondaryContainer = SoftCardBg,
    onSecondaryContainer = MainText,
    tertiary = GentleSuccess,
    background = WarmCream,
    onBackground = MainText,
    surface = SoftWhite,
    onSurface = MainText,
    surfaceVariant = SoftCardBg,
    onSurfaceVariant = MutedText,
    outline = CardBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkSoftLavender,
    onPrimary = DarkBg,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = DarkMainText,
    secondary = DarkDeepLavender,
    onSecondary = DarkMainText,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = DarkMainText,
    tertiary = GentleSuccess,
    background = DarkBg,
    onBackground = DarkMainText,
    surface = DarkSurface,
    onSurface = DarkMainText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMutedText,
    outline = DarkBorder
)

@Composable
fun KhawlaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
