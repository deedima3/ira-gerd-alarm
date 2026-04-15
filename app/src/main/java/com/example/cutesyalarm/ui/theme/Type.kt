package com.example.cutesyalarm.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Using system fonts with custom styling
val CuteFontFamily = FontFamily.Default

@Composable
fun getScreenWidthDp(): Float {
    return LocalConfiguration.current.screenWidthDp.toFloat()
}

@Composable
fun getScaleFactor(): Float {
    val screenWidth = getScreenWidthDp()
    // Base width is 360dp (medium phone), scale down for smaller screens
    return when {
        screenWidth < 320 -> 0.75f  // Very small phones
        screenWidth < 360 -> 0.85f  // Small phones
        screenWidth < 400 -> 0.95f  // Medium phones
        else -> 1.0f  // Large phones
    }
}

@Composable
fun getResponsiveTypography(): Typography {
    val scale = getScaleFactor()
    
    return Typography(
        displayLarge = TextStyle(
            fontFamily = CuteFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (48 * scale).sp,
            lineHeight = (56 * scale).sp,
            letterSpacing = (-0.5).sp,
            color = CuteColors.TextPrimary
        ),
        displayMedium = TextStyle(
            fontFamily = CuteFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (36 * scale).sp,
            lineHeight = (44 * scale).sp,
            letterSpacing = (-0.25).sp,
            color = CuteColors.TextPrimary
        ),
        displaySmall = TextStyle(
            fontFamily = CuteFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (28 * scale).sp,
            lineHeight = (36 * scale).sp,
            letterSpacing = 0.sp,
            color = CuteColors.TextPrimary
        ),
        headlineLarge = TextStyle(
            fontFamily = CuteFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (32 * scale).sp,
            lineHeight = (40 * scale).sp,
            letterSpacing = 0.sp,
            color = CuteColors.TextPrimary
        ),
        headlineMedium = TextStyle(
            fontFamily = CuteFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (28 * scale).sp,
            lineHeight = (36 * scale).sp,
            letterSpacing = 0.sp,
            color = CuteColors.TextPrimary
        ),
        headlineSmall = TextStyle(
            fontFamily = CuteFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (24 * scale).sp,
            lineHeight = (32 * scale).sp,
            letterSpacing = 0.sp,
            color = CuteColors.TextPrimary
        ),
        titleLarge = TextStyle(
            fontFamily = CuteFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (22 * scale).sp,
            lineHeight = (28 * scale).sp,
            letterSpacing = 0.sp,
            color = CuteColors.TextPrimary
        ),
        titleMedium = TextStyle(
            fontFamily = CuteFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (16 * scale).sp,
            lineHeight = (24 * scale).sp,
            letterSpacing = 0.15.sp,
            color = CuteColors.TextPrimary
        ),
        titleSmall = TextStyle(
            fontFamily = CuteFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp,
            letterSpacing = 0.1.sp,
            color = CuteColors.TextPrimary
        ),
        bodyLarge = TextStyle(
            fontFamily = CuteFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * scale).sp,
            lineHeight = (24 * scale).sp,
            letterSpacing = 0.5.sp,
            color = CuteColors.TextPrimary
        ),
        bodyMedium = TextStyle(
            fontFamily = CuteFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp,
            letterSpacing = 0.25.sp,
            color = CuteColors.TextPrimary
        ),
        bodySmall = TextStyle(
            fontFamily = CuteFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * scale).sp,
            lineHeight = (16 * scale).sp,
            letterSpacing = 0.4.sp,
            color = CuteColors.TextSecondary
        ),
        labelLarge = TextStyle(
            fontFamily = CuteFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp,
            letterSpacing = 0.1.sp,
            color = CuteColors.TextPrimary
        ),
        labelMedium = TextStyle(
            fontFamily = CuteFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * scale).sp,
            lineHeight = (16 * scale).sp,
            letterSpacing = 0.5.sp,
            color = CuteColors.TextSecondary
        ),
        labelSmall = TextStyle(
            fontFamily = CuteFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * scale).sp,
            lineHeight = (16 * scale).sp,
            letterSpacing = 0.5.sp,
            color = CuteColors.TextLight
        )
    )
}

// Legacy non-scaled typography for backward compatibility
val CuteTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = CuteFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.5).sp,
        color = CuteColors.TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = CuteFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.25).sp,
        color = CuteColors.TextPrimary
    ),
    displaySmall = TextStyle(
        fontFamily = CuteFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
        color = CuteColors.TextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = CuteFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
        color = CuteColors.TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = CuteFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
        color = CuteColors.TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = CuteFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        color = CuteColors.TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = CuteFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = CuteColors.TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = CuteFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        color = CuteColors.TextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily = CuteFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = CuteColors.TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = CuteFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = CuteColors.TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = CuteFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = CuteColors.TextPrimary
    ),
    bodySmall = TextStyle(
        fontFamily = CuteFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = CuteColors.TextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = CuteFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = CuteColors.TextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = CuteFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = CuteColors.TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = CuteFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = CuteColors.TextLight
    )
)