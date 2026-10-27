package com.example.blocknotes.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.blocknotes.data.AppDb
import com.example.blocknotes.data.Block
import com.example.blocknotes.data.BlockType
import com.example.blocknotes.data.Page
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun now(): Long = System.currentTimeMillis()

class ListVM(app: Application, private val parentId: String?) : AndroidViewModel(app) {
    private val db = AppDb.get(app)
    val pages = db.pages().children(parentId).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun create(onCreated: (String) -> Unit) = createPage(parentId, "", "", onCreated)

    fun quickNote(onCreated: (String) -> Unit) =
        createPage(null, "Apunte " + SimpleDateFormat("d MMM HH:mm", Locale.getDefault()).format(Date()), "📝", onCreated)

    fun trash(id: String) = viewModelScope.launch { db.pages().setDeleted(id, true, now()) }

    private fun createPage(parent: String?, title: String, icon: String, onCreated: (String) -> Unit) =
        viewModelScope.launch {
            val p = Page(parentId = parent, title = title, icon = icon)
            db.pages().upsert(p)
            db.blocks().upsert(Block(pageId = p.id, position = 1.0))
            onCreated(p.id)
        }

    companion object {
        fun factory(parentId: String?) = viewModelFactory {
            initializer { ListVM(this[APPLICATION_KEY] as Application, parentId) }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SearchVM(app: Application) : AndroidViewModel(app) {
    private val db = AppDb.get(app)
    val query = MutableStateFlow("")
    val results: StateFlow<List<Page>> = query
        .debounce(200)
        .flatMapLatest { q -> if (q.isBlank()) flowOf(emptyList<Page>()) else db.pages().search(q.trim()) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

class TrashVM(app: Application) : AndroidViewModel(app) {
    private val db = AppDb.get(app)
    val trashed = db.pages().trashed().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    fun restore(id: String) = viewModelScope.launch { db.pages().setDeleted(id, false, now()) }
}

class EditorVM(app: Application, val pageId: String) : AndroidViewModel(app) {
    private val db = AppDb.get(app)
    val page = db.pages().page(pageId).stateIn(viewModelScope, SharingStarted.Lazily, null)
    val blocks = db.blocks().blocks(pageId).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    var focusId by mutableStateOf<String?>(null)
    var crumbs by mutableStateOf<List<Page>>(emptyList())

    fun loadCrumbs() = viewModelScope.launch {
        val chain = mutableListOf<Page>()
        var cur = db.pages().get(pageId)?.parentId
        while (cur != null && chain.size < 20) {
            val p = db.pages().get(cur) ?: break
            chain.add(0, p)
            cur = p.parentId
        }
        crumbs = chain
    }

    fun updatePage(transform: (Page) -> Page) = viewModelScope.launch {
        page.value?.let { db.pages().upsert(transform(it).copy(updatedAt = now())) }
    }

    fun update(b: Block) = viewModelScope.launch {
        db.blocks().upsert(b.copy(updatedAt = now()))
    }

    /** Inserta un bloque después de [after]; si es null, al final. */
    fun insertAfter(after: Block?, text: String = "", type: BlockType = BlockType.TEXT, meta: String = "") =
        viewModelScope.launch {
            val list = blocks.value
            val i = list.indexOfFirst { it.id == after?.id }
            val prev = if (i >= 0) list[i].position else (list.lastOrNull()?.position ?: 0.0)
            val next = if (i >= 0 && i + 1 < list.size) list[i + 1].position else prev + 2.0
            val nb = Block(pageId = pageId, type = type, text = text, meta = meta, position = (prev + next) / 2)
            db.blocks().upsert(nb)
            focusId = nb.id
        }

    fun remove(b: Block) = viewModelScope.launch {
        val list = blocks.value
        val i = list.indexOfFirst { it.id == b.id }
        if (list.size <= 1) {
            db.blocks().upsert(b.copy(text = "", meta = "", type = BlockType.TEXT, checked = false, updatedAt = now()))
            return@launch
        }
        focusId = list.getOrNull(i - 1)?.id ?: list.getOrNull(i + 1)?.id
        db.blocks().softDelete(b.id, now())
    }

    /** Intercambia la posición con el bloque vecino (dir = -1 sube, 1 baja). */
    fun move(b: Block, dir: Int) = viewModelScope.launch {
        val list = blocks.value
        val i = list.indexOfFirst { it.id == b.id }
        val j = i + dir
        if (i < 0 || j < 0 || j >= list.size) return@launch
        val mine = list[i]
        val other = list[j]
        db.blocks().upsert(mine.copy(position = other.position, updatedAt = now()))
        db.blocks().upsert(other.copy(position = mine.position, updatedAt = now()))
    }

    companion object {
        fun factory(pageId: String) = viewModelFactory {
            initializer { EditorVM(this[APPLICATION_KEY] as Application, pageId) }
        }
    }
}
