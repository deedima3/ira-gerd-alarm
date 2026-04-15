package com.example.cutesyalarm.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = CuteColors.PrimaryPink,
    secondary = CuteColors.Mint,
    tertiary = CuteColors.Lavender,
    background = CuteColors.Background,
    surface = CuteColors.Surface,
    onPrimary = CuteColors.TextPrimary,
    onSecondary = CuteColors.TextPrimary,
    onTertiary = CuteColors.TextPrimary,
    onBackground = CuteColors.TextPrimary,
    onSurface = CuteColors.TextPrimary,
    error = CuteColors.AccentPink,
    onError = CuteColors.White
)

private val DarkColorScheme = darkColorScheme(
    primary = CuteColors.AccentPink,
    secondary = CuteColors.DarkMint,
    tertiary = CuteColors.Lavender,
    background = CuteColors.TextPrimary,
    surface = Color(0xFF4A3F4E),
    onPrimary = CuteColors.White,
    onSecondary = CuteColors.White,
    onTertiary = CuteColors.TextPrimary,
    onBackground = CuteColors.White,
    onSurface = CuteColors.White
)

@Composable
fun CutesyAlarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getResponsiveTypography(),
        content = content
    )
}