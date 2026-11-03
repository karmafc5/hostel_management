package com.hostelmaintenance.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

private val DefaultTypography = Typography()
private val AppTypography = Typography(
    displayLarge = DefaultTypography.displayLarge.copy(fontWeight = FontWeight.Bold),
    displayMedium = DefaultTypography.displayMedium.copy(fontWeight = FontWeight.Bold),
    displaySmall = DefaultTypography.displaySmall.copy(fontWeight = FontWeight.Bold),
    headlineLarge = DefaultTypography.headlineLarge.copy(fontWeight = FontWeight.Bold),
    headlineMedium = DefaultTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = DefaultTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
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
    primary = Navy,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = Teal,
    background = CanvasLight,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF344054)
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFB7C9E2),
    secondary = androidx.compose.ui.graphics.Color(0xFF78D1D1),
    background = NavyDark,
    surface = SurfaceDark,
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFD5DEEA)
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
