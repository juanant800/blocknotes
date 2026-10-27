package com.example.blocknotes.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

enum class BlockType { TEXT, H1, H2, TODO, BULLET, CODE }

// Sync-ready: UUID ids, updatedAt (last-write-wins) y borrado lógico.
@Entity(tableName = "pages")
data class Page(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val parentId: String? = null,
    val title: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false,
)

@Entity(tableName = "blocks", indices = [Index("pageId")])
data class Block(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val pageId: String,
    val type: BlockType = BlockType.TEXT,
    val text: String = "",
    val checked: Boolean = false,
    val position: Double,            // fractional index: insertar entre dos bloques sin reordenar todo
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false,
)

@Dao
interface PageDao {
    @Query("SELECT * FROM pages WHERE deleted = 0 AND parentId IS :parentId ORDER BY updatedAt DESC")
    fun children(parentId: String?): Flow<List<Page>>
    @Query("SELECT * FROM pages WHERE id = :id") fun page(id: String): Flow<Page?>
    @Upsert suspend fun upsert(p: Page)
    @Query("UPDATE pages SET deleted = 1, updatedAt = :t WHERE id = :id") suspend fun softDelete(id: String, t: Long)
}

@Dao
interface BlockDao {
    @Query("SELECT * FROM blocks WHERE pageId = :pageId AND deleted = 0 ORDER BY position")
    fun blocks(pageId: String): Flow<List<Block>>
    @Upsert suspend fun upsert(b: Block)
    @Query("UPDATE blocks SET deleted = 1, updatedAt = :t WHERE id = :id") suspend fun softDelete(id: String, t: Long)
}

@Database(entities = [Page::class, Block::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun pages(): PageDao
    abstract fun blocks(): BlockDao
    companion object {
        @Volatile private var inst: AppDb? = null
        fun get(c: Context): AppDb = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(c.applicationContext, AppDb::class.java, "blocknotes.db").build().also { inst = it }
        }
    }
}
