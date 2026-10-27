package com.example.blocknotes.ui

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.blocknotes.data.Block
import com.example.blocknotes.data.BlockType
import com.example.blocknotes.data.Page

val EMOJIS = listOf("📘", "📝", "💡", "🎯", "🚀", "📚", "✅", "🧠", "🌱", "🔥", "🎨", "🛒", "💼", "🏠", "🎵", "⭐")
val COVER_COLORS = listOf("", "#F1F1EF", "#FBE3E4", "#FADEC9", "#FDECC8", "#DBEDDB", "#D3E5EF", "#E8DEEE")
val FONTS = listOf("SANS" to "Moderna", "SERIF" to "Serif", "MONO" to "Mono", "CURSIVE" to "Manuscrita")

fun labelOf(t: BlockType): String = when (t) {
    BlockType.TEXT -> "Texto"
    BlockType.H1 -> "Título 1"
    BlockType.H2 -> "Título 2"
    BlockType.H3 -> "Título 3"
    BlockType.TODO -> "Tarea"
    BlockType.BULLET -> "Lista"
    BlockType.NUMBERED -> "Lista numerada"
    BlockType.TOGGLE -> "Desplegable"
    BlockType.QUOTE -> "Cita"
    BlockType.CALLOUT -> "Destacado"
    BlockType.CODE -> "Código"
    BlockType.DIVIDER -> "Divisor"
    BlockType.IMAGE -> "Imagen"
    BlockType.TOC -> "Índice"
}

fun parseColor(hex: String): Color = Color(android.graphics.Color.parseColor(hex))

fun nextEmoji(cur: String): String {
    val c = cur.ifBlank { "💡" }
    val i = EMOJIS.indexOf(c)
    return EMOJIS[(i + 1) % EMOJIS.size]
}

fun loadBitmap(resolver: ContentResolver, uri: String): ImageBitmap? = try {
    val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
    resolver.openInputStream(Uri.parse(uri))?.use { BitmapFactory.decodeStream(it, null, opts) }?.asImageBitmap()
} catch (e: Exception) {
    null
}

fun pageToMarkdown(page: Page, blocks: List<Block>): String = buildString {
    append("# ").append(page.title.ifBlank { "Sin título" }).append("\n\n")
    var n = 0
    for (b in blocks) {
        if (b.type == BlockType.NUMBERED) {
            n++
        } else {
            n = 0
        }
        when (b.type) {
            BlockType.TEXT -> append(b.text).append("\n\n")
            BlockType.H1 -> append("# ").append(b.text).append("\n\n")
            BlockType.H2 -> append("## ").append(b.text).append("\n\n")
            BlockType.H3 -> append("### ").append(b.text).append("\n\n")
            BlockType.TODO -> append("- [").append(if (b.checked) "x" else " ").append("] ").append(b.text).append("\n")
            BlockType.BULLET -> append("- ").append(b.text).append("\n")
            BlockType.NUMBERED -> append(n).append(". ").append(b.text).append("\n")
            BlockType.TOGGLE -> append("**").append(b.text).append("**\n\n").append(b.meta).append("\n\n")
            BlockType.QUOTE, BlockType.CALLOUT -> append("> ").append(b.text).append("\n\n")
            BlockType.CODE -> append("```\n").append(b.text).append("\n```\n\n")
            BlockType.DIVIDER -> append("---\n\n")
            BlockType.IMAGE, BlockType.TOC -> {}
        }
    }
}

fun shareText(ctx: Context, title: String, text: String) {
    val i = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    ctx.startActivity(Intent.createChooser(i, "Exportar"))
}
