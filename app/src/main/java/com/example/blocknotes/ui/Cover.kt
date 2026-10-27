package com.example.blocknotes.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.blocknotes.data.Page

private val gradients = listOf(
    listOf(Color(0xFF6D4AFF), Color(0xFFFF7AC6)),
    listOf(Color(0xFF0EA5E9), Color(0xFF22C55E)),
    listOf(Color(0xFFF97316), Color(0xFFEF4444)),
    listOf(Color(0xFF14B8A6), Color(0xFF6366F1)),
    listOf(Color(0xFFEAB308), Color(0xFFF97316)),
    listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)),
)

/** Portada de una página: foto de la galería, color elegido o degradado automático. */
@Composable
fun PageCover(p: Page, modifier: Modifier, content: @Composable BoxScope.() -> Unit = {}) {
    val ctx = LocalContext.current
    val bmp = remember(p.coverUri) { if (p.coverUri.isBlank()) null else loadBitmap(ctx.contentResolver, p.coverUri) }
    val grad = gradients[(p.id.hashCode() and Int.MAX_VALUE) % gradients.size]
    Box(modifier) {
        if (bmp != null) {
            Image(bitmap = bmp, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else if (p.coverColor.isNotBlank()) {
            Box(Modifier.fillMaxSize().background(parseColor(p.coverColor)))
        } else {
            Box(Modifier.fillMaxSize().background(Brush.linearGradient(grad)))
        }
        content()
    }
}
