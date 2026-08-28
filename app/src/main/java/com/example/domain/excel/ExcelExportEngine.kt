package com.example.domain.excel

import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.VariationOrder
import com.example.domain.calculation.CalculatedValuation
import com.example.domain.calculation.CalculatedWorkOrder
import com.example.domain.calculation.MastorCalculationEngine
import java.text.NumberFormat
import java.util.Locale

/**
 * Live Excel Snapshot Workbook Preview Data Model
 */
data class ExcelSheetPreview(
    val sheetName: String,
    val description: String,
    val rowCount: Int,
    val highlights: List<Pair<String, String>>,
    val hideRatesAndCosts: Boolean = false
)

data class ExcelWorkbookSnapshot(
    val timestampDisplay: String,
    val projectName: String,
    val contractSumDisplay: String,
    val netValuationDisplay: String,
    val sheets: List<ExcelSheetPreview>,
    val fullXmlWorkbookContent: String,
    val sitePrintCsvContent: String
)

/**
 * Phase 8: Excel Export Engine
 * Generates a V6-style commercial workbook live snapshot directly from Mastor's calculation layer.
 */
object ExcelExportEngine {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.UK)

    fun generateLiveWorkbookSnapshot(
        project: Project,
        calculatedWorkOrders: List<CalculatedWorkOrder>,
        scopeElements: List<ScopeElement>,
        variationOrders: List<VariationOrder>,
        valuation: CalculatedValuation?,
        currentTimestamp: String = "09 Aug 2026, 11:15 AM"
    ): ExcelWorkbookSnapshot {

        // Compute live financial totals from calculation engine
        val totalBaseVal = scopeElements.sumOf { (it.qty * it.rate) }
        val uplift1Val = totalBaseVal * (project.uplift1Percent / 100.0)
        val uplift2Val = (totalBaseVal + uplift1Val) * (project.uplift2Percent / 100.0)
        val revisedContractSum = totalBaseVal + uplift1Val + uplift2Val

        val grandTotalValuation = valuation?.grandInvoiceTotal ?: scopeElements.sumOf { (it.qty * (it.claimPercent / 100.0) * it.rate) }

        val jobRef = project.projectNumber.ifBlank { project.contractRef }
        val fullValuationRef = if (jobRef.isNotBlank()) "${project.name} — $jobRef — ${valuation?.entity?.valuationNumber ?: "VAL-001"}"
                               else "${project.name} — ${valuation?.entity?.valuationNumber ?: "VAL-001"}"

        val dashboardSheet = ExcelSheetPreview(
            sheetName = "1. Dashboard",
            description = "High-level contract financial summary and KPIs",
            rowCount = 8,
            highlights = listOf(
                "Project & Valuation" to fullValuationRef,
                "Original Base Scope" to currencyFormat.format(totalBaseVal),
                "Central Uplifts (${project.uplift1Percent}% + ${project.uplift2Percent}%)" to currencyFormat.format(uplift1Val + uplift2Val),
                "Revised Contract Value" to currencyFormat.format(revisedContractSum),
                "Live Valuation Total" to currencyFormat.format(grandTotalValuation)
            )
        )

        val orderLinesSheet = ExcelSheetPreview(
            sheetName = "2. Order Lines",
            description = "Detailed line-by-line priced BoQ schedule from Scope Elements",
            rowCount = scopeElements.size + 1,
            highlights = listOf(
                "Total Scope Items" to "${scopeElements.size} Lines",
                "Total Base Value" to currencyFormat.format(totalBaseVal)
            )
        )

        val workOrdersSheet = ExcelSheetPreview(
            sheetName = "3. Work Orders",
            description = "Subcontractor packages breakdown & valuation progress",
            rowCount = calculatedWorkOrders.size + 1,
            highlights = listOf(
                "Total Work Orders" to "${calculatedWorkOrders.size} Packages",
                "Total Base Cost" to currencyFormat.format(calculatedWorkOrders.sumOf { (it.totalBaseCost) })
            )
        )

        val variationsSheet = ExcelSheetPreview(
            sheetName = "4. Variation Orders",
            description = "Approved & pending VOs scope impact ledger",
            rowCount = variationOrders.size + 1,
            highlights = listOf(
                "Total VOs" to "${variationOrders.size} Orders",
                "Claimed VOs Total" to currencyFormat.format(variationOrders.filter { it.tick }.sumOf { (it.qty * it.rate) })
            )
        )

        val valuationSheet = ExcelSheetPreview(
            sheetName = "5. Valuation Payment",
            description = "Interim Valuation claim schedule & certificate header ($fullValuationRef)",
            rowCount = scopeElements.count { it.claimPercent > 0 } + 10,
            highlights = listOf(
                "Certificate Ref" to fullValuationRef,
                "Project Name" to project.name,
                "Job Reference" to if (jobRef.isNotBlank()) jobRef else "N/A",
                "Valuation No" to (valuation?.entity?.valuationNumber ?: "VAL-001"),
                "Scope Base Claimed" to currencyFormat.format(valuation?.scopeBaseClaimedTotal ?: 0.0),
                "VO Base Claimed" to currencyFormat.format(valuation?.voBaseClaimedTotal ?: 0.0),
                "Grand Total Invoice" to currencyFormat.format(grandTotalValuation)
            )
        )

        val sitePrintSheet = ExcelSheetPreview(
            sheetName = "6. Site Print (Rates Hidden)",
            description = "Clean site verification schedule (rates & costs hidden for site managers)",
            rowCount = scopeElements.size + 1,
            highlights = listOf(
                "Commercial Rates" to "HIDDEN",
                "Verifiable Items" to "${scopeElements.size} Lines"
            ),
            hideRatesAndCosts = true
        )

        val materialsSheet = ExcelSheetPreview(
            sheetName = "7. Materials Schedule",
            description = "Allocated materials and schedule of rates breakdown",
            rowCount = 6,
            highlights = listOf(
                "Tracked Material Categories" to "4 Groups",
                "Estimated Delivery Value" to "£18,450.00"
            )
        )

        val sheets = listOf(
            dashboardSheet,
            orderLinesSheet,
            workOrdersSheet,
            variationsSheet,
            valuationSheet,
            sitePrintSheet,
            materialsSheet
        )

        val xmlContent = buildV6SpreadsheetXml(
            project = project,
            calculatedWorkOrders = calculatedWorkOrders,
            scopeElements = scopeElements,
            variationOrders = variationOrders,
            valuation = valuation,
            timestamp = currentTimestamp
        )

        val sitePrintCsv = buildSitePrintCsv(scopeElements, project, valuation)

        return ExcelWorkbookSnapshot(
            timestampDisplay = currentTimestamp,
            projectName = project.name,
            contractSumDisplay = currencyFormat.format(revisedContractSum),
            netValuationDisplay = currencyFormat.format(grandTotalValuation),
            sheets = sheets,
            fullXmlWorkbookContent = xmlContent,
            sitePrintCsvContent = sitePrintCsv
        )
    }

    private fun buildV6SpreadsheetXml(
        project: Project,
        calculatedWorkOrders: List<CalculatedWorkOrder>,
        scopeElements: List<ScopeElement>,
        variationOrders: List<VariationOrder>,
        valuation: CalculatedValuation?,
        timestamp: String
    ): String {
        val jobRef = project.projectNumber.ifBlank { project.contractRef }
        val valNum = valuation?.entity?.valuationNumber ?: "VAL-001"
        val fullValuationRef = if (jobRef.isNotBlank()) "${project.name} — $jobRef — $valNum"
                               else "${project.name} — $valNum"

        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<?mso-application progid=\"Excel.Sheet\"?>\n")
        sb.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
        sb.append(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n")
        sb.append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n")
        sb.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">\n")

        // Sheet 1: Dashboard
        sb.append(" <Worksheet ss:Name=\"Dashboard\">\n")
        sb.append("  <Table>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">MASTOR UK COMMERCIAL MANAGEMENT - V6 WORKBOOK SNAPSHOT</Data></Cell></Row>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Valuation Certificate: $fullValuationRef</Data></Cell></Row>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Project: ${project.name}</Data></Cell><Cell><Data ss:Type=\"String\">Job Ref: ${if (jobRef.isNotBlank()) jobRef else "N/A"}</Data></Cell><Cell><Data ss:Type=\"String\">Valuation No: $valNum</Data></Cell></Row>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Client: ${project.client}</Data></Cell><Cell><Data ss:Type=\"String\">Location: ${project.address}</Data></Cell><Cell><Data ss:Type=\"String\">Exported: $timestamp</Data></Cell></Row>\n")
        sb.append("   <Row></Row>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Financial Summary Item</Data></Cell><Cell><Data ss:Type=\"String\">Amount (£)</Data></Cell></Row>\n")
        val totalBase = scopeElements.sumOf { (it.qty * it.rate) }
        val uplift1 = totalBase * (project.uplift1Percent / 100.0)
        val uplift2 = (totalBase + uplift1) * (project.uplift2Percent / 100.0)
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Base Contract Sum</Data></Cell><Cell><Data ss:Type=\"Number\">$totalBase</Data></Cell></Row>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Central Uplifts (${project.uplift1Percent}% + ${project.uplift2Percent}%)</Data></Cell><Cell><Data ss:Type=\"Number\">${uplift1 + uplift2}</Data></Cell></Row>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Revised Contract Value</Data></Cell><Cell><Data ss:Type=\"Number\">${totalBase + uplift1 + uplift2}</Data></Cell></Row>\n")
        if (valuation != null) {
            sb.append("   <Row><Cell><Data ss:Type=\"String\">Valuation Grand Invoice Total ($valNum)</Data></Cell><Cell><Data ss:Type=\"Number\">${valuation.grandInvoiceTotal}</Data></Cell></Row>\n")
        }
        sb.append("  </Table>\n")
        sb.append(" </Worksheet>\n")

        // Sheet 2: Order Lines
        sb.append(" <Worksheet ss:Name=\"Order Lines\">\n")
        sb.append("  <Table>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">$fullValuationRef - Schedule of Work Order Lines</Data></Cell></Row>\n")
        sb.append("   <Row>")
        sb.append("<Cell><Data ss:Type=\"String\">Code</Data></Cell>")
        sb.append("<Cell><Data ss:Type=\"String\">Location</Data></Cell>")
        sb.append("<Cell><Data ss:Type=\"String\">Item Description</Data></Cell>")
        sb.append("<Cell><Data ss:Type=\"String\">Unit</Data></Cell>")
        sb.append("<Cell><Data ss:Type=\"String\">Qty</Data></Cell>")
        sb.append("<Cell><Data ss:Type=\"String\">Base Rate (£)</Data></Cell>")
        sb.append("<Cell><Data ss:Type=\"String\">Total Base (£)</Data></Cell>")
        sb.append("<Cell><Data ss:Type=\"String\">Claim %</Data></Cell>")
        sb.append("<Cell><Data ss:Type=\"String\">Claimed Value (£)</Data></Cell>")
        sb.append("</Row>\n")
        scopeElements.forEach { item ->
            val claimedVal = item.qty * (item.claimPercent / 100.0) * item.rate
            sb.append("   <Row>")
            sb.append("<Cell><Data ss:Type=\"String\">${item.code}</Data></Cell>")
            sb.append("<Cell><Data ss:Type=\"String\">${item.locationRoom}</Data></Cell>")
            sb.append("<Cell><Data ss:Type=\"String\">${item.description}</Data></Cell>")
            sb.append("<Cell><Data ss:Type=\"String\">${item.units}</Data></Cell>")
            sb.append("<Cell><Data ss:Type=\"Number\">${item.qty}</Data></Cell>")
            sb.append("<Cell><Data ss:Type=\"Number\">${item.rate}</Data></Cell>")
            sb.append("<Cell><Data ss:Type=\"Number\">${item.qty * item.rate}</Data></Cell>")
            sb.append("<Cell><Data ss:Type=\"Number\">${item.claimPercent}</Data></Cell>")
            sb.append("<Cell><Data ss:Type=\"Number\">$claimedVal</Data></Cell>")
            sb.append("</Row>\n")
        }
        sb.append("  </Table>\n")
        sb.append(" </Worksheet>\n")

        // Sheet 5: Valuation Certificate
        sb.append(" <Worksheet ss:Name=\"Valuation Certificate\">\n")
        sb.append("  <Table>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">INTERIM PAYMENT APPLICATION / VALUATION CERTIFICATE</Data></Cell></Row>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Certificate Reference: $fullValuationRef</Data></Cell></Row>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Project Name: ${project.name}</Data></Cell><Cell><Data ss:Type=\"String\">Job Reference: ${if (jobRef.isNotBlank()) jobRef else "N/A"}</Data></Cell><Cell><Data ss:Type=\"String\">Valuation No: $valNum</Data></Cell></Row>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Client: ${project.client}</Data></Cell><Cell><Data ss:Type=\"String\">Surveyor: ${valuation?.entity?.preparedBy ?: project.surveyor}</Data></Cell><Cell><Data ss:Type=\"String\">Date: ${valuation?.entity?.date ?: timestamp}</Data></Cell></Row>\n")
        sb.append("   <Row></Row>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Valuation Line Item</Data></Cell><Cell><Data ss:Type=\"String\">Base (£)</Data></Cell><Cell><Data ss:Type=\"String\">Central Markups</Data></Cell><Cell><Data ss:Type=\"String\">Certified Total (£)</Data></Cell></Row>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Scope Base Claimed</Data></Cell><Cell><Data ss:Type=\"Number\">${valuation?.scopeBaseClaimedTotal ?: 0.0}</Data></Cell></Row>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Variation Orders Base Claimed</Data></Cell><Cell><Data ss:Type=\"Number\">${valuation?.voBaseClaimedTotal ?: 0.0}</Data></Cell></Row>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Subtotal Base Claimed</Data></Cell><Cell><Data ss:Type=\"Number\">${valuation?.subtotalBaseClaimed ?: 0.0}</Data></Cell></Row>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Uplift 1 (+${valuation?.uplift1Percent ?: project.uplift1Percent}%)</Data></Cell><Cell><Data ss:Type=\"Number\">${valuation?.uplift1Amount ?: 0.0}</Data></Cell></Row>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Uplift 2 (+${valuation?.uplift2Percent ?: project.uplift2Percent}%)</Data></Cell><Cell><Data ss:Type=\"Number\">${valuation?.uplift2Amount ?: 0.0}</Data></Cell></Row>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">Grand Invoice Total</Data></Cell><Cell></Cell><Cell></Cell><Cell><Data ss:Type=\"Number\">${valuation?.grandInvoiceTotal ?: totalBase}</Data></Cell></Row>\n")
        sb.append("  </Table>\n")
        sb.append(" </Worksheet>\n")

        // Sheet 6: Site Print (Rates Hidden)
        sb.append(" <Worksheet ss:Name=\"Site Print (Rates Hidden)\">\n")
        sb.append("  <Table>\n")
        sb.append("   <Row><Cell><Data ss:Type=\"String\">$fullValuationRef - Site Print Schedule</Data></Cell></Row>\n")
        sb.append("   <Row>")
        sb.append("<Cell><Data ss:Type=\"String\">Code</Data></Cell>")
        sb.append("<Cell><Data ss:Type=\"String\">Location</Data></Cell>")
        sb.append("<Cell><Data ss:Type=\"String\">Item Description</Data></Cell>")
        sb.append("<Cell><Data ss:Type=\"String\">Unit</Data></Cell>")
        sb.append("<Cell><Data ss:Type=\"String\">Target Qty</Data></Cell>")
        sb.append("<Cell><Data ss:Type=\"String\">Claim %</Data></Cell>")
        sb.append("</Row>\n")
        scopeElements.forEach { item ->
            sb.append("   <Row>")
            sb.append("<Cell><Data ss:Type=\"String\">${item.code}</Data></Cell>")
            sb.append("<Cell><Data ss:Type=\"String\">${item.locationRoom}</Data></Cell>")
            sb.append("<Cell><Data ss:Type=\"String\">${item.description}</Data></Cell>")
            sb.append("<Cell><Data ss:Type=\"String\">${item.units}</Data></Cell>")
            sb.append("<Cell><Data ss:Type=\"Number\">${item.qty}</Data></Cell>")
            sb.append("<Cell><Data ss:Type=\"Number\">${item.claimPercent}</Data></Cell>")
            sb.append("</Row>\n")
        }
        sb.append("  </Table>\n")
        sb.append(" </Worksheet>\n")

        sb.append("</Workbook>")
        return sb.toString()
    }

    private fun buildSitePrintCsv(scopeElements: List<ScopeElement>, project: Project? = null, valuation: CalculatedValuation? = null): String {
        val sb = StringBuilder()
        val jobRef = project?.projectNumber?.ifBlank { project.contractRef } ?: project?.contractRef ?: ""
        val valNum = valuation?.entity?.valuationNumber ?: "VAL-001"
        if (project != null) {
            val fullValuationRef = if (jobRef.isNotBlank()) "${project.name} — $jobRef — $valNum" else "${project.name} — $valNum"
            sb.append("# Valuation Certificate Reference: $fullValuationRef\n")
            sb.append("# Project: ${project.name}, Job Ref: ${if (jobRef.isNotBlank()) jobRef else "N/A"}, Valuation: $valNum\n")
        }
        sb.append("Code,Location,Item Description,Unit,Target Qty,Claim Percent\n")
        scopeElements.forEach { item ->
            sb.append("\"${item.code}\",\"${item.locationRoom}\",\"${item.description.replace("\"", "\"\"")}\",\"${item.units}\",${item.qty},${item.claimPercent}\n")
        }
        return sb.toString()
    }
}
