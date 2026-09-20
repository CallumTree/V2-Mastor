package com.example.domain.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.IOException

data class VoiceNoteSample(
    val title: String,
    val audioDuration: String,
    val sampleSpeechText: String,
    val trade: String
) {
    val transcript: String get() = sampleSpeechText
}

// Legacy alias for compatibility
typealias SampleVoiceNote = VoiceNoteSample

sealed class AudioRecordingState {
    object Idle : AudioRecordingState()
    object Recording : AudioRecordingState()
    data class Recorded(val file: File, val base64Data: String, val durationSeconds: Int) : AudioRecordingState()
    data class Error(val message: String) : AudioRecordingState()
}

object SiteAudioRecorder {

    val sampleVoiceNotes = listOf(
        VoiceNoteSample(
            title = "Drylining & Plastering Daily Walkthrough",
            audioDuration = "0:42",
            sampleSpeechText = "Site diary for Thursday 26th Feb. Kitchen Flat 3 - drywall subbie has finished metal stud partition framing in the hallway and master bedroom. They've started hanging SoundBloc plasterboards. Corridor B is now signed off by the building control inspector. We need an order of 30 additional 15mm acoustic sheets for the ceiling drop tomorrow morning. Plasterers are on site skimming room 2. 50% milestone reached on the drywall package.",
            trade = "Drylining & Partitions"
        ),
        VoiceNoteSample(
            title = "M&E First Fix & Chasing Inspection",
            audioDuration = "0:35",
            sampleSpeechText = "QS site inspection update for Flat 1 and 2. Electrical first fix containment and floor chasing complete. Plumbing contractor has installed copper hot and cold feeds to the kitchen riser. Air pressure test completed on the primary pipe run with zero drop. Need to chase subby for test certification sheet before Friday valuation cutoff. Claim ready at 40% completion.",
            trade = "M&E Services"
        ),
        VoiceNoteSample(
            title = "External Scaffolding & Brickwork Snagging",
            audioDuration = "0:28",
            sampleSpeechText = "Walked the North elevation scaffold with brickwork foreman. Coping stones on the parapet wall have been bedded in lime mortar and pointing completed to high standard. Scaffolders are scheduled to strike the top lift on Monday. Ready for final valuation measurement.",
            trade = "Masonry & External"
        )
    )

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var recordingStartTime: Long = 0L

    var isRecording: Boolean = false
        private set

    var recordingState: AudioRecordingState = AudioRecordingState.Idle
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
            recordingState = AudioRecordingState.Recording
            true
        } catch (e: IOException) {
            Log.e("SiteAudioRecorder", "Failed to start recording: ${e.message}", e)
            recordingState = AudioRecordingState.Error("Failed to start recording: ${e.message}")
            cleanup()
            false
        } catch (e: SecurityException) {
            Log.e("SiteAudioRecorder", "Microphone permission denied: ${e.message}", e)
            recordingState = AudioRecordingState.Error("Microphone permission denied")
            cleanup()
            false
        } catch (e: Exception) {
            Log.e("SiteAudioRecorder", "Recording initialization error: ${e.message}", e)
            recordingState = AudioRecordingState.Error("Recording error: ${e.message}")
            cleanup()
            false
        }
    }

    fun stopRecording(): File? {
        val duration = getRecordingDurationSeconds()
        if (isRecording) {
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
        }

        val file = currentOutputFile
        if (file != null && file.exists() && file.length() > 0L) {
            try {
                val bytes = file.readBytes()
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                recordingState = AudioRecordingState.Recorded(file, base64, duration)
                return file
            } catch (e: Exception) {
                Log.e("SiteAudioRecorder", "Error reading recorded audio bytes: ${e.message}")
                recordingState = AudioRecordingState.Error("Failed to process audio recording")
                return null
            }
        } else {
            recordingState = AudioRecordingState.Error("Recording was too short or empty")
            return null
        }
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
