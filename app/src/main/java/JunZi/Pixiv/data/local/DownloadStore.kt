package JunZi.Pixiv.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import JunZi.Pixiv.DownloadItem
import JunZi.Pixiv.DownloadStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 下载元数据的 SQLite 持久化（puxiv_downloads.db）。
 *
 * 取代旧的 SharedPreferences JSON 串：下载条目携带完整 Illust payload，逐行落库 +
 * 事务内整体替换，比每次全量序列化一个巨型 JSON 更稳；列表顺序用 position 列维护，
 * 与内存列表（新任务在最前）严格一致。
 *
 * 首次构造时从旧 SP（puxiv_auth 里的 downloads 键）做一次性迁移，迁移完移除旧键。
 */
class DownloadStore(private val appContext: Context) :
    SQLiteOpenHelper(appContext, DATABASE_NAME, null, DATABASE_VERSION) {

    private val gson = Gson()

    init {
        migrateFromSharedPreferences()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_DOWNLOADS (
                $COLUMN_KEY TEXT PRIMARY KEY NOT NULL,
                $COLUMN_ILLUST_ID INTEGER NOT NULL,
                $COLUMN_POSITION INTEGER NOT NULL,
                $COLUMN_PAYLOAD TEXT NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX $INDEX_POSITION
            ON $TABLE_DOWNLOADS ($COLUMN_POSITION)
            """.trimIndent(),
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DOWNLOADS")
        onCreate(db)
    }

    fun all(): List<DownloadItem> {
        val cursor = readableDatabase.query(
            TABLE_DOWNLOADS,
            arrayOf(COLUMN_PAYLOAD),
            null,
            null,
            null,
            null,
            "$COLUMN_POSITION ASC",
        )
        cursor.use {
            val payloadIndex = it.getColumnIndexOrThrow(COLUMN_PAYLOAD)
            val items = ArrayList<DownloadItem>(it.count.coerceAtLeast(0))
            while (it.moveToNext()) {
                val payload = it.getString(payloadIndex)
                val item = payload
                    ?.takeIf { value -> value.isNotBlank() }
                    ?.let { value -> runCatching { gson.fromJson(value, DownloadItem::class.java) }.getOrNull() }
                    ?.normalized()
                if (item != null) {
                    items += item
                }
            }
            return items
        }
    }

    fun replaceAll(items: List<DownloadItem>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_DOWNLOADS, null, null)
            items.forEachIndexed { index, item ->
                db.insertWithOnConflict(
                    TABLE_DOWNLOADS,
                    null,
                    ContentValues().apply {
                        put(COLUMN_KEY, item.key)
                        put(COLUMN_ILLUST_ID, item.illustId)
                        put(COLUMN_POSITION, index)
                        put(COLUMN_PAYLOAD, gson.toJson(item))
                    },
                    SQLiteDatabase.CONFLICT_REPLACE,
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun delete(key: String) {
        writableDatabase.delete(TABLE_DOWNLOADS, "$COLUMN_KEY = ?", arrayOf(key))
    }

    fun clear() {
        writableDatabase.delete(TABLE_DOWNLOADS, null, null)
    }

    /** 旧 SP 一次性迁移：puxiv_auth 的 downloads 键 → 本库，迁移完移除旧键。 */
    private fun migrateFromSharedPreferences() {
        val preferences = appContext.getSharedPreferences(LEGACY_PREF_NAME, Context.MODE_PRIVATE)
        if (!preferences.contains(LEGACY_KEY_DOWNLOADS)) return
        val legacyItems = preferences.getString(LEGACY_KEY_DOWNLOADS, null)
            ?.takeIf { it.isNotBlank() }
            ?.let { json ->
                runCatching {
                    val parsed: List<DownloadItem?>? = gson.fromJson(json, DOWNLOAD_LIST_TYPE)
                    parsed.orEmpty().mapNotNull { it?.normalized() }
                }.getOrDefault(emptyList())
            }
            .orEmpty()
        runCatching {
            if (legacyItems.isNotEmpty()) {
                replaceAll(legacyItems)
            }
        }
        preferences.edit().remove(LEGACY_KEY_DOWNLOADS).apply()
    }

    private fun DownloadItem.normalized(): DownloadItem {
        val cleanSavedUris = savedUris.orEmpty()
            .mapNotNull(::nonBlankStringOrNull)
            .ifEmpty { listOfNotNull(savedUri?.takeIf { it.isNotBlank() }) }
        return copy(
            title = title.orEmpty(),
            fileName = fileName.orEmpty(),
            status = status ?: DownloadStatus.Failed,
            pageCount = pageCount.coerceAtLeast(cleanSavedUris.size.coerceAtLeast(1)),
            relativePath = relativePath.orEmpty(),
            detail = detail.orEmpty(),
            savedUri = savedUri?.takeIf { it.isNotBlank() } ?: cleanSavedUris.firstOrNull(),
            savedUris = cleanSavedUris,
            zipSavedUri = zipSavedUri?.takeIf { it.isNotBlank() },
        )
    }

    private companion object {
        const val DATABASE_NAME = "puxiv_downloads.db"
        const val DATABASE_VERSION = 1
        const val TABLE_DOWNLOADS = "download_items"
        const val COLUMN_KEY = "item_key"
        const val COLUMN_ILLUST_ID = "illust_id"
        const val COLUMN_POSITION = "position"
        const val COLUMN_PAYLOAD = "payload"
        const val INDEX_POSITION = "idx_download_items_position"

        const val LEGACY_PREF_NAME = "puxiv_auth"
        const val LEGACY_KEY_DOWNLOADS = "downloads"

        val DOWNLOAD_LIST_TYPE = object : TypeToken<List<DownloadItem?>>() {}.type
    }
}

private fun nonBlankStringOrNull(value: String?): String? {
    return value?.takeIf { it.isNotBlank() }
}
