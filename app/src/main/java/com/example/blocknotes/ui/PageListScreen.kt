package com.example.blocknotes.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageListScreen(
    parentId: String?,
    onOpen: (String) -> Unit,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onTrash: () -> Unit,
) {
    val vm: ListVM = viewModel(key = "list-$parentId", factory = ListVM.factory(parentId))
    val pages by vm.pages.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (parentId == null) "Mis páginas" else "Subpáginas",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    if (parentId != null) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                    }
                },
                actions = {
                    IconButton(onClick = onSearch) { Icon(Icons.Default.Search, "Buscar") }
                    if (parentId == null) {
                        IconButton(onClick = onTrash) { Icon(Icons.Default.Delete, "Papelera") }
                    }
                },
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (parentId == null) {
                    SmallFloatingActionButton(onClick = { vm.quickNote(onOpen) }) {
                        Icon(Icons.Default.Edit, "Apunte rápido")
                    }
                }
                FloatingActionButton(onClick = { vm.create(onOpen) }) {
                    Icon(Icons.Default.Add, "Nueva página")
                }
            }
        },
    ) { pad ->
        if (pages.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text(
                    "Aún no hay páginas. Pulsa + para crear una.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(Modifier.padding(pad)) {
                items(pages, key = { it.id }) { p ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(p.id) }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(p.icon.ifBlank { "📄" }, fontSize = 20.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            p.title.ifBlank { "Sin título" },
                            Modifier.weight(1f),
                            fontFamily = fontFamilyOf(p.font),
                            fontWeight = FontWeight.Medium,
                        )
                        IconButton(onClick = { vm.trash(p.id) }) {
                            Icon(Icons.Default.Delete, "Mover a la papelera")
                        }
                    }
                }
            }
        }
    }
}
