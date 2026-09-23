package com.example.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MastorCopper

// Shared stroke width helper matching MastorIcons.kt's 1.6dp-on-24-grid rule.
private fun sw(u: Float) = 1.6f * u

/* ============================================================================
 * DRAFTING TOOLS — Diary, Photo, Video, Log Variation
 * ========================================================================== */

/** Mic — drawn as a surveyor's level vial rather than a capsule: a vertical tube with a
 * horizontal cross-hair and the copper notch sitting where the bubble would rest. Reads as
 * "recording an on-site instrument reading," which is closer to what a diary entry actually is
 * than a generic microphone glyph. */
@Composable
fun MastorMicIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        // Vial body
        drawRect(colour, topLeft = Offset(9f * u, 4f * u), size = androidx.compose.ui.geometry.Size(6f * u, 13f * u), style = Stroke(sw(u)))
        // Cross-hair (level line)
        drawLine(colour, Offset(9f * u, 10.5f * u), Offset(15f * u, 10.5f * u), strokeWidth = sw(u))
        // Base / stand
        drawLine(colour, Offset(7f * u, 21f * u), Offset(17f * u, 21f * u), strokeWidth = sw(u), cap = StrokeCap.Square)
        drawLine(colour, Offset(12f * u, 17f * u), Offset(12f * u, 21f * u), strokeWidth = sw(u))
        // Signature notch — the "bubble"
        drawCircle(MastorCopper, radius = 1.1f * u, center = Offset(12f * u, 10.5f * u))
    }
}

/** Camera — a viewfinder crosshair in a square housing rather than the rounded lozenge-with-lens
 * Material draws. Square body matches the drawing language; the copper notch sits as the
 * shutter button. */
@Composable
fun MastorCameraIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        drawRect(colour, topLeft = Offset(3f * u, 7f * u), size = androidx.compose.ui.geometry.Size(18f * u, 13f * u), style = Stroke(sw(u)))
        // Prism / flash bump
        drawLine(colour, Offset(8f * u, 7f * u), Offset(9.5f * u, 4f * u), strokeWidth = sw(u))
        drawLine(colour, Offset(9.5f * u, 4f * u), Offset(14.5f * u, 4f * u), strokeWidth = sw(u))
        drawLine(colour, Offset(14.5f * u, 4f * u), Offset(16f * u, 7f * u), strokeWidth = sw(u))
        // Viewfinder crosshair (square, not circular lens)
        val cx = 12f; val cy = 13.5f; val r = 3.5f
        drawRect(colour, topLeft = Offset((cx - r) * u, (cy - r) * u), size = androidx.compose.ui.geometry.Size(r * 2 * u, r * 2 * u), style = Stroke(sw(u)))
        drawLine(colour, Offset((cx - r - 1.2f) * u, cy * u), Offset((cx - r) * u, cy * u), strokeWidth = sw(u))
        drawLine(colour, Offset((cx + r) * u, cy * u), Offset((cx + r + 1.2f) * u, cy * u), strokeWidth = sw(u))
        drawCircle(MastorCopper, radius = 1f * u, center = Offset(5.5f * u, 9.5f * u)) // shutter notch
    }
}

/** Video — the camera housing with a directional play-chevron cut into it (chamfered, not a
 * rounded triangle), so it reads as a distinct sibling of MastorCameraIcon rather than an
 * unrelated glyph. */
@Composable
fun MastorVideoIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        drawRect(colour, topLeft = Offset(3f * u, 6f * u), size = androidx.compose.ui.geometry.Size(13f * u, 12f * u), style = Stroke(sw(u)))
        // Chamfered lens housing to the right
        val path = Path().apply {
            moveTo(16f * u, 9.5f * u)
            lineTo(20f * u, 7f * u)
            lineTo(21f * u, 7.6f * u)
            lineTo(21f * u, 16.4f * u)
            lineTo(20f * u, 17f * u)
            lineTo(16f * u, 14.5f * u)
            close()
        }
        drawPath(path, colour, style = Stroke(sw(u)))
        // Play chamfer inside the frame (solid copper accent doubling as the signature notch)
        val playPath = Path().apply {
            moveTo(8f * u, 9.5f * u)
            lineTo(12f * u, 12f * u)
            lineTo(8f * u, 14.5f * u)
            close()
        }
        drawPath(playPath, MastorCopper)
    }
}

