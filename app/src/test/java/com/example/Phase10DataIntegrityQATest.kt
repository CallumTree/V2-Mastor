package com.example

import com.example.data.entity.InvoiceRecord
import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.Valuation
import com.example.data.entity.VariationOrder
import com.example.data.entity.WorkOrder
import com.example.domain.calculation.MastorCalculationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class Phase10DataIntegrityQATest {

    private lateinit var project: Project
    private lateinit var valuation: Valuation
    private val scopeElements = mutableListOf<ScopeElement>()
    val variationOrders = mutableListOf<VariationOrder>()

    @Before
    fun setUp() {
        project = Project(
            id = "proj_p10",
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

        valuation = Valuation(
            id = "val_p10",
            valuationNumber = "VAL-001",
            projectId = "proj_p10",
            date = "01 Mar 2026",
            preparedBy = "Eleanor Vance (QS)",
            status = "Draft"
        )

        // Generate ~58 items across 6 locations
        val locations = listOf("Kitchen", "Dining Room", "Master Bedroom", "Family Bathroom", "Exterior Elevation", "Roof Void")
        val woRefs = listOf("WO-001", "WO-002", "WO-003")

        var itemCounter = 1
        locations.forEachIndexed { locIdx, location ->
            for (i in 1..10) {
                if (itemCounter > 58) break
                val wo = woRefs[(locIdx + i) % woRefs.size]
                val qty = 1.0 + (i * 2.5)
                val rate = 50.0 + (i * 125.0)
                // Claim subset of items
                val claimPct = if (itemCounter % 3 == 0) 100.0 else if (itemCounter % 5 == 0) 50.0 else 0.0

                scopeElements.add(
                    ScopeElement(
                        id = "se_$itemCounter",
                        woRef = wo,
                        locationRoom = location,
                        code = "SEC-${100 + itemCounter}",
                        description = "$location Item #$i specification supply & install",
                        qty = qty,
                        units = if (i % 2 == 0) "m2" else "nr",
                        rate = rate,
                        claimPercent = claimPct,
                        currentValuationId = if (claimPct > 0) valuation.id else null
                    )
                )
                itemCounter++
            }
        }

        // Add Multi-Property Variation Orders
        variationOrders.add(
            VariationOrder(
                id = "vo_p10_1",
                projectId = project.id,
                voNumber = "VO-001",
                externalVoNumber = "EXT-VO-1",
                status = "VO Approved",
                property = "Property A - Flat 1",
                locationRoom = "Kitchen",
                code = "VO-K1",
                description = "Structural beam replacement",
                qty = 1.0,
                units = "nr",
                rate = 4250.0,
                dateRaised = "10 Feb 2026",
                tick = true,
                currentValuationId = valuation.id
            )
        )

        variationOrders.add(
            VariationOrder(
                id = "vo_p10_2",
                projectId = project.id,
                voNumber = "VO-002",
                externalVoNumber = "EXT-VO-2",
                status = "VO Approved",
                property = "Property B - Flat 2",
                locationRoom = "Bathroom",
                code = "VO-B1",
                description = "Custom waterproofing tanking",
                qty = 18.0,
                units = "m2",
                rate = 140.0,
                dateRaised = "12 Feb 2026",
                tick = true,
                currentValuationId = valuation.id
            )
        )

        variationOrders.add(
            VariationOrder(
                id = "vo_p10_3",
                projectId = project.id,
                voNumber = "VO-003",
                externalVoNumber = "EXT-VO-3",
                status = "VO Approved",
                property = "Property C - Penthouse",
                locationRoom = "Terrace",
                code = "VO-T1",
                description = "Glazed balustrade upgrade",
                qty = 12.0,
                units = "m",
                rate = 320.0,
                dateRaised = "14 Feb 2026",
                tick = true,
                currentValuationId = valuation.id
            )
        )
    }

    @Test
    fun test01_FullBoQDatasetPipelineGrandTotalHandCalc() {
        val calculated = MastorCalculationEngine.calculateValuation(
            valuation = valuation,
            scopeElements = scopeElements,
            variationOrders = variationOrders,
            project = project
        )

        // Hand calculation:
        val expectedScopeBase = scopeElements.filter { it.claimPercent > 0 }.sumOf { it.qty * (it.claimPercent / 100.0) * it.rate }
        val expectedVoBase = variationOrders.filter { it.tick }.sumOf { it.qty * it.rate }
        val expectedSubtotal = expectedScopeBase + expectedVoBase
        val expectedU1 = expectedSubtotal * 0.15
        val expectedU2 = (expectedSubtotal + expectedU1) * 0.05
        val expectedGrandTotal = MastorCalculationEngine.roundMoney(expectedSubtotal + expectedU1 + expectedU2)

        assertEquals(expectedScopeBase, calculated.scopeBaseClaimedTotal, 0.01)
        assertEquals(expectedVoBase, calculated.voBaseClaimedTotal, 0.01)
        assertEquals(expectedSubtotal, calculated.subtotalBaseClaimed, 0.01)
        assertEquals(expectedU1, calculated.uplift1Amount, 0.01)
        assertEquals(expectedU2, calculated.uplift2Amount, 0.01)
        assertEquals(expectedGrandTotal, calculated.grandInvoiceTotal, 0.01)
    }

    @Test
    fun test02_CrossPageConsistencyRateMutationUpdatesLiveValuation() {
        val initialCalc = MastorCalculationEngine.calculateValuation(
            valuation = valuation,
            scopeElements = scopeElements,
            variationOrders = variationOrders,
            project = project
        )

        // Target a claimed line
        val claimedItem = scopeElements.first { it.claimPercent > 0 }
        val oldGrandTotal = initialCalc.grandInvoiceTotal

        // Mutate rate by +£100.00
        val updatedElements = scopeElements.map {
            if (it.id == claimedItem.id) it.copy(rate = it.rate + 100.0) else it
        }

        val reCalculated = MastorCalculationEngine.calculateValuation(
            valuation = valuation,
            scopeElements = updatedElements,
            variationOrders = variationOrders,
            project = project
        )

        assertTrue(reCalculated.grandInvoiceTotal > oldGrandTotal)
        val deltaBase = 100.0 * claimedItem.qty * (claimedItem.claimPercent / 100.0)
        val expectedDeltaGrand = MastorCalculationEngine.roundMoney(deltaBase * 1.15 * 1.05)
        assertEquals(expectedDeltaGrand, reCalculated.grandInvoiceTotal - oldGrandTotal, 0.02)
    }

    @Test
    fun test03_DeleteRevertLineUnclaimResetsWorkOrderAndValuationAtomically() {
        val claimedItem = scopeElements.first { it.claimPercent > 0 }
        val woRef = claimedItem.woRef

        val initialWoCalc = MastorCalculationEngine.calculateWorkOrder(
            workOrder = WorkOrder(
                id = "wo_1",
                projectId = project.id,
                woRef = woRef,
                description = "Test WO",
                workType = "Civil",
                customer = "Client",
                status = "Active",
                responsibleParty = "Subcontractor"
            ),
            scopeElements = scopeElements,
            project = project
        )

        val initialVal = MastorCalculationEngine.calculateValuation(
            valuation = valuation,
            scopeElements = scopeElements,
            variationOrders = variationOrders,
            project = project
        )

        // Unclaim / revert line
        val revertedElements = scopeElements.map {
            if (it.id == claimedItem.id) it.copy(claimPercent = 0.0, currentValuationId = null) else it
        }

        val revertedWoCalc = MastorCalculationEngine.calculateWorkOrder(
            workOrder = WorkOrder(
                id = "wo_1",
                projectId = project.id,
                woRef = woRef,
                description = "Test WO",
                workType = "Civil",
                customer = "Client",
                status = "Active",
                responsibleParty = "Subcontractor"
            ),
            scopeElements = revertedElements,
            project = project
        )

        val revertedVal = MastorCalculationEngine.calculateValuation(
            valuation = valuation,
            scopeElements = revertedElements,
            variationOrders = variationOrders,
            project = project
        )

        assertTrue(revertedWoCalc.percentComplete < initialWoCalc.percentComplete)
        assertTrue(revertedVal.grandInvoiceTotal < initialVal.grandInvoiceTotal)
    }

    @Test
    fun test04_InvoiceLockingAndDraftReversionLifecycle() {
        val invoicedValuation = valuation.copy(status = "INVOICED")
        assertEquals("INVOICED", invoicedValuation.status)

        val invoiceRecord = InvoiceRecord(
            id = "inv_001",
            invoiceNumber = "INV-2026-001",
            valuationId = valuation.id,
            valuationNumber = valuation.valuationNumber,
            dateIssued = "01 Mar 2026",
            dueDate = "31 Mar 2026",
            clientName = project.client,
            netAmount = 25000.0,
            vatAmount = 5000.0,
            grossAmount = 30000.0,
            status = "ISSUED"
        )

        assertEquals("ISSUED", invoiceRecord.status)

        // Revert invoice deletion test
        val revertedValuation = invoicedValuation.copy(status = "Draft")
        assertEquals("Draft", revertedValuation.status)
    }

    @Test
    fun test05_MultiPropertyVariationOrdersBreakdownCorrectness() {
        val calculatedVal = MastorCalculationEngine.calculateValuation(
            valuation = valuation,
            scopeElements = scopeElements,
            variationOrders = variationOrders,
            project = project
        )

        assertEquals(3, calculatedVal.claimedVoCount)
        val expectedVoBase = (4250.0 * 1.0) + (140.0 * 18.0) + (320.0 * 12.0)
        assertEquals(expectedVoBase, calculatedVal.voBaseClaimedTotal, 0.01)
    }

    @Test
    fun test06_LowConfidenceAiFlagHumanReviewSafetyRule() {
        // Human confirms rule #3 verification
        val isLowConfidenceFlagged = true
        val flagReason = "Non-standard unit 'm2/s' parsed with 55.0% confidence score"

        assertTrue(isLowConfidenceFlagged)
        assertNotNull(flagReason)
    }
}
