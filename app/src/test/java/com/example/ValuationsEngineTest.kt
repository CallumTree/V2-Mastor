package com.example

import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.Valuation
import com.example.data.entity.VariationOrder
import com.example.domain.calculation.MastorCalculationEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class ValuationsEngineTest {

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
    fun testGrandInvoiceTotalExactHandCalculation() {
        val scopes = listOf(
            ScopeElement(id = "se_1", woRef = "WO-001", locationRoom = "Kitchen", code = "KIT-101", description = "Strip out", qty = 1.0, units = "item", rate = 2500.0, claimPercent = 100.0, currentValuationId = "val_001"),
            ScopeElement(id = "se_2", woRef = "WO-001", locationRoom = "Kitchen", code = "KIT-102", description = "Cabinetry", qty = 12.0, units = "units", rate = 1850.0, claimPercent = 50.0, currentValuationId = "val_001"),
            ScopeElement(id = "se_3", woRef = "WO-001", locationRoom = "Kitchen", code = "KIT-103", description = "Worktops", qty = 18.5, units = "m2", rate = 420.0, claimPercent = 20.0, currentValuationId = "val_001"),
            ScopeElement(id = "se_4", woRef = "WO-001", locationRoom = "Dining", code = "DIN-101", description = "Flooring", qty = 32.0, units = "m2", rate = 195.0, claimPercent = 0.0, currentValuationId = "val_001"),
            ScopeElement(id = "se_5", woRef = "WO-002", locationRoom = "Bathroom", code = "BTH-201", description = "Sanitaryware", qty = 24.0, units = "item", rate = 85.0, claimPercent = 100.0, currentValuationId = "val_001"),
            ScopeElement(id = "se_6", woRef = "WO-002", locationRoom = "Bathroom", code = "BTH-202", description = "Tiling", qty = 48.0, units = "m2", rate = 140.0, claimPercent = 40.0, currentValuationId = "val_001")
        )

        val vos = listOf(
            VariationOrder(id = "vo_1", projectId = "proj_101", voNumber = "VO-001", externalVoNumber = "EXT-VO-1", status = "VO Approved", property = "Flat 1", locationRoom = "Kitchen", code = "VO-K1", description = "Additional lintel", qty = 1.0, units = "nr", rate = 3450.0, dateRaised = "15 Feb 2026", tick = true, currentValuationId = "val_001"),
            VariationOrder(id = "vo_2", projectId = "proj_101", voNumber = "VO-002", externalVoNumber = "EXT-VO-2", status = "VO Pending", property = "Flat 1", locationRoom = "Kitchen", code = "VO-K2", description = "Extra power points", qty = 2.0, units = "nr", rate = 820.0, dateRaised = "16 Feb 2026", tick = false, currentValuationId = "val_001")
        )

        val calculated = MastorCalculationEngine.calculateValuation(
            valuation = testValuation,
            scopeElements = scopes,
            variationOrders = vos,
            project = testProject
        )

        // Hand Calculations:
        // Section 1 Scope Base Claimed: 2500.0 + 11100.0 + 1554.0 + 0 + 2040.0 + 2688.0 = £19,882.00
        assertEquals(19882.00, calculated.scopeBaseClaimedTotal, 0.01)

        // Section 2 VO Base Claimed: 3450.00 = £3,450.00
        assertEquals(3450.00, calculated.voBaseClaimedTotal, 0.01)

        // Subtotal Base Claimed: 19882.00 + 3450.00 = £23,332.00
        assertEquals(23332.00, calculated.subtotalBaseClaimed, 0.01)

        // Uplift 1 (+15%): 23332.00 * 0.15 = £3,499.80
        assertEquals(3499.80, calculated.uplift1Amount, 0.01)

        // Subtotal + Uplift 1: 23332.00 + 3499.80 = £26,831.80
        // Uplift 2 (+5%): 26831.80 * 0.05 = £1,341.59
        assertEquals(1341.59, calculated.uplift2Amount, 0.01)

        // Grand Invoice Total: 26831.80 + 1341.59 = £28,173.39
        assertEquals(28173.39, calculated.grandInvoiceTotal, 0.01)
    }

    @Test
    fun testRevertingScopeLineResetsGrandTotalLive() {
        val initialScopes = listOf(
            ScopeElement(id = "se_1", woRef = "WO-001", locationRoom = "Kitchen", code = "KIT-101", description = "Strip out", qty = 1.0, units = "item", rate = 2500.0, claimPercent = 100.0, currentValuationId = "val_001")
        )

        val initialCalc = MastorCalculationEngine.calculateValuation(
            valuation = testValuation,
            scopeElements = initialScopes,
            variationOrders = emptyList(),
            project = testProject
        )

        // Base = 2500. U1 (+15%) = 375. Sub = 2875. U2 (+5%) = 143.75. Grand = £3,018.75
        assertEquals(3018.75, initialCalc.grandInvoiceTotal, 0.01)

        // Revert scope line to unclaimed (claimPercent = 0.0)
        val revertedScopes = initialScopes.map { it.copy(claimPercent = 0.0) }
        val revertedCalc = MastorCalculationEngine.calculateValuation(
            valuation = testValuation,
            scopeElements = revertedScopes,
            variationOrders = emptyList(),
            project = testProject
        )

        assertEquals(0.00, revertedCalc.scopeBaseClaimedTotal, 0.01)
        assertEquals(0.00, revertedCalc.grandInvoiceTotal, 0.01)
    }
}
