package com.example.blocknotes.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import java.util.UUID

enum class BlockType { TEXT, H1, H2, H3, TODO, BULLET, NUMBERED, TOGGLE, QUOTE, CALLOUT, CODE, DIVIDER, IMAGE, TOC }

@Entity(tableName = "pages")
data class Page(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val parentId: String? = null,
    val title: String = "",
    val icon: String = "",
    val coverColor: String = "",
    val coverUri: String = "",
    val font: String = "SANS",
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false,
)

@Entity(tableName = "blocks", indices = [Index("pageId")])
data class Block(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val pageId: String,
    val type: BlockType = BlockType.TEXT,
    val text: String = "",
    val meta: String = "",
    val checked: Boolean = false,
    val position: Double,
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false,
)

@Entity(tableName = "events")
data class Event(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val startAt: Long,
    val pageId: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false,
)

@Dao
interface PageDao {
    @Query("SELECT * FROM pages WHERE deleted = 0 AND parentId IS :parentId ORDER BY updatedAt DESC")
    fun children(parentId: String?): Flow<List<Page>>

    @Query("SELECT * FROM pages WHERE id = :id")
    fun page(id: String): Flow<Page?>

    @Query("SELECT * FROM pages WHERE id = :id")
    suspend fun get(id: String): Page?

    @Query("SELECT * FROM pages WHERE deleted = 0 ORDER BY updatedAt DESC LIMIT 5")
    fun recent(): Flow<List<Page>>

    @Query("SELECT * FROM pages WHERE deleted = 1 ORDER BY updatedAt DESC")
    fun trashed(): Flow<List<Page>>

    @Query("SELECT * FROM pages WHERE deleted = 0 AND (title LIKE '%' || :q || '%' OR id IN (SELECT pageId FROM blocks WHERE deleted = 0 AND text LIKE '%' || :q || '%')) ORDER BY updatedAt DESC")
    fun search(q: String): Flow<List<Page>>

    @Upsert suspend fun upsert(p: Page)

    @Query("UPDATE pages SET deleted = :deleted, updatedAt = :t WHERE id = :id")
    suspend fun setDeleted(id: String, deleted: Boolean, t: Long)
}

@Dao
interface BlockDao {
    @Query("SELECT * FROM blocks WHERE pageId = :pageId AND deleted = 0 ORDER BY position")
    fun blocks(pageId: String): Flow<List<Block>>

    @Upsert suspend fun upsert(b: Block)

    @Query("UPDATE blocks SET deleted = 1, updatedAt = :t WHERE id = :id")
    suspend fun softDelete(id: String, t: Long)
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE deleted = 0 AND startAt >= :from AND startAt < :to ORDER BY startAt")
    fun between(from: Long, to: Long): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE deleted = 0 AND startAt >= :from ORDER BY startAt LIMIT 30")
    fun upcoming(from: Long): Flow<List<Event>>

    @Upsert suspend fun upsert(e: Event)

    @Query("UPDATE events SET deleted = 1, updatedAt = :t WHERE id = :id")
    suspend fun softDelete(id: String, t: Long)
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS events (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, startAt INTEGER NOT NULL, pageId TEXT, updatedAt INTEGER NOT NULL, deleted INTEGER NOT NULL)")
    }
}

@Database(entities = [Page::class, Block::class, Event::class], version = 3, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun pages(): PageDao
    abstract fun blocks(): BlockDao
    abstract fun events(): EventDao

    companion object {
        @Volatile private var inst: AppDb? = null
        fun get(c: Context): AppDb = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(c.applicationContext, AppDb::class.java, "blocknotes.db")
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                .also { inst = it }
        }
    }
}
