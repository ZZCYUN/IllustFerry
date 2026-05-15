package JunZi.Pixiv.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class HistoryEntry(
    val illustId: Long,
    val viewedAtMillis: Long,
)

class HistoryStore(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_HISTORY (
                $COLUMN_ILLUST_ID INTEGER PRIMARY KEY,
                $COLUMN_VIEWED_AT INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX $INDEX_VIEWED_AT
            ON $TABLE_HISTORY ($COLUMN_VIEWED_AT DESC)
            """.trimIndent(),
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        onCreate(db)
    }

    fun save(illustId: Long, viewedAtMillis: Long = System.currentTimeMillis()) {
        val values = ContentValues().apply {
            put(COLUMN_ILLUST_ID, illustId)
            put(COLUMN_VIEWED_AT, viewedAtMillis)
        }
        writableDatabase.insertWithOnConflict(TABLE_HISTORY, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        trimToMaxRows()
    }

    fun recent(limit: Int = DEFAULT_LIMIT): List<HistoryEntry> = recentPage(limit = limit, offset = 0)

    fun recentPage(limit: Int = DEFAULT_LIMIT, offset: Int = 0): List<HistoryEntry> {
        val queryLimit = limit.coerceIn(1, MAX_ROWS)
        val queryOffset = offset.coerceAtLeast(0)
        val cursor = readableDatabase.query(
            TABLE_HISTORY,
            arrayOf(COLUMN_ILLUST_ID, COLUMN_VIEWED_AT),
            null,
            null,
            null,
            null,
            "$COLUMN_VIEWED_AT DESC",
            "$queryLimit OFFSET $queryOffset",
        )
        cursor.use {
            val illustIdIndex = it.getColumnIndexOrThrow(COLUMN_ILLUST_ID)
            val viewedAtIndex = it.getColumnIndexOrThrow(COLUMN_VIEWED_AT)
            val items = ArrayList<HistoryEntry>(it.count.coerceAtLeast(0))
            while (it.moveToNext()) {
                items += HistoryEntry(
                    illustId = it.getLong(illustIdIndex),
                    viewedAtMillis = it.getLong(viewedAtIndex),
                )
            }
            return items
        }
    }

    fun delete(illustId: Long) {
        writableDatabase.delete(TABLE_HISTORY, "$COLUMN_ILLUST_ID = ?", arrayOf(illustId.toString()))
    }

    fun clear() {
        writableDatabase.delete(TABLE_HISTORY, null, null)
    }

    private fun trimToMaxRows() {
        writableDatabase.execSQL(
            """
            DELETE FROM $TABLE_HISTORY
            WHERE $COLUMN_ILLUST_ID IN (
                SELECT $COLUMN_ILLUST_ID
                FROM $TABLE_HISTORY
                ORDER BY $COLUMN_VIEWED_AT DESC
                LIMIT -1 OFFSET $MAX_ROWS
            )
            """.trimIndent(),
        )
    }

    private companion object {
        const val DATABASE_NAME = "puxiv_history.db"
        const val DATABASE_VERSION = 2
        const val TABLE_HISTORY = "illust_history"
        const val COLUMN_ILLUST_ID = "illust_id"
        const val COLUMN_VIEWED_AT = "viewed_at"
        const val INDEX_VIEWED_AT = "idx_illust_history_viewed_at"
        const val MAX_ROWS = 1000
        const val DEFAULT_LIMIT = 200
    }
}
