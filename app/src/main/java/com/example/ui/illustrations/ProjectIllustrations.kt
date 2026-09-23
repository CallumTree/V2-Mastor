package com.example.ui.illustrations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.data.entity.Project
import com.example.ui.theme.MastorBracketLabel
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.theme.BracketLabel
import com.example.ui.theme.MastorBody
import com.example.ui.theme.MastorCharcoal
import com.example.ui.theme.MastorCopper
import com.example.ui.theme.MastorCreamMuted
import com.example.ui.theme.MastorCreamText
import com.example.ui.theme.MastorDisplayMedium
import com.example.ui.theme.MastorFinancialMed
import com.example.ui.theme.MastorStatusBadge
import kotlin.math.abs

/**
 * 24 SVG-based Compose @Composable drawables organized by project type.
 * Rendered using pure Compose Canvas drawing primitives on a 400x200 logical canvas,
 * scaled proportionally to fill any container dimensions.
 */
enum class ProjectIllustrationType {
    RESIDENTIAL_PPR,
    COMMERCIAL,
    ROOFING,
    INTERNAL_WORKS,
    EXTERNAL_WORKS,
    UNKNOWN
}

object ProjectIllustrationPicker {

    fun getType(projectType: String?): ProjectIllustrationType {
        val type = projectType?.trim() ?: return ProjectIllustrationType.UNKNOWN
        val lower = type.lowercase()
        return when {
            lower.contains("ppr") ||
            lower.contains("residential") ||
            lower.contains("whqs") ||
            lower.contains("social housing") ||
            lower.contains("housing") -> ProjectIllustrationType.RESIDENTIAL_PPR

            lower.contains("commercial") ||
            lower.contains("fitout") ||
            lower.contains("fit-out") ||
            lower.contains("office") -> ProjectIllustrationType.COMMERCIAL

            lower.contains("roofing") ||
            lower.contains("roof") -> ProjectIllustrationType.ROOFING

            lower.contains("internal") ||
            lower.contains("refurb") ||
            lower.contains("internal works") -> ProjectIllustrationType.INTERNAL_WORKS

            lower.contains("external") ||
            lower.contains("groundworks") ||
            lower.contains("landscaping") -> ProjectIllustrationType.EXTERNAL_WORKS

            else -> ProjectIllustrationType.UNKNOWN
        }
    }

    fun getVariantIndex(projectId: String, variantCount: Int): Int {
        if (variantCount <= 0) return 0
        return abs(projectId.hashCode()) % variantCount
    }

