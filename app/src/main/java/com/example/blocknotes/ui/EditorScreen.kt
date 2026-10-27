package com.example.blocknotes.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blocknotes.data.Block
import com.example.blocknotes.data.BlockType
import com.example.blocknotes.data.Page

private fun numberedIndex(list: List<Block>, idx: Int): Int {
    var n = 0
    for (i in idx downTo 0) {
        if (list[i].type == BlockType.NUMBERED) n++ else break
    }
    return n
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(pageId: String, onSubpages: (String) -> Unit, onBack: () -> Unit) {
    val vm: EditorVM = viewModel(key = "page-$pageId", factory = EditorVM.factory(pageId))
    val pageState by vm.page.collectAsStateWithLifecycle()
    val blocks by vm.blocks.collectAsStateWithLifecycle()
    val p: Page = pageState ?: Page(id = pageId)
    val ctx = LocalContext.current
    val font = fontFamilyOf(p.font)
    var showSettings by remember { mutableStateOf(false) }
    var imageAfter by remember { mutableStateOf<Block?>(null) }
    var title by remember(pageId) { mutableStateOf<String?>(null) }
    val shownTitle = title ?: p.title
    val headings = blocks.filter { it.type == BlockType.H1 || it.type == BlockType.H2 || it.type == BlockType.H3 }

    LaunchedEffect(pageId) { vm.loadCrumbs() }

    val pickCover = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            runCatching { ctx.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            vm.updatePage { it.copy(coverUri = uri.toString(), coverColor = "") }
        }
    }
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val after = imageAfter
        if (uri != null) {
            runCatching { ctx.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            vm.insertAfter(after, meta = uri.toString(), type = BlockType.IMAGE)
        }
        imageAfter = null
    }
    val pickCoverRequest = { pickCover.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        vm.crumbs.joinToString("  ›  ") { it.title.ifBlank { "Sin título" } },
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                },
                actions = {
                    IconButton(onClick = { shareText(ctx, shownTitle, pageToMarkdown(p, blocks)) }) {
                        Icon(Icons.Default.Share, "Exportar")
                    }
                    IconButton(onClick = { onSubpages(pageId) }) {
                        Icon(Icons.AutoMirrored.Filled.List, "Subpáginas")
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, "Ajustes de página")
                    }
                },
            )
        },
    ) { pad ->
        LazyColumn(
            Modifier.padding(pad).fillMaxSize().imePadding(),
            contentPadding = PaddingValues(bottom = 240.dp),
        ) {
            item {
                CoverArea(p) { showSettings = true }
            }
            item {
                Column(Modifier.padding(horizontal = 24.dp).padding(top = 16.dp, bottom = 8.dp)) {
                    if (p.icon.isNotBlank()) {
                        Text(
                            p.icon,
                            fontSize = 44.sp,
                            modifier = Modifier.clickable { showSettings = true }.padding(bottom = 4.dp),
                        )
                    } else {
                        Text(
                            "＋ Añadir icono",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { showSettings = true }.padding(bottom = 4.dp),
                        )
                    }
                    BasicTextField(
                        value = shownTitle,
                        onValueChange = { v ->
                            title = v
                            vm.updatePage { pg -> pg.copy(title = v) }
                        },
                        textStyle = TextStyle(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = font,
                            lineHeight = 42.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (shownTitle.isEmpty()) {
                                Text(
                                    "Sin título",
                                    style = TextStyle(
                                        fontSize = 34.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = font,
                                        color = MaterialTheme.colorScheme.outline,
                                    ),
                                )
                            }
                            inner()
                        },
                    )
                }
            }
            itemsIndexed(blocks, key = { _, b -> b.id }) { idx, b ->
                Box(Modifier.padding(horizontal = 16.dp)) {
                    BlockRow(
                        b = b,
                        numberedIdx = numberedIndex(blocks, idx),
                        font = font,
                        headings = headings,
                        vm = vm,
                        onPickImage = { after ->
                            imageAfter = after
                            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        canUp = idx > 0,
                        canDown = idx < blocks.lastIndex,
                    )
                }
            }
            item {
                TextButton(onClick = { vm.insertAfter(null) }, modifier = Modifier.padding(start = 16.dp)) {
                    Text("+ Añadir bloque")
                }
            }
        }
    }

    if (showSettings) {
        PageSettingsSheet(
            page = p,
            onDismiss = { showSettings = false },
            onUpdate = { f -> vm.updatePage(f) },
            onPickCover = pickCoverRequest,
        )
    }
}

@Composable
private fun CoverArea(p: Page, onClick: () -> Unit) {
    val ctx = LocalContext.current
    val bmp = remember(p.coverUri) {
        if (p.coverUri.isBlank()) null else loadBitmap(ctx.contentResolver, p.coverUri)
    }
    if (p.coverColor.isNotBlank() || p.coverUri.isNotBlank()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    if (p.coverColor.isNotBlank()) parseColor(p.coverColor) else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable(onClick = onClick),
        ) {
            if (bmp != null) {
                Image(
                    bitmap = bmp,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PageSettingsSheet(
    page: Page,
    onDismiss: () -> Unit,
    onUpdate: ((Page) -> Page) -> Unit,
    onPickCover: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text("Icono", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    Text(
                        "Ninguno",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onUpdate { it.copy(icon = "") } }
                            .padding(8.dp),
                    )
                }
                items(EMOJIS) { e ->
                    Text(
                        e,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onUpdate { it.copy(icon = e) } }
                            .padding(6.dp),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Portada", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                COVER_COLORS.forEach { c ->
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (c.isBlank()) MaterialTheme.colorScheme.surfaceVariant else parseColor(c))
                            .clickable { onUpdate { it.copy(coverColor = c, coverUri = "") } },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onPickCover, modifier = Modifier.fillMaxWidth()) {
                Text("Elegir imagen de mi galería")
            }
            TextButton(onClick = { onUpdate { it.copy(coverColor = "", coverUri = "") } }) {
                Text("Quitar portada")
            }

            Spacer(Modifier.height(20.dp))
            Text("Tipografía", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FONTS.forEach { (code, label) ->
                    FilterChip(
                        selected = page.font == code,
                        onClick = { onUpdate { it.copy(font = code) } },
                        label = { Text(label, fontFamily = fontFamilyOf(code)) },
                    )
                }
            }
        }
    }
}