/** Log Variation (AddCircle replacement) — a squared plan-marker: a chamfered diamond (like a
 * survey peg mark on a site plan) with a plus cut through it, copper notch at the join. Reads as
 * "flagging something on the drawing" rather than a generic add-circle. */
@Composable
fun MastorFlagVariationIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        val path = Path().apply {
            moveTo(12f * u, 3f * u)
            lineTo(21f * u, 12f * u)
            lineTo(12f * u, 21f * u)
            lineTo(3f * u, 12f * u)
            close()
        }
        drawPath(path, colour, style = Stroke(sw(u)))
        drawLine(colour, Offset(12f * u, 8f * u), Offset(12f * u, 16f * u), strokeWidth = sw(u))
        drawLine(colour, Offset(8f * u, 12f * u), Offset(16f * u, 12f * u), strokeWidth = sw(u))
        drawCircle(MastorCopper, radius = 1.2f * u, center = Offset(12f * u, 12f * u))
    }
}

/* ============================================================================
 * SITE & BUILDING CONCEPTS — Scope, Variations(edit), Receipts, Upload, Home,
 * Settings, Client, Location, Calendar
 * ========================================================================== */

/** Scope (List replacement) — a stack of drawing sheets, seen edge-on, exactly the way a roll
 * of drawings sits on a site-office table. Three offset rectangles, not bullet-point lines. */
@Composable
fun MastorScopeIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        drawRect(colour, topLeft = Offset(7f * u, 3f * u), size = androidx.compose.ui.geometry.Size(14f * u, 17f * u), style = Stroke(sw(u)))
        drawRect(colour.copy(alpha = colour.alpha * 0.7f), topLeft = Offset(4f * u, 6f * u), size = androidx.compose.ui.geometry.Size(14f * u, 17f * u), style = Stroke(sw(u)))
        // Rule lines on the top sheet
        drawLine(colour, Offset(10f * u, 8f * u), Offset(18f * u, 8f * u), strokeWidth = sw(u) * 0.75f)
        drawLine(colour, Offset(10f * u, 11f * u), Offset(18f * u, 11f * u), strokeWidth = sw(u) * 0.75f)
        drawLine(colour, Offset(10f * u, 14f * u), Offset(15f * u, 14f * u), strokeWidth = sw(u) * 0.75f)
        drawCircle(MastorCopper, radius = 1f * u, center = Offset(9f * u, 4.6f * u))
    }
}

/** Variations (Edit replacement) — a red-line mark-up pencil crossing a drawn line, the exact
 * gesture of marking a variation onto a drawing on site. */
@Composable
fun MastorMarkupIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        // Base drawn line being marked up
        drawLine(colour.copy(alpha = colour.alpha * 0.55f), Offset(3f * u, 19f * u), Offset(15f * u, 19f * u), strokeWidth = sw(u))
        // Pencil body (chamfered, not rounded)
        val pencil = Path().apply {
            moveTo(9f * u, 15f * u)
            lineTo(17f * u, 7f * u)
            lineTo(20f * u, 10f * u)
            lineTo(12f * u, 18f * u)
            lineTo(8f * u, 19f * u)
            close()
        }
        drawPath(pencil, colour, style = Stroke(sw(u)))
        drawLine(colour, Offset(16f * u, 8f * u), Offset(19f * u, 11f * u), strokeWidth = sw(u) * 0.8f)
        drawCircle(MastorCopper, radius = 1.1f * u, center = Offset(19.5f * u, 5.5f * u)) // pencil tip mark
    }
}

/** Receipt / Valuation — a certificate/valuation sheet with a chamfered bottom edge (like a
 * tear-off certificate) and a copper tick, rather than the Material zig-zag receipt. */
