package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Phase 7: Provider-Agnostic Linked Document Entity.
 * Stores metadata for files linked from Microsoft OneDrive or Google Drive.
 */
@Entity(
    tableName = "linked_documents",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("project_id")]
)
data class LinkedDocument(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "project_id")
    val projectId: String,

    @ColumnInfo(name = "storage_provider")
    val storageProvider: String, // "ONEDRIVE" or "GOOGLE_DRIVE"

    @ColumnInfo(name = "file_id")
    val fileId: String,

    @ColumnInfo(name = "file_name")
    val fileName: String,

    @ColumnInfo(name = "mime_type")
    val mimeType: String = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",

    @ColumnInfo(name = "file_size_display")
    val fileSizeDisplay: String = "142 KB",

    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: String,

    @ColumnInfo(name = "is_primary_boq")
    val isPrimaryBoq: Boolean = true,

    @ColumnInfo(name = "cloud_path")
    val cloudPath: String = "/",

    @ColumnInfo(name = "content_snippet")
    val contentSnippet: String? = null
)
