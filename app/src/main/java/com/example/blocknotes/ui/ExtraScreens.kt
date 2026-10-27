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
fun SearchScreen(onOpen: (String) -> Unit, onBack: () -> Unit) {
    val vm: SearchVM = viewModel()
    val results by vm.results.collectAsStateWithLifecycle()
    var q by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = q,
                        onValueChange = {
                            q = it
                            vm.query.value = it
                        },
                        placeholder = { Text("Buscar páginas y texto") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                },
            )
        },
    ) { pad ->
        LazyColumn(Modifier.padding(pad)) {
            items(results, key = { it.id }) { p ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(p.id) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(p.icon.ifBlank { "📄" }, fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(p.title.ifBlank { "Sin título" }, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(onBack: () -> Unit) {
    val vm: TrashVM = viewModel()
    val trashed by vm.trashed.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Papelera", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                },
            )
        },
    ) { pad ->
        if (trashed.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("La papelera está vacía", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(Modifier.padding(pad)) {
                items(trashed, key = { it.id }) { p ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(p.title.ifBlank { "Sin título" }, Modifier.weight(1f), fontSize = 16.sp)
                        TextButton(onClick = { vm.restore(p.id) }) { Text("Restaurar") }
                    }
                }
            }
        }
    }
}
