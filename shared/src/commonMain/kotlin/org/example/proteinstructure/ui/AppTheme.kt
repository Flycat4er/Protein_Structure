package org.example.proteinstructure.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF006494),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCBE6FF),
    onPrimaryContainer = Color(0xFF001E30),
    secondary = Color(0xFF50606E),
    onSecondary = Color(0xFFFFFFFF),
    surface = Color(0xFFFCFCFF),
    onSurface = Color(0xFF191C1E),
    surfaceContainer = Color(0xFFF0F4F8),
    surfaceContainerHigh = Color(0xFFE7ECEF),
    background = Color(0xFFFCFCFF),
    onBackground = Color(0xFF191C1E),
    outline = Color(0xFF71787E),
    outlineVariant = Color(0xFFC1C7CE),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FCDFF),
    onPrimary = Color(0xFF003450),
    primaryContainer = Color(0xFF004B71),
    onPrimaryContainer = Color(0xFFCBE6FF),
    secondary = Color(0xFFB7C8D8),
    onSecondary = Color(0xFF22323F),
    surface = Color(0xFF111416),
    onSurface = Color(0xFFE1E2E5),
    surfaceContainer = Color(0xFF1D2023),
    surfaceContainerHigh = Color(0xFF282B2D),
    background = Color(0xFF111416),
    onBackground = Color(0xFFE1E2E5),
    outline = Color(0xFF8B9298),
    outlineVariant = Color(0xFF41474D),
)

@Composable
fun AppTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
