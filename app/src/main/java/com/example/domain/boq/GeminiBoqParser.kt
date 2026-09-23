package com.example.domain.boq

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

object GeminiBoqParser {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_PROMPT = """
You are an expert UK Quantity Surveyor assistant parsing a Bill of Quantities (BoQ) or construction scope document into structured Work Orders and Scope Elements.

STRICT PARSING RULES:
1. Extract exact descriptions, codes, locations/rooms, quantities, units, and rates from the provided text.
2. BASE RATE ONLY: Do NOT apply any uplift or profit margin markup. Extract the net base rate as specified.
3. VERBATIM & NO INVENTED DATA: Do not invent missing quantities or rates.
4. CONFIDENCE & FLAG REASON: For every scope element line, assign confidence as "HIGH", "MEDIUM", or "LOW".
   - Assign "LOW" or "MEDIUM" if a rate is estimated, unit is ambiguous, description is vague, or room location had to be inferred.
   - For every "LOW" or "MEDIUM" line, provide a clear, calm 'flagReason' explaining why (e.g. "Ambiguous unit specification", "Rate requires quantity surveyor verification", "Location inferred from context").
5. Group items logically by Work Order reference (e.g. WO-001, WO-002) and work type ("Internal Works" or "PPR").

Respond strictly with a JSON object containing 'workOrders' array matching the requested schema.
"""

