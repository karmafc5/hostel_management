package com.hostelmaintenance.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = Teal,
    background = CanvasLight,
    surface = androidx.compose.ui.graphics.Color.White
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFB7C9E2),
    secondary = androidx.compose.ui.graphics.Color(0xFF78D1D1),
    background = NavyDark,
    surface = SurfaceDark
)

@Composable
fun HostelMaintenanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
