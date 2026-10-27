package com.example.blocknotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blocknotes.data.BlockType

data class TypeInfo(val emoji: String, val desc: String, val color: Color)

fun infoOf(t: BlockType): TypeInfo = when (t) {
    BlockType.TEXT -> TypeInfo("Aa", "Párrafo de texto normal", Color(0xFF9EA3B8))
    BlockType.H1 -> TypeInfo("H1", "Encabezado grande", Color(0xFFC7B4FF))
    BlockType.H2 -> TypeInfo("H2", "Encabezado mediano", Color(0xFFB39DFF))
    BlockType.H3 -> TypeInfo("H3", "Encabezado pequeño", Color(0xFFA08BFF))
    BlockType.TODO -> TypeInfo("☑", "Lista con casillas para marcar", Color(0xFF4ADE80))
    BlockType.BULLET -> TypeInfo("•", "Lista con viñetas", Color(0xFF60A5FA))
    BlockType.NUMBERED -> TypeInfo("1.", "Lista numerada automática", Color(0xFF38BDF8))
    BlockType.TOGGLE -> TypeInfo("▸", "Bloque que se puede plegar", Color(0xFFF472B6))
    BlockType.QUOTE -> TypeInfo("❝", "Cita con barra lateral", Color(0xFFFB923C))
    BlockType.CALLOUT -> TypeInfo("💡", "Aviso con emoji y fondo", Color(0xFFFACC15))
    BlockType.CODE -> TypeInfo("</>", "Código con letra monoespaciada", Color(0xFF94A3B8))
    BlockType.DIVIDER -> TypeInfo("—", "Línea separadora", Color(0xFF9EA3B8))
    BlockType.IMAGE -> TypeInfo("🖼", "Imagen desde tu galería", Color(0xFF2DD4BF))
    BlockType.TOC -> TypeInfo("📑", "Índice con tus títulos", Color(0xFFA78BFF))
}

/** Cuadrícula de tipos de bloque: icono, nombre y descripción en cada tarjeta. */
@Composable
fun BlockTypeList(types: List<BlockType>, onPick: (BlockType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        types.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { t -> TypeTile(t, Modifier.weight(1f)) { onPick(t) } }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TypeTile(t: BlockType, modifier: Modifier, onClick: () -> Unit) {
    val info = infoOf(t)
    Row(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(info.color.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(info.emoji, color = info.color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(labelOf(t), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1)
            Text(info.desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
    }
}
