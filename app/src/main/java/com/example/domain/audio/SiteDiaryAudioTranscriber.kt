package com.example.domain.audio

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SiteDiaryAudioAnalysis(
    val headline: String,
    val suggestedWoRef: String?,
    val suggestedStatus: String,
    val rawTranscription: String,
    val weatherNotes: String,
    val laborCount: Int,
    val tasks: List<String>,
    val todos: List<String>,
    val finishedItems: List<String>,
    val valuationNotes: String,
    val taskScheduling: String,
    /** Extra works found on site that are NOT in the original scope — candidate variations. */
    val variations: List<DetectedVariation> = emptyList(),
    /**
     * False when the AI call failed and this is a local keyword-only result.
     * UI must tell the user; nothing in a failed result may be auto-claimed.
     */
    val aiSucceeded: Boolean = true
) {
    fun tasksAsJson(): String = JSONArray(tasks).toString()
    fun todosAsJson(): String = JSONArray(todos).toString()
    fun finishedItemsAsJson(): String = JSONArray(finishedItems).toString()
}

/**
 * A candidate variation heard in the site diary. Quantities are only ever what
 * the speaker actually said — never estimated by the model.
 */
data class DetectedVariation(
    val description: String,
    val locationRoom: String,
    val qty: Double?,
    val unit: String?,
    val reason: String
)

// Legacy alias for compatibility
typealias AudioDiaryAnalysis = SiteDiaryAudioAnalysis

object SiteDiaryAudioTranscriber {

    private const val GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_PROMPT = """
You are a UK construction site diary assistant for a contractor delivering Schedule of Rates
(SoR) repair and refurbishment works for local authorities. You receive a site manager's
spoken walk-round. The diary is a contractual record — accuracy matters more than completeness.

ABSOLUTE RULES
- Only record what the speaker actually said. Never invent tasks, items, quantities, weather,
  labour numbers or money figures. If something was not said, leave it empty.
- rawTranscription: verbatim transcript of what was spoken.

FIELDS
- headline: short title of today's activity, from what was said.
- suggestedWoRef: a works order reference only if one was spoken, else empty string.
- suggestedStatus: one of 'Progress On Track', 'Material Delay', 'Weather Stoppage',
  'Safety Inspection Flag' — pick based on what was said.
- weatherNotes: only if weather was mentioned, else empty string.
- laborCount: number of operatives only if stated, else 0.
- tasks: work in progress that was mentioned.
- todos: actions, orders, follow-ups the speaker said need doing.
- finishedItems: ONLY work the speaker explicitly said is complete, done, finished, fitted,
  hung, signed off, or a stated percentage. Include the room and any stated percentage in
  the text, e.g. "Bathroom wall tiling complete", "Bedroom 1 skim 50%". Never infer completion.
- valuationNotes: only money/valuation comments actually spoken, else empty string.
- taskScheduling: only sequencing comments actually spoken, else empty string.
- variations: EXTRA work discovered on site that is outside the original job — e.g.
  "found rotten joists under the bath", "client wants an extra socket", "ceiling came down
  when we stripped it", "need to replace the cill as well". For each:
    description: what the extra work is, in plain trade language
    locationRoom: the room/area if said, else empty string
    qty: number ONLY if the speaker gave one, else null
    unit: unit ONLY if said or obvious from the stated qty (m, m2, nr, lm, item), else null
    reason: why it's needed, from what was said
  Do NOT list normal scoped work as a variation. If unsure whether it's extra, still include
  it — the site manager will review every variation before it goes anywhere.

Respond strictly in JSON matching the schema.
"""

