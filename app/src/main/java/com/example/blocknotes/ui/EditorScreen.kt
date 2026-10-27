package com.example.blocknotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blocknotes.data.Block
import com.example.blocknotes.data.BlockType

private val slashMenu = listOf(
    "Texto" to BlockType.TEXT, "Título 1" to BlockType.H1, "Título 2" to BlockType.H2,
    "Tarea" to BlockType.TODO, "Lista" to BlockType.BULLET, "Código" to BlockType.CODE,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(pageId: String, onSubpages: (String) -> Unit, onBack: () -> Unit) {
    val vm: EditorVM = viewModel(key = "page-$pageId", factory = EditorVM.factory(pageId))
    val page by vm.page.collectAsStateWithLifecycle()
    val blocks by vm.blocks.collectAsStateWithLifecycle()
    var title by remember(page?.id) { mutableStateOf(page?.title ?: "") }

    Scaffold(topBar = {
        TopAppBar(
            title = {},
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            actions = { IconButton(onClick = { onSubpages(pageId) }) { Icon(Icons.AutoMirrored.Filled.List, "Subpáginas") } },
        )
    }) { pad ->
        LazyColumn(Modifier.padding(pad).imePadding().padding(horizontal = 16.dp)) {
            item {
                BasicTextField(
                    value = title, onValueChange = { title = it; vm.setTitle(it) },
                    textStyle = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { inner -> if (title.isEmpty()) Text("Sin título", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline); inner() },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                )
            }
            items(blocks, key = { it.id }) { b -> BlockRow(b, vm) }
            item { TextButton(onClick = { vm.insertAfter(null) }) { Text("+ Añadir bloque") } }
        }
    }
}

@Composable
private fun BlockRow(b: Block, vm: EditorVM) {
    var text by remember(b.id) { mutableStateOf(b.text) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(vm.focusId) { if (vm.focusId == b.id) { focus.requestFocus(); vm.focusId = null } }

    val style = when (b.type) {
        BlockType.H1 -> TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold)
        BlockType.H2 -> TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        BlockType.CODE -> TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace)
        else -> TextStyle(fontSize = 16.sp)
    }.let { it.copy(color = MaterialTheme.colorScheme.onSurface,
        textDecoration = if (b.type == BlockType.TODO && b.checked) TextDecoration.LineThrough else null) }

    Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when (b.type) {
                BlockType.TODO -> Checkbox(b.checked, { vm.update(b.copy(checked = it, text = text)) })
                BlockType.BULLET -> Text("•  ", fontSize = 18.sp)
                else -> {}
            }
            BasicTextField(
                value = text,
                onValueChange = { new ->
                    if ('\n' in new) {                       // Enter → parte el bloque en dos
                        val (before, after) = new.substringBefore('\n') to new.substringAfter('\n')
                        text = before; vm.update(b.copy(text = before))
                        val keep = if (b.type == BlockType.TODO || b.type == BlockType.BULLET) b.type else BlockType.TEXT
                        vm.insertAfter(b, after, keep)
                    } else { text = new; vm.update(b.copy(text = new)) }
                },
                textStyle = style,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions.Default,
                modifier = Modifier.weight(1f).focusRequester(focus)
                    .then(if (b.type == BlockType.CODE) Modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp) else Modifier)
                    .onPreviewKeyEvent { e ->                // Backspace en bloque vacío → lo borra
                        if (e.type == KeyEventType.KeyDown && e.key == Key.Backspace && text.isEmpty()) { vm.remove(b); true } else false
                    },
            )
        }
        if (text.startsWith("/")) {                           // menú "/" para cambiar el tipo
            val q = text.drop(1).lowercase()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                slashMenu.filter { it.first.lowercase().contains(q) }.take(4).forEach { (label, type) ->
                    AssistChip(onClick = { text = ""; vm.update(b.copy(type = type, text = "")) }, label = { Text(label) })
                }
            }
        }
    }
}
