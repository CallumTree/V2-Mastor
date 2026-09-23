package com.example.domain.evidence

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Where site evidence (photos, videos) lives on the device.
 *
 * Uses permanent app storage (filesDir), NOT cacheDir — Android deletes cache files when the
 * phone is low on space, which would silently destroy photo evidence for diaries and variations.
 * Covered by the existing <files-path name="internal_files" path="."/> FileProvider entry.
 */
object EvidenceStorage {

    private fun dir(context: Context, sub: String): File =
        File(context.filesDir, "evidence/$sub").apply { mkdirs() }

    private fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /** New empty file for the camera to write a photo into. */
    fun newPhotoUri(context: Context, prefix: String = "photo"): Uri {
        val file = File.createTempFile("${prefix}_${System.currentTimeMillis()}_", ".jpg", dir(context, "photos"))
        return uriFor(context, file)
    }

    /** New empty file for the camera to write a video into. */
    fun newVideoUri(context: Context, prefix: String = "video"): Uri {
        val file = File.createTempFile("${prefix}_${System.currentTimeMillis()}_", ".mp4", dir(context, "videos"))
        return uriFor(context, file)
    }
}
