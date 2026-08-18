package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray

@Entity(tableName = "site_diary_entries")
data class SiteDiaryEntry(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "project_id") val projectId: String,
    @ColumnInfo(name = "work_order_id") val workOrderId: String? = null,
    @ColumnInfo(name = "work_order_title") val workOrderTitle: String? = null,
    @ColumnInfo(name = "author") val author: String,
    @ColumnInfo(name = "date_display") val dateDisplay: String,
    @ColumnInfo(name = "status_update") val statusUpdate: String,
    @ColumnInfo(name = "weather_notes") val weatherNotes: String? = null,
    @ColumnInfo(name = "labor_count") val laborCount: Int = 0,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "photo_url") val photoUrl: String,
    @ColumnInfo(name = "gemini_summary") val geminiSummary: String? = null,
    @ColumnInfo(name = "is_voice_transcribed") val isVoiceTranscribed: Boolean = false,
    @ColumnInfo(name = "audio_transcript") val audioTranscript: String? = null,
    @ColumnInfo(name = "audio_tasks_json") val audioTasksJson: String? = null,
    @ColumnInfo(name = "audio_todos_json") val audioTodosJson: String? = null,
    @ColumnInfo(name = "audio_finished_items_json") val audioFinishedItemsJson: String? = null,
    @ColumnInfo(name = "audio_valuation_notes") val audioValuationNotes: String? = null,
    @ColumnInfo(name = "audio_scheduling_notes") val audioSchedulingNotes: String? = null,
    @ColumnInfo(name = "created_at_timestamp") val createdAtTimestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_cached_offline") val isCachedOffline: Boolean = true,
    @ColumnInfo(name = "sync_status") val syncStatus: String = "SYNCED", // "SYNCED", "PENDING_UPLOAD", "LOCAL_ONLY"
    @ColumnInfo(name = "cached_timestamp") val cachedTimestamp: Long = System.currentTimeMillis()
) {
    fun getTasksList(): List<String> = parseJsonArrayString(audioTasksJson)
    fun getTodosList(): List<String> = parseJsonArrayString(audioTodosJson)
    fun getFinishedItemsList(): List<String> = parseJsonArrayString(audioFinishedItemsJson)

    private fun parseJsonArrayString(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val item = arr.optString(i)
                if (item.isNotBlank()) list.add(item)
            }
            list
        } catch (e: Throwable) {
            emptyList()
        }
    }
}
