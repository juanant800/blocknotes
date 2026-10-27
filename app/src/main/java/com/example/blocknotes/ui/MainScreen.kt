package com.example.blocknotes.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blocknotes.data.Event
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onOpenPage: (String) -> Unit, onSearch: () -> Unit, onTrash: () -> Unit) {
    val home: HomeVM = viewModel()
    val cal: CalendarVM = viewModel()
    var tab by remember { mutableStateOf(0) }
    var showNewEvent by remember { mutableStateOf(false) }
    val upcoming by home.upcoming.collectAsStateWithLifecycle()
    val soon = upcoming.any { it.startAt - System.currentTimeMillis() < 86_400_000L }
    val labels = listOf("Inicio", "Páginas", "Calendario", "Avisos", "Perfil")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Folder,
        Icons.Default.DateRange,
        Icons.Default.Notifications,
        Icons.Default.Person,
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                labels.forEachIndexed { i, label ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = {
                            if (i == 3 && soon) {
                                BadgedBox(badge = { Badge() }) { Icon(icons[i], label) }
                            } else {
                                Icon(icons[i], label)
                            }
                        },
                        label = { Text(label) },
                    )
                }
            }
        },
        floatingActionButton = {
            if (tab == 0) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SmallFloatingActionButton(onClick = { home.newPage("📝") { onOpenPage(it) } }) {
                        Icon(Icons.Default.Edit, "Apunte rápido")
                    }
                    FloatingActionButton(onClick = { home.newPage("📄") { onOpenPage(it) } }) {
                        Icon(Icons.Default.Add, "Nueva página")
                    }
                }
            }
        },
    ) { pad ->
        when (tab) {
            0 -> HomeTab(
                pad = pad,
                vm = home,
                onOpenPage = onOpenPage,
                onSearch = onSearch,
                onTrash = onTrash,
                onGoTab = { tab = it },
                onAddEvent = { showNewEvent = true },
            )
            1 -> PagesTab(pad, onOpenPage, onSearch, onTrash)
            2 -> CalendarTab(pad, cal, onOpenPage)
            3 -> NotificationsTab(pad, home, onOpenPage)
            else -> ProfileTab(pad)
        }
    }

    if (showNewEvent) {
        EventDialog(
            date = LocalDate.now(),
            onDismiss = { showNewEvent = false },
            onSave = { t, h, m ->
                home.addEvent(t, LocalDate.now(), h, m)
                showNewEvent = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PagesTab(pad: PaddingValues, onOpenPage: (String) -> Unit, onSearch: () -> Unit, onTrash: () -> Unit) {
    val vm: ListVM = viewModel(key = "list-root", factory = ListVM.factory(null))
    val pages by vm.pages.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(pad).windowInsetsPadding(WindowInsets.statusBars)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Páginas", fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = onSearch) { Icon(Icons.Default.Search, "Buscar") }
            IconButton(onClick = onTrash) { Icon(Icons.Default.Delete, "Papelera") }
        }
        if (pages.isEmpty()) {
            Text(
                "Aún no hay páginas. Crea una desde Inicio.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        } else {
            LazyColumn {
                items(pages, key = { it.id }) { p ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPage(p.id) }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PageCover(p, Modifier.size(52.dp).clip(RoundedCornerShape(12.dp))) {
                            Text(p.icon.ifBlank { "📄" }, fontSize = 20.sp, modifier = Modifier.align(Alignment.Center))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(p.title.ifBlank { "Sin título" }, Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        IconButton(onClick = { vm.trash(p.id) }) { Icon(Icons.Default.Delete, "Mover a la papelera") }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsTab(pad: PaddingValues, vm: HomeVM, onOpenPage: (String) -> Unit) {
    val upcoming by vm.upcoming.collectAsStateWithLifecycle()
    Column(
        Modifier
            .fillMaxSize()
            .padding(pad)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(16.dp),
    ) {
        Text("Avisos", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Próximos eventos", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        if (upcoming.isEmpty()) {
            Text("No tienes eventos próximos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LazyColumn {
            items(upcoming, key = { it.id }) { e: Event ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { e.pageId?.let { onOpenPage(it) } }
                        .padding(14.dp),
                ) {
                    Text(fmtDateTime(e.startAt), color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    Text(e.title.ifBlank { "Evento" }, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ProfileTab(pad: PaddingValues) {
    val ctx = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .padding(pad)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Perfil", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = Prefs.name,
            onValueChange = { Prefs.save(ctx, it) },
            label = { Text("Tu nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text("BlockNotes · versión de prueba", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    }
}