    /**
     * Parses raw BoQ text into structured ParsedBoqResult.
     * Uses Gemini API if API key is present, falling back to structured deterministic parsing.
     */
    suspend fun parseBoqText(rawText: String): ParsedBoqResult = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }

        var aiProblem = "AI parsing unavailable (no API key)."
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "null") {
            try {
                val result = callGeminiApi(rawText, apiKey)
                if (result != null && result.workOrders.isNotEmpty()) {
                    return@withContext result
                }
                aiProblem = "AI couldn't read any line items from this document."
            } catch (e: Exception) {
                aiProblem = "AI parsing failed — check your signal and try again."
            }
        }

        // Local fallback only handles structured CSV/TXT. It never substitutes sample data.
        val local = parseBoqTextLocally(rawText)
        if (local.workOrders.isEmpty()) {
            return@withContext ParsedBoqResult(
                failureReason = "$aiProblem Nothing has been imported. Try again with signal, or upload a CSV export of the schedule."
            )
        }
        return@withContext local
    }

    private fun callGeminiApi(rawText: String, apiKey: String): ParsedBoqResult? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

        val jsonSchema = JSONObject().apply {
            put("type", "OBJECT")
            put("properties", JSONObject().apply {
                put("workOrders", JSONObject().apply {
                    put("type", "ARRAY")
                    put("items", JSONObject().apply {
                        put("type", "OBJECT")
                        put("properties", JSONObject().apply {
                            put("woRef", JSONObject().put("type", "STRING"))
                            put("description", JSONObject().put("type", "STRING"))
                            put("workType", JSONObject().put("type", "STRING"))
                            put("customer", JSONObject().put("type", "STRING"))
                            put("responsibleParty", JSONObject().put("type", "STRING"))
                            put("scopeElements", JSONObject().apply {
                                put("type", "ARRAY")
                                put("items", JSONObject().apply {
                                    put("type", "OBJECT")
                                    put("properties", JSONObject().apply {
                                        put("code", JSONObject().put("type", "STRING"))
                                        put("locationRoom", JSONObject().put("type", "STRING"))
                                        put("description", JSONObject().put("type", "STRING"))
                                        put("qty", JSONObject().put("type", "NUMBER"))
                                        put("units", JSONObject().put("type", "STRING"))
                                        put("rate", JSONObject().put("type", "NUMBER"))
                                        put("confidence", JSONObject().put("type", "STRING"))
                                        put("flagReason", JSONObject().put("type", "STRING"))
                                    })
                                })
                            })
                        })
                    })
                })
            })
        }

        val requestBodyJson = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", "Parse the following Bill of Quantities document:\n\n$rawText")
                }))
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
        if (!httpResponse.isSuccessful) return null

        val responseString = httpResponse.body?.string() ?: return null
        val rootJson = JSONObject(responseString)
        val candidates = rootJson.optJSONArray("candidates") ?: return null
        val textResponse = candidates.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text") ?: return null

        val parsedObj = JSONObject(textResponse)
        val wosArray = parsedObj.optJSONArray("workOrders") ?: return null

        val parsedWos = mutableListOf<ParsedWorkOrder>()
        for (i in 0 until wosArray.length()) {
            val woObj = wosArray.getJSONObject(i)
            val woRef = woObj.optString("woRef", "").trim().ifBlank { "WO-${(i + 1).toString().padStart(2, '0')}" }
            val woDesc = woObj.optString("description", "Work Order $woRef")
            val workType = woObj.optString("workType", "Internal Works")
            val customer = woObj.optString("customer", "")
            val responsibleParty = woObj.optString("responsibleParty", "")

            val elementsArray = woObj.optJSONArray("scopeElements")
            val parsedElements = mutableListOf<ParsedScopeElement>()

            if (elementsArray != null) {
                for (j in 0 until elementsArray.length()) {
                    val elemObj = elementsArray.getJSONObject(j)
                    // Missing values are NEVER filled with invented numbers. Anything missing is
                    // left at 0/blank and forced to LOW confidence so it's caught at review.
                    val code = elemObj.optString("code", "").trim()
                    val room = elemObj.optString("locationRoom", "").trim().ifBlank { "General" }
                    val desc = elemObj.optString("description", "").trim()
                    if (desc.isBlank()) continue
                    val hasQty = elemObj.has("qty") && !elemObj.isNull("qty")
                    val hasRate = elemObj.has("rate") && !elemObj.isNull("rate")
                    val qty = if (hasQty) elemObj.optDouble("qty", 0.0).takeIf { !it.isNaN() } ?: 0.0 else 0.0
                    val units = elemObj.optString("units", "").trim().ifBlank { "item" }
                    val rate = if (hasRate) elemObj.optDouble("rate", 0.0).takeIf { !it.isNaN() } ?: 0.0 else 0.0
                    val missing = listOfNotNull(
                        if (code.isBlank()) "SoR code" else null,
                        if (!hasQty || qty <= 0.0) "quantity" else null,
                        if (!hasRate || rate <= 0.0) "rate" else null
                    )
                    val modelConf = elemObj.optString("confidence", "LOW").uppercase()
                    val conf = if (missing.isNotEmpty()) "LOW" else modelConf
                    val modelFlag = if (elemObj.has("flagReason") && !elemObj.isNull("flagReason")) elemObj.getString("flagReason") else null
                    val flag = if (missing.isNotEmpty()) {
                        "Missing ${missing.joinToString(", ")} — check against the works order" +
                            (if (!modelFlag.isNullOrBlank()) ". $modelFlag" else "")
                    } else modelFlag

                    parsedElements.add(
                        ParsedScopeElement(
                            woRef = woRef,
                            code = code,
                            locationRoom = room,
                            description = desc,
                            qty = qty,
                            units = units,
                            rate = rate,
                            confidence = conf,
                            flagReason = flag
                        )
                    )
                }
            }

            parsedWos.add(
                ParsedWorkOrder(
                    woRef = woRef,
                    description = woDesc,
                    workType = workType,
                    customer = customer,
                    responsibleParty = responsibleParty,
                    scopeElements = parsedElements
                )
            )
        }

        return ParsedBoqResult(workOrders = parsedWos)
    }

    /**
     * Local deterministic fallback parser for CSV / TXT / Sample BoQs.
     */
    fun parseBoqTextLocally(rawText: String): ParsedBoqResult {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val woMap = mutableMapOf<String, MutableList<ParsedScopeElement>>()
        val woDescMap = mutableMapOf<String, String>()

        var currentWoRef = "IMPORT"
        woDescMap[currentWoRef] = "Imported Schedule"

        for (line in lines) {
            if (line.startsWith("WO:", ignoreCase = true) || line.startsWith("Work Order:", ignoreCase = true)) {
                val parts = line.split(":", limit = 2)
                if (parts.size == 2) {
                    val subParts = parts[1].split("-", limit = 2)
                    currentWoRef = subParts[0].trim()
                    val desc = if (subParts.size == 2) subParts[1].trim() else "Work Order $currentWoRef"
                    woDescMap[currentWoRef] = desc
                }
            } else if (line.contains(",") || line.contains("|") || line.contains("\t")) {
                val delimiter = if (line.contains(",")) "," else if (line.contains("|")) "|" else "\t"
                val cols = line.split(delimiter).map { it.trim() }
                if (cols.size >= 4 && !cols[0].equals("Code", ignoreCase = true) && !cols[0].equals("WO", ignoreCase = true)) {
                    val rawCode = cols.getOrNull(0)
                    val rawQty = cols.getOrNull(3)?.toDoubleOrNull()
                    val rawRate = cols.getOrNull(5)?.toDoubleOrNull()

                    // Never invent SoR codes or quantities — blanks are flagged LOW for review.
                    val code = rawCode ?: ""
                    val room = cols.getOrNull(1) ?: "General"
                    val desc = cols.getOrNull(2) ?: "Scope Item"
                    val qty = rawQty ?: 0.0
                    val units = cols.getOrNull(4) ?: "item"
                    val rate = rawRate ?: 0.0

                    // Evaluate confidence — anything we had to fabricate (missing code,
                    // qty, or rate) MUST be flagged so it can never reach a valuation
                    // unreviewed.
                    val isLowConfidence = desc.contains("tbc", ignoreCase = true) ||
                            desc.contains("provisional", ignoreCase = true) ||
                            units.contains("item", ignoreCase = true) ||
                            rawCode == null ||
                            rawQty == null ||
                            rawRate == null ||
                            rate == 0.0

                    val confidence = if (isLowConfidence) "LOW" else "HIGH"
                    val flagReason = if (isLowConfidence) {
                        if (desc.contains("provisional", ignoreCase = true)) "Provisional sum item requiring quantity surveyor verification"
                        else if (units.contains("item", ignoreCase = true)) "Lump sum item specification needing line breakdown"
                        else "Rate or quantity flagged for QS review"
                    } else null

                    val list = woMap.getOrPut(currentWoRef) { mutableListOf() }
                    list.add(
                        ParsedScopeElement(
                            woRef = currentWoRef,
                            code = code,
                            locationRoom = room,
                            description = desc,
                            qty = qty,
                            units = units,
                            rate = rate,
                            confidence = confidence,
                            flagReason = flagReason
                        )
                    )
                }
            }
        }

        // Nothing structured found: return empty. NEVER substitute sample data for a real document.
        if (woMap.isEmpty() || woMap.values.all { it.isEmpty() }) {
            return ParsedBoqResult()
        }

        val workOrders = woMap.map { (woRef, scopeList) ->
            ParsedWorkOrder(
                woRef = woRef,
                description = woDescMap[woRef] ?: "Work Order $woRef",
                workType = if (woRef.contains("PPR", ignoreCase = true)) "PPR" else "Internal Works",
                scopeElements = scopeList
            )
        }

        return ParsedBoqResult(workOrders = workOrders)
    }

    // --- Pre-packaged Sample BoQ Documents for UI Quick-Select ---

    val SAMPLE_BOQ_1_TEXT = """
WO: WO-001 - Flat 1 Refurbishment
CODE, ROOM, DESCRIPTION, QTY, UNITS, BASE_RATE
KIT-101, Kitchen, Supply and install bespoke oak veneer base and wall cabinets, 12.0, m, 220.00
KIT-102, Kitchen, 30mm Quartz worktop cut to fit with undermount sink cutout, 6.5, m, 280.00
BTH-101, Bathroom, Porcelain floor tiling 600x600mm non-slip, 18.5, m², 85.00
BTH-102, Bathroom, Thermostatic concealed shower valve & rainfall head (Provisional), 1.0, item, 350.00
LIV-101, Living Room, Engineered oak floorboard installation, 42.0, m², 95.00
LIV-102, Living Room, Acoustic timber wall slatted feature panel (TBC spec), 1.0, item, 450.00

WO: WO-002 - PPR Roof & Exterior Masonry
CODE, ROOM, DESCRIPTION, QTY, UNITS, BASE_RATE
EXT-201, Exterior, Repointing brickwork lime mortar historic facade, 115.0, m², 65.00
ROF-201, Roof, Natural Welsh slate roof tile replacement, 85.0, m², 140.00
ROF-202, Roof, Lead flashing code 5 lead gutter lining, 24.0, m, 110.00
    """.trimIndent()

    val SAMPLE_BOQ_2_TEXT = """
WO: WO-003 - Commercial Fitout M&E Package
CODE, ROOM, DESCRIPTION, QTY, UNITS, BASE_RATE
MEC-301, Plant Room, VRF Air conditioning outdoor heat pump system, 2.0, item, 3200.00
MEC-302, Office Area, Ceiling cassette fan coil units, 8.0, nr, 680.00
ELE-301, Reception, Recessed LED architectural linear lighting grid, 35.0, m, 125.00
ELE-302, Server Room, Dedicated sub-distribution board and 3-phase power, 1.0, item, 1850.00
    """.trimIndent()

    fun getSampleBoqResult1(): ParsedBoqResult {
        return ParsedBoqResult(
            projectReference = "MAYFAIR-REF-2026",
            workOrders = listOf(
                ParsedWorkOrder(
                    woRef = "WO-001",
                    description = "Flat 1 High-End Refurbishment",
                    workType = "Internal Works",
                    customer = "Mayfair Heritage Holdings",
                    responsibleParty = "Apex Interiors Ltd",
                    scopeElements = listOf(
                        ParsedScopeElement(
                            woRef = "WO-001",
                            code = "KIT-101",
                            locationRoom = "Kitchen",
                            description = "Supply and install bespoke oak veneer base and wall cabinets",
                            qty = 12.0,
                            units = "m",
                            rate = 220.00,
                            confidence = "HIGH"
                        ),
                        ParsedScopeElement(
                            woRef = "WO-001",
                            code = "KIT-102",
                            locationRoom = "Kitchen",
                            description = "30mm Quartz worktop cut to fit with undermount sink cutout",
                            qty = 6.5,
                            units = "m",
                            rate = 280.00,
                            confidence = "HIGH"
                        ),
                        ParsedScopeElement(
                            woRef = "WO-001",
                            code = "BTH-101",
                            locationRoom = "Bathroom",
                            description = "Porcelain floor tiling 600x600mm non-slip with tanking kit",
                            qty = 18.5,
                            units = "m²",
                            rate = 85.00,
                            confidence = "HIGH"
                        ),
                        ParsedScopeElement(
                            woRef = "WO-001",
                            code = "BTH-102",
                            locationRoom = "Bathroom",
                            description = "Thermostatic concealed shower valve & rainfall head (Provisional Rate)",
                            qty = 1.0,
                            units = "item",
                            rate = 350.00,
                            confidence = "LOW",
                            flagReason = "Provisional sum item: base rate requires final QS invoice verification"
                        ),
                        ParsedScopeElement(
                            woRef = "WO-001",
                            code = "LIV-101",
                            locationRoom = "Living Room",
                            description = "Engineered oak floorboard installation over acoustic underlay",
                            qty = 42.0,
                            units = "m²",
                            rate = 95.00,
                            confidence = "HIGH"
                        ),
                        ParsedScopeElement(
                            woRef = "WO-001",
                            code = "LIV-102",
                            locationRoom = "Living Room",
                            description = "Acoustic timber wall slatted feature panel (TBC acoustic rating)",
                            qty = 1.0,
                            units = "item",
                            rate = 450.00,
                            confidence = "MEDIUM",
                            flagReason = "Acoustic rating specification pending architect approval"
                        )
                    )
                ),
                ParsedWorkOrder(
                    woRef = "WO-002",
                    description = "PPR Roof & Exterior Masonry Facade",
                    workType = "PPR",
                    customer = "Mayfair Heritage Holdings",
                    responsibleParty = "Grosvenor Roofing & Building",
                    scopeElements = listOf(
                        ParsedScopeElement(
                            woRef = "WO-002",
                            code = "EXT-201",
                            locationRoom = "Exterior",
                            description = "Repointing brickwork with traditional lime mortar on historic facade",
                            qty = 115.0,
                            units = "m²",
                            rate = 65.00,
                            confidence = "HIGH"
                        ),
                        ParsedScopeElement(
                            woRef = "WO-002",
                            code = "ROF-201",
                            locationRoom = "Roof",
                            description = "Natural Welsh slate roof tile replacement including breather membrane",
                            qty = 85.0,
                            units = "m²",
                            rate = 140.00,
                            confidence = "HIGH"
                        ),
                        ParsedScopeElement(
                            woRef = "WO-002",
                            code = "ROF-202",
                            locationRoom = "Roof",
                            description = "Lead flashing code 5 lead gutter lining and valley detailing",
                            qty = 24.0,
                            units = "m",
                            rate = 110.00,
                            confidence = "HIGH"
                        )
                    )
                )
            )
        )
    }

    fun getSampleBoqResult2(): ParsedBoqResult {
        return ParsedBoqResult(
            projectReference = "COMM-MEC-2026",
            workOrders = listOf(
                ParsedWorkOrder(
                    woRef = "WO-003",
                    description = "Commercial Mechanical & HVAC Package",
                    workType = "Internal Works",
                    customer = "Mayfair Heritage Holdings",
                    responsibleParty = "Apex Mechanical Systems",
                    scopeElements = listOf(
                        ParsedScopeElement(
                            woRef = "WO-003",
                            code = "MEC-301",
                            locationRoom = "Plant Room",
                            description = "VRF Air conditioning outdoor heat pump system with refrigerant pipework",
                            qty = 2.0,
                            units = "item",
                            rate = 3200.00,
                            confidence = "HIGH"
                        ),
                        ParsedScopeElement(
                            woRef = "WO-003",
                            code = "MEC-302",
                            locationRoom = "Office Area",
                            description = "Ceiling cassette fan coil units connected to BMS control grid",
                            qty = 8.0,
                            units = "nr",
                            rate = 680.00,
                            confidence = "HIGH"
                        ),
                        ParsedScopeElement(
                            woRef = "WO-003",
                            code = "ELE-301",
                            locationRoom = "Reception",
                            description = "Recessed LED architectural linear lighting grid with dimming ballast",
                            qty = 35.0,
                            units = "m",
                            rate = 125.00,
                            confidence = "HIGH"
                        ),
                        ParsedScopeElement(
                            woRef = "WO-003",
                            code = "ELE-302",
                            locationRoom = "Server Room",
                            description = "Dedicated sub-distribution board & 3-phase power supply",
                            qty = 1.0,
                            units = "item",
                            rate = 1850.00,
                            confidence = "MEDIUM",
                            flagReason = "Sub-distribution board capacity requires electrical engineer sign-off"
                        )
                    )
                )
            )
        )
    }
}
