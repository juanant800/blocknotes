package com.example.blocknotes.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

private val Light = lightColorScheme(
    primary = Color(0xFF2383E2), onPrimary = Color.White,
    background = Color(0xFFFFFFFF), onBackground = Color(0xFF37352F),
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF37352F),
    surfaceVariant = Color(0xFFF1F1EF), onSurfaceVariant = Color(0xFF787774),
    outline = Color(0xFFE3E2DE),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF529CCA), onPrimary = Color.Black,
    background = Color(0xFF191919), onBackground = Color(0xFFEFEFEF),
    surface = Color(0xFF191919), onSurface = Color(0xFFEFEFEF),
    surfaceVariant = Color(0xFF2F2F2F), onSurfaceVariant = Color(0xFF9B9A97),
    outline = Color(0xFF3A3A3A),
)

@Composable
fun BlockNotesTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}

fun fontFamilyOf(code: String): FontFamily = when (code) {
    "SERIF" -> FontFamily.Serif
    "MONO" -> FontFamily.Monospace
    "CURSIVE" -> FontFamily.Cursive
    else -> FontFamily.SansSerif
}
