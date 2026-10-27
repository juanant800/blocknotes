package com.example.blocknotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blocknotes.data.Block
import com.example.blocknotes.data.BlockType

private val listTypes = setOf(BlockType.TODO, BlockType.BULLET, BlockType.NUMBERED)

// Atajos tipo Markdown: se activan al escribir el prefijo al inicio de un bloque de texto.
private val shortcuts = listOf(
    "# " to BlockType.H1,
    "## " to BlockType.H2,
    "### " to BlockType.H3,
    "- " to BlockType.BULLET,
    "* " to BlockType.BULLET,
    "1. " to BlockType.NUMBERED,
    "[] " to BlockType.TODO,
    "> " to BlockType.QUOTE,
)

private fun styleFor(t: BlockType, font: FontFamily, color: androidx.compose.ui.graphics.Color): TextStyle = when (t) {
    BlockType.H1 -> TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = font, lineHeight = 36.sp, color = color)
    BlockType.H2 -> TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, fontFamily = font, lineHeight = 30.sp, color = color)
    BlockType.H3 -> TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, fontFamily = font, lineHeight = 26.sp, color = color)
    BlockType.CODE -> TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace, lineHeight = 20.sp, color = color)
    BlockType.QUOTE -> TextStyle(fontSize = 17.sp, fontStyle = FontStyle.Italic, fontFamily = font, lineHeight = 24.sp, color = color)
    else -> TextStyle(fontSize = 16.sp, fontFamily = font, lineHeight = 24.sp, color = color)
}

private fun placeholderFor(t: BlockType): String = when (t) {
    BlockType.TEXT -> "Escribe / para ver comandos…"
    BlockType.TODO -> "Tarea pendiente"
    BlockType.BULLET -> "Elemento de lista"
    BlockType.NUMBERED -> "Elemento numerado"
    else -> ""
}

