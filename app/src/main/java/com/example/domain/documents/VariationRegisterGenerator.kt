package com.example.domain.documents

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.data.entity.Project
import com.example.data.entity.VariationOrder
import com.example.domain.calculation.MastorCalculationEngine
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Variation Register — every VO on a job with its evidence, for the client's QS.
 *
 * Laid out in Mastor's drawing language: a title block header, hairline ruling, square
 * corners, monospace figures. Photos are embedded (up to 3 per VO) because a variation
 * without evidence is the one the QS pushes back on.
 *
 * Honest by construction: unpriced VOs are shown as UNPRICED, not £0.00, and missing
 * quantities as "not measured" — the register never implies a figure that wasn't set.
 */
object VariationRegisterGenerator {

    private const val W = 595
    private const val H = 842
    private const val L = 36f
    private const val R = 559f
    private const val TOP = 40f
    private const val BOTTOM = 800f

    private val INK = Color.rgb(26, 26, 46)
    private val MUTED = Color.rgb(90, 90, 122)
    private val RULE = Color.rgb(200, 192, 176)
    private val COPPER = Color.rgb(201, 123, 63)
    private val AMBER = Color.rgb(180, 83, 9)
    private val PAPER = Color.rgb(250, 247, 241)

    fun generate(context: Context, project: Project, vos: List<VariationOrder>): File {
        val doc = PdfDocument()
        var pageNo = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNo).create())
        var c: Canvas = page.canvas
        var y = TOP

        val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); textSize = 6.5f
            color = MUTED; letterSpacing = 0.18f
        }
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); textSize = 9f; color = INK
        }
        val bodyBold = Paint(body).apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) }
        val mono = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.MONOSPACE; textSize = 9f; color = INK
        }
        val monoBig = Paint(mono).apply { textSize = 18f; color = COPPER }
        val rule = Paint().apply { color = RULE; strokeWidth = 0.6f; style = Paint.Style.STROKE }
        val fill = Paint().apply { style = Paint.Style.FILL }

        val today = SimpleDateFormat("dd MMM yyyy", Locale.UK).format(Date())
        val totalPriced = vos.filter { it.rate > 0 && it.qty > 0 }.sumOf { MastorCalculationEngine.roundMoney(it.qty * it.rate) }
        val unpricedCount = vos.count { it.rate <= 0.0 }

        fun footer() {
            c.drawLine(L, BOTTOM + 8f, R, BOTTOM + 8f, rule)
            c.drawText("MASTOR · VARIATION REGISTER · ${project.contractRef.ifBlank { project.name }}".uppercase(), L, BOTTOM + 20f, label)
            val p = "SHEET $pageNo"
            c.drawText(p, R - label.measureText(p), BOTTOM + 20f, label)
        }

        fun newPage() {
            footer()
            doc.finishPage(page)
            pageNo++
            page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNo).create())
            c = page.canvas
            y = TOP
        }

        fun ensure(space: Float) { if (y + space > BOTTOM) newPage() }

        fun wrap(text: String, paint: Paint, width: Float): List<String> {
            if (text.isBlank()) return emptyList()
            val out = mutableListOf<String>()
            text.split("\n").forEach { para ->
                var line = ""
                para.split(" ").forEach { word ->
                    val t = if (line.isEmpty()) word else "$line $word"
                    if (paint.measureText(t) > width && line.isNotEmpty()) { out.add(line); line = word } else line = t
                }
                if (line.isNotEmpty()) out.add(line)
            }
            return out
        }

        // ---------------- TITLE BLOCK HEADER ----------------
        val tbTop = y
        val tbH = 118f
        fill.color = PAPER
        c.drawRect(L, tbTop, R, tbTop + tbH, fill)
        c.drawRect(L, tbTop, R, tbTop + tbH, rule)
        c.drawText("VARIATION REGISTER", L + 10f, tbTop + 16f, label)
        val titleP = Paint(bodyBold).apply { textSize = 16f }
        c.drawText(project.name.take(60), L + 10f, tbTop + 36f, titleP)
        c.drawText(listOf(project.client, project.address).filter { it.isNotBlank() }.joinToString(" · ").take(95), L + 10f, tbTop + 50f, body)
        c.drawLine(L, tbTop + 60f, R, tbTop + 60f, rule)
        val cells = listOf(
            "CONTRACT REF" to project.contractRef.ifBlank { "—" },
            "PO NUMBER" to project.projectNumber.ifBlank { "NOT SET" },
            "VARIATIONS" to vos.size.toString(),
            "ISSUED" to today
        )
        val cw = (R - L) / cells.size
        cells.forEachIndexed { i, (k, v) ->
            val x = L + cw * i
            if (i > 0) c.drawLine(x, tbTop + 60f, x, tbTop + tbH, rule)
            c.drawText(k, x + 10f, tbTop + 76f, label)
            c.drawText(v.take(18), x + 10f, tbTop + 92f, mono)
        }
        c.drawText("PRICED TOTAL (EXCL. UPLIFTS)", L + 10f, tbTop + 108f, label)
        val tot = MastorCalculationEngine.formatCurrency(totalPriced)
        c.drawText(tot, R - 10f - monoBig.measureText(tot), tbTop + 110f, monoBig)
        y = tbTop + tbH + 18f

        if (unpricedCount > 0) {
            val warn = Paint(bodyBold).apply { color = AMBER }
            c.drawText("$unpricedCount variation${if (unpricedCount == 1) " is" else "s are"} not yet priced and excluded from the total.", L, y, warn)
            y += 16f
        }

        // ---------------- ONE BLOCK PER VO ----------------
        val sorted = vos.sortedBy { Regex("""(\d+)""").find(it.voNumber)?.value?.toIntOrNull() ?: Int.MAX_VALUE }
        sorted.forEach { vo ->
            val descLines = wrap(vo.description, bodyBold, R - L - 20f)
            val noteLines = wrap(vo.notes, body, R - L - 20f)
            val photos = vo.photoList().take(3)
            val blockH = 40f + descLines.size * 12f + noteLines.size * 11f + 26f + (if (photos.isNotEmpty()) 96f else 0f)
            ensure(blockH + 10f)
            val top = y
            c.drawRect(L, top, R, top + blockH, rule)

            // Header strip
            c.drawText(vo.voNumber, L + 10f, top + 16f, Paint(mono).apply { color = COPPER; textSize = 11f })
            val meta = listOfNotNull(
                vo.status.uppercase(),
                "RAISED ${vo.dateRaised}".uppercase(),
                if (vo.externalVoNumber.isNotBlank()) "CLIENT REF ${vo.externalVoNumber}".uppercase() else "NO CLIENT REF YET"
            ).joinToString("   ·   ")
            c.drawText(meta, R - 10f - label.measureText(meta), top + 15f, label)
            c.drawLine(L, top + 24f, R, top + 24f, rule)

            var yy = top + 38f
            descLines.forEach { c.drawText(it, L + 10f, yy, bodyBold); yy += 12f }
            c.drawText("LOCATION  ${vo.locationRoom.uppercase()}" + if (vo.code.isNotBlank()) "     SOR ${vo.code}" else "", L + 10f, yy + 2f, label)
            yy += 12f
            noteLines.forEach { c.drawText(it, L + 10f, yy, Paint(body).apply { color = MUTED }); yy += 11f }

            // Figures row
            yy += 4f
            c.drawLine(L, yy - 8f, R, yy - 8f, rule)
            val qtyText = if (vo.qty > 0) "${formatQty(vo.qty)} ${vo.units}" else "NOT MEASURED"
            val rateText = if (vo.rate > 0) MastorCalculationEngine.formatCurrency(vo.rate) else "UNPRICED"
            val lineTotal = if (vo.rate > 0 && vo.qty > 0) MastorCalculationEngine.formatCurrency(MastorCalculationEngine.roundMoney(vo.qty * vo.rate)) else "—"
            c.drawText("QTY", L + 10f, yy + 2f, label); c.drawText(qtyText, L + 34f, yy + 3f, Paint(mono).apply { color = if (vo.qty > 0) INK else AMBER })
            c.drawText("RATE", L + 180f, yy + 2f, label); c.drawText(rateText, L + 208f, yy + 3f, Paint(mono).apply { color = if (vo.rate > 0) INK else AMBER })
            val tp = Paint(mono).apply { textSize = 11f; color = COPPER }
            c.drawText(lineTotal, R - 10f - tp.measureText(lineTotal), yy + 3f, tp)
            yy += 12f

            // Photos
            if (photos.isNotEmpty()) {
                val pw = 120f; val ph = 84f
                photos.forEachIndexed { i, uriStr ->
                    val bmp = loadScaled(context, uriStr, 480)
                    val x = L + 10f + i * (pw + 8f)
                    val dst = RectF(x, yy, x + pw, yy + ph)
                    if (bmp != null) {
                        c.drawBitmap(centerCrop(bmp, pw / ph), null, dst, null)
                    } else {
                        c.drawText("photo unavailable", x + 8f, yy + ph / 2, label)
                    }
                    c.drawRect(dst, rule)
                }
            }
            y = top + blockH + 10f
        }

        // ---------------- SUMMARY ----------------
        ensure(40f + vos.groupBy { it.status }.size * 14f)
        c.drawText("SUMMARY BY STATUS", L, y + 10f, label)
        y += 18f
        vos.groupBy { it.status }.forEach { (status, list) ->
            val v = list.filter { it.rate > 0 && it.qty > 0 }.sumOf { MastorCalculationEngine.roundMoney(it.qty * it.rate) }
            c.drawText("$status  (${list.size})", L, y, body)
            val s = MastorCalculationEngine.formatCurrency(v)
            c.drawText(s, R - mono.measureText(s), y, mono)
            c.drawLine(L, y + 4f, R, y + 4f, rule)
            y += 14f
        }

        footer()
        doc.finishPage(page)

        val dir = File(context.filesDir, "documents/variation_registers").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd-HHmm", Locale.UK).format(Date())
        val safeRef = project.contractRef.ifBlank { project.name }.replace(Regex("[^A-Za-z0-9-]"), "_").take(30)
        val out = File(dir, "VO-Register-$safeRef-$stamp.pdf")
        FileOutputStream(out).use { doc.writeTo(it) }
        doc.close()
        return out
    }

    private fun formatQty(q: Double) = if (q % 1.0 == 0.0) q.toInt().toString() else "%.2f".format(q)

    private fun loadScaled(context: Context, uriStr: String, maxDim: Int): Bitmap? = try {
        val uri = Uri.parse(uriStr)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > maxDim || bounds.outHeight / sample > maxDim) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    } catch (e: Exception) { null }

    private fun centerCrop(src: Bitmap, aspect: Float): Bitmap {
        val srcAspect = src.width.toFloat() / src.height
        return if (srcAspect > aspect) {
            val w = (src.height * aspect).toInt()
            Bitmap.createBitmap(src, (src.width - w) / 2, 0, w, src.height)
        } else {
            val h = (src.width / aspect).toInt()
            Bitmap.createBitmap(src, 0, (src.height - h) / 2, src.width, h)
        }
    }
}
