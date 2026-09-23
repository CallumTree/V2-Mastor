package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A translucent "vellum" button — for use ONLY on the drawn illustration backdrops (project
 * hero cards, dashboard header). Reads as tracing paper laid over a technical drawing, not
 * smartphone glass — the theme throughout Mastor is a construction drawing, not a photo app,
 * so the see-through surface is paper-coloured (warm cream) with dark ink text, not a dark
 * tinted pane with light text.
 *
 * Not used on plain cream/charcoal screens — there's nothing underneath to see through there,
 * so a normal MastorPrimaryButton / MastorSecondaryButton applies instead.
 */
@Composable
fun MastorGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MastorCream.copy(alpha = 0.30f))
            .border(BorderStroke(1.dp, MastorCream.copy(alpha = 0.55f)), RoundedCornerShape(12.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MastorInk,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MastorTitle.copy(color = MastorInk, fontSize = 14.sp)
        )
    }
}
