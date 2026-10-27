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
import com.example.blocknotes.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListVM(app: Application, private val parentId: String?) : AndroidViewModel(app) {
    private val db = AppDb.get(app)
    val pages = db.pages().children(parentId).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun create(onCreated: (String) -> Unit) = viewModelScope.launch {
        val p = Page(parentId = parentId)
        db.pages().upsert(p)
        db.blocks().upsert(Block(pageId = p.id, position = 1.0))
        onCreated(p.id)
    }
    fun delete(id: String) = viewModelScope.launch { db.pages().softDelete(id, System.currentTimeMillis()) }

    companion object {
        fun factory(parentId: String?) = viewModelFactory { initializer { ListVM(this[APPLICATION_KEY] as Application, parentId) } }
    }
}

class EditorVM(app: Application, val pageId: String) : AndroidViewModel(app) {
    private val db = AppDb.get(app)
    val page = db.pages().page(pageId).stateIn(viewModelScope, SharingStarted.Lazily, null)
    val blocks = db.blocks().blocks(pageId).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    var focusId by mutableStateOf<String?>(null)

    fun setTitle(t: String) = viewModelScope.launch {
        page.value?.let { db.pages().upsert(it.copy(title = t, updatedAt = System.currentTimeMillis())) }
    }
    fun update(b: Block) = viewModelScope.launch { db.blocks().upsert(b.copy(updatedAt = System.currentTimeMillis())) }

    /** Inserta un bloque justo después de [after] (o al final si es null). */
    fun insertAfter(after: Block?, text: String = "", type: BlockType = BlockType.TEXT) = viewModelScope.launch {
        val list = blocks.value
        val i = list.indexOfFirst { it.id == after?.id }
        val prev = if (i >= 0) list[i].position else (list.lastOrNull()?.position ?: 0.0)
        val next = list.getOrNull(i + 1)?.position ?: (prev + 2.0)
        val nb = Block(pageId = pageId, type = type, text = text, position = (prev + next) / 2)
        db.blocks().upsert(nb)
        focusId = nb.id
    }
    fun remove(b: Block) = viewModelScope.launch {
        val list = blocks.value
        val i = list.indexOfFirst { it.id == b.id }
        if (list.size <= 1) return@launch
        focusId = list.getOrNull(i - 1)?.id ?: list.getOrNull(i + 1)?.id
        db.blocks().softDelete(b.id, System.currentTimeMillis())
    }

    companion object {
        fun factory(pageId: String) = viewModelFactory { initializer { EditorVM(this[APPLICATION_KEY] as Application, pageId) } }
    }
}
