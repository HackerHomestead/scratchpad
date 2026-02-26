package com.hackercomputercompany.scratchpad.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BbsColorScheme = darkColorScheme(
    primary = BbsGreen,
    onPrimary = BbsBlack,
    secondary = BbsGreenDark,
    onSecondary = BbsBlack,
    tertiary = BbsGreen,
    onTertiary = BbsBlack,
    background = BbsBlack,
    onBackground = BbsGreen,
    surface = BbsBlackLight,
    onSurface = BbsGreen,
    surfaceVariant = BbsGray,
    onSurfaceVariant = BbsGreen
)

@Composable
fun ScratchpadTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = BbsColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BbsBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
