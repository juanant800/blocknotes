package com.example.blocknotes.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

private val Dark = darkColorScheme(
    primary = Color(0xFFC7B4FF), onPrimary = Color(0xFF1E1233),
    background = Color(0xFF0E0F16), onBackground = Color(0xFFF2F2F7),
    surface = Color(0xFF171923), onSurface = Color(0xFFF2F2F7),
    surfaceVariant = Color(0xFF232636), onSurfaceVariant = Color(0xFF9EA3B8),
    outline = Color(0xFF2C2F40),
)

@Composable
fun BlockNotesTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Dark, content = content)
}

fun fontFamilyOf(code: String): FontFamily = when (code) {
    "SERIF" -> FontFamily.Serif
    "MONO" -> FontFamily.Monospace
    "CURSIVE" -> FontFamily.Cursive
    else -> FontFamily.SansSerif
}