    fun resolveIllustration(projectId: String, projectType: String?): @Composable () -> Unit {
        val type = getType(projectType)
        val index = when (type) {
            ProjectIllustrationType.RESIDENTIAL_PPR -> getVariantIndex(projectId, 5)
            ProjectIllustrationType.UNKNOWN -> getVariantIndex(projectId, 3)
            else -> getVariantIndex(projectId, 4)
        }
        return {
            when (type) {
                ProjectIllustrationType.RESIDENTIAL_PPR -> ResidentialPprIllustration(variant = index, modifier = Modifier.fillMaxSize())
                ProjectIllustrationType.COMMERCIAL -> CommercialIllustration(variant = index, modifier = Modifier.fillMaxSize())
                ProjectIllustrationType.ROOFING -> RoofingIllustration(variant = index, modifier = Modifier.fillMaxSize())
                ProjectIllustrationType.INTERNAL_WORKS -> InternalWorksIllustration(variant = index, modifier = Modifier.fillMaxSize())
                ProjectIllustrationType.EXTERNAL_WORKS -> ExternalWorksIllustration(variant = index, modifier = Modifier.fillMaxSize())
                ProjectIllustrationType.UNKNOWN -> UnknownIllustration(variant = index, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Helper to scale a 400x200 logical drawing to any canvas size with bottom overlay
// -----------------------------------------------------------------------------
@Composable
private fun ScaledCanvasIllustration(
    modifier: Modifier = Modifier,
    drawIllustration: DrawScope.() -> Unit
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val logicalWidth = 400f
        val logicalHeight = 200f

        val scaleX = size.width / logicalWidth
        val scaleY = size.height / logicalHeight
        val scaleFactor = maxOf(scaleX, scaleY) // Fill viewport proportionally

        // Draw the vector illustration centered & scaled
        scale(scaleFactor, pivot = Offset(size.width / 2f, size.height / 2f)) {
            // Center the 400x200 box in canvas
            val offsetX = (size.width / scaleFactor - logicalWidth) / 2f
            val offsetY = (size.height / scaleFactor - logicalHeight) / 2f
            drawContext.transform.translate(offsetX, offsetY)
            drawIllustration()
            drawContext.transform.translate(-offsetX, -offsetY)
        }

        // Subtle gradient overlay from transparent to MastorCharcoal at 60% opacity at bottom
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    MastorCharcoal.copy(alpha = 0.2f),
                    MastorCharcoal.copy(alpha = 0.6f)
                ),
                startY = size.height * 0.4f,
                endY = size.height
            ),
            size = size
        )
    }
}

// =============================================================================
// RESIDENTIAL_PPR (5 Variants)
// Warm terracotta tones (#C97B3F at 15-25% opacity base, #C97B3F / #EDE6D8 for line work)
// =============================================================================
@Composable
fun ResidentialPprIllustration(variant: Int, modifier: Modifier = Modifier) {
    val terracottaBase = Color(0xFFC97B3F).copy(alpha = 0.20f)
    val terracottaAccent = Color(0xFFC97B3F).copy(alpha = 0.35f)
    val lineStroke = Color(0xFFEDE6D8).copy(alpha = 0.45f)

    ScaledCanvasIllustration(modifier = modifier) {
        // Base ground line
        drawLine(
            color = lineStroke.copy(alpha = 0.6f),
            start = Offset(0f, 175f),
            end = Offset(400f, 175f),
            strokeWidth = 2f
        )

        when (variant % 5) {
            // Variant 0: Terraced row — 4 connected houses, pitched roofs, chimneys, simple windows
            0 -> {
                val houseW = 75f
                val startX = 50f
                for (i in 0 until 4) {
                    val x = startX + i * houseW
                    // House Body
                    drawRect(
                        color = terracottaBase,
                        topLeft = Offset(x, 95f),
                        size = Size(houseW, 80f)
                    )
                    drawRect(
                        color = lineStroke,
                        topLeft = Offset(x, 95f),
                        size = Size(houseW, 80f),
                        style = Stroke(width = 1.5f)
                    )
                    // Pitched Roof
                    val roofPath = Path().apply {
                        moveTo(x, 95f)
                        lineTo(x + houseW / 2f, 55f)
                        lineTo(x + houseW, 95f)
                        close()
                    }
                    drawPath(roofPath, color = terracottaAccent)
                    drawPath(roofPath, color = lineStroke, style = Stroke(width = 1.5f))

                    // Chimney
                    drawRect(
                        color = terracottaBase,
                        topLeft = Offset(x + houseW - 14f, 48f),
                        size = Size(8f, 18f)
                    )
                    drawRect(
                        color = lineStroke,
                        topLeft = Offset(x + houseW - 14f, 48f),
                        size = Size(8f, 18f),
                        style = Stroke(width = 1.2f)
                    )

                    // Door
                    drawRect(
                        color = lineStroke.copy(alpha = 0.7f),
                        topLeft = Offset(x + 12f, 140f),
                        size = Size(16f, 35f),
                        style = Stroke(width = 1.2f)
                    )
                    // Windows: Ground floor and First floor
                    drawRect(
                        color = lineStroke.copy(alpha = 0.7f),
                        topLeft = Offset(x + 40f, 135f),
                        size = Size(20f, 22f),
                        style = Stroke(width = 1.2f)
                    )
                    drawRect(
                        color = lineStroke.copy(alpha = 0.7f),
                        topLeft = Offset(x + 12f, 108f),
                        size = Size(18f, 18f),
                        style = Stroke(width = 1.2f)
                    )
                    drawRect(
                        color = lineStroke.copy(alpha = 0.7f),
                        topLeft = Offset(x + 42f, 108f),
                        size = Size(18f, 18f),
                        style = Stroke(width = 1.2f)
                    )
                }
            }

            // Variant 1: Semi-detached pair — two houses sharing a wall, front gardens suggested
            1 -> {
                val pairW = 120f
                val startX = 80f

                // House 1 (Left)
                drawRect(color = terracottaBase, topLeft = Offset(startX, 90f), size = Size(pairW, 85f))
                drawRect(color = lineStroke, topLeft = Offset(startX, 90f), size = Size(pairW, 85f), style = Stroke(1.5f))

                // Shared Roof
                val roofPath = Path().apply {
                    moveTo(startX - 10f, 90f)
                    lineTo(startX + pairW, 45f)
                    lineTo(startX + pairW * 2f + 10f, 90f)
                    close()
                }
                drawPath(roofPath, color = terracottaAccent)
                drawPath(roofPath, color = lineStroke, style = Stroke(1.5f))

                // House 2 (Right)
                drawRect(color = terracottaBase, topLeft = Offset(startX + pairW, 90f), size = Size(pairW, 85f))
                drawRect(color = lineStroke, topLeft = Offset(startX + pairW, 90f), size = Size(pairW, 85f), style = Stroke(1.5f))

                // Central party wall line
                drawLine(lineStroke, Offset(startX + pairW, 45f), Offset(startX + pairW, 175f), strokeWidth = 1.5f)

                // Central chimney stack
                drawRect(terracottaBase, Offset(startX + pairW - 10f, 30f), Size(20f, 20f))
                drawRect(lineStroke, Offset(startX + pairW - 10f, 30f), Size(20f, 20f), style = Stroke(1.5f))

                // Front garden low fence suggestion
                for (fx in 40..340 step 15) {
                    drawLine(lineStroke.copy(alpha = 0.4f), Offset(fx.toFloat(), 165f), Offset(fx.toFloat(), 175f), strokeWidth = 1f)
                }
                drawLine(lineStroke.copy(alpha = 0.5f), Offset(40f, 165f), Offset(340f, 165f), strokeWidth = 1f)

                // Doors on outer edges
                drawRect(lineStroke, Offset(startX + 14f, 135f), Size(18f, 40f), style = Stroke(1.2f))
                drawRect(lineStroke, Offset(startX + pairW * 2f - 32f, 135f), Size(18f, 40f), style = Stroke(1.2f))

                // Paired Windows
                drawRect(lineStroke, Offset(startX + 48f, 132f), Size(30f, 24f), style = Stroke(1.2f))
                drawRect(lineStroke, Offset(startX + pairW + 42f, 132f), Size(30f, 24f), style = Stroke(1.2f))
                drawRect(lineStroke, Offset(startX + 48f, 102f), Size(30f, 20f), style = Stroke(1.2f))
                drawRect(lineStroke, Offset(startX + pairW + 42f, 102f), Size(30f, 20f), style = Stroke(1.2f))
            }

            // Variant 2: End-terrace — single house with side return, bay window suggestion
            2 -> {
                val startX = 110f
                // Main House Block
                drawRect(color = terracottaBase, topLeft = Offset(startX, 85f), size = Size(130f, 90f))
                drawRect(color = lineStroke, topLeft = Offset(startX, 85f), size = Size(130f, 90f), style = Stroke(1.5f))

                // Side Return (Lower Extension)
                drawRect(color = terracottaBase.copy(alpha = 0.15f), topLeft = Offset(startX + 130f, 115f), size = Size(55f, 60f))
                drawRect(color = lineStroke, topLeft = Offset(startX + 130f, 115f), size = Size(55f, 60f), style = Stroke(1.5f))
                // Extension lean-to roof
                val extRoof = Path().apply {
                    moveTo(startX + 130f, 115f)
                    lineTo(startX + 185f, 130f)
                    lineTo(startX + 130f, 130f)
                    close()
                }
                drawPath(extRoof, color = terracottaAccent)
                drawPath(extRoof, color = lineStroke, style = Stroke(1.2f))

                // Main Gable Roof
                val mainRoof = Path().apply {
                    moveTo(startX - 10f, 85f)
                    lineTo(startX + 65f, 40f)
                    lineTo(startX + 140f, 85f)
                    close()
                }
                drawPath(mainRoof, color = terracottaAccent)
                drawPath(mainRoof, color = lineStroke, style = Stroke(1.5f))

                // Bay Window Projection
                val bayPath = Path().apply {
                    moveTo(startX + 20f, 135f)
                    lineTo(startX + 28f, 130f)
                    lineTo(startX + 62f, 130f)
                    lineTo(startX + 70f, 135f)
                    lineTo(startX + 70f, 175f)
                    lineTo(startX + 20f, 175f)
                    close()
                }
                drawPath(bayPath, color = terracottaAccent)
                drawPath(bayPath, color = lineStroke, style = Stroke(1.2f))

                // Door with transom
                drawRect(lineStroke, Offset(startX + 85f, 132f), Size(22f, 43f), style = Stroke(1.2f))
                drawRect(lineStroke, Offset(startX + 85f, 124f), Size(22f, 8f), style = Stroke(1f))

                // First floor window
                drawRect(lineStroke, Offset(startX + 25f, 98f), Size(40f, 22f), style = Stroke(1.2f))
                drawRect(lineStroke, Offset(startX + 85f, 98f), Size(22f, 22f), style = Stroke(1.2f))
            }

            // Variant 3: Bungalow profile — low wide house, hipped roof, wide front
            3 -> {
                val startX = 60f
                val width = 280f
                // Low Wide Body
                drawRect(color = terracottaBase, topLeft = Offset(startX, 110f), size = Size(width, 65f))
                drawRect(color = lineStroke, topLeft = Offset(startX, 110f), size = Size(width, 65f), style = Stroke(1.5f))

                // Hipped Roof
                val hippedRoof = Path().apply {
                    moveTo(startX - 15f, 110f)
                    lineTo(startX + 60f, 55f)
                    lineTo(startX + width - 60f, 55f)
                    lineTo(startX + width + 15f, 110f)
                    close()
                }
                drawPath(hippedRoof, color = terracottaAccent)
                drawPath(hippedRoof, color = lineStroke, style = Stroke(1.5f))

                // Hip ridge lines
                drawLine(lineStroke, Offset(startX + 60f, 55f), Offset(startX + width - 60f, 55f), strokeWidth = 1.5f)
                drawLine(lineStroke.copy(alpha = 0.5f), Offset(startX + 60f, 55f), Offset(startX + 60f, 110f), strokeWidth = 1f)
                drawLine(lineStroke.copy(alpha = 0.5f), Offset(startX + width - 60f, 55f), Offset(startX + width - 60f, 110f), strokeWidth = 1f)

                // Wide front entrance canopy
                drawRect(terracottaAccent, Offset(startX + 120f, 118f), Size(40f, 6f))
                drawRect(lineStroke, Offset(startX + 120f, 118f), Size(40f, 6f), style = Stroke(1.2f))
                drawRect(lineStroke, Offset(startX + 128f, 124f), Size(24f, 51f), style = Stroke(1.2f))

                // Wide horizontal picture windows
                drawRect(lineStroke, Offset(startX + 25f, 125f), Size(65f, 32f), style = Stroke(1.2f))
                drawRect(lineStroke, Offset(startX + 190f, 125f), Size(65f, 32f), style = Stroke(1.2f))
                // Window mullion bars
                drawLine(lineStroke.copy(alpha = 0.4f), Offset(startX + 57f, 125f), Offset(startX + 57f, 157f), strokeWidth = 1f)
                drawLine(lineStroke.copy(alpha = 0.4f), Offset(startX + 222f, 125f), Offset(startX + 222f, 157f), strokeWidth = 1f)
            }

            // Variant 4: Flat block — 3-storey block, regular window grid, flat roof with parapet
            else -> {
                val startX = 75f
                val blockW = 250f
                val blockH = 125f

                // Main 3-storey mass
                drawRect(color = terracottaBase, topLeft = Offset(startX, 50f), size = Size(blockW, blockH))
                drawRect(color = lineStroke, topLeft = Offset(startX, 50f), size = Size(blockW, blockH), style = Stroke(1.5f))

                // Parapet coping line at roof
                drawRect(color = terracottaAccent, topLeft = Offset(startX - 8f, 44f), size = Size(blockW + 16f, 6f))
                drawRect(color = lineStroke, topLeft = Offset(startX - 8f, 44f), size = Size(blockW + 16f, 6f), style = Stroke(1.2f))

                // Central entrance with canopy
                drawRect(terracottaAccent, Offset(startX + 105f, 140f), Size(40f, 6f))
                drawRect(lineStroke, Offset(startX + 105f, 140f), Size(40f, 6f), style = Stroke(1.2f))
                drawRect(lineStroke, Offset(startX + 112f, 146f), Size(26f, 29f), style = Stroke(1.2f))

                // Regular window grid: 3 floors, 5 columns (excluding entrance)
                val floors = listOf(62f, 98f, 134f)
                val cols = listOf(startX + 20f, startX + 65f, startX + 165f, startX + 210f)

                floors.forEachIndexed { floorIdx, yPos ->
                    cols.forEach { xPos ->
                        drawRect(
                            color = lineStroke.copy(alpha = 0.7f),
                            topLeft = Offset(xPos, yPos),
                            size = Size(26f, 22f),
                            style = Stroke(1.2f)
                        )
                        // Window pane cross
                        drawLine(lineStroke.copy(alpha = 0.3f), Offset(xPos + 13f, yPos), Offset(xPos + 13f, yPos + 22f), 1f)
                    }
                    // Central column upper floors
                    if (floorIdx < 2) {
                        drawRect(
                            color = lineStroke.copy(alpha = 0.7f),
                            topLeft = Offset(startX + 115f, yPos),
                            size = Size(20f, 22f),
                            style = Stroke(1.2f)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// COMMERCIAL (4 Variants)
// Cool slate tones (#252540 at 20% opacity base, #2DD4BF accent lines)
// =============================================================================
@Composable
fun CommercialIllustration(variant: Int, modifier: Modifier = Modifier) {
    val slateBase = Color(0xFF252540).copy(alpha = 0.22f)
    val slateMid = Color(0xFF252540).copy(alpha = 0.38f)
    val tealAccent = Color(0xFF2DD4BF)

    ScaledCanvasIllustration(modifier = modifier) {
        // Ground datum
        drawLine(slateMid, Offset(0f, 175f), Offset(400f, 175f), strokeWidth = 2f)

        when (variant % 4) {
            // Variant 0: Flat-roof office — clean rectangular block, ribbon windows, entrance canopy
            0 -> {
                val startX = 60f
                val width = 280f
                // Main Block
                drawRect(slateBase, Offset(startX, 55f), Size(width, 120f))
                drawRect(slateMid, Offset(startX, 55f), Size(width, 120f), style = Stroke(1.5f))

                // Parapet
                drawRect(slateMid, Offset(startX - 6f, 48f), Size(width + 12f, 7f))

                // Continuous ribbon windows across upper 2 levels
                drawRect(slateMid, Offset(startX + 15f, 72f), Size(width - 30f, 22f))
                drawRect(tealAccent, Offset(startX + 15f, 72f), Size(width - 30f, 22f), style = Stroke(1.2f))
                for (rx in 90..310 step 30) {
                    drawLine(tealAccent.copy(alpha = 0.6f), Offset(rx.toFloat(), 72f), Offset(rx.toFloat(), 94f), 1f)
                }

                drawRect(slateMid, Offset(startX + 15f, 106f), Size(width - 30f, 22f))
                drawRect(tealAccent, Offset(startX + 15f, 106f), Size(width - 30f, 22f), style = Stroke(1.2f))
                for (rx in 90..310 step 30) {
                    drawLine(tealAccent.copy(alpha = 0.6f), Offset(rx.toFloat(), 106f), Offset(rx.toFloat(), 128f), 1f)
                }

                // Glazed entrance & cantilever canopy
                val canopy = Path().apply {
                    moveTo(startX + 105f, 142f)
                    lineTo(startX + 175f, 142f)
                    lineTo(startX + 170f, 146f)
                    lineTo(startX + 110f, 146f)
                    close()
                }
                drawPath(canopy, color = tealAccent)
                drawRect(tealAccent, Offset(startX + 115f, 146f), Size(50f, 29f), style = Stroke(1.5f))
                drawLine(tealAccent, Offset(startX + 140f, 146f), Offset(startX + 140f, 175f), 1f)
            }

            // Variant 1: Retail unit — wide single-storey, large shopfront opening, signage zone
            1 -> {
                val startX = 50f
                val width = 300f
                // Single-storey building mass
                drawRect(slateBase, Offset(startX, 65f), Size(width, 110f))
                drawRect(slateMid, Offset(startX, 65f), Size(width, 110f), style = Stroke(1.5f))

                // Fascia / Signage zone
                drawRect(slateMid, Offset(startX + 15f, 75f), Size(width - 30f, 28f))
                drawRect(tealAccent, Offset(startX + 15f, 75f), Size(width - 30f, 28f), style = Stroke(1.5f))
                // Signage lettering suggestion
                drawLine(tealAccent, Offset(startX + 40f, 89f), Offset(startX + 140f, 89f), 2.5f, cap = StrokeCap.Round)
                drawLine(tealAccent.copy(alpha = 0.6f), Offset(startX + 155f, 89f), Offset(startX + 260f, 89f), 2f, cap = StrokeCap.Round)

                // Large glazed shopfront with mullions
                drawRect(slateMid.copy(alpha = 0.3f), Offset(startX + 20f, 112f), Size(width - 40f, 63f))
                drawRect(tealAccent, Offset(startX + 20f, 112f), Size(width - 40f, 63f), style = Stroke(1.5f))
                for (mx in 90..330 step 35) {
                    drawLine(tealAccent.copy(alpha = 0.5f), Offset(mx.toFloat(), 112f), Offset(mx.toFloat(), 175f), 1f)
                }
                // Automatic sliding door center frame
                drawRect(tealAccent, Offset(startX + 125f, 120f), Size(50f, 55f), style = Stroke(1.8f))
            }

            // Variant 2: Warehouse — large low profile, roller door, loading bay suggestion
            2 -> {
                val startX = 40f
                val width = 320f
                // Low pitched industrial warehouse profile
                val warehousePath = Path().apply {
                    moveTo(startX, 175f)
                    lineTo(startX, 90f)
                    lineTo(startX + width / 2f, 60f)
                    lineTo(startX + width, 90f)
                    lineTo(startX + width, 175f)
                    close()
                }
                drawPath(warehousePath, color = slateBase)
                drawPath(warehousePath, color = slateMid, style = Stroke(1.5f))

                // Cladding profile lines
                for (cx in 60..340 step 20) {
                    drawLine(slateMid.copy(alpha = 0.3f), Offset(cx.toFloat(), 95f), Offset(cx.toFloat(), 175f), 1f)
                }

                // High-level strip clerestory windows
                drawRect(tealAccent, Offset(startX + 40f, 95f), Size(70f, 14f), style = Stroke(1.2f))
                drawRect(tealAccent, Offset(startX + width - 110f, 95f), Size(70f, 14f), style = Stroke(1.2f))

                // Large Industrial Roller Shutter Door
                drawRect(slateMid, Offset(startX + 105f, 115f), Size(110f, 60f))
                drawRect(tealAccent, Offset(startX + 105f, 115f), Size(110f, 60f), style = Stroke(1.8f))
                // Shutter horizontal slats
                for (sy in 122..170 step 6) {
                    drawLine(tealAccent.copy(alpha = 0.6f), Offset(startX + 105f, sy.toFloat()), Offset(startX + 215f, sy.toFloat()), 1f)
                }
                // Loading bay yellow/teal striped bumper outline
                drawLine(tealAccent, Offset(startX + 95f, 174f), Offset(startX + 225f, 174f), strokeWidth = 3f)
            }

            // Variant 3: Glass-fronted commercial — angled perspective suggestion, curtain wall grid
            else -> {
                val p1 = Offset(70f, 45f)
                val p2 = Offset(250f, 30f)
                val p3 = Offset(330f, 60f)
                val p4 = Offset(330f, 175f)
                val p5 = Offset(250f, 175f)
                val p6 = Offset(70f, 175f)

                // Front curtain facade
                val frontFacade = Path().apply {
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p5.x, p5.y)
                    lineTo(p6.x, p6.y)
                    close()
                }
                drawPath(frontFacade, color = slateBase)
                drawPath(frontFacade, color = slateMid, style = Stroke(1.5f))

                // Side angled facade
                val sideFacade = Path().apply {
                    moveTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    lineTo(p4.x, p4.y)
                    lineTo(p5.x, p5.y)
                    close()
                }
                drawPath(sideFacade, color = slateMid.copy(alpha = 0.4f))
                drawPath(sideFacade, color = slateMid, style = Stroke(1.5f))

                // Front Curtain Wall Grid (Teal accent lines)
                val floors = listOf(70f, 100f, 130f, 155f)
                floors.forEach { y ->
                    val ratio = (y - 45f) / 130f
                    val yLeft = 45f + ratio * 130f
                    val yRight = 30f + ratio * 145f
                    drawLine(tealAccent, Offset(70f, yLeft), Offset(250f, yRight), strokeWidth = 1.2f)
                }
                for (gx in 100..220 step 30) {
                    val progress = (gx - 70f) / 180f
                    val topY = 45f - progress * 15f
                    drawLine(tealAccent.copy(alpha = 0.7f), Offset(gx.toFloat(), topY), Offset(gx.toFloat(), 175f), 1.2f)
                }

                // Side wall grid lines
                drawLine(tealAccent.copy(alpha = 0.5f), Offset(250f, 70f), Offset(330f, 90f), 1f)
                drawLine(tealAccent.copy(alpha = 0.5f), Offset(250f, 115f), Offset(330f, 130f), 1f)
                drawLine(tealAccent.copy(alpha = 0.5f), Offset(290f, 45f), Offset(290f, 175f), 1f)
            }
        }
    }
}

// =============================================================================
// ROOFING (4 Variants)
// Deep charcoal tones (#1A1A2E at 30% opacity, slate texture suggestion)
// =============================================================================
@Composable
fun RoofingIllustration(variant: Int, modifier: Modifier = Modifier) {
    val charcoalBase = Color(0xFF2E2E4A).copy(alpha = 0.45f)
    val charcoalMid = Color(0xFFEDE6D8).copy(alpha = 0.40f)
    val copperHighlight = MastorCopper

    ScaledCanvasIllustration(modifier = modifier) {
        drawLine(charcoalMid, Offset(0f, 175f), Offset(400f, 175f), strokeWidth = 1.5f)

        when (variant % 4) {
            // Variant 0: Pitched roof profile — clean gable end, ridge, eaves, fascia detail
            0 -> {
                val startX = 60f
                val width = 280f
                // Eaves & Fascia beam
                drawRect(charcoalMid, Offset(startX - 15f, 105f), Size(width + 30f, 8f))

                // Main pitched slope
                val roofPitch = Path().apply {
                    moveTo(startX - 15f, 105f)
                    lineTo(startX + width / 2f, 40f)
                    lineTo(startX + width + 15f, 105f)
                    close()
                }
                drawPath(roofPitch, color = charcoalBase)
                drawPath(roofPitch, color = charcoalMid, style = Stroke(2f))

                // Ridge board capping in copper
                drawLine(copperHighlight, Offset(startX + width / 2f - 20f, 38f), Offset(startX + width / 2f + 20f, 38f), 3.5f, cap = StrokeCap.Round)

                // Slate rows with staggered vertical lap joints
                for (rowY in 52..95 step 11) {
                    val progress = (rowY - 40f) / 65f
                    val halfSpan = (width / 2f + 15f) * progress
                    val leftX = (startX + width / 2f) - halfSpan
                    val rightX = (startX + width / 2f) + halfSpan
                    drawLine(charcoalMid, Offset(leftX, rowY.toFloat()), Offset(rightX, rowY.toFloat()), 1.2f)

                    // Staggered slate tile dividers
                    val stepSize = 22f
                    var curX = leftX + 10f
                    while (curX < rightX - 10f) {
                        drawLine(charcoalMid.copy(alpha = 0.4f), Offset(curX, rowY.toFloat()), Offset(curX, rowY + 11f), 1f)
                        curX += stepSize
                    }
                }

                // Gutter run & downpipe detail
                drawLine(copperHighlight, Offset(startX - 20f, 113f), Offset(startX + width + 20f, 113f), 2.5f)
                drawLine(copperHighlight, Offset(startX - 18f, 113f), Offset(startX - 18f, 175f), 2f)
            }

            // Variant 1: Flat roof detail — parapet, upstand, drain suggestion, membrane lines
            1 -> {
                // Building profile section
                drawRect(charcoalBase, Offset(50f, 80f), Size(300f, 95f))
                drawRect(charcoalMid, Offset(50f, 80f), Size(300f, 95f), style = Stroke(1.5f))

                // Left & right parapet upstands
                drawRect(charcoalMid, Offset(40f, 60f), Size(25f, 35f))
                drawRect(charcoalMid, Offset(335f, 60f), Size(25f, 35f))
                // Coping stones on parapet
                drawRect(copperHighlight, Offset(36f, 56f), Size(33f, 6f))
                drawRect(copperHighlight, Offset(331f, 56f), Size(33f, 6f))

                // Multi-layer roofing membrane lines
                val membraneY = listOf(84f, 88f, 92f)
                membraneY.forEachIndexed { idx, y ->
                    drawLine(
                        color = if (idx == 0) copperHighlight else charcoalMid,
                        start = Offset(65f, y),
                        end = Offset(335f, y),
                        strokeWidth = if (idx == 0) 2f else 1.2f
                    )
                }

                // Parapet waterproof upstand flashing fillets
                val leftFlashing = Path().apply {
                    moveTo(65f, 92f)
                    lineTo(65f, 70f)
                    lineTo(75f, 92f)
                    close()
                }
                drawPath(leftFlashing, color = copperHighlight.copy(alpha = 0.6f))

                // Roof outlet / drain sump suggestion
                drawRect(copperHighlight, Offset(110f, 86f), Size(18f, 14f))
                drawLine(copperHighlight, Offset(119f, 100f), Offset(119f, 175f), 2f)

                // Vapour control layer hatch dots
                for (dx in 80..320 step 20) {
                    drawCircle(charcoalMid.copy(alpha = 0.5f), radius = 2f, center = Offset(dx.toFloat(), 105f))
                }
            }

            // Variant 2: Dormer cross-section — main roof with dormer projection, cheeks, flashing
            2 -> {
                val startX = 50f
                // Main pitched slope
                val mainRoof = Path().apply {
                    moveTo(startX, 150f)
                    lineTo(startX + 300f, 40f)
                }
                drawPath(mainRoof, color = charcoalMid, style = Stroke(2.5f))

                // Dormer Body emerging from pitched roof
                val dormerWall = Path().apply {
                    moveTo(150f, 113f)
                    lineTo(150f, 65f)
                    lineTo(240f, 65f)
                    lineTo(240f, 80f)
                    close()
                }
                drawPath(dormerWall, color = charcoalBase)
                drawPath(dormerWall, color = charcoalMid, style = Stroke(1.5f))

                // Dormer Flat/Pitched Roof
                val dormerRoof = Path().apply {
                    moveTo(140f, 65f)
                    lineTo(195f, 48f)
                    lineTo(250f, 65f)
                    close()
                }
                drawPath(dormerRoof, color = charcoalMid)
                drawPath(dormerRoof, color = copperHighlight, style = Stroke(1.8f))

                // Dormer Window Frame
                drawRect(copperHighlight, Offset(158f, 72f), Size(40f, 35f), style = Stroke(1.5f))
                drawLine(copperHighlight.copy(alpha = 0.6f), Offset(178f, 72f), Offset(178f, 107f), 1.2f)

                // Lead flashing apron suggestion (Copper accent)
                val flashingApron = Path().apply {
                    moveTo(145f, 118f)
                    lineTo(205f, 118f)
                    lineTo(215f, 128f)
                    lineTo(135f, 128f)
                    close()
                }
                drawPath(flashingApron, color = copperHighlight.copy(alpha = 0.5f))
                drawPath(flashingApron, color = copperHighlight, style = Stroke(1.5f))

                // Main roof rafter lines
                drawLine(charcoalMid.copy(alpha = 0.4f), Offset(80f, 139f), Offset(80f, 175f), 1.2f)
                drawLine(charcoalMid.copy(alpha = 0.4f), Offset(270f, 69f), Offset(270f, 175f), 1.2f)
            }

            // Variant 3: Roof plan grid — overhead view, ridge line, hip returns, valley
            3 -> {
                val cx = 200f
                val cy = 105f
                val halfW = 130f
                val halfH = 65f

                // Outer Eaves Perimeter
                drawRect(charcoalBase, Offset(cx - halfW, cy - halfH), Size(halfW * 2f, halfH * 2f))
                drawRect(charcoalMid, Offset(cx - halfW, cy - halfH), Size(halfW * 2f, halfH * 2f), style = Stroke(2f))

                // Central Ridge Line
                val ridgeSpan = 70f
                drawLine(copperHighlight, Offset(cx - ridgeSpan, cy), Offset(cx + ridgeSpan, cy), strokeWidth = 3f, cap = StrokeCap.Round)

                // Four Hip Lines radiating to corners
                drawLine(charcoalMid, Offset(cx - ridgeSpan, cy), Offset(cx - halfW, cy - halfH), strokeWidth = 1.8f)
                drawLine(charcoalMid, Offset(cx - ridgeSpan, cy), Offset(cx - halfW, cy + halfH), strokeWidth = 1.8f)
                drawLine(charcoalMid, Offset(cx + ridgeSpan, cy), Offset(cx + halfW, cy - halfH), strokeWidth = 1.8f)
                drawLine(charcoalMid, Offset(cx + ridgeSpan, cy), Offset(cx + halfW, cy + halfH), strokeWidth = 1.8f)

                // Pitch slope contour lines / batten runs
                for (offset in 15..50 step 12) {
                    val f = offset.toFloat()
                    drawLine(charcoalMid.copy(alpha = 0.35f), Offset(cx - ridgeSpan + f * 0.5f, cy - f), Offset(cx + ridgeSpan - f * 0.5f, cy - f), 1f)
                    drawLine(charcoalMid.copy(alpha = 0.35f), Offset(cx - ridgeSpan + f * 0.5f, cy + f), Offset(cx + ridgeSpan - f * 0.5f, cy + f), 1f)
                }

                // Fall arrow indicator
                drawLine(copperHighlight, Offset(cx, cy - 35f), Offset(cx, cy - 15f), 1.5f)
                drawLine(copperHighlight, Offset(cx - 5f, cy - 23f), Offset(cx, cy - 15f), 1.5f)
                drawLine(copperHighlight, Offset(cx + 5f, cy - 23f), Offset(cx, cy - 15f), 1.5f)
            }
        }
    }
}

// =============================================================================
// INTERNAL_WORKS (4 Variants)
// Warm amber tones (#F59E0B at 10% opacity base)
// =============================================================================
@Composable
fun InternalWorksIllustration(variant: Int, modifier: Modifier = Modifier) {
    val amberBase = Color(0xFFF59E0B).copy(alpha = 0.15f)
    val amberMid = Color(0xFFF59E0B).copy(alpha = 0.45f)
    val lineStroke = Color(0xFFEDE6D8).copy(alpha = 0.45f)
    val amberAccent = Color(0xFFF59E0B)

    ScaledCanvasIllustration(modifier = modifier) {
        when (variant % 4) {
            // Variant 0: Floor plan grid — room layout suggestion, walls as thick lines, door swings
            0 -> {
                val startX = 65f
                val startY = 35f
                val width = 270f
                val height = 135f

                // Perimeter Floor Plan
                drawRect(amberBase, Offset(startX, startY), Size(width, height))
                drawRect(lineStroke, Offset(startX, startY), Size(width, height), style = Stroke(4f))

                // Internal partition walls (thick lines)
                val midX = startX + 150f
                val midY = startY + 75f

                // Vertical main wall with door gap
                drawLine(lineStroke, Offset(midX, startY), Offset(midX, startY + 40f), strokeWidth = 3.5f)
                drawLine(lineStroke, Offset(midX, startY + 68f), Offset(midX, startY + height), strokeWidth = 3.5f)

                // Horizontal partition wall
                drawLine(lineStroke, Offset(startX, midY), Offset(midX - 28f, midY), strokeWidth = 3.5f)

                // Door 1 Swing arc (90 degrees)
                drawArc(
                    color = amberAccent,
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(midX - 28f, startY + 40f),
                    size = Size(56f, 56f),
                    style = Stroke(1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
                )
                drawLine(lineStroke, Offset(midX, startY + 40f), Offset(midX + 24f, startY + 54f), 2f)

                // Door 2 Swing arc in partition
                drawArc(
                    color = amberAccent,
                    startAngle = 270f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(midX - 56f, midY - 28f),
                    size = Size(56f, 56f),
                    style = Stroke(1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
                )
                drawLine(lineStroke, Offset(midX - 28f, midY), Offset(midX - 28f, midY - 24f), 2f)

                // Window openings on outer walls (double thin lines)
                drawLine(Color.White, Offset(startX + 35f, startY), Offset(startX + 85f, startY), 4f)
                drawLine(amberAccent, Offset(startX + 35f, startY - 1f), Offset(startX + 85f, startY - 1f), 1.5f)
                drawLine(amberAccent, Offset(startX + 35f, startY + 1f), Offset(startX + 85f, startY + 1f), 1.5f)

                drawLine(Color.White, Offset(startX + width, startY + 35f), Offset(startX + width, startY + 95f), 4f)
                drawLine(amberAccent, Offset(startX + width - 1f, startY + 35f), Offset(startX + width - 1f, startY + 95f), 1.5f)
                drawLine(amberAccent, Offset(startX + width + 1f, startY + 35f), Offset(startX + width + 1f, startY + 95f), 1.5f)
            }

            // Variant 1: Room elevation — wall with window, skirting, picture rail, door frame
            1 -> {
                val startX = 50f
                val width = 300f
                // Wall Canvas
                drawRect(amberBase, Offset(startX, 40f), Size(width, 135f))
                drawRect(lineStroke, Offset(startX, 40f), Size(width, 135f), style = Stroke(1.5f))

                // Skirting board bottom (thick profile)
                drawRect(amberMid, Offset(startX, 163f), Size(width, 12f))
                drawLine(lineStroke, Offset(startX, 163f), Offset(startX + width, 163f), 2f)
                drawLine(lineStroke, Offset(startX, 175f), Offset(startX + width, 175f), 2f)

                // Picture rail & Dado rail
                drawLine(amberAccent, Offset(startX, 65f), Offset(startX + width, 65f), strokeWidth = 2f)
                drawLine(amberMid, Offset(startX, 115f), Offset(startX + width, 115f), strokeWidth = 1.5f)

                // Internal 4-panel Door Frame on Left
                drawRect(lineStroke, Offset(startX + 30f, 75f), Size(55f, 88f), style = Stroke(2f))
                drawRect(amberMid.copy(alpha = 0.3f), Offset(startX + 35f, 80f), Size(45f, 78f))
                // 4 Panels
                drawRect(lineStroke, Offset(startX + 39f, 85f), Size(17f, 30f), style = Stroke(1f))
                drawRect(lineStroke, Offset(startX + 59f, 85f), Size(17f, 30f), style = Stroke(1f))
                drawRect(lineStroke, Offset(startX + 39f, 122f), Size(17f, 32f), style = Stroke(1f))
                drawRect(lineStroke, Offset(startX + 59f, 122f), Size(17f, 32f), style = Stroke(1f))
                // Brass door handle
                drawCircle(amberAccent, radius = 2.5f, center = Offset(startX + 36f, 120f))

                // Casement Window on Right with architrave
                drawRect(lineStroke, Offset(startX + 145f, 60f), Size(110f, 75f), style = Stroke(2f))
                drawRect(amberBase, Offset(startX + 150f, 65f), Size(100f, 65f))
                // Window Sill
                drawRect(amberAccent, Offset(startX + 140f, 135f), Size(120f, 6f))
                // Window Glazing bars
                drawLine(lineStroke, Offset(startX + 200f, 65f), Offset(startX + 200f, 135f), 1.5f)
                drawLine(lineStroke, Offset(startX + 150f, 95f), Offset(startX + 250f, 95f), 1.2f)
            }

            // Variant 2: Staircase cross-section — treads, risers, handrail, newel post
            2 -> {
                val startX = 60f
                val startY = 175f
                val stepW = 22f
                val stepH = 13f
                val numSteps = 9

                // Stair profile stepped path
                val stairPath = Path().apply {
                    moveTo(startX, startY)
                    var curX = startX
                    var curY = startY
                    for (i in 0 until numSteps) {
                        curY -= stepH
                        lineTo(curX, curY)
                        curX += stepW
                        lineTo(curX, curY)
                    }
                    // Wall return
                    lineTo(curX, startY)
                    close()
                }
                drawPath(stairPath, color = amberBase)
                drawPath(stairPath, color = lineStroke, style = Stroke(2f))

                // Stringer board underneath
                val stringer = Path().apply {
                    moveTo(startX, startY)
                    lineTo(startX + numSteps * stepW, startY - numSteps * stepH)
                    lineTo(startX + numSteps * stepW, startY - numSteps * stepH + 18f)
                    lineTo(startX + 18f, startY)
                    close()
                }
                drawPath(stringer, color = amberMid)

                // Handrail & Balusters
                val railOffset = 42f
                val railPath = Path().apply {
                    moveTo(startX + 5f, startY - railOffset)
                    lineTo(startX + numSteps * stepW, startY - numSteps * stepH - railOffset)
                }
                drawPath(railPath, color = amberAccent, style = Stroke(3.5f, cap = StrokeCap.Round))

                // Newel posts at bottom and top
                drawRect(lineStroke, Offset(startX + 2f, startY - railOffset - 8f), Size(8f, railOffset + 8f))
                drawRect(lineStroke, Offset(startX + numSteps * stepW - 6f, startY - numSteps * stepH - railOffset - 8f), Size(8f, railOffset + 8f))

                // Vertical Balusters
                for (i in 1 until numSteps) {
                    val bx = startX + i * stepW - stepW / 2f
                    val by = startY - i * stepH
                    drawLine(lineStroke.copy(alpha = 0.6f), Offset(bx, by), Offset(bx, by - railOffset), strokeWidth = 1.5f)
                }
            }

            // Variant 3: Interior perspective — simple 1-point perspective room, floor, ceiling, back wall
            3 -> {
                val cx = 200f
                val cy = 100f
                val backW = 140f
                val backH = 80f

                // Back Wall
                val backLeft = cx - backW / 2f
                val backTop = cy - backH / 2f
                drawRect(amberBase, Offset(backLeft, backTop), Size(backW, backH))
                drawRect(lineStroke, Offset(backLeft, backTop), Size(backW, backH), style = Stroke(1.8f))

                // Perspective Corner Lines to Outer Frame
                drawLine(lineStroke, Offset(0f, 0f), Offset(backLeft, backTop), strokeWidth = 1.8f)
                drawLine(lineStroke, Offset(400f, 0f), Offset(backLeft + backW, backTop), strokeWidth = 1.8f)
                drawLine(lineStroke, Offset(0f, 200f), Offset(backLeft, backTop + backH), strokeWidth = 1.8f)
                drawLine(lineStroke, Offset(400f, 200f), Offset(backLeft + backW, backTop + backH), strokeWidth = 1.8f)

                // Floor Planks / Tiles Converging Lines
                for (fx in 40..360 step 45) {
                    drawLine(amberMid.copy(alpha = 0.5f), Offset(fx.toFloat(), 200f), Offset(cx, cy), strokeWidth = 1f)
                }

                // Ceiling recessed light spot suggestions
                drawCircle(amberAccent, radius = 3f, center = Offset(170f, 40f))
                drawCircle(amberAccent, radius = 3f, center = Offset(230f, 40f))

                // Back Wall Feature: Architectural Artwork or Window
                drawRect(amberMid.copy(alpha = 0.4f), Offset(cx - 30f, cy - 25f), Size(60f, 50f))
                drawRect(amberAccent, Offset(cx - 30f, cy - 25f), Size(60f, 50f), style = Stroke(1.5f))
                drawLine(amberAccent, Offset(cx, cy - 25f), Offset(cx, cy + 25f), 1f)
            }
        }
    }
}

// =============================================================================
// EXTERNAL_WORKS (4 Variants)
// Muted sage tones (#22C55E at 10% opacity base)
// =============================================================================
@Composable
fun ExternalWorksIllustration(variant: Int, modifier: Modifier = Modifier) {
    val sageBase = Color(0xFF22C55E).copy(alpha = 0.15f)
    val sageMid = Color(0xFF22C55E).copy(alpha = 0.40f)
    val sageAccent = Color(0xFF22C55E)
    val lineStroke = Color(0xFFEDE6D8).copy(alpha = 0.45f)

    ScaledCanvasIllustration(modifier = modifier) {
        when (variant % 4) {
            // Variant 0: Site plan overhead — plot boundary, building footprint, path, gate
            0 -> {
                val boundary = Path().apply {
                    moveTo(50f, 30f)
                    lineTo(350f, 30f)
                    lineTo(350f, 170f)
                    lineTo(50f, 170f)
                    close()
                }
                drawPath(boundary, color = sageBase)
                drawPath(
                    boundary,
                    color = sageAccent,
                    style = Stroke(2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f)))
                )

                // Building Footprint in center
                drawRect(lineStroke.copy(alpha = 0.2f), Offset(130f, 55f), Size(140f, 75f))
                drawRect(lineStroke, Offset(130f, 55f), Size(140f, 75f), style = Stroke(2f))
                // Diagonal cross through building
                drawLine(lineStroke.copy(alpha = 0.3f), Offset(130f, 55f), Offset(270f, 130f), 1f)
                drawLine(lineStroke.copy(alpha = 0.3f), Offset(270f, 55f), Offset(130f, 130f), 1f)

                // Winding Access Path from Gate
                val pathRun = Path().apply {
                    moveTo(50f, 140f)
                    cubicTo(90f, 140f, 110f, 110f, 130f, 110f)
                    lineTo(130f, 125f)
                    cubicTo(110f, 125f, 90f, 155f, 50f, 155f)
                    close()
                }
                drawPath(pathRun, color = sageMid)
                drawPath(pathRun, color = lineStroke, style = Stroke(1.2f))

                // Entrance Gate Posts on boundary
                drawCircle(lineStroke, radius = 4f, center = Offset(50f, 140f))
                drawCircle(lineStroke, radius = 4f, center = Offset(50f, 155f))
                drawLine(sageAccent, Offset(50f, 140f), Offset(65f, 135f), 2f)

                // Tree Canopy symbol
                drawCircle(sageAccent.copy(alpha = 0.25f), radius = 18f, center = Offset(310f, 70f))
                drawCircle(sageAccent, radius = 18f, center = Offset(310f, 70f), style = Stroke(1.5f))
                drawCircle(lineStroke, radius = 2f, center = Offset(310f, 70f))
            }

            // Variant 1: Path and boundary — concrete path, fence line, gate post
            1 -> {
                drawLine(lineStroke.copy(alpha = 0.4f), Offset(0f, 175f), Offset(400f, 175f), 1.5f)

                // Concrete Slab Paved Path in perspective
                val pavedPath = Path().apply {
                    moveTo(120f, 175f)
                    lineTo(280f, 175f)
                    lineTo(220f, 95f)
                    lineTo(180f, 95f)
                    close()
                }
                drawPath(pavedPath, color = sageBase)
                drawPath(pavedPath, color = lineStroke, style = Stroke(1.5f))

                // Paving slab joint lines
                val joints = listOf(150f, 130f, 115f, 105f)
                joints.forEach { y ->
                    val ratio = (y - 95f) / 80f
                    val left = 180f - ratio * 60f
                    val right = 220f + ratio * 60f
                    drawLine(lineStroke, Offset(left, y), Offset(right, y), strokeWidth = 1.2f)
                }

                // Timber Fence Line running parallel
                val fencePosts = listOf(40f, 75f, 110f, 145f)
                fencePosts.forEach { px ->
                    drawRect(lineStroke, Offset(px, 100f), Size(6f, 75f))
                }
                // Two horizontal fence rails
                drawLine(sageAccent, Offset(30f, 120f), Offset(160f, 120f), 2.5f)
                drawLine(sageAccent, Offset(30f, 150f), Offset(160f, 150f), 2.5f)

                // Feature Gatepost on Right
                drawRect(lineStroke, Offset(300f, 80f), Size(16f, 95f))
                drawRect(sageAccent, Offset(296f, 74f), Size(24f, 8f))
            }

            // Variant 2: Garden elevation — rear garden profile, fence, shed suggestion, level changes
            2 -> {
                // Stepped retaining wall (level change)
                val groundProfile = Path().apply {
                    moveTo(0f, 175f)
                    lineTo(160f, 175f)
                    lineTo(160f, 145f) // step up
                    lineTo(400f, 145f)
                    lineTo(400f, 200f)
                    lineTo(0f, 200f)
                    close()
                }
                drawPath(groundProfile, color = sageBase)
                drawPath(groundProfile, color = lineStroke, style = Stroke(2f))

                // Brick retaining wall hatching
                drawRect(sageMid, Offset(150f, 145f), Size(12f, 30f))
                drawRect(lineStroke, Offset(150f, 145f), Size(12f, 30f), style = Stroke(1.2f))

                // Garden Shed on raised terrace
                val shedX = 260f
                drawRect(sageBase, Offset(shedX, 90f), Size(80f, 55f))
                drawRect(lineStroke, Offset(shedX, 90f), Size(80f, 55f), style = Stroke(1.5f))
                // Shed apex roof
                val shedRoof = Path().apply {
                    moveTo(shedX - 6f, 90f)
                    lineTo(shedX + 40f, 65f)
                    lineTo(shedX + 86f, 90f)
                    close()
                }
                drawPath(shedRoof, color = sageMid)
                drawPath(shedRoof, color = lineStroke, style = Stroke(1.5f))

                // Shed door & window
                drawRect(lineStroke, Offset(shedX + 12f, 105f), Size(22f, 40f), style = Stroke(1.2f))
                drawRect(sageAccent, Offset(shedX + 45f, 105f), Size(22f, 20f), style = Stroke(1.2f))

                // Boundary Featheredge Fence in background
                for (fx in 10..220 step 15) {
                    drawLine(lineStroke.copy(alpha = 0.4f), Offset(fx.toFloat(), 125f), Offset(fx.toFloat(), 175f), 1.5f)
                }
                drawLine(sageAccent, Offset(10f, 135f), Offset(220f, 135f), 2f)
            }

            // Variant 3: Drainage plan — pipe runs, inspection chamber, fall direction arrows
            3 -> {
                val cx = 200f
                val cy = 100f

                // Main Inspection Chamber (Manhole) - Square outer, circle inner
                drawRect(sageMid, Offset(cx - 25f, cy - 25f), Size(50f, 50f))
                drawRect(lineStroke, Offset(cx - 25f, cy - 25f), Size(50f, 50f), style = Stroke(2f))
                drawCircle(sageBase, radius = 18f, center = Offset(cx, cy))
                drawCircle(sageAccent, radius = 18f, center = Offset(cx, cy), style = Stroke(2f))

                // Main 110mm Foul Drainage Pipe Run (Horizontal thick line)
                drawLine(lineStroke, Offset(40f, cy), Offset(cx - 25f, cy), strokeWidth = 4f, cap = StrokeCap.Round)
                drawLine(lineStroke, Offset(cx + 25f, cy), Offset(360f, cy), strokeWidth = 4f, cap = StrokeCap.Round)

                // Branch Inlets (Angled at 45 degrees)
                val branch1 = Path().apply {
                    moveTo(90f, 40f)
                    lineTo(140f, cy)
                }
                drawPath(branch1, color = sageAccent, style = Stroke(3f, cap = StrokeCap.Round))

                val branch2 = Path().apply {
                    moveTo(260f, cy)
                    lineTo(310f, 160f)
                }
                drawPath(branch2, color = sageAccent, style = Stroke(3f, cap = StrokeCap.Round))

                // Fall Direction Gradient Arrows (-->)
                fun drawArrow(x: Float, y: Float) {
                    drawLine(sageAccent, Offset(x - 14f, y), Offset(x + 14f, y), 2f)
                    drawLine(sageAccent, Offset(x + 6f, y - 6f), Offset(x + 14f, y), 2f)
                    drawLine(sageAccent, Offset(x + 6f, y + 6f), Offset(x + 14f, y), 2f)
                }
                drawArrow(90f, cy - 18f)
                drawArrow(300f, cy - 18f)

                // Technical Invert Level & Fall text annotation labels simulation
                drawLine(lineStroke.copy(alpha = 0.5f), Offset(cx - 35f, cy + 36f), Offset(cx + 35f, cy + 36f), 1.2f)
                drawLine(lineStroke.copy(alpha = 0.5f), Offset(cx - 20f, cy + 44f), Offset(cx + 20f, cy + 44f), 1f)
            }
        }
    }
}

// =============================================================================
// UNKNOWN (3 Variants)
// Geometric patterns using MastorCopper at 15% opacity
// =============================================================================
@Composable
fun UnknownIllustration(variant: Int, modifier: Modifier = Modifier) {
    val copperTint = MastorCopper.copy(alpha = 0.20f)
    val copperLine = MastorCopper.copy(alpha = 0.50f)

    ScaledCanvasIllustration(modifier = modifier) {
        when (variant % 3) {
            // Variant 0: Grid of construction crosses (+) arranged in a regular pattern
            0 -> {
                val stepX = 40f
                val stepY = 35f
                val crossSize = 7f

                var y = 25f
                while (y <= 180f) {
                    var x = 30f
                    while (x <= 370f) {
                        drawLine(copperLine, Offset(x - crossSize, y), Offset(x + crossSize, y), strokeWidth = 1.5f)
                        drawLine(copperLine, Offset(x, y - crossSize), Offset(x, y + crossSize), strokeWidth = 1.5f)
                        drawCircle(copperTint, radius = 2f, center = Offset(x, y))
                        x += stepX
                    }
                    y += stepY
                }
            }

            // Variant 1: Diagonal hatching pattern — construction drawing convention
            1 -> {
                val spacing = 22f
                var offset = -150f
                while (offset <= 500f) {
                    drawLine(
                        color = copperLine,
                        start = Offset(offset, 0f),
                        end = Offset(offset + 180f, 200f),
                        strokeWidth = 1.2f
                    )
                    offset += spacing
                }
                // Counter subtle horizontal guidelines
                for (gy in 30..170 step 35) {
                    drawLine(
                        color = copperTint,
                        start = Offset(0f, gy.toFloat()),
                        end = Offset(400f, gy.toFloat()),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                }
            }

            // Variant 2: Concentric rectangles — blueprint-style abstract
            else -> {
                val cx = 200f
                val cy = 100f
                val steps = listOf(
                    Pair(170f, 85f),
                    Pair(140f, 70f),
                    Pair(110f, 55f),
                    Pair(80f, 40f),
                    Pair(50f, 25f)
                )

                steps.forEachIndexed { index, (w, h) ->
                    val color = if (index % 2 == 0) copperLine else copperTint
                    drawRect(
                        color = color,
                        topLeft = Offset(cx - w, cy - h),
                        size = Size(w * 2f, h * 2f),
                        style = Stroke(
                            width = if (index == 0) 2f else 1.2f,
                            pathEffect = if (index % 2 == 1) PathEffect.dashPathEffect(floatArrayOf(8f, 6f)) else null
                        )
                    )
                }

                // Center crosshair lines
                drawLine(copperLine, Offset(cx - 30f, cy), Offset(cx + 30f, cy), strokeWidth = 1.5f)
                drawLine(copperLine, Offset(cx, cy - 20f), Offset(cx, cy + 20f), strokeWidth = 1.5f)
            }
        }
    }
}

// =============================================================================
// MastorProjectHeroCard
// Full width, 180dp height, 16dp corner radius, MastorCharcoal background
// =============================================================================
@Composable
fun MastorProjectHeroCard(
    projectId: String,
    projectName: String,
    projectType: String?,
    clientName: String,
    contractValue: Double,
    status: String,
    contractRef: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val illustrationComposable = ProjectIllustrationPicker.resolveIllustration(projectId, projectType)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MastorCharcoal)
            .clickable { onClick() }
            .testTag("job_card_$projectId")
    ) {
        // 1. Background Illustration filling full card
        Box(modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
        ) {
            illustrationComposable()
        }

        // 2. Bottom 60% Gradient Overlay (transparent -> charcoal at 60% opacity)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp) // 60% of 180dp
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MastorCharcoal.copy(alpha = 0.35f),
                            MastorCharcoal.copy(alpha = 0.60f)
                        )
                    )
                )
        )

        // 3. Foreground Content Layer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: [ CONTRACT_REF ] at top-left, Status Badge at top-right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BracketLabel(
                    text = contractRef.ifBlank { "PROJ-$projectId" },
                    color = MastorCreamMuted
                )

                MastorStatusBadge(status = status)
            }

            // Bottom Row: Project Details at bottom-left, Contract Value at bottom-right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Left Column: Project Name + Client Name
                Column(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = projectName,
                        style = MastorDisplayMedium.copy(fontSize = 20.sp, lineHeight = 24.sp),
                        color = MastorCreamText,
                        maxLines = 2, // maxLines=2 intentional — fixed-height hero card
                        overflow = TextOverflow.Clip
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = clientName,
                        style = MastorBody,
                        color = MastorCreamMuted,
                        maxLines = 2, // maxLines=2 intentional — fixed-height hero card
                        overflow = TextOverflow.Clip
                    )
                }

