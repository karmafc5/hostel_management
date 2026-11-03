package com.hostelmaintenance.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

private val DefaultTypography = Typography()
private val AppTypography = Typography(
    displayLarge = DefaultTypography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
    displayMedium = DefaultTypography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
    displaySmall = DefaultTypography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
    headlineLarge = DefaultTypography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
    headlineMedium = DefaultTypography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
    headlineSmall = DefaultTypography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
    titleLarge = DefaultTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = DefaultTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = DefaultTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = DefaultTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
    bodyMedium = DefaultTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
    bodySmall = DefaultTypography.bodySmall.copy(fontWeight = FontWeight.Medium),
    labelLarge = DefaultTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = DefaultTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
    labelSmall = DefaultTypography.labelSmall.copy(fontWeight = FontWeight.Medium)
)

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = Teal,
    background = CanvasLight,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = Navy,
    onSurfaceVariant = MutedText
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF9DDDEB),
    secondary = androidx.compose.ui.graphics.Color(0xFF9DDDEB),
    background = NavyDark,
    surface = SurfaceDark,
    onSurface = TextOnDark,
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFCAD5DF)
)

@Composable
fun HostelMaintenanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
