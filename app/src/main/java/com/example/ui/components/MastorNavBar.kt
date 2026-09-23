package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.Phase2Tab
import com.example.ui.theme.BracketLabel
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.MastorBracketLabel
import com.example.ui.theme.MastorCharcoal
import com.example.ui.theme.MastorCharcoalLight
import com.example.ui.theme.MastorCharcoalMid
import com.example.ui.theme.MastorCopper
import com.example.ui.theme.MastorCreamBorder
import com.example.ui.theme.MastorCreamMuted
import com.example.ui.theme.MastorCreamText
import kotlinx.coroutines.launch

private data class MastorBottomBarTab(
    val tab: Phase2Tab?,
    val label: String,
    val drawIcon: @Composable (Modifier, androidx.compose.ui.graphics.Color) -> Unit,
    val isMore: Boolean = false
)

private data class MoreOptionItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/**
 * MastorBottomNavBar — Rebuilt from scratch using Mastor Design System v3 tokens.
 *
 * Structure:
 * - Background: MastorCharcoal
 * - Height: 64dp + WindowInsets.navigationBars bottom padding (safe area)
 * - Top border: 1dp, MastorCharcoalLight colour
 * - No elevation shadow — flat against the charcoal background
 * - 7 Project Tabs: Scope, Diary, Vals, VOs, BoQ, Setup, More
 * - Active state: MastorCopper icon + MastorCopper label + 2dp copper line at top
 * - Inactive state: MastorCreamMuted at 60% opacity
 * - Active indicator line slides between tabs using animateDpAsState with spring()
 * - More bottom sheet with: Procurement, Invoices, Excel Export, Documents
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MastorBottomNavBar(
    selectedTab: Phase2Tab,
    onTabSelected: (Phase2Tab) -> Unit,
    modifier: Modifier = Modifier,
    onOpenExcelExport: (() -> Unit)? = null
) {
    var showMoreSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val moreSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val tabs = remember {
        listOf(
            MastorBottomBarTab(tab = Phase2Tab.DASHBOARD, label = "Home", drawIcon = { m, c -> com.example.ui.icons.MastorHomeIcon(modifier = m, size = 22.dp, lineColor = c) }),
            MastorBottomBarTab(tab = Phase2Tab.SITE_DIARY, label = "Diary", drawIcon = { m, c -> com.example.ui.icons.MastorMicIcon(modifier = m, size = 22.dp, lineColor = c) }),
            MastorBottomBarTab(tab = Phase2Tab.SCOPE, label = "Scope", drawIcon = { m, c -> com.example.ui.icons.MastorScopeIcon(modifier = m, size = 22.dp, lineColor = c) }),
            MastorBottomBarTab(tab = Phase2Tab.VARIATIONS, label = "VOs", drawIcon = { m, c -> com.example.ui.icons.MastorMarkupIcon(modifier = m, size = 22.dp, lineColor = c) }),
            MastorBottomBarTab(tab = Phase2Tab.VALUATIONS, label = "Vals", drawIcon = { m, c -> com.example.ui.icons.MastorValuationIcon(modifier = m, size = 22.dp, lineColor = c) }),
            MastorBottomBarTab(tab = null, label = "More", drawIcon = { m, c -> Icon(imageVector = Icons.Default.MoreHoriz, contentDescription = "More", tint = c, modifier = m) }, isMore = true)
        )
    }

    val activeTabIndex = when (selectedTab) {
        Phase2Tab.DASHBOARD -> 0
        Phase2Tab.SITE_DIARY -> 1
        Phase2Tab.SCOPE -> 2
        Phase2Tab.VARIATIONS -> 3
        Phase2Tab.VALUATIONS -> 4
        Phase2Tab.BOQ_IMPORT,
        Phase2Tab.PROJECT_SETUP,
        Phase2Tab.PROCUREMENT,
        Phase2Tab.INVOICES,
        Phase2Tab.DOCUMENTS -> 5
        else -> -1
    }

    // Safe area handling: bottom padding ensures content sits above system gesture pill or 3-button nav
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Surface(
        color = MastorCharcoal,
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("mastor_bottom_nav_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = navBarBottomPadding)
        ) {
            // 1dp Top border, MastorCharcoalLight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MastorCharcoalLight)
            )

            // 64dp Tab container with equal share tab widths
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                val tabCount = tabs.size
                val tabWidth = maxWidth / tabCount

                // Animated active indicator line: 2dp height, MastorCopper, positioned at top edge
                val indicatorOffset by animateDpAsState(
                    targetValue = if (activeTabIndex >= 0) tabWidth * activeTabIndex else 0.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "nav_indicator_offset"
                )

                val indicatorAlpha by animateFloatAsState(
                    targetValue = if (activeTabIndex >= 0) 1f else 0f,
                    animationSpec = tween(durationMillis = 200),
                    label = "nav_indicator_alpha"
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, item ->
                        val isSelected = (activeTabIndex == index)

                        // Smooth fade animation between active and inactive opacity/color
                        val activeFraction by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0f,
                            animationSpec = tween(durationMillis = 200),
                            label = "tab_fade_${item.label}"
                        )

                        val iconColor = lerp(
                            start = MastorCreamMuted.copy(alpha = 0.60f),
                            stop = MastorCopper,
                            fraction = activeFraction
                        )
                        val textColor = lerp(
                            start = MastorCreamMuted.copy(alpha = 0.60f),
                            stop = MastorCopper,
                            fraction = activeFraction
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .heightIn(min = 48.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = MastorCopper.copy(alpha = 0.2f))
                                ) {
                                    if (item.isMore) {
                                        showMoreSheet = true
                                    } else if (item.tab != null) {
                                        onTabSelected(item.tab)
                                    }
                                }
                                .testTag("nav_tab_${item.label.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 2.dp, vertical = 6.dp)
                            ) {
                                item.drawIcon(Modifier.size(22.dp), iconColor)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.label.uppercase(),
                                    style = MastorBracketLabel.copy(
                                        fontSize = 9.sp,
                                        lineHeight = 11.sp,
                                        letterSpacing = 1.2.sp,
                                        color = textColor,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Active indicator line: 2dp height, MastorCopper, top edge of active tab, full tab width
                if (indicatorAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .offset(x = indicatorOffset, y = 0.dp)
                            .width(tabWidth)
                            .height(2.dp)
                            .graphicsLayer { alpha = indicatorAlpha }
                            .background(MastorCopper)
                    )
                }
            }
        }
    }

    // ModalBottomSheet for "More" options
    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = moreSheetState,
            containerColor = MastorCharcoalMid,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MastorCharcoalLight)
                )
            },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                // Header with BracketLabel("MORE OPTIONS")
                BracketLabel(
                    text = "MORE OPTIONS",
                    color = MastorCopper,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // MVP: Procurement and Documents hidden (code retained, not surfaced).
                val moreOptions = remember {
                    listOf(
                        MoreOptionItem(
                            label = "Import BoQ / Works Order",
                            icon = Icons.Default.CloudUpload,
                            onClick = { onTabSelected(Phase2Tab.BOQ_IMPORT) }
                        ),
                        MoreOptionItem(
                            label = "Invoices",
                            icon = Icons.Default.ReceiptLong,
                            onClick = { onTabSelected(Phase2Tab.INVOICES) }
                        ),
                        MoreOptionItem(
                            label = "Excel Export",
                            icon = Icons.Default.FileDownload,
                            onClick = {
                                if (onOpenExcelExport != null) {
                                    onOpenExcelExport()
                                } else {
                                    onTabSelected(Phase2Tab.DASHBOARD)
                                }
                            }
                        ),
                        MoreOptionItem(
                            label = "Job Setup & PO Number",
                            icon = Icons.Default.Settings,
                            onClick = { onTabSelected(Phase2Tab.PROJECT_SETUP) }
                        )
                    )
                }

                moreOptions.forEachIndexed { index, option ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MastorCreamBorder.copy(alpha = 0.35f),
                            thickness = 1.dp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch {
                                    moreSheetState.hide()
                                    showMoreSheet = false
                                    option.onClick()
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .testTag("more_option_${option.label.lowercase().replace(" ", "_")}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = option.label,
                            tint = MastorCopper,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = MastorCreamText,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MastorCreamMuted.copy(alpha = 0.45f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