                // Right Column: Contract Value
                Text(
                    text = MastorCalculationEngine.formatCurrency(contractValue),
                    style = MastorFinancialMed,
                    color = MastorCopper,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}

/**
 * MastorDashboardHero
 * Full-bleed hero panel using the ProjectIllustration system for the project dashboard header.
 *
 * Full width, 220dp height:
 * - Background: MastorCharcoal
 * - Illustration: ProjectIllustrationPicker.resolveIllustration(project.id, project.workType) scaled to fill entire panel
 * - Bottom gradient overlay: transparent -> MastorCharcoal 70% opacity over bottom 65% (143dp)
 * - Top gradient overlay: MastorCharcoal 50% opacity -> transparent over top 40% (88dp)
 * - Top row:
 *     Left: back arrow icon button, MastorCreamText tint
 *     Centre: BracketLabel(project.contractRef, color = MastorCreamMuted)
 *     Right: MastorStatusBadge(project.status)
 * - Bottom section:
 *     Row 1: Project name in MastorDisplayMedium style, 22sp, MastorCreamText, max 2 lines, bold
 *     Row 2: Client name in MastorBody, MastorCreamMuted
 *     Row 3: Three metric pills in a horizontal row:
 *            [ £XXX,XXX CONTRACT ]
 *            [ XX% COMPLETE ]
 *            [ ACTIVE ] or current status
 */
@Composable
fun MastorDashboardHero(
    project: Project,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    percentComplete: Double = 0.0,
    contractSumOverride: Double? = null,
    onQuickDiary: (() -> Unit)? = null,
    onQuickVariation: (() -> Unit)? = null
) {
    val illustration = ProjectIllustrationPicker.resolveIllustration(
        projectId = project.id,
        projectType = project.workType
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(MastorCharcoal)
    ) {
        // 1. Vector Illustration backdrop scaled to fill entire panel
        Box(modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
        ) {
            illustration()
        }

        // 2. Top gradient overlay: MastorCharcoal 50% opacity over top 40% (88dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MastorCharcoal.copy(alpha = 0.50f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 3. Bottom gradient overlay: transparent -> MastorCharcoal 70% opacity over bottom 65% (143dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(143.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MastorCharcoal.copy(alpha = 0.35f),
                            MastorCharcoal.copy(alpha = 0.70f)
                        )
                    )
                )
        )

        // 4. Foreground Content Layer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Back arrow, BracketLabel in centre, MastorStatusBadge at right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MastorCreamText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                BracketLabel(
                    text = project.contractRef.ifBlank { "PROJ-${project.id}" },
                    color = MastorCreamMuted
                )

                MastorStatusBadge(status = project.status)
            }

            // Bottom Section:
            // Row 1: Project name in MastorDisplayMedium style, 22sp, MastorCreamText, max 2 lines, bold
            // Row 2: Client name in MastorBody, MastorCreamMuted
            // Row 3: Three metric pills: [ £XXX,XXX CONTRACT ], [ XX% COMPLETE ], [ ACTIVE ]
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = project.name,
                    style = MastorDisplayMedium.copy(
                        fontSize = 22.sp,
                        lineHeight = 26.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    color = MastorCreamText,
                    maxLines = 2, // maxLines=2 intentional — fixed-height hero card
                    overflow = TextOverflow.Clip
                )

                Text(
                    text = project.client.ifBlank { "Client Unspecified" },
                    style = MastorBody,
                    color = MastorCreamMuted,
                    maxLines = 2, // maxLines=2 intentional — fixed-height hero card
                    overflow = TextOverflow.Clip
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Row 3: Three metric pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pill 1: [ £XXX,XXX CONTRACT ]
                    val effectiveContractValue = contractSumOverride ?: project.contractValue
                    val formattedValue = MastorCalculationEngine.formatCurrency(effectiveContractValue)
                    DashboardMetricPill(text = "$formattedValue CONTRACT")

                    // Pill 2: [ XX% COMPLETE ]
                    val formattedPercent = "%.0f".format(percentComplete)
                    DashboardMetricPill(text = "$formattedPercent% COMPLETE")

                    // Pill 3: [ ACTIVE ] or current status
                    val statusText = project.status.ifBlank { "ACTIVE" }.uppercase()
                    DashboardMetricPill(text = statusText)
                }

                // Quick actions — glass buttons over the backdrop. Only shown when the caller
                // wants them here; the hero stays plain on screens that don't need them.
                if (onQuickDiary != null || onQuickVariation != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onQuickDiary != null) {
                            MastorGlassButton(
                                text = "Record Diary",
                                icon = Icons.Default.Mic,
                                onClick = onQuickDiary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (onQuickVariation != null) {
                            MastorGlassButton(
                                text = "Log Variation",
                                icon = Icons.Default.AddCircle,
                                onClick = onQuickVariation,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Metric Pill for MastorDashboardHero:
 * MastorCharcoal 60% opacity background, MastorCreamText, MastorBracketLabel typography,
 * 6dp radius, 8dp horizontal padding, 4dp vertical padding.
 */
@Composable
private fun DashboardMetricPill(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MastorCharcoal.copy(alpha = 0.60f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MastorBracketLabel,
            color = MastorCreamText
        )
    }
}

