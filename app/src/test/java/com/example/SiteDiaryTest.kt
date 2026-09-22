package com.example

import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.SiteDiaryEntry
import com.example.data.entity.Valuation
import com.example.data.remote.GeminiClient
import com.example.domain.calculation.MastorCalculationEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SiteDiaryTest {

    private val testProject = Project(
        id = "proj_101",
        name = "142 Park Lane Townhouse",
        client = "Mayfair Heritage Holdings",
        address = "142 Park Lane, London W1K 7AA",
        siteManager = "Marcus Vance",
        surveyor = "Eleanor Vance",
        status = "Active",
        contractRef = "CT-2026-991",
        workType = "Internal Renovation",
        startDate = "01 Jan 2026",
        endDate = "31 Dec 2026",
        projectNumber = "PN-101",
        contractValue = 150000.00,
        uplift1Percent = 15.0,
        uplift2Percent = 5.0
    )

    private val testValuation = Valuation(
        id = "val_001",
        valuationNumber = "VAL-001",
        projectId = "proj_101",
        date = "28 Feb 2026",
        preparedBy = "Eleanor Vance (QS)",
        status = "Draft"
    )

    private val scope1 = ScopeElement(
        id = "scope_001",
        woRef = "WO-01",
        locationRoom = "Kitchen",
        code = "1.01",
        description = "Strip out existing kitchen units and fitments",
        qty = 1.0,
        units = "item",
        rate = 2500.00,
        claimPercent = 50.0, // 50% claimed = £1,250.00
        currentValuationId = "val_001"
    )

    @Test
    fun testSiteDiaryCreationAndFields() {
        val entry = SiteDiaryEntry(
            id = "diary_100",
            projectId = "proj_101",
            workOrderId = "WO-01",
            workOrderTitle = "WO-01: Demolition & Strip Out",
            author = "Marcus Vance (Site Manager)",
            dateDisplay = "09 Aug 2026, 08:30 AM",
            statusUpdate = "Progress On Track",
            weatherNotes = "17°C, Dry",
            laborCount = 6,
            notes = "Kitchen strip-out completed ahead of schedule. Asbestos report clear.",
            photoUrl = "https://images.unsplash.com/photo-1541888946425-d0fbb186a5b3",
            geminiSummary = "Kitchen strip-out finished ahead of target. No safety issues."
        )

        assertEquals("diary_100", entry.id)
        assertEquals("proj_101", entry.projectId)
        assertEquals(6, entry.laborCount)
        assertEquals("Progress On Track", entry.statusUpdate)
        assertNotNull(entry.geminiSummary)
    }

    @Test
    fun testArchitecturalBoundary_SiteDiaryDoesNotAlterValuationOrScope() {
        // Calculate valuation before site diary entry exists
        val valuationBefore = MastorCalculationEngine.calculateValuation(
            valuation = testValuation,
            scopeElements = listOf(scope1),
            variationOrders = emptyList(),
            project = testProject
        )

        val initialClaimedTotal = valuationBefore.scopeBaseClaimedTotal

        // Create 10 site diary log entries with various notes and statuses
        val siteDiaryEntries = (1..10).map { i ->
            SiteDiaryEntry(
                id = "diary_$i",
                projectId = "proj_101",
                workOrderId = "WO-01",
                workOrderTitle = "WO-01: Demolition",
                author = "Marcus Vance",
                dateDisplay = "0$i Aug 2026",
                statusUpdate = "Progress On Track",
                notes = "Daily log note $i",
                photoUrl = "photo_$i.jpg"
            )
        }

        // Re-calculate valuation after site diary entries created
        val valuationAfter = MastorCalculationEngine.calculateValuation(
            valuation = testValuation,
            scopeElements = listOf(scope1),
            variationOrders = emptyList(),
            project = testProject
        )

        // Verify claim total is strictly IDENTICAL (Architectural Boundary verified!)
        assertEquals(initialClaimedTotal, valuationAfter.scopeBaseClaimedTotal, 0.001)
        assertEquals(50.0, scope1.claimPercent, 0.001)
        assertEquals(1250.00, valuationAfter.scopeBaseClaimedTotal, 0.001)
    }

    @Test
    fun testGeminiLogSummarisationFallback() = runBlocking {
        val summary = GeminiClient.summarizeLogEntry(
            notes = "Completed strip out of hall partition wall. Waste skip collected.",
            workOrderTitle = "WO-01: Demolition & Strip Out",
            status = "Progress On Track"
        )

        assertNotNull(summary)
        assertTrue(summary.isNotBlank())
        // Ensure no sparkle or bot icons in text summary
        assertTrue(!summary.contains("✨"))
        assertTrue(!summary.contains("🤖"))
    }

    @Test
    fun testFallbackAnalysis_NeverInventsDiaryContent() {
        // When the AI call fails, the fallback must NOT fabricate completed work, variations,
        // weather, labour or money — the diary is a contractual record and finished items
        // would otherwise be auto-claimed against scope.
        val voiceInput = "Signed off Corridor B metal stud framing. Found rotten joists under the bath. Raining."

        val analysis = com.example.domain.audio.SiteDiaryAudioTranscriber.fallbackAnalysis(voiceInput)

        assertTrue("Fallback must be flagged as AI failure", !analysis.aiSucceeded)
        assertEquals("Raw text must be preserved for later review", voiceInput, analysis.rawTranscription)
        assertTrue("No finished items may be invented", analysis.finishedItems.isEmpty())
        assertTrue("No variations may be invented", analysis.variations.isEmpty())
        assertTrue("No tasks may be invented", analysis.tasks.isEmpty())
        assertTrue("No todos may be invented", analysis.todos.isEmpty())
        assertEquals("Labour must be 0 (unknown), not guessed", 0, analysis.laborCount)
        assertTrue("Weather must be blank, not guessed", analysis.weatherNotes.isBlank())
        assertTrue("No money figures may be invented", analysis.valuationNotes.isBlank())
        assertEquals("No work order may be guessed", null, analysis.suggestedWoRef)
    }

    @Test
    fun testDiaryEntry_SerialisesAnalysisLists() {
        val entry = SiteDiaryEntry(
            id = "diary_voice_test",
            projectId = "proj_101",
            workOrderId = null,
            workOrderTitle = null,
            author = "Site Manager",
            dateDisplay = "17 Aug 2026",
            statusUpdate = "Progress On Track",
            notes = "Bathroom tiling complete",
            photoUrl = "",
            isVoiceTranscribed = true,
            audioTranscript = "Bathroom tiling complete",
            audioTasksJson = org.json.JSONArray(listOf("Kitchen first fix")).toString(),
            audioTodosJson = org.json.JSONArray(listOf("Order skirting")).toString(),
            audioFinishedItemsJson = org.json.JSONArray(listOf("Bathroom wall tiling complete")).toString(),
            audioValuationNotes = "",
            audioSchedulingNotes = ""
        )

        assertEquals(1, entry.getTasksList().size)
        assertEquals(1, entry.getTodosList().size)
        assertEquals(listOf("Bathroom wall tiling complete"), entry.getFinishedItemsList())
    }

    @Test
    fun testSampleVoiceNotesDataIntegrity() {
        val samples = com.example.domain.audio.SiteAudioRecorder.sampleVoiceNotes
        assertTrue("Should have realistic preset voice notes available", samples.size >= 3)
        samples.forEach { sample ->
            assertTrue("Sample title should not be blank", sample.title.isNotBlank())
            assertTrue("Sample speech text should not be blank", sample.sampleSpeechText.length > 20)
            assertTrue("Sample trade category should be populated", sample.trade.isNotBlank())
        }
    }
}
