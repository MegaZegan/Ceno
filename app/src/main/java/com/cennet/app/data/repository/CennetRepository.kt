package com.cennet.app.data.repository

import android.content.Context
import androidx.core.content.edit
import com.cennet.app.data.database.CennetDatabase
import com.cennet.app.data.database.DiaryEntry
import com.cennet.app.model.MerchSlot
import com.cennet.app.widget.CenoWidgets
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class CennetRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("cennet_preferences", Context.MODE_PRIVATE)
    private val diary = CennetDatabase.get(appContext).diaryDao()

    val entries: Flow<List<DiaryEntry>> = diary.observeAll()
    suspend fun saveDiary(entry: DiaryEntry): Long = diary.save(entry).also { CenoWidgets.refreshAll(appContext) }
    suspend fun deleteDiary(entry: DiaryEntry) { diary.delete(entry); CenoWidgets.refreshAll(appContext) }
    suspend fun allDiaryEntries(): List<DiaryEntry> = diary.getAll()

    var displayName: String
        get() = prefs.getString("display_name", "Ceno") ?: "Ceno"
        set(value) { prefs.edit { putString("display_name", value) }; CenoWidgets.refreshAll(appContext) }
    var birthday: String
        get() = prefs.getString("birthday", "03-09") ?: "03-09"
        set(value) { prefs.edit { putString("birthday", value) }; CenoWidgets.refreshAll(appContext) }
    var photoOfDay: String?
        get() = prefs.getString("photo_of_day", null)
        set(value) { prefs.edit { putString("photo_of_day", value) }; CenoWidgets.refreshAll(appContext) }
    var photoPool: List<String>
        get() = prefs.getString("photo_pool", "").orEmpty().lines().filter { it.isNotBlank() }
        set(value) { prefs.edit { putString("photo_pool", value.distinct().joinToString("\n")) }; CenoWidgets.refreshAll(appContext) }
    var themeIndex: Int
        get() = prefs.getInt("theme", 0)
        set(value) = prefs.edit { putInt("theme", value) }
    fun loadMerchSlots(): List<MerchSlot> = runCatching {
        val savedLayout = prefs.getString("merch_layout", null)
        if (savedLayout != null) {
            val array = JSONArray(savedLayout)
            List(16) { index ->
                val item = array.optJSONObject(index)
                MerchSlot(
                    id = index + 1,
                    localImagePath = item?.optString("path")?.takeIf { it.isNotBlank() },
                    columnSpan = item?.optInt("columns", 1)?.coerceIn(1, 4) ?: 1,
                    rowSpan = item?.optInt("rows", 1)?.coerceIn(1, 4) ?: 1
                )
            }
        } else {
            val legacy = JSONArray(prefs.getString("merch_slots", "[]"))
            List(16) { index -> MerchSlot(index + 1, legacy.optString(index).takeIf { it.isNotBlank() }) }
        }
    }.getOrDefault(List(16) { MerchSlot(it + 1) })

    fun saveMerchSlots(slots: List<MerchSlot>) {
        val array = JSONArray()
        List(16) { index -> slots.getOrElse(index) { MerchSlot(index + 1) } }.forEach { slot ->
            array.put(JSONObject().put("path", slot.localImagePath.orEmpty()).put("columns", slot.columnSpan).put("rows", slot.rowSpan))
        }
        prefs.edit { putString("merch_layout", array.toString()); remove("merch_slots") }
        CenoWidgets.refreshAll(appContext)
    }

    suspend fun resetAll() {
        diary.clearAll()
        appContext.filesDir.resolve("merch_slots").deleteRecursively()
        listOf("cennet_preferences", "garden", "minik_dostum", "moa", "references").forEach {
            appContext.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().apply()
        }
        CenoWidgets.refreshAll(appContext)
    }
}
