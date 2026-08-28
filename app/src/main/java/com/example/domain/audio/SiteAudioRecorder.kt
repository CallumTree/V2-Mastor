package com.example.domain.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

data class SampleVoiceNote(
    val title: String,
    val author: String,
    val audioDuration: String,
    val transcript: String,
    val trade: String
)

object SiteAudioRecorder {

    val sampleVoiceNotes = listOf(
        SampleVoiceNote(
            title = "Drylining & Plastering Daily Walkthrough",
            author = "Dave Jenkins (Site Manager)",
            audioDuration = "0:42",
            transcript = "Site diary for Thursday 26th Feb. Kitchen Flat 3 - drywall subbie has finished metal stud partition framing in the hallway and master bedroom. They've started hanging SoundBloc plasterboards. Corridor B is now signed off by the building control inspector. We need an order of 30 additional 15mm acoustic sheets for the ceiling drop tomorrow morning. Plasterers are on site skimming room 2. 50% milestone reached on the drywall package.",
            trade = "Drylining & Partitions"
        ),
        SampleVoiceNote(
            title = "M&E First Fix & Chasing Inspection",
            author = "Eleanor Vance (Commercial QS)",
            audioDuration = "0:35",
            transcript = "QS site inspection update for Flat 1 and 2. Electrical first fix containment and floor chasing complete. Plumbing contractor has installed copper hot and cold feeds to the kitchen riser. Air pressure test completed on the primary pipe run with zero drop. Need to chase subby for test certification sheet before Friday valuation cutoff. Claim ready at 40% completion.",
            trade = "M&E Services"
        ),
        SampleVoiceNote(
            title = "External Scaffolding & Brickwork Snagging",
            author = "Dave Jenkins (Site Manager)",
            audioDuration = "0:28",
            transcript = "Walked the North elevation scaffold with brickwork foreman. Coping stones on the parapet wall have been bedded in lime mortar and pointing completed to high standard. Scaffolders are scheduled to strike the top lift on Monday. Ready for final valuation measurement.",
            trade = "Masonry & External"
        )
    )

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var recordingStartTime: Long = 0L

    var isRecording: Boolean = false
        private set

    fun startRecording(context: Context): Boolean {
        if (isRecording) return true
        return try {
            val outputDir = context.cacheDir
            currentOutputFile = File.createTempFile("site_voice_", ".m4a", outputDir)

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(currentOutputFile?.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            isRecording = true
            recordingStartTime = System.currentTimeMillis()
            true
        } catch (e: IOException) {
            Log.e("SiteAudioRecorder", "Failed to start recording: ${e.message}", e)
            cleanup()
            false
        } catch (e: SecurityException) {
            Log.e("SiteAudioRecorder", "Microphone permission denied: ${e.message}", e)
            cleanup()
            false
        } catch (e: Exception) {
            Log.e("SiteAudioRecorder", "Recording initialization error: ${e.message}", e)
            cleanup()
            false
        }
    }

    fun stopRecording(): File? {
        if (!isRecording) return currentOutputFile
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.w("SiteAudioRecorder", "Error stopping recorder: ${e.message}")
        } finally {
            mediaRecorder = null
            isRecording = false
        }
        return currentOutputFile
    }

    fun getRecordingDurationSeconds(): Int {
        if (!isRecording || recordingStartTime == 0L) return 0
        return ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt()
    }

    private fun cleanup() {
        try {
            mediaRecorder?.release()
        } catch (ignored: Exception) {}
        mediaRecorder = null
        isRecording = false
        recordingStartTime = 0L
    }
}