@Composable
private fun BlockField(
    value: String,
    onValue: (String) -> Unit,
    style: TextStyle,
    modifier: Modifier = Modifier,
    focus: FocusRequester? = null,
    onKey: ((KeyEvent) -> Boolean)? = null,
    placeholder: String = "",
) {
    var m = modifier
    if (focus != null) m = m.focusRequester(focus)
    if (onKey != null) m = m.onPreviewKeyEvent(onKey)
    BasicTextField(
        value = value,
        onValueChange = onValue,
        textStyle = style,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = m,
        decorationBox = { inner ->
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(placeholder, style = style.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
            inner()
        },
    )
}

@Composable
private fun ImageBlock(uri: String) {
    val ctx = LocalContext.current
    val bmp = remember(uri) { loadBitmap(ctx.contentResolver, uri) }
    if (bmp != null) {
        Image(
            bitmap = bmp,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.FillWidth,
        )
    } else {
        Text("Imagen no disponible", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TocBlock(headings: List<Block>) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        if (headings.isEmpty()) {
            Text("Añade títulos para ver el índice aquí.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        headings.forEach { h ->
            val indent = when (h.type) {
                BlockType.H2 -> 16.dp
                BlockType.H3 -> 32.dp
                else -> 0.dp
            }
            Text(
                h.text.ifBlank { "Sin título" },
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = indent, vertical = 3.dp),
            )
        }
    }
}

@Composable
fun BlockRow(
    b: Block,
    numberedIdx: Int,
    font: FontFamily,
    headings: List<Block>,
    vm: EditorVM,
    onPickImage: (Block) -> Unit,
    canUp: Boolean,
    canDown: Boolean,
) {
    val onColor = MaterialTheme.colorScheme.onSurface
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val hasText = b.type != BlockType.DIVIDER && b.type != BlockType.IMAGE && b.type != BlockType.TOC
    var text by remember(b.id) { mutableStateOf(b.text) }
    var body by remember(b.id) { mutableStateOf(b.meta) }
    var menu by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }

    LaunchedEffect(vm.focusId) {
        if (vm.focusId == b.id) {
            if (hasText) runCatching { focus.requestFocus() }
            vm.focusId = null
        }
    }

    fun onTextChange(new: String) {
        if (b.type == BlockType.TEXT && new == "---") {
            vm.update(b.copy(type = BlockType.DIVIDER, text = ""))
            vm.insertAfter(b)
            return
        }
        if (b.type == BlockType.TEXT) {
            val sc = shortcuts.firstOrNull { new.startsWith(it.first) }
            if (sc != null) {
                text = new.removePrefix(sc.first)
                vm.update(b.copy(type = sc.second, text = text))
                return
            }
        }
        if ('\n' in new && b.type != BlockType.CODE) {
            val before = new.substringBefore('\n')
            val after = new.substringAfter('\n')
            if (before.isEmpty() && after.isEmpty() && b.type in listTypes) {
                text = ""
                vm.update(b.copy(type = BlockType.TEXT, text = ""))
                return
            }
            text = before
            vm.update(b.copy(text = before))
            val keep = if (b.type in listTypes || b.type == BlockType.QUOTE) b.type else BlockType.TEXT
            vm.insertAfter(b, after, keep)
            return
        }
        text = new
        vm.update(b.copy(text = new))
    }

    val backspace: (KeyEvent) -> Boolean = { e ->
        if (e.type == KeyEventType.KeyDown && e.key == Key.Backspace && text.isEmpty()) {
            if (b.type != BlockType.TEXT) vm.update(b.copy(type = BlockType.TEXT, text = "")) else vm.remove(b)
            true
        } else {
            false
        }
    }

    val baseStyle = styleFor(b.type, font, onColor).let {
        if (b.type == BlockType.TODO && b.checked) it.copy(textDecoration = TextDecoration.LineThrough) else it
    }
    val fieldFocus = if (hasText) focus else null

    Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(Modifier.width(26.dp).padding(top = 4.dp), contentAlignment = Alignment.TopCenter) {
                when (b.type) {
                    BlockType.TODO -> Checkbox(
                        checked = b.checked,
                        onCheckedChange = { c -> vm.update(b.copy(checked = c, text = text)) },
                        modifier = Modifier.size(22.dp),
                    )
                    BlockType.BULLET -> Text("•", fontSize = 18.sp, color = onColor)
                    BlockType.NUMBERED -> Text("$numberedIdx.", fontSize = 16.sp, color = onColor)
                    BlockType.TOGGLE -> Icon(
                        imageVector = if (b.checked) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = "Desplegar",
                        tint = onColor,
                        modifier = Modifier.size(22.dp).clickable { vm.update(b.copy(checked = !b.checked, text = text)) },
                    )
                    else -> Unit
                }
            }

            Column(Modifier.weight(1f)) {
                when (b.type) {
                    BlockType.DIVIDER -> HorizontalDivider(
                        Modifier.padding(vertical = 14.dp),
                        color = MaterialTheme.colorScheme.outline,
                    )
                    BlockType.IMAGE -> ImageBlock(b.meta)
                    BlockType.TOC -> TocBlock(headings)
                    BlockType.CALLOUT -> Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = b.meta.ifBlank { "💡" },
                            fontSize = 20.sp,
                            modifier = Modifier.clickable { vm.update(b.copy(meta = nextEmoji(b.meta), text = text)) },
                        )
                        Spacer(Modifier.width(10.dp))
                        BlockField(
                            value = text,
                            onValue = { onTextChange(it) },
                            style = baseStyle,
                            modifier = Modifier.weight(1f),
                            focus = fieldFocus,
                            onKey = backspace,
                            placeholder = "Destacado…",
                        )
                    }
                    BlockType.QUOTE -> Row(Modifier.height(IntrinsicSize.Min)) {
                        Box(Modifier.width(3.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                        Spacer(Modifier.width(12.dp))
                        BlockField(
                            value = text,
                            onValue = { onTextChange(it) },
                            style = baseStyle,
                            modifier = Modifier.weight(1f),
                            focus = fieldFocus,
                            onKey = backspace,
                            placeholder = "Cita…",
                        )
                    }
                    BlockType.TOGGLE -> Column(Modifier.fillMaxWidth()) {
                        BlockField(
                            value = text,
                            onValue = { onTextChange(it) },
                            style = baseStyle,
                            modifier = Modifier.fillMaxWidth(),
                            focus = fieldFocus,
                            onKey = backspace,
                            placeholder = "Desplegable",
                        )
                        if (b.checked) {
                            BlockField(
                                value = body,
                                onValue = { body = it; vm.update(b.copy(meta = it, text = text)) },
                                style = styleFor(BlockType.TEXT, font, muted),
                                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 6.dp),
                                placeholder = "Contenido oculto…",
                            )
                        }
                    }
                    BlockType.CODE -> Box(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp),
                    ) {
                        BlockField(
                            value = text,
                            onValue = { onTextChange(it) },
                            style = baseStyle,
                            modifier = Modifier.fillMaxWidth(),
                            focus = fieldFocus,
                            onKey = backspace,
                            placeholder = "Código…",
                        )
                    }
                    else -> BlockField(
                        value = text,
                        onValue = { onTextChange(it) },
                        style = baseStyle,
                        modifier = Modifier.fillMaxWidth(),
                        focus = fieldFocus,
                        onKey = backspace,
                        placeholder = placeholderFor(b.type),
                    )
                }

                if (hasText && text.startsWith("/")) {
                    val q = text.drop(1).lowercase()
                    LazyRow(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(BlockType.entries.filter { labelOf(it).lowercase().contains(q) }) { t ->
                            AssistChip(
                                onClick = {
                                    text = ""
                                    if (t == BlockType.IMAGE) onPickImage(b) else vm.update(b.copy(type = t, text = ""))
                                },
                                label = { Text(labelOf(t)) },
                            )
                        }
                    }
                }
            }

            Box(Modifier.width(28.dp)) {
                IconButton(onClick = { menu = true }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones del bloque",
                        tint = muted,
                        modifier = Modifier.size(18.dp),
                    )
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    if (canUp) {
                        DropdownMenuItem(
                            text = { Text("Subir") },
                            leadingIcon = { Icon(Icons.Default.ArrowUpward, null) },
                            onClick = { menu = false; vm.move(b, -1) },
                        )
                    }
                    if (canDown) {
                        DropdownMenuItem(
                            text = { Text("Bajar") },
                            leadingIcon = { Icon(Icons.Default.ArrowDownward, null) },
                            onClick = { menu = false; vm.move(b, 1) },
                        )
                    }
                    HorizontalDivider()
                    BlockType.entries.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(labelOf(t)) },
                            onClick = {
                                menu = false
                                if (t == BlockType.IMAGE) onPickImage(b) else vm.update(b.copy(type = t))
                            },
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Borrar") },
                        leadingIcon = { Icon(Icons.Default.Delete, null) },
                        onClick = { menu = false; vm.remove(b) },
                    )
                }
            }
        }
    }
}
