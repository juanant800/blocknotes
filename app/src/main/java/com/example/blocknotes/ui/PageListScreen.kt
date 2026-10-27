package com.example.blocknotes.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageListScreen(parentId: String?, onOpen: (String) -> Unit, onBack: () -> Unit) {
    val vm: ListVM = viewModel(key = "list-$parentId", factory = ListVM.factory(parentId))
    val pages by vm.pages.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (parentId == null) "Páginas" else "Subpáginas") },
                navigationIcon = { if (parentId != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = { vm.create(onOpen) }) { Icon(Icons.Default.Add, "Nueva página") } },
    ) { pad ->
        LazyColumn(Modifier.padding(pad)) {
            items(pages, key = { it.id }) { p ->
                ListItem(
                    headlineContent = { Text(p.title.ifBlank { "Sin título" }) },
                    trailingContent = { IconButton(onClick = { vm.delete(p.id) }) { Icon(Icons.Default.Delete, "Borrar") } },
                    modifier = Modifier.clickable { onOpen(p.id) },
                )
            }
        }
    }
}
