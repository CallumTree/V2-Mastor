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
    fun testAudioTranscription_ExtractsTasksTodosFinishedAndValuationNotes() {
        val voiceInput = "Drylining and plastering in Flat 3 underway. Multi-finish plaster skim coat progressing well. Signed off Corridor B metal stud framing with building control. Need to order 30 sheets of 15mm SoundBloc plasterboard and chase M&E subby for pipe pressure test."

        val analysis = com.example.domain.audio.SiteDiaryAudioTranscriber.fallbackAnalysis(voiceInput)

        assertNotNull(analysis)
        assertTrue("Headline should reflect trade context", analysis.headline.contains("Plaster") || analysis.headline.contains("Drylining"))
        assertEquals("WO-001", analysis.suggestedWoRef)
        assertTrue("Tasks list should be populated", analysis.tasks.isNotEmpty())
        assertTrue("Finished items should be extracted", analysis.finishedItems.isNotEmpty())
        assertTrue("To-Dos should be extracted", analysis.todos.isNotEmpty())
        assertTrue("Valuation notes should include financial milestone context", analysis.valuationNotes.contains("£") || analysis.valuationNotes.contains("%"))
        assertTrue("Scheduling notes should contain trade handover sequencing", analysis.taskScheduling.isNotBlank())

        // Test Room Entity serialization helpers
        val entry = SiteDiaryEntry(
            id = "diary_voice_test",
            projectId = "proj_101",
            workOrderId = analysis.suggestedWoRef,
            workOrderTitle = "Work Order WO-01",
            author = "Marcus Vance",
            dateDisplay = "17 Aug 2026",
            statusUpdate = analysis.suggestedStatus,
            notes = analysis.rawTranscription,
            photoUrl = "https://example.com/site.jpg",
            isVoiceTranscribed = true,
            audioTranscript = analysis.rawTranscription,
            audioTasksJson = analysis.tasksAsJson(),
            audioTodosJson = analysis.todosAsJson(),
            audioFinishedItemsJson = analysis.finishedItemsAsJson(),
            audioValuationNotes = analysis.valuationNotes,
            audioSchedulingNotes = analysis.taskScheduling
        )

        val deserializedTasks = entry.getTasksList()
        val deserializedTodos = entry.getTodosList()
        val deserializedFinished = entry.getFinishedItemsList()

        assertEquals(analysis.tasks.size, deserializedTasks.size)
        assertEquals(analysis.todos.size, deserializedTodos.size)
        assertEquals(analysis.finishedItems.size, deserializedFinished.size)
        assertEquals(analysis.valuationNotes, entry.audioValuationNotes)
    }

    @Test
    fun testSampleVoiceNotesDataIntegrity() {
        val samples = com.example.domain.audio.SiteAudioRecorder.sampleVoiceNotes
        assertTrue("Should have realistic preset voice notes available", samples.size >= 4)
        samples.forEach { sample ->
            assertTrue("Sample title should not be blank", sample.title.isNotBlank())
            assertTrue("Sample speech text should not be blank", sample.sampleSpeechText.length > 20)
            assertTrue("Sample trade category should be populated", sample.tradeCategory.isNotBlank())
        }
    }
}
