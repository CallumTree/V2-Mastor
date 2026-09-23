package com.example.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MastorCopper

/**
 * MASTOR ICON SET — "Construction Drawing" family.
 *
 * Every icon in this file is drawn with the same pen, by rule:
 *   - Logical grid: 24 x 24 units, scaled to whatever size the caller requests.
 *   - Stroke weight: 1.6dp for the main figure, ALWAYS via `stroke()` below — no filled shapes
 *     except the single copper accent notch each icon carries.
 *   - Corners: square or 45-degree chamfer only. No rounded joins/caps anywhere — matches the
 *     illustration set's roofline/floor-plan geometry (RoundedCornerShape is a UI-chrome idea,
 *     not a drawing idea, and doesn't belong in the icon linework).
 *   - Signature: exactly one small solid copper element per icon (a dot, a short tick, a
 *     filled triangle) — the same trick Linear/Stripe use with a repeated corner-cut or dot to
 *     make an otherwise-plain glyph set read as one family at a glance.
 *   - Colour: the line colour is passed in (so an icon can sit on cream OR charcoal); the
 *     copper accent is always MastorCopper regardless of context — it's the constant.
 *
 * Do not add a new icon to this file without following all four rules above. An icon that
 * breaks the stroke weight, uses rounded corners, or skips the accent will read as a different
 * hand and undoes the reason this file exists.
 */

private val STROKE = Stroke(
    width = 1.6f,
    cap = StrokeCap.Square,
    join = StrokeJoin.Miter,
    miter = 2f
)

private fun DrawScope.grid(block: DrawScope.(unit: Float) -> Unit) {
    val unit = size.width / 24f
    block(unit)
}

/** Common wrapper: fixed-size Canvas on the 24-unit logical grid described above. */
@Composable
private fun MastorIconCanvas(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 24.dp,
    draw: DrawScope.(lineColor: Color, unit: Float) -> Unit,
    lineColor: Color
) {
    Canvas(modifier = modifier.size(size)) {
        grid { unit -> draw(lineColor, unit) }
    }
}

private fun DrawScope.line(lineColor: Color, x1: Float, y1: Float, x2: Float, y2: Float, u: Float) {
    drawLine(lineColor, Offset(x1 * u, y1 * u), Offset(x2 * u, y2 * u), strokeWidth = STROKE.width * u)
}

private fun DrawScope.rect(lineColor: Color, x: Float, y: Float, w: Float, h: Float, u: Float) {
    drawRect(lineColor, topLeft = Offset(x * u, y * u), size = Size(w * u, h * u), style = Stroke(width = STROKE.width * u))
}

/** The one recurring signature element every icon carries — a small solid copper notch. */
private fun DrawScope.copperNotch(cx: Float, cy: Float, u: Float, radius: Float = 1.3f) {
    drawCircle(MastorCopper, radius = radius * u, center = Offset(cx * u, cy * u))
}
