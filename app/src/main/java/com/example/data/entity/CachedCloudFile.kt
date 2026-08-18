package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing locally cached cloud storage files (OneDrive, Google Drive, Local)
 * stored in Room database to enable offline browsing, search, and previewing without an active connection.
 */
@Entity(tableName = "cached_cloud_files")
data class CachedCloudFile(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "file_name")
    val name: String,

    @ColumnInfo(name = "provider")
    val provider: String, // "ONEDRIVE", "GOOGLE_DRIVE", "LOCAL"

    @ColumnInfo(name = "mime_type")
    val mimeType: String,

    @ColumnInfo(name = "size_display")
    val sizeDisplay: String,

    @ColumnInfo(name = "last_modified")
    val lastModified: String,

    @ColumnInfo(name = "is_folder")
    val isFolder: Boolean = false,

    @ColumnInfo(name = "cloud_path")
    val cloudPath: String = "/",

    @ColumnInfo(name = "sample_content")
    val sampleContent: String? = null,

    @ColumnInfo(name = "associated_project")
    val associatedProject: String? = null,

    @ColumnInfo(name = "project_id")
    val projectId: String? = null,

    @ColumnInfo(name = "cached_at_timestamp")
    val cachedAtTimestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_offline_available")
    val isOfflineAvailable: Boolean = true,

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "CACHED_LOCAL" // "CACHED_LOCAL", "SYNCED", "PENDING_SYNC"
)