    /**
     * Transcribes audio or analyzes speech text using Gemini multimodal API, with robust local fallback.
     */
    suspend fun transcribeAndExtract(
        audioBase64: String? = null,
        mimeType: String = "audio/mp4",
        speechTextFallback: String? = null
    ): SiteDiaryAudioAnalysis = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "null" && apiKey != "DEFAULT_KEY") {
            try {
                val result = callGemini(audioBase64, mimeType, speechTextFallback, apiKey)
                if (result != null) {
                    return@withContext result
                }
            } catch (e: Throwable) {
                Log.w("SiteDiaryAudioTranscriber", "Gemini API call failed, falling back to local extractor: ${e.message}")
            }
        }

        val textToAnalyze = speechTextFallback ?: ""
        return@withContext fallbackAnalysis(textToAnalyze)
    }

    /**
     * Legacy wrapper for backward compatibility.
     */
    suspend fun analyzeVoiceNote(voiceInput: String): SiteDiaryAudioAnalysis {
        return transcribeAndExtract(audioBase64 = null, mimeType = "audio/mp4", speechTextFallback = voiceInput)
    }

    private fun callGemini(
        audioBase64: String?,
        mimeType: String,
        speechText: String?,
        apiKey: String
    ): SiteDiaryAudioAnalysis? {
        val url = "$GEMINI_URL?key=$apiKey"

        val jsonSchema = JSONObject().apply {
            put("type", "OBJECT")
            put("properties", JSONObject().apply {
                put("headline", JSONObject().put("type", "STRING"))
                put("suggestedWoRef", JSONObject().put("type", "STRING"))
                put("suggestedStatus", JSONObject().put("type", "STRING"))
                put("rawTranscription", JSONObject().put("type", "STRING"))
                put("weatherNotes", JSONObject().put("type", "STRING"))
                put("laborCount", JSONObject().put("type", "INTEGER"))
                put("tasks", JSONObject().apply {
                    put("type", "ARRAY")
                    put("items", JSONObject().put("type", "STRING"))
                })
                put("todos", JSONObject().apply {
                    put("type", "ARRAY")
                    put("items", JSONObject().put("type", "STRING"))
                })
                put("finishedItems", JSONObject().apply {
                    put("type", "ARRAY")
                    put("items", JSONObject().put("type", "STRING"))
                })
                put("valuationNotes", JSONObject().put("type", "STRING"))
                put("taskScheduling", JSONObject().put("type", "STRING"))
                put("variations", JSONObject().apply {
                    put("type", "ARRAY")
                    put("items", JSONObject().apply {
                        put("type", "OBJECT")
                        put("properties", JSONObject().apply {
                            put("description", JSONObject().put("type", "STRING"))
                            put("locationRoom", JSONObject().put("type", "STRING"))
                            put("qty", JSONObject().put("type", "NUMBER").put("nullable", true))
                            put("unit", JSONObject().put("type", "STRING").put("nullable", true))
                            put("reason", JSONObject().put("type", "STRING"))
                        })
                        put("required", JSONArray(listOf("description", "locationRoom", "reason")))
                    })
                })
            })
            put("required", JSONArray(listOf("headline", "rawTranscription", "weatherNotes", "laborCount", "tasks", "todos", "finishedItems", "valuationNotes", "taskScheduling", "variations")))
        }

        val partsArray = JSONArray()

        if (!audioBase64.isNullOrBlank()) {
            val audioInline = JSONObject().apply {
                put("inlineData", JSONObject().apply {
                    put("mimeType", mimeType)
                    put("data", audioBase64)
                })
            }
            partsArray.put(audioInline)
            partsArray.put(JSONObject().apply {
                put("text", "Transcribe this site manager's recording and extract the diary fields. Only record what was actually said.")
            })
        } else if (!speechText.isNullOrBlank()) {
            partsArray.put(JSONObject().apply {
                put("text", "Site Voice Audio Recording Transcription:\n\n\"$speechText\"")
            })
        } else {
            return null
        }

        val requestBodyJson = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", partsArray)
            }))
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", SYSTEM_PROMPT)
                }))
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("responseSchema", jsonSchema)
            })
        }

        val requestBody = requestBodyJson.toString().toRequestBody("application/json".toMediaType())
        val httpRequest = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val httpResponse = okHttpClient.newCall(httpRequest).execute()
        if (!httpResponse.isSuccessful) {
            Log.w("SiteDiaryAudioTranscriber", "HTTP ${httpResponse.code}: ${httpResponse.message}")
            return null
        }

        val responseString = httpResponse.body?.string() ?: return null
        val rootJson = JSONObject(responseString)
        val candidates = rootJson.optJSONArray("candidates") ?: return null
        val textResponse = candidates.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text") ?: return null

        val obj = JSONObject(textResponse)

        fun strList(key: String): List<String> {
            val arr = obj.optJSONArray(key) ?: return emptyList()
            return (0 until arr.length()).mapNotNull { i ->
                arr.optString(i).trim().takeIf { it.isNotBlank() }
            }
        }

        val variations = mutableListOf<DetectedVariation>()
        obj.optJSONArray("variations")?.let { arr ->
            for (i in 0 until arr.length()) {
                val v = arr.optJSONObject(i) ?: continue
                val desc = v.optString("description").trim()
                if (desc.isBlank()) continue
                val qty = if (v.has("qty") && !v.isNull("qty")) v.optDouble("qty").takeIf { !it.isNaN() && it > 0 } else null
                val unit = if (v.has("unit") && !v.isNull("unit")) v.optString("unit").trim().takeIf { it.isNotBlank() } else null
                variations.add(
                    DetectedVariation(
                        description = desc,
                        locationRoom = v.optString("locationRoom").trim(),
                        qty = qty,
                        unit = unit,
                        reason = v.optString("reason").trim()
                    )
                )
            }
        }

        val woRef = obj.optString("suggestedWoRef").trim().takeIf { it.isNotBlank() && it != "null" }

        return SiteDiaryAudioAnalysis(
            headline = obj.optString("headline").trim().ifBlank { "Site Diary Entry" },
            suggestedWoRef = woRef,
            suggestedStatus = obj.optString("suggestedStatus").trim().ifBlank { "Progress On Track" },
            rawTranscription = obj.optString("rawTranscription").trim().ifBlank { speechText ?: "" },
            weatherNotes = obj.optString("weatherNotes").trim(),
            laborCount = obj.optInt("laborCount", 0).coerceAtLeast(0),
            tasks = strList("tasks"),
            todos = strList("todos"),
            finishedItems = strList("finishedItems"),
            valuationNotes = obj.optString("valuationNotes").trim(),
            taskScheduling = obj.optString("taskScheduling").trim(),
            variations = variations,
            aiSucceeded = true
        )
    }

    /**
     * Used only when the AI call fails (no signal, API error, no key).
     *
     * Deliberately does NOT extract finished items, variations, weather, labour or money.
     * A site diary is a contractual record: a failed AI call must never produce invented
     * entries, and nothing from this result may be auto-claimed against scope.
     * The raw text (if any) is kept so the site manager can review and re-analyse later.
     */
    fun fallbackAnalysis(voiceInput: String): SiteDiaryAudioAnalysis {
        return SiteDiaryAudioAnalysis(
            headline = "Site Diary Entry (AI unavailable — review needed)",
            suggestedWoRef = null,
            suggestedStatus = "Progress On Track",
            rawTranscription = voiceInput,
            weatherNotes = "",
            laborCount = 0,
            tasks = emptyList(),
            todos = emptyList(),
            finishedItems = emptyList(),
            valuationNotes = "",
            taskScheduling = "",
            variations = emptyList(),
            aiSucceeded = false
        )
    }
}
