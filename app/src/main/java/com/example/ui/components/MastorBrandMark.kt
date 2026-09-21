package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MastorCopper
import com.example.ui.theme.MastorInk
import com.example.ui.theme.MastorInkMuted

/**
 * Mastor Brand Icon: A Canvas-drawn brand mark featuring a rounded-rect slate background
 * containing a mason's set-square (triangle, apex up, stroke only, gold) with a plumb line
 * dropping from the apex to a small plumb-bob circle beneath it.
 */
@Composable
fun MastorIcon(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    backgroundColor: Color = MastorInk,
    glyphColor: Color = MastorCopper,
    cornerRadius: Dp = size * 0.28f
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val rPx = cornerRadius.toPx()

        // 1. Background slate rounded rect
        drawRoundRect(
            color = backgroundColor,
            cornerRadius = CornerRadius(rPx, rPx)
        )

        // 2. Geometry calculations
        val padX = w * 0.22f
        val topY = h * 0.20f
        val bottomY = h * 0.78f
        val midX = w * 0.5f

        // Mason's set-square triangle path (apex up, stroke only)
        val trianglePath = Path().apply {
            moveTo(midX, topY)
            lineTo(w - padX, bottomY)
            lineTo(padX, bottomY)
            close()
        }

        val strokeWidth = (w * 0.058f).coerceIn(1.5f, 4.5f)
        drawPath(
            path = trianglePath,
            color = glyphColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 3. Plumb line dropping from apex to plumb-bob circle
        val plumbBobCenterY = bottomY - h * 0.08f
        val plumbBobCenter = Offset(midX, plumbBobCenterY)
        val lineStroke = (strokeWidth * 0.7f).coerceAtLeast(1.2f)

        drawLine(
            color = glyphColor,
            start = Offset(midX, topY),
            end = plumbBobCenter,
            strokeWidth = lineStroke,
            cap = StrokeCap.Round
        )

        // Plumb-bob circle beneath apex
        val plumbBobRadius = (w * 0.065f).coerceIn(1.8f, 5.5f)
        drawCircle(
            color = glyphColor,
            radius = plumbBobRadius,
            center = plumbBobCenter
        )
    }
}

/**
 * Mastor Wordmark: Row with MastorIcon followed by "MASTOR" in Serif bold,
 * and an optional uppercase tagline below in smaller serif.
 */
@Composable
fun MastorWordmark(
    modifier: Modifier = Modifier,
    iconSize: Dp = 36.dp,
    tagline: String? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MastorIcon(size = iconSize)
        Column {
            Text(
                text = "MASTOR",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                fontSize = (iconSize.value * 0.48f).sp,
                color = MastorInk
            )
            if (tagline != null) {
                Text(
                    text = tagline.uppercase(),
                    fontFamily = FontFamily.Serif,
                    fontSize = (iconSize.value * 0.28f).sp,
                    color = MastorInkMuted,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
