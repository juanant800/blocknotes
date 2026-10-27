package com.example.blocknotes.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blocknotes.data.AppDb
import com.example.blocknotes.data.Block
import com.example.blocknotes.data.BlockType
import com.example.blocknotes.data.Event
import com.example.blocknotes.data.Page
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class HomeVM(app: Application) : AndroidViewModel(app) {
    private val db = AppDb.get(app)
    val rootPages = db.pages().children(null).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val recent = db.pages().recent().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val upcoming = db.events().upcoming(System.currentTimeMillis()).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun newPage(icon: String, todo: Boolean = false, onCreated: (String) -> Unit) = viewModelScope.launch {
        val p = Page(icon = icon)
        db.pages().upsert(p)
        db.blocks().upsert(Block(pageId = p.id, position = 1.0, type = if (todo) BlockType.TODO else BlockType.TEXT))
        onCreated(p.id)
    }

    fun addEvent(title: String, date: LocalDate, hour: Int, minute: Int) = viewModelScope.launch {
        db.events().upsert(Event(title = title.ifBlank { "Evento" }, startAt = toMillis(date, hour, minute)))
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarVM(app: Application) : AndroidViewModel(app) {
    private val db = AppDb.get(app)
    val month = MutableStateFlow(YearMonth.now())
    val selected = MutableStateFlow(LocalDate.now())

    val monthEvents: StateFlow<List<Event>> = month
        .flatMapLatest { m -> db.events().between(dayStart(m.atDay(1)), dayStart(m.plusMonths(1).atDay(1))) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val dayEvents: StateFlow<List<Event>> = selected
        .flatMapLatest { d -> db.events().between(dayStart(d), dayStart(d.plusDays(1))) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun select(d: LocalDate) {
        selected.value = d
        month.value = YearMonth.from(d)
    }

    fun shiftMonth(n: Long) {
        month.value = month.value.plusMonths(n)
    }

    fun deleteEvent(id: String) = viewModelScope.launch {
        db.events().softDelete(id, System.currentTimeMillis())
    }

    fun addEvent(title: String, date: LocalDate, hour: Int, minute: Int) = viewModelScope.launch {
        db.events().upsert(Event(title = title.ifBlank { "Evento" }, startAt = toMillis(date, hour, minute)))
    }

    fun noteForDay(date: LocalDate, onCreated: (String) -> Unit) = viewModelScope.launch {
        val p = Page(title = "Apunte " + date.format(DateTimeFormatter.ofPattern("d MMM", SpanishLocale)), icon = "📝")
        db.pages().upsert(p)
        db.blocks().upsert(Block(pageId = p.id, position = 1.0))
        db.events().upsert(Event(title = p.title, startAt = toMillis(date, 9, 0), pageId = p.id))
        onCreated(p.id)
    }
}
