package com.example.blocknotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blocknotes.data.Page
import java.time.LocalTime

@Composable
fun HomeTab(
    pad: PaddingValues,
    vm: HomeVM,
    onOpenPage: (String) -> Unit,
    onSearch: () -> Unit,
    onTrash: () -> Unit,
    onGoTab: (Int) -> Unit,
    onAddEvent: () -> Unit,
) {
    val roots by vm.rootPages.collectAsStateWithLifecycle()
    val recent by vm.recent.collectAsStateWithLifecycle()
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Buenos días,"
        hour < 20 -> "Buenas tardes,"
        else -> "Buenas noches,"
    }

    LazyColumn(Modifier.fillMaxSize().padding(pad)) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF3A2A6E), Color(0xFF8A3E6E), Color(0xFF1B1C2C), Color(0xFF0E0F16)),
                        ),
                    ),
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(greeting, color = Color.White.copy(alpha = 0.85f), fontSize = 18.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(Prefs.name, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(8.dp))
                                Text("👑", fontSize = 22.sp)
                            }
                        }
                        CircleButton(Icons.Default.Search, "Buscar", onSearch)
                        Spacer(Modifier.width(10.dp))
                        CircleButton(Icons.Default.Delete, "Papelera", onTrash)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Grandes cosas también empiezan en páginas en blanco.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                    )
                }
            }
        }

        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard("📝", "Nueva nota", "Escribe tus ideas", Modifier.weight(1f)) {
                        vm.newPage("📝") { onOpenPage(it) }
                    }
                    ActionCard("✅", "Nueva tarea", "Organiza tu día", Modifier.weight(1f)) {
                        vm.newPage("✅", todo = true) { onOpenPage(it) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard("📅", "Nuevo evento", "No lo olvides", Modifier.weight(1f)) { onAddEvent() }
                    ActionCard("🚀", "Nuevo proyecto", "Hazlo realidad", Modifier.weight(1f)) {
                        vm.newPage("🚀") { onOpenPage(it) }
                    }
                }
            }
        }

        item {
            SectionCard {
                SectionTitle("⭐", "Accesos rápidos")
                Spacer(Modifier.height(12.dp))
                val shortcuts = listOf(
                    Triple("📝", "Mis notas", 1),
                    Triple("📁", "Proyectos", 1),
                    Triple("✅", "Tareas", 1),
                    Triple("📚", "Estudio", 1),
                    Triple("📅", "Calendario", 2),
                    Triple("🏋️", "Entrenamiento", 1),
                )
                shortcuts.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (emoji, label, tab) ->
                            ShortcutTile(emoji, label, Modifier.weight(1f)) { onGoTab(tab) }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionTitleInline("📄", "Mis páginas", Modifier.weight(1f))
                    IconButton(onClick = onSearch) { Icon(Icons.Default.Search, "Buscar") }
                }
                roots.take(6).forEach { p -> PageLine(p) { onOpenPage(p.id) } }
                if (roots.isEmpty()) {
                    Text("Aún no tienes páginas. Pulsa + para crear una.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Column(Modifier.padding(bottom = 16.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    SectionTitleInline("🕘", "Recientes", Modifier.weight(1f))
                    TextButton(onClick = { onGoTab(1) }) { Text("Ver todo") }
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(recent, key = { it.id }) { p -> PageCard(p) { onOpenPage(p.id) } }
                }
            }
        }

    }
}

@Composable
private fun CircleButton(icon: ImageVector, desc: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
    ) {
        Icon(icon, desc, tint = Color.White)
    }
}

@Composable
private fun ActionCard(emoji: String, title: String, subtitle: String, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Text(emoji, fontSize = 26.sp)
        Spacer(Modifier.height(12.dp))
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        Icon(Icons.Default.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(16.dp),
        content = content,
    )
}

@Composable
private fun SectionTitle(emoji: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SectionTitleInline(emoji, title, Modifier.weight(1f))
    }
}

@Composable
private fun SectionTitleInline(emoji: String, title: String, modifier: Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
    }
}

@Composable
private fun ShortcutTile(emoji: String, label: String, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(label, Modifier.weight(1f), fontSize = 14.sp)
        Icon(Icons.Default.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PageLine(p: Page, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(p.icon.ifBlank { "📄" }, fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Text(p.title.ifBlank { "Sin título" }, Modifier.weight(1f), maxLines = 1, fontFamily = fontFamilyOf(p.font))
        Icon(Icons.Default.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PageCard(p: Page, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier
            .width(170.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick),
    ) {
        PageCover(p, Modifier.fillMaxWidth().height(96.dp)) {
            Text(
                p.icon.ifBlank { "📄" },
                fontSize = 22.sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 6.dp),
            )
        }
        Column(Modifier.padding(12.dp)) {
            Text(p.title.ifBlank { "Sin título" }, maxLines = 1, fontWeight = FontWeight.SemiBold, fontFamily = fontFamilyOf(p.font))
            Text(relTime(p.updatedAt), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