@Composable
fun MastorValuationIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        val path = Path().apply {
            moveTo(5f * u, 3f * u)
            lineTo(19f * u, 3f * u)
            lineTo(19f * u, 18f * u)
            lineTo(15.5f * u, 21f * u)
            lineTo(12f * u, 18f * u)
            lineTo(8.5f * u, 21f * u)
            lineTo(5f * u, 18f * u)
            close()
        }
        drawPath(path, colour, style = Stroke(sw(u)))
        drawLine(colour, Offset(8f * u, 8f * u), Offset(16f * u, 8f * u), strokeWidth = sw(u) * 0.75f)
        drawLine(colour, Offset(8f * u, 11f * u), Offset(16f * u, 11f * u), strokeWidth = sw(u) * 0.75f)
        drawLine(colour, Offset(8f * u, 14f * u), Offset(13f * u, 14f * u), strokeWidth = sw(u) * 0.75f)
        drawCircle(MastorCopper, radius = 1.1f * u, center = Offset(16.5f * u, 14.2f * u))
    }
}

/** Upload (BoQ Import) — a drawing sheet feeding upward into a drafting-tray slot; the copper
 * notch marks the leading corner of the sheet. */
@Composable
fun MastorUploadIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        // Tray
        drawLine(colour, Offset(4f * u, 20f * u), Offset(20f * u, 20f * u), strokeWidth = sw(u))
        drawLine(colour, Offset(4f * u, 20f * u), Offset(4f * u, 16f * u), strokeWidth = sw(u))
        drawLine(colour, Offset(20f * u, 20f * u), Offset(20f * u, 16f * u), strokeWidth = sw(u))
        // Sheet with folded corner, rising
        drawRect(colour, topLeft = Offset(8f * u, 3f * u), size = androidx.compose.ui.geometry.Size(8f * u, 11f * u), style = Stroke(sw(u)))
        val fold = Path().apply {
            moveTo(13f * u, 3f * u); lineTo(16f * u, 6f * u); lineTo(13f * u, 6f * u); close()
        }
        drawPath(fold, colour, style = Stroke(sw(u) * 0.8f))
        drawCircle(MastorCopper, radius = 1.1f * u, center = Offset(8f * u, 3f * u))
    }
}

/** Home — a single pitched-roof house, exactly the roofline silhouette the illustration set
 * already uses, reduced to icon scale, so nav and hero speak the same shape language. */
@Composable
fun MastorHomeIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        val roof = Path().apply {
            moveTo(3f * u, 12f * u); lineTo(12f * u, 4f * u); lineTo(21f * u, 12f * u)
        }
        drawPath(roof, colour, style = Stroke(sw(u)))
        drawRect(colour, topLeft = Offset(6f * u, 12f * u), size = androidx.compose.ui.geometry.Size(12f * u, 8f * u), style = Stroke(sw(u)))
        drawLine(colour, Offset(10.5f * u, 20f * u), Offset(10.5f * u, 15f * u), strokeWidth = sw(u))
        drawLine(colour, Offset(10.5f * u, 15f * u), Offset(13.5f * u, 15f * u), strokeWidth = sw(u))
        drawLine(colour, Offset(13.5f * u, 15f * u), Offset(13.5f * u, 20f * u), strokeWidth = sw(u))
        drawCircle(MastorCopper, radius = 1f * u, center = Offset(17f * u, 8.4f * u)) // chimney-position notch
    }
}

/** Settings — a spanner/wrench crossing a bolt-head hex, a site-tool rather than a gear cog. */
@Composable
fun MastorSettingsIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        // Hex bolt head
        val hex = Path().apply {
            val cx = 9f; val cy = 15f; val r = 4f
            for (i in 0..5) {
                val angle = Math.toRadians((60 * i - 30).toDouble())
                val x = (cx + r * kotlin.math.cos(angle)).toFloat() * u
                val y = (cy + r * kotlin.math.sin(angle)).toFloat() * u
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(hex, colour, style = Stroke(sw(u)))
        // Spanner handle crossing it
        drawLine(colour, Offset(13f * u, 11f * u), Offset(21f * u, 3f * u), strokeWidth = sw(u) * 1.3f)
        drawCircle(MastorCopper, radius = 1.1f * u, center = Offset(20f * u, 4f * u))
    }
}

/** Client (Business replacement) — a small commercial block outline with a chamfered door,
 * echoing the CommercialIllustration silhouette rather than a briefcase. */
