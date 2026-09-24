package com.example.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * MASTOR SIGNATURE: the drawing title block.
 *
 * Every construction drawing carries a title block — ruled cells, tiny labels, the key facts.
 * Mastor presents its headline commercial figures the same way. This is the one typographic
 * treatment used nowhere else in the app:
 *   - labels: 8sp, Inter Bold, very wide tracking, top-left of each cell
 *   - values: JetBrains Mono LIGHT at display size — thin, large numerals are the signature
 *   - hairline 1dp ruling between cells, no rounded corners (it's a drawing, not a card)
 *
 * Use sparingly — one title block per screen at most, for the figures that matter most.
 */

val TitleBlockLabel = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 8.sp,
    letterSpacing = 2.2.sp
)

val TitleBlockValue = TextStyle(
    fontFamily = JetBrainsMonoFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 22.sp,
    letterSpacing = (-0.5).sp
)

val TitleBlockHeadline = TextStyle(
    fontFamily = JetBrainsMonoFontFamily,
    fontWeight = FontWeight.Light,
    fontSize = 40.sp,
    letterSpacing = (-1.5).sp
)

data class TitleBlockCell(
    val label: String,
    val value: String,
    val valueColour: Color? = null
)

/**
 * @param headline the one figure that matters most — spans the full width, largest type.
 * @param rows pairs of cells beneath it.
 * @param sheetRef small reference printed top-right, like a drawing number (e.g. the job's ref).
 */
@Composable
fun MastorTitleBlock(
    headline: TitleBlockCell,
    rows: List<Pair<TitleBlockCell, TitleBlockCell>>,
    modifier: Modifier = Modifier,
    sheetRef: String = "",
    lineColour: Color = MastorCharcoalLight,
    labelColour: Color = MastorCreamMuted,
    valueColour: Color = MastorCreamText
) {
    Column(
        modifier = modifier.border(1.dp, lineColour)
    ) {
        // Headline cell
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(headline.label.uppercase(), style = TitleBlockLabel.copy(color = labelColour), modifier = Modifier.weight(1f))
                if (sheetRef.isNotBlank()) {
                    Text(sheetRef.uppercase(), style = TitleBlockLabel.copy(color = labelColour.copy(alpha = 0.6f), letterSpacing = 1.2.sp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(headline.value, style = TitleBlockHeadline.copy(color = headline.valueColour ?: MastorCopper))
        }
        rows.forEach { (left, right) ->
            HairlineH(lineColour)
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                TitleCell(left, labelColour, valueColour, Modifier.weight(1f))
                Box(modifier = Modifier.fillMaxHeight().width(1.dp).border(0.5.dp, lineColour))
                TitleCell(right, labelColour, valueColour, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TitleCell(cell: TitleBlockCell, labelColour: Color, valueColour: Color, modifier: Modifier) {
    Column(modifier = modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(cell.label.uppercase(), style = TitleBlockLabel.copy(color = labelColour))
        Spacer(modifier = Modifier.height(4.dp))
        Text(cell.value, style = TitleBlockValue.copy(color = cell.valueColour ?: valueColour))
    }
}

@Composable
private fun HairlineH(colour: Color) {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).border(0.5.dp, colour))
}
