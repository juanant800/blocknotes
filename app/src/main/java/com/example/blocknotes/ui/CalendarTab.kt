package com.example.blocknotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blocknotes.data.Event
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarTab(pad: PaddingValues, vm: CalendarVM, onOpenPage: (String) -> Unit) {
    val month by vm.month.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    val monthEvents by vm.monthEvents.collectAsStateWithLifecycle()
    val dayEvents by vm.dayEvents.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val marked = monthEvents.map { dateOf(it.startAt).dayOfMonth }.toSet()
    val monthTitle = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", SpanishLocale))
        .replaceFirstChar { it.uppercase() }

    Column(
        Modifier
            .fillMaxSize()
            .padding(pad)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(monthTitle, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = { vm.select(today) }) { Text("Hoy") }
            IconButton(onClick = { vm.shiftMonth(-1) }) { Icon(Icons.Default.KeyboardArrowLeft, "Mes anterior") }
            IconButton(onClick = { vm.shiftMonth(1) }) { Icon(Icons.Default.KeyboardArrowRight, "Mes siguiente") }
        }

        Row(Modifier.fillMaxWidth()) {
            listOf("L", "M", "X", "J", "V", "S", "D").forEach { d ->
                Text(
                    d,
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        val first = month.atDay(1)
        val offset = first.dayOfWeek.value - 1
        val days = month.lengthOfMonth()
        val cells: List<Int?> = List(offset) { null } + (1..days).map { it } +
            List((7 - (offset + days) % 7) % 7) { null }

        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { dom ->
                    Box(Modifier.weight(1f).height(46.dp), contentAlignment = Alignment.Center) {
                        if (dom != null) {
                            val date = month.atDay(dom)
                            val isSel = date == selected
                            val isToday = date == today
                            val base = Modifier.size(40.dp).clip(CircleShape)
                            val styled = when {
                                isSel -> base.background(MaterialTheme.colorScheme.primary)
                                isToday -> base.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                else -> base
                            }
                            Column(
                                styled.clickable { vm.select(date) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    dom.toString(),
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                )
                                if (marked.contains(dom)) {
                                    Box(
                                        Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            selected.format(DateTimeFormatter.ofPattern("EEEE d de MMMM", SpanishLocale)).replaceFirstChar { it.uppercase() },
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = { showAdd = true }, label = { Text("+ Evento") })
            AssistChip(onClick = { vm.noteForDay(selected) { onOpenPage(it) } }, label = { Text("+ Apunte") })
        }
        Spacer(Modifier.height(8.dp))

        LazyColumn(Modifier.weight(1f)) {
            if (dayEvents.isEmpty()) {
                item {
                    Text(
                        "Nada programado este día",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
            }
            items(dayEvents, key = { it.id }) { e ->
                EventRow(
                    e = e,
                    onOpen = { e.pageId?.let { onOpenPage(it) } },
                    onDelete = { vm.deleteEvent(e.id) },
                )
            }
        }
    }

    if (showAdd) {
        EventDialog(
            date = selected,
            onDismiss = { showAdd = false },
            onSave = { title, h, m ->
                vm.addEvent(title, selected, h, m)
                showAdd = false
            },
        )
    }
}

@Composable
private fun EventRow(e: Event, onOpen: () -> Unit, onDelete: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onOpen)
            .padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(timeOf(e.startAt), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(e.title.ifBlank { "Evento" }, Modifier.weight(1f), maxLines = 1)
        if (e.pageId != null) Text("📝", fontSize = 14.sp)
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Borrar evento") }
    }
}

@Composable
fun EventDialog(date: LocalDate, onDismiss: () -> Unit, onSave: (String, Int, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var hh by remember { mutableStateOf("09") }
    var mm by remember { mutableStateOf("00") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo evento · ${date.dayOfMonth}/${date.monthValue}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hh,
                        onValueChange = { v -> if (v.length <= 2) hh = v.filter { it.isDigit() } },
                        label = { Text("Hora") },
                        singleLine = true,
                        modifier = Modifier.width(96.dp),
                    )
                    Text(":")
                    OutlinedTextField(
                        value = mm,
                        onValueChange = { v -> if (v.length <= 2) mm = v.filter { it.isDigit() } },
                        label = { Text("Min") },
                        singleLine = true,
                        modifier = Modifier.width(96.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(title, hh.toIntOrNull()?.coerceIn(0, 23) ?: 9, mm.toIntOrNull()?.coerceIn(0, 59) ?: 0)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