@Composable
fun MastorClientIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        drawRect(colour, topLeft = Offset(4f * u, 6f * u), size = androidx.compose.ui.geometry.Size(16f * u, 15f * u), style = Stroke(sw(u)))
        drawLine(colour, Offset(4f * u, 6f * u), Offset(12f * u, 3f * u), strokeWidth = sw(u))
        drawLine(colour, Offset(12f * u, 3f * u), Offset(20f * u, 6f * u), strokeWidth = sw(u))
        // Ribbon windows
        for (i in 0..2) {
            val x = 7f + i * 4f
            drawRect(colour, topLeft = Offset(x * u, 9f * u), size = androidx.compose.ui.geometry.Size(2.4f * u, 2.4f * u), style = Stroke(sw(u) * 0.75f))
        }
        drawRect(colour, topLeft = Offset(10.5f * u, 15f * u), size = androidx.compose.ui.geometry.Size(3f * u, 6f * u), style = Stroke(sw(u) * 0.85f))
        drawCircle(MastorCopper, radius = 1f * u, center = Offset(12f * u, 5.2f * u))
    }
}

/** Location — a chamfered site-boundary pin: a diamond peg with a copper ground-mark, closer to
 * a surveyor's stake than the Material teardrop pin. */
@Composable
fun MastorLocationIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        val peg = Path().apply {
            moveTo(12f * u, 2f * u)
            lineTo(19f * u, 9f * u)
            lineTo(15f * u, 13f * u)
            lineTo(12f * u, 20f * u)
            lineTo(9f * u, 13f * u)
            lineTo(5f * u, 9f * u)
            close()
        }
        drawPath(peg, colour, style = Stroke(sw(u)))
        // Ground mark
        drawLine(colour, Offset(7f * u, 21.5f * u), Offset(17f * u, 21.5f * u), strokeWidth = sw(u) * 0.8f)
        drawCircle(MastorCopper, radius = 1.2f * u, center = Offset(12f * u, 9f * u))
    }
}

/** Calendar — a wall planner grid with one filled copper cell rather than a generic day-grid
 * icon, echoing a site programme/Gantt look. */
@Composable
fun MastorCalendarIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        drawRect(colour, topLeft = Offset(3f * u, 5f * u), size = androidx.compose.ui.geometry.Size(18f * u, 16f * u), style = Stroke(sw(u)))
        drawLine(colour, Offset(3f * u, 9f * u), Offset(21f * u, 9f * u), strokeWidth = sw(u))
        drawLine(colour, Offset(8f * u, 3f * u), Offset(8f * u, 6.5f * u), strokeWidth = sw(u))
        drawLine(colour, Offset(16f * u, 3f * u), Offset(16f * u, 6.5f * u), strokeWidth = sw(u))
        for (col in 0..3) {
            val x = 6f + col * 4f
            drawLine(colour.copy(alpha = colour.alpha * 0.5f), Offset(x * u, 12f * u), Offset(x * u, 18f * u), strokeWidth = sw(u) * 0.6f)
        }
        // Filled "today" cell
        drawRect(MastorCopper, topLeft = Offset(13.4f * u, 13f * u), size = androidx.compose.ui.geometry.Size(3.2f * u, 3.2f * u))
    }
}

/* ============================================================================
 * STATUS MARKS — Warning, CheckCircle, Engineering, AssignmentTurnedIn, PriceCheck
 * ========================================================================== */

/** Warning — a chamfered triangle (not rounded) with the exclamation as a drawn line + copper
 * dot, matching the icon family's no-rounded-joins rule that Material's triangle breaks. */
@Composable
fun MastorWarningIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        val tri = Path().apply {
            moveTo(12f * u, 3f * u); lineTo(21f * u, 20f * u); lineTo(3f * u, 20f * u); close()
        }
        drawPath(tri, colour, style = Stroke(sw(u)))
        drawLine(colour, Offset(12f * u, 9f * u), Offset(12f * u, 14.5f * u), strokeWidth = sw(u) * 1.1f)
        drawCircle(MastorCopper, radius = 1f * u, center = Offset(12f * u, 17.2f * u))
    }
}

/** CheckCircle — a chamfered octagon (site "stop/inspection tag" shape) rather than a circle,
 * with the tick as two straight chamfered strokes. */
