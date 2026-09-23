package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MastorCopper
import com.example.ui.theme.MastorCream
import com.example.ui.theme.MastorCreamBorder
import com.example.ui.theme.MastorInk
import com.example.ui.theme.MastorInkMuted
import com.example.ui.theme.MastorCreamDark

data class MastorTabItem(
    val label: String,
    val icon: ImageVector? = null,
    val badgeCount: Int? = null
)

/**
 * Reusable segmented tab control with consistent styling across the application.
 * Uses MastorCopper for selected state and clean low-contrast borders.
 */
@Composable
fun MastorSegmentedTabs(
    tabs: List<MastorTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MastorCream,
        border = BorderStroke(1.dp, MastorCreamBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex

                val animatedBgColor by animateColorAsState(
                    targetValue = if (isSelected) MastorCopper else Color.Transparent,
                    animationSpec = tween(durationMillis = 180),
                    label = "tab_bg_$index"
                )

                val animatedContentColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MastorInk,
                    animationSpec = tween(durationMillis = 180),
                    label = "tab_content_$index"
                )

                val tabTag = "tab_${tab.label.lowercase().replace(" ", "_").replace("&", "and")}"

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTabSelected(index) }
                        .testTag(tabTag),
                    shape = RoundedCornerShape(8.dp),
                    color = animatedBgColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (tab.icon != null) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = animatedContentColor,
                                modifier = Modifier.size(15.dp)
                            )
                            if (tab.label.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                        }

                        if (tab.label.isNotEmpty()) {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = animatedContentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (tab.badgeCount != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) Color.White.copy(alpha = 0.25f) else MastorCreamBorder
                            ) {
                                Text(
                                    text = tab.badgeCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MastorInk,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Horizontally scrollable segmented tab control for views with multiple tabs or longer labels,
 * ensuring no text truncation occurs on smaller viewports.
 */
@Composable
fun MastorScrollableSegmentedTabs(
    tabs: List<MastorTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MastorCream,
        border = BorderStroke(1.dp, MastorCreamBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex

                val animatedBgColor by animateColorAsState(
                    targetValue = if (isSelected) MastorCopper else Color.Transparent,
                    animationSpec = tween(durationMillis = 180),
                    label = "tab_scroll_bg_$index"
                )

                val animatedContentColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MastorInk,
                    animationSpec = tween(durationMillis = 180),
                    label = "tab_scroll_content_$index"
                )

                val tabTag = "tab_${tab.label.lowercase().replace(" ", "_").replace("&", "and")}"

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTabSelected(index) }
                        .testTag(tabTag),
                    shape = RoundedCornerShape(8.dp),
                    color = animatedBgColor,
                    border = if (!isSelected) BorderStroke(1.dp, MastorCreamBorder.copy(alpha = 0.5f)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (tab.icon != null) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = animatedContentColor,
                                modifier = Modifier.size(15.dp)
                            )
                            if (tab.label.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                        }

                        if (tab.label.isNotEmpty()) {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = animatedContentColor,
                                maxLines = 1
                            )
                        }

                        if (tab.badgeCount != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) Color.White.copy(alpha = 0.25f) else MastorCreamBorder
                            ) {
                                Text(
                                    text = tab.badgeCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MastorInk,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

