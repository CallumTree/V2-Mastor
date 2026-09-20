package com.example.domain.invoice

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.Valuation
import com.example.data.entity.VariationOrder
import com.example.domain.calculation.MastorCalculationEngine
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Generates a professional Interim Valuation Certificate PDF from a finalised valuation
 * using Android's native PdfDocument API.
 */
object InvoiceGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_LEFT = 36f
    private const val MARGIN_RIGHT = 559f
    private const val MARGIN_TOP = 40f
    private const val MARGIN_BOTTOM = 802f
    private const val ROW_HEIGHT = 18f
    private const val SECTION_HEADER_HEIGHT = 20f

    // Color Palette
    private val COLOR_SLATE_DARK = Color.rgb(15, 23, 42)
    private val COLOR_SLATE_TEXT = Color.rgb(51, 65, 85)
    private val COLOR_SLATE_MUTED = Color.rgb(100, 116, 139)
    private val COLOR_SLATE_BORDER = Color.rgb(203, 213, 225)
    private val COLOR_TABLE_HEADER_BG = Color.rgb(241, 245, 249)
    private val COLOR_SCOPE_SECTION_BG = Color.rgb(226, 232, 240)
    private val COLOR_VO_SECTION_BG = Color.rgb(254, 243, 199) // Amber tint
    private val COLOR_VO_SECTION_TEXT = Color.rgb(180, 83, 9)
    private val COLOR_ROW_ALT = Color.rgb(248, 250, 252)
    private val COLOR_GOLD_BOX_BG = Color.rgb(254, 249, 195) // Gold tint
    private val COLOR_GOLD_BORDER = Color.rgb(217, 119, 6)
    private val COLOR_GOLD_TEXT = Color.rgb(180, 83, 9)

    private data class ItemLine(
        val ref: String,
        val description: String,
        val location: String,
        val qty: Double,
        val units: String,
        val rate: Double,
        val prevCertPercent: Double,
        val thisClaimPercent: Double,
        val thisClaimValue: Double,
        val prevCertValue: Double,
        val cumulativeValue: Double
    )

    fun generateInvoicePdf(
        context: Context,
        project: Project,
        valuation: Valuation,
        scopeItems: List<ScopeElement>,
        variationItems: List<VariationOrder>,
        upliftPercent: Double
    ): File {
        val pdfDocument = PdfDocument()

        // 1. Process Scope Element items
        val scopeLines = scopeItems.map { item ->
            val baseCost = MastorCalculationEngine.roundMoney(item.qty * item.rate)
            val thisClaimPct = (item.claimPercent - item.previouslyCertifiedPercent).coerceAtLeast(0.0)
            val thisClaimVal = MastorCalculationEngine.roundMoney(baseCost * (thisClaimPct / 100.0))
            val prevVal = MastorCalculationEngine.roundMoney(baseCost * (item.previouslyCertifiedPercent / 100.0))
            val cumVal = MastorCalculationEngine.roundMoney(baseCost * (item.claimPercent / 100.0))
            ItemLine(
                ref = item.code.ifBlank { item.woRef },
                description = item.description,
                location = item.locationRoom,
                qty = item.qty,
                units = item.units,
                rate = item.rate,
                prevCertPercent = item.previouslyCertifiedPercent,
                thisClaimPercent = thisClaimPct,
                thisClaimValue = thisClaimVal,
                prevCertValue = prevVal,
                cumulativeValue = cumVal
            )
        }

        // 2. Process Variation Order items
        val voLines = variationItems.map { vo ->
            val baseCost = MastorCalculationEngine.roundMoney(vo.qty * vo.rate)
            val effectiveClaimPct = if (vo.tick && vo.claimPercent <= 0.0) 100.0 else vo.claimPercent
            val thisClaimPct = (effectiveClaimPct - vo.previouslyCertifiedPercent).coerceAtLeast(0.0)
            val thisClaimVal = MastorCalculationEngine.roundMoney(baseCost * (thisClaimPct / 100.0))
            val prevVal = MastorCalculationEngine.roundMoney(baseCost * (vo.previouslyCertifiedPercent / 100.0))
            val cumVal = MastorCalculationEngine.roundMoney(baseCost * (effectiveClaimPct / 100.0))
            ItemLine(
                ref = vo.voNumber.ifBlank { vo.externalVoNumber },
                description = vo.description,
                location = vo.locationRoom,
                qty = vo.qty,
                units = vo.units,
                rate = vo.rate,
                prevCertPercent = vo.previouslyCertifiedPercent,
                thisClaimPercent = thisClaimPct,
                thisClaimValue = thisClaimVal,
                prevCertValue = prevVal,
                cumulativeValue = cumVal
            )
        }

        // Filter: One row per element where thisClaimValue > 0 (skip items with zero this-period claim)
        val activeScopeLines = scopeLines.filter { it.thisClaimValue > 0.001 }.ifEmpty {
            scopeLines.filter { it.cumulativeValue > 0.001 }
        }
        val activeVoLines = voLines.filter { it.thisClaimValue > 0.001 }.ifEmpty {
            voLines.filter { it.cumulativeValue > 0.001 }
        }

        // Cumulative Totals
        val cumulativeScopeSubtotal = MastorCalculationEngine.roundMoney(scopeLines.sumOf { it.cumulativeValue })
        val cumulativeVoSubtotal = MastorCalculationEngine.roundMoney(voLines.sumOf { it.cumulativeValue })
        val cumulativeBaseTotal = MastorCalculationEngine.roundMoney(cumulativeScopeSubtotal + cumulativeVoSubtotal)

        // Previous Certified Totals (Gross)
        val prevCertBase = MastorCalculationEngine.roundMoney(scopeLines.sumOf { it.prevCertValue } + voLines.sumOf { it.prevCertValue })
        val prevCertGross = MastorCalculationEngine.roundMoney(prevCertBase * (1.0 + (upliftPercent / 100.0)))

        // Uplifts and Gross Valuation
        val overheadProfitAmount = MastorCalculationEngine.roundMoney(cumulativeBaseTotal * (upliftPercent / 100.0))
        val grossValuation = MastorCalculationEngine.roundMoney(cumulativeBaseTotal + overheadProfitAmount)

        // This Period Claim and Net Due
        val thisPeriodClaim = MastorCalculationEngine.roundMoney((grossValuation - prevCertGross).coerceAtLeast(0.0))
        val retentionAmount = 0.00 // Default UK standard without specific deduction
        val netPaymentDue = MastorCalculationEngine.roundMoney(thisPeriodClaim - retentionAmount)

        // --- PDF Rendering State ---
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var currentPage = pdfDocument.startPage(pageInfo)
        var canvas = currentPage.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var currentY = MARGIN_TOP

        // Helper: Draw Header on Page 1
        fun drawPage1Header() {
            // "MASTOR" Platform Watermark Top Left
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.textSize = 20f
            paint.color = COLOR_SLATE_DARK
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("MASTOR", MARGIN_LEFT, currentY + 16f, paint)

            // "INTERIM VALUATION CERTIFICATE" Title Centred
            paint.textSize = 13f
            paint.color = COLOR_SLATE_DARK
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("INTERIM VALUATION CERTIFICATE", PAGE_WIDTH / 2f, currentY + 14f, paint)

            // Valuation number & Date Right Aligned
            paint.textAlign = Paint.Align.RIGHT
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.color = COLOR_GOLD_TEXT
            val valNoDisplay = if (valuation.valuationNumber.startsWith("IV-", ignoreCase = true)) {
                valuation.valuationNumber
            } else {
                "IV-${valuation.valuationNumber}"
            }
            canvas.drawText(valNoDisplay, MARGIN_RIGHT, currentY + 10f, paint)

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textSize = 8.5f
            paint.color = COLOR_SLATE_MUTED
            canvas.drawText("Date: ${valuation.date.ifBlank { "Current" }}", MARGIN_RIGHT, currentY + 22f, paint)

            currentY += 34f

            // Horizontal Divider
            paint.color = COLOR_SLATE_BORDER
            paint.strokeWidth = 1f
            canvas.drawLine(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY, paint)
            currentY += 12f

            // Two Columns: FROM and TO blocks
            val colLeftX = MARGIN_LEFT
            val colRightX = 310f

            // Left: FROM Block
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.color = COLOR_SLATE_MUTED
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("FROM (CONTRACTOR)", colLeftX, currentY + 8f, paint)

            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.color = COLOR_SLATE_DARK
            canvas.drawText(project.name.ifBlank { "Mastor Contracting Ltd" }, colLeftX, currentY + 22f, paint)

            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.color = COLOR_SLATE_TEXT
            val siteMgrStr = if (project.siteManager.isNotBlank()) "Site Manager: ${project.siteManager}" else ""
            val surveyorStr = if (project.surveyor.isNotBlank()) "Surveyor: ${project.surveyor}" else ""
            val mgrLine = listOf(siteMgrStr, surveyorStr).filter { it.isNotBlank() }.joinToString(" | ")
            if (mgrLine.isNotBlank()) {
                canvas.drawText(mgrLine, colLeftX, currentY + 34f, paint)
            }

            // Right: TO Block
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.color = COLOR_SLATE_MUTED
            canvas.drawText("TO (CLIENT)", colRightX, currentY + 8f, paint)

            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.color = COLOR_SLATE_DARK
            canvas.drawText(project.client.ifBlank { "Client Representative" }, colRightX, currentY + 22f, paint)

            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.color = COLOR_SLATE_TEXT
            val poDisplay = if (project.projectNumber.isNotBlank()) "PO / Job Ref: ${project.projectNumber}" else "Contract Ref: ${project.contractRef}"
            canvas.drawText(poDisplay, colRightX, currentY + 34f, paint)

            currentY += 46f

            // Contract reference, project address, and contract value on separate lines below
            paint.textSize = 8f
            paint.color = COLOR_SLATE_TEXT
            canvas.drawText("Contract Reference: ${project.contractRef.ifBlank { "Standard JCT/NEC" }}", MARGIN_LEFT, currentY + 8f, paint)
            currentY += 12f
            canvas.drawText("Project Address: ${project.address.ifBlank { "Site Location As Contract" }}", MARGIN_LEFT, currentY + 8f, paint)
            currentY += 12f
            canvas.drawText("Original Contract Value: ${MastorCalculationEngine.formatCurrency(project.contractValue)}   |   Work Type: ${project.workType}", MARGIN_LEFT, currentY + 8f, paint)
            currentY += 16f

            // Horizontal Divider before table
            paint.color = COLOR_SLATE_BORDER
            paint.strokeWidth = 1f
            canvas.drawLine(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY, paint)
            currentY += 8f
        }

        // Helper: Draw Table Header
        fun drawTableHeader() {
            val bgRect = RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + SECTION_HEADER_HEIGHT)
            paint.color = COLOR_TABLE_HEADER_BG
            paint.style = Paint.Style.FILL
            canvas.drawRect(bgRect, paint)

            paint.color = COLOR_SLATE_BORDER
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f
            canvas.drawRect(bgRect, paint)

            paint.style = Paint.Style.FILL
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.textSize = 7.5f
            paint.color = COLOR_SLATE_TEXT

            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("REF", 40f, currentY + 13f, paint)
            canvas.drawText("DESCRIPTION", 86f, currentY + 13f, paint)
            canvas.drawText("LOCATION", 230f, currentY + 13f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("QTY", 320f, currentY + 13f, paint)

            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("UNIT", 336f, currentY + 13f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("RATE", 400f, currentY + 13f, paint)
            canvas.drawText("PREV CERT", 450f, currentY + 13f, paint)
            canvas.drawText("THIS PERIOD", 500f, currentY + 13f, paint)
            canvas.drawText("VALUE", 555f, currentY + 13f, paint)

            currentY += SECTION_HEADER_HEIGHT
        }

        // Helper: Draw Page Continuation Header (for Page 2+)
        fun drawContinuationHeader() {
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.textSize = 10f
            paint.color = COLOR_SLATE_DARK
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("MASTOR — INTERIM VALUATION CERTIFICATE (${valuation.valuationNumber})", MARGIN_LEFT, currentY + 12f, paint)

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textSize = 8.5f
            paint.color = COLOR_SLATE_MUTED
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Page $pageNumber", MARGIN_RIGHT, currentY + 12f, paint)

            currentY += 18f
            paint.color = COLOR_SLATE_BORDER
            paint.strokeWidth = 1f
            canvas.drawLine(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY, paint)
            currentY += 8f

            drawTableHeader()
        }

        // Draw Footer at bottom of page
        fun drawFooter() {
            paint.color = COLOR_SLATE_BORDER
            paint.strokeWidth = 0.8f
            canvas.drawLine(MARGIN_LEFT, MARGIN_BOTTOM - 16f, MARGIN_RIGHT, MARGIN_BOTTOM - 16f, paint)

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textSize = 7.5f
            paint.color = COLOR_SLATE_MUTED
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("This valuation certificate has been prepared in accordance with the contract conditions", MARGIN_LEFT, MARGIN_BOTTOM - 4f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Page $pageNumber", MARGIN_RIGHT, MARGIN_BOTTOM - 4f, paint)
        }

        // Page break logic
        fun checkAndBreakPage(requiredHeight: Float) {
            if (currentY + requiredHeight > MARGIN_BOTTOM - 24f) {
                drawFooter()
                pdfDocument.finishPage(currentPage)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage.canvas
                currentY = MARGIN_TOP
                drawContinuationHeader()
            }
        }

        // Truncate text with ellipsis if it exceeds maxWidth
        fun ellipsize(text: String, maxWidth: Float, p: Paint): String {
            if (p.measureText(text) <= maxWidth) return text
            var result = text
            while (result.isNotEmpty() && p.measureText("$result…") > maxWidth) {
                result = result.dropLast(1)
            }
            return "$result…"
        }

        // Helper: Draw Section Row
        fun drawSectionRow(title: String, bgColor: Int, textColor: Int) {
            checkAndBreakPage(SECTION_HEADER_HEIGHT)
            val rect = RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + SECTION_HEADER_HEIGHT)
            paint.color = bgColor
            paint.style = Paint.Style.FILL
            canvas.drawRect(rect, paint)

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.textSize = 8f
            paint.color = textColor
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(title, 40f, currentY + 13f, paint)

            currentY += SECTION_HEADER_HEIGHT
        }

        // Helper: Draw Line Item Row
        fun drawItemRow(item: ItemLine, isEven: Boolean) {
            checkAndBreakPage(ROW_HEIGHT)

            if (isEven) {
                val rect = RectF(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + ROW_HEIGHT)
                paint.color = COLOR_ROW_ALT
                paint.style = Paint.Style.FILL
                canvas.drawRect(rect, paint)
            }

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textSize = 7.5f
            paint.color = COLOR_SLATE_TEXT

            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(ellipsize(item.ref, 42f, paint), 40f, currentY + 12f, paint)
            canvas.drawText(ellipsize(item.description, 138f, paint), 86f, currentY + 12f, paint)
            canvas.drawText(ellipsize(item.location, 64f, paint), 230f, currentY + 12f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format(Locale.UK, "%.1f", item.qty), 320f, currentY + 12f, paint)

            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(ellipsize(item.units, 24f, paint), 336f, currentY + 12f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format(Locale.UK, "£%.2f", item.rate), 400f, currentY + 12f, paint)
            canvas.drawText(String.format(Locale.UK, "%.0f%%", item.prevCertPercent), 450f, currentY + 12f, paint)

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.color = COLOR_SLATE_DARK
            canvas.drawText(String.format(Locale.UK, "%.0f%%", item.thisClaimPercent), 500f, currentY + 12f, paint)

            paint.color = COLOR_GOLD_TEXT
            canvas.drawText(MastorCalculationEngine.formatCurrency(item.thisClaimValue), 555f, currentY + 12f, paint)

            // Row bottom hairline divider
            paint.color = COLOR_SLATE_BORDER
            paint.strokeWidth = 0.5f
            canvas.drawLine(MARGIN_LEFT, currentY + ROW_HEIGHT, MARGIN_RIGHT, currentY + ROW_HEIGHT, paint)

            currentY += ROW_HEIGHT
        }

        // --- Render Page 1 Header and Initial Table Headers ---
        drawPage1Header()
        drawTableHeader()

        // --- Section 1: CONTRACT SCOPE ITEMS ---
        drawSectionRow("CONTRACT SCOPE ITEMS", COLOR_SCOPE_SECTION_BG, COLOR_SLATE_DARK)
        if (activeScopeLines.isEmpty()) {
            checkAndBreakPage(ROW_HEIGHT)
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
            paint.textSize = 7.5f
            paint.color = COLOR_SLATE_MUTED
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("No contract scope elements claiming new progress in this period.", 44f, currentY + 12f, paint)
            currentY += ROW_HEIGHT
        } else {
            activeScopeLines.forEachIndexed { idx, line ->
                drawItemRow(line, idx % 2 == 1)
            }
        }

        // --- Section 2: VARIATION ORDERS ---
        drawSectionRow("VARIATION ORDERS", COLOR_VO_SECTION_BG, COLOR_VO_SECTION_TEXT)
        if (activeVoLines.isEmpty()) {
            checkAndBreakPage(ROW_HEIGHT)
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
            paint.textSize = 7.5f
            paint.color = COLOR_SLATE_MUTED
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("No variation orders claiming new progress in this period.", 44f, currentY + 12f, paint)
            currentY += ROW_HEIGHT
        } else {
            activeVoLines.forEachIndexed { idx, line ->
                drawItemRow(line, idx % 2 == 1)
            }
        }

        // --- Final Totals Summary ---
        // Ensure at least 230pt is available for the Totals Summary
        checkAndBreakPage(230f)
        currentY += 12f

        val summaryLeft = 240f
        val summaryRight = MARGIN_RIGHT

        // Divider before totals
        paint.color = COLOR_SLATE_BORDER
        paint.strokeWidth = 1f
        canvas.drawLine(summaryLeft, currentY, summaryRight, currentY, paint)
        currentY += 8f

        fun drawSummaryRow(label: String, value: String, isBold: Boolean = false, isMuted: Boolean = false, textColor: Int = COLOR_SLATE_DARK) {
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, if (isBold) Typeface.BOLD else Typeface.NORMAL)
            paint.color = if (isMuted) COLOR_SLATE_MUTED else COLOR_SLATE_TEXT
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(label, summaryLeft + 4f, currentY + 11f, paint)

            paint.color = if (isMuted) COLOR_SLATE_MUTED else textColor
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(value, summaryRight - 4f, currentY + 11f, paint)

            currentY += 15f
        }

        drawSummaryRow(
            label = "CONTRACT SCOPE SUBTOTAL",
            value = MastorCalculationEngine.formatCurrency(cumulativeScopeSubtotal)
        )
        drawSummaryRow(
            label = "VARIATIONS SUBTOTAL",
            value = MastorCalculationEngine.formatCurrency(cumulativeVoSubtotal)
        )

        // Divider
        paint.color = COLOR_SLATE_BORDER
        paint.strokeWidth = 0.8f
        canvas.drawLine(summaryLeft, currentY + 2f, summaryRight, currentY + 2f, paint)
        currentY += 6f

        drawSummaryRow(
            label = "BASE TOTAL",
            value = MastorCalculationEngine.formatCurrency(cumulativeBaseTotal),
            isBold = true
        )
        drawSummaryRow(
            label = "OVERHEAD & PROFIT (+${String.format(Locale.UK, "%.1f", upliftPercent)}%)",
            value = MastorCalculationEngine.formatCurrency(overheadProfitAmount)
        )
        drawSummaryRow(
            label = "GROSS VALUATION",
            value = MastorCalculationEngine.formatCurrency(grossValuation),
            isBold = true
        )
        drawSummaryRow(
            label = "LESS PREVIOUSLY CERTIFIED",
            value = MastorCalculationEngine.formatCurrency(prevCertGross),
            isMuted = true
        )

        // Divider
        paint.color = COLOR_SLATE_BORDER
        paint.strokeWidth = 0.8f
        canvas.drawLine(summaryLeft, currentY + 2f, summaryRight, currentY + 2f, paint)
        currentY += 6f

        // "THIS PERIOD CLAIM: £X,XXX" — large, bold, gold tinted box
        val goldBoxRect = RectF(summaryLeft, currentY, summaryRight, currentY + 32f)
        paint.color = COLOR_GOLD_BOX_BG
        paint.style = Paint.Style.FILL
        canvas.drawRect(goldBoxRect, paint)

        paint.color = COLOR_GOLD_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        canvas.drawRect(goldBoxRect, paint)

        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 9.5f
        paint.color = COLOR_GOLD_TEXT
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("THIS PERIOD CLAIM", summaryLeft + 8f, currentY + 20f, paint)

        paint.textSize = 12f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(MastorCalculationEngine.formatCurrency(thisPeriodClaim), summaryRight - 8f, currentY + 21f, paint)

        currentY += 38f

        drawSummaryRow(
            label = "RETENTION (if applicable)",
            value = MastorCalculationEngine.formatCurrency(retentionAmount),
            isMuted = true
        )

        currentY += 4f

        // "NET PAYMENT DUE: £X,XXX" — largest text on the page
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 13f
        paint.color = COLOR_SLATE_DARK
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("NET PAYMENT DUE", summaryLeft + 4f, currentY + 14f, paint)

        paint.textSize = 15f
        paint.color = COLOR_GOLD_TEXT
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(MastorCalculationEngine.formatCurrency(netPaymentDue), summaryRight - 4f, currentY + 15f, paint)

        currentY += 28f

        // PO Number if set: "Please quote PO Ref: XXXXXXX on all correspondence"
        val poRefString = if (project.projectNumber.isNotBlank()) {
            "Please quote PO Ref: ${project.projectNumber} on all correspondence"
        } else {
            "Please quote Contract Ref: ${project.contractRef} on all correspondence"
        }

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 8.5f
        paint.color = COLOR_SLATE_TEXT
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(poRefString, PAGE_WIDTH / 2f, currentY + 10f, paint)

        // Draw final page footer
        drawFooter()
        pdfDocument.finishPage(currentPage)

        // Save PDF to context.cacheDir/invoices/IV-XX-[projectRef]-[date].pdf and return File
        val invoicesDir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val cleanValNum = if (valuation.valuationNumber.startsWith("IV-", ignoreCase = true)) {
            valuation.valuationNumber
        } else {
            "IV-${valuation.valuationNumber}"
        }.replace("[^a-zA-Z0-9_-]".toRegex(), "_")

        val cleanProjectRef = project.contractRef.ifBlank { project.projectNumber }.ifBlank { project.id }
            .replace("[^a-zA-Z0-9_-]".toRegex(), "_")

        val cleanDate = valuation.date.ifBlank { "current" }
            .replace("[^a-zA-Z0-9_-]".toRegex(), "_")

        val fileName = "${cleanValNum}-${cleanProjectRef}-${cleanDate}.pdf"
        val outputFile = File(invoicesDir, fileName)

        FileOutputStream(outputFile).use { fos ->
            pdfDocument.writeTo(fos)
        }
        pdfDocument.close()

        return outputFile
    }
}