@Composable
fun MastorCheckIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        val oct = Path().apply {
            val pts = listOf(
                8f to 3f, 16f to 3f, 21f to 8f, 21f to 16f,
                16f to 21f, 8f to 21f, 3f to 16f, 3f to 8f
            )
            pts.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(x * u, y * u) else lineTo(x * u, y * u) }
            close()
        }
        drawPath(oct, colour, style = Stroke(sw(u)))
        drawLine(MastorCopper, Offset(7.5f * u, 12.5f * u), Offset(10.5f * u, 15.5f * u), strokeWidth = sw(u) * 1.2f)
        drawLine(MastorCopper, Offset(10.5f * u, 15.5f * u), Offset(16.5f * u, 8.5f * u), strokeWidth = sw(u) * 1.2f)
    }
}

/** Engineering — a set-square + rule crossed, the actual drafting instruments, rather than the
 * Material hard-hat glyph. */
@Composable
fun MastorEngineeringIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        // Set-square (right triangle)
        val square = Path().apply {
            moveTo(3f * u, 20f * u); lineTo(3f * u, 6f * u); lineTo(17f * u, 20f * u); close()
        }
        drawPath(square, colour, style = Stroke(sw(u)))
        // Rule crossing it
        drawLine(colour, Offset(9f * u, 3f * u), Offset(21f * u, 15f * u), strokeWidth = sw(u) * 1.1f)
        drawLine(colour, Offset(11f * u, 3f * u), Offset(23f * u, 15f * u), strokeWidth = sw(u) * 1.1f)
        drawCircle(MastorCopper, radius = 1f * u, center = Offset(15f * u, 9f * u))
    }
}

/** AssignmentTurnedIn (approved/tick) — a clipboard-tag with a chamfered corner fold and a
 * copper tick clip at the top, distinct from MastorCheckIcon by carrying the "document" frame. */
@Composable
fun MastorApprovedDocIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        drawRect(colour, topLeft = Offset(4f * u, 5f * u), size = androidx.compose.ui.geometry.Size(16f * u, 16f * u), style = Stroke(sw(u)))
        // Clip
        drawRect(colour, topLeft = Offset(9f * u, 2.5f * u), size = androidx.compose.ui.geometry.Size(6f * u, 4f * u), style = Stroke(sw(u)))
        drawLine(colour, Offset(8f * u, 10f * u), Offset(16f * u, 10f * u), strokeWidth = sw(u) * 0.75f)
        drawLine(colour, Offset(8f * u, 13f * u), Offset(16f * u, 13f * u), strokeWidth = sw(u) * 0.75f)
        drawLine(MastorCopper, Offset(8.5f * u, 16.5f * u), Offset(10.5f * u, 18.5f * u), strokeWidth = sw(u) * 1.1f)
        drawLine(MastorCopper, Offset(10.5f * u, 18.5f * u), Offset(15.5f * u, 13.5f * u), strokeWidth = sw(u) * 1.1f)
    }
}

/** PriceCheck — a tag with a copper £ mark, since Mastor is UK SoR-work and the currency symbol
 * itself is the clearer, more relevant motif than a generic tick-price glyph. */
@Composable
fun MastorPriceCheckIcon(modifier: Modifier = Modifier, size: Dp = 24.dp, lineColor: Color = Color.Unspecified) {
    val colour = if (lineColor == Color.Unspecified) MastorCopper else lineColor
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 24f
        val tag = Path().apply {
            moveTo(3f * u, 12f * u)
            lineTo(11f * u, 4f * u)
            lineTo(20f * u, 4f * u)
            lineTo(20f * u, 13f * u)
            lineTo(12f * u, 21f * u)
            close()
        }
        drawPath(tag, colour, style = Stroke(sw(u)))
        drawCircle(MastorCopper, radius = 1.4f * u, center = Offset(15.5f * u, 8.5f * u))
        // £ mark, drawn as strokes (no text glyph dependency)
        val px = 15.5f; val py = 8.5f
        drawLine(Color.White, Offset((px - 0.9f) * u, (py + 1.3f) * u), Offset((px + 0.9f) * u, (py + 1.3f) * u), strokeWidth = sw(u) * 0.55f)
        drawLine(Color.White, Offset((px - 0.4f) * u, (py - 1.2f) * u), Offset((px - 0.4f) * u, (py + 1.3f) * u), strokeWidth = sw(u) * 0.55f)
        drawLine(Color.White, Offset((px - 1f) * u, (py - 0.1f) * u), Offset((px + 0.4f) * u, (py - 0.1f) * u), strokeWidth = sw(u) * 0.55f)
    }
}
