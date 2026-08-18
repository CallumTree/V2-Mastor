package com.example

import com.example.data.entity.Project
import com.example.data.entity.Valuation
import com.example.data.entity.VariationOrder
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.screens.VO_STAGES
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VariationOrdersTest {

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
        uplift1Percent = 15.0, // +15%
        uplift2Percent = 5.0   // +5%
    )

    private val testValuation = Valuation(
        id = "val_001",
        valuationNumber = "VAL-001",
        projectId = "proj_101",
        date = "28 Feb 2026",
        preparedBy = "Eleanor Vance (QS)",
        status = "Draft"
    )

    @Test
    fun testFiveStatusStagesAreDefined() {
        assertEquals(5, VO_STAGES.size)
        assertEquals("VO Identified", VO_STAGES[0])
        assertEquals("VO Received", VO_STAGES[1])
        assertEquals("VO Completed", VO_STAGES[2])
        assertEquals("VO Invoiced", VO_STAGES[3])
        assertEquals("VO Paid", VO_STAGES[4])
    }

    @Test
    fun testMultiPropertyTicketMarkupCalculations() {
        // Multi-property ticket VO-003 spanning 2 properties with 3 lines
        val line1 = VariationOrder(
            id = "vo_003_p1", projectId = "proj_101", voNumber = "VO-003", externalVoNumber = "EXT-COUNCIL-991",
            status = "VO Completed", property = "142 Park Lane (Flat 1)", locationRoom = "Hallway", code = "VO-FIRE-01",
            description = "FD30S Fire-rated door set", qty = 4.0, units = "nr", rate = 650.0, dateRaised = "08 Feb 2026",
            tick = true, currentValuationId = "val_001"
        )
        val line2 = VariationOrder(
            id = "vo_003_p2", projectId = "proj_101", voNumber = "VO-003", externalVoNumber = "EXT-COUNCIL-991",
            status = "VO Completed", property = "142 Park Lane (Flat 1)", locationRoom = "Lobby", code = "VO-FIRE-02",
            description = "Emergency LED Exit Lighting", qty = 2.0, units = "nr", rate = 480.0, dateRaised = "08 Feb 2026",
            tick = true, currentValuationId = "val_001"
        )
        val line3 = VariationOrder(
            id = "vo_003_p3", projectId = "proj_101", voNumber = "VO-003", externalVoNumber = "EXT-COUNCIL-991",
            status = "VO Completed", property = "144 Park Lane (Flat 2)", locationRoom = "Stairwell", code = "VO-FIRE-03",
            description = "Intumescent timber fireproofing", qty = 35.0, units = "m²", rate = 95.0, dateRaised = "08 Feb 2026",
            tick = false, currentValuationId = "val_001"
        )

        val ticketLines = listOf(line1, line2, line3)

        // Hand Calculations:
        // Line 1 Base: 4 * 650 = 2600.0
        // Line 2 Base: 2 * 480 = 960.0
        // Line 3 Base: 35 * 95 = 3325.0
        // Total Base = 2600 + 960 + 3325 = £6,885.00
        val baseTotal = ticketLines.sumOf { it.qty * it.rate }
        assertEquals(6885.00, baseTotal, 0.01)

        // Uplifts:
        // Markup 1 (+15%): 6885 * 0.15 = 1032.75
        // Subtotal = 6885 + 1032.75 = 7917.75
        // Markup 2 (+5%): 7917.75 * 0.05 = 395.89
        // Tender Total = 7917.75 + 395.89 = £8,313.64
        val (u1, u2, tenderTotal) = MastorCalculationEngine.calculateProjectUplifts(
            baseAmount = baseTotal,
            uplift1Percent = testProject.uplift1Percent,
            uplift2Percent = testProject.uplift2Percent
        )

        assertEquals(1032.75, u1, 0.01)
        assertEquals(395.89, u2, 0.01)
        assertEquals(8313.64, tenderTotal, 0.01)
    }

    @Test
    fun testTickingVoLinesFeedsSection2InValuation() {
        val line1 = VariationOrder(
            id = "vo_003_p1", projectId = "proj_101", voNumber = "VO-003", externalVoNumber = "EXT-COUNCIL-991",
            status = "VO Completed", property = "142 Park Lane (Flat 1)", locationRoom = "Hallway", code = "VO-FIRE-01",
            description = "FD30S Fire-rated door set", qty = 4.0, units = "nr", rate = 650.0, dateRaised = "08 Feb 2026",
            tick = true, currentValuationId = "val_001"
        )
        val line2 = VariationOrder(
            id = "vo_003_p2", projectId = "proj_101", voNumber = "VO-003", externalVoNumber = "EXT-COUNCIL-991",
            status = "VO Completed", property = "142 Park Lane (Flat 1)", locationRoom = "Lobby", code = "VO-FIRE-02",
            description = "Emergency LED Exit Lighting", qty = 2.0, units = "nr", rate = 480.0, dateRaised = "08 Feb 2026",
            tick = true, currentValuationId = "val_001"
        )
        val line3Unticked = VariationOrder(
            id = "vo_003_p3", projectId = "proj_101", voNumber = "VO-003", externalVoNumber = "EXT-COUNCIL-991",
            status = "VO Completed", property = "144 Park Lane (Flat 2)", locationRoom = "Stairwell", code = "VO-FIRE-03",
            description = "Intumescent timber fireproofing", qty = 35.0, units = "m²", rate = 95.0, dateRaised = "08 Feb 2026",
            tick = false, currentValuationId = "val_001"
        )

        val valCalc = MastorCalculationEngine.calculateValuation(
            valuation = testValuation,
            scopeElements = emptyList(),
            variationOrders = listOf(line1, line2, line3Unticked),
            project = testProject
        )

        // Only ticked lines feed Section 2:
        // Line 1 (2600.0) + Line 2 (960.0) = £3,560.00
        assertEquals(3560.00, valCalc.voBaseClaimedTotal, 0.01)

        // Now tick line 3 and check live calculation update
        val line3Ticked = line3Unticked.copy(tick = true)
        val updatedValCalc = MastorCalculationEngine.calculateValuation(
            valuation = testValuation,
            scopeElements = emptyList(),
            variationOrders = listOf(line1, line2, line3Ticked),
            project = testProject
        )

        // Base Claimed now includes line 3 (3325.0): 3560.0 + 3325.0 = £6,885.00
        assertEquals(6885.00, updatedValCalc.voBaseClaimedTotal, 0.01)
    }
}
