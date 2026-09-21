package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MastorCopper
import com.example.ui.theme.MastorCream
import com.example.ui.theme.MastorCopper
import com.example.ui.theme.MastorCreamBorder
import com.example.ui.theme.MastorInk
import com.example.ui.theme.MastorInkMuted
import com.example.ui.theme.MastorInk
import com.example.ui.theme.MastorCreamDark
import com.example.ui.theme.StatusClaimedBg
import com.example.ui.theme.StatusClaimedGreen
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StatusPendingBg

/**
 * Banner and control component demonstrating Room Database Local Caching
 * for Site Diary Entries and Cloud File Lists in offline mode.
 */
@Composable
fun OfflineCacheStatusBar(
    isOfflineMode: Boolean,
    onToggleOfflineMode: () -> Unit,
    cachedEntriesCount: Int,
    cachedFilesCount: Int,
    pendingSyncCount: Int,
    onSyncPendingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_cache_status_bar"),
        shape = RoundedCornerShape(14.dp),
        color = if (isOfflineMode) Color(0xFFFFFBEB) else Color(0xFFF0FDF4),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isOfflineMode) Color(0xFFFDE68A) else Color(0xFFBBF7D0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOfflineMode) StatusPendingAmber.copy(alpha = 0.15f)
                                else StatusClaimedGreen.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isOfflineMode) Icons.Default.WifiOff else Icons.Default.Wifi,
                            contentDescription = if (isOfflineMode) "Offline" else "Online",
                            tint = if (isOfflineMode) StatusPendingAmber else StatusClaimedGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOfflineMode) "Offline Mode (Room Local Cache)" else "Online Mode (Cloud Connected)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOfflineMode) Color(0xFF92400E) else Color(0xFF166534)
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isOfflineMode) Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isOfflineMode) "ROOM DB ACTIVE" else "LIVE SYNC",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isOfflineMode) Color(0xFFB45309) else Color(0xFF15803D)
                                )
                            }
                        }
                        Text(
                            text = if (isOfflineMode)
                                "Reading diary entries & file lists directly from local SQLite database"
                            else
                                "All site diary entries & cloud file lists cached locally for offline use",
                            fontSize = 11.sp,
                            color = MastorInkMuted
                        )
                    }
                }

                // Interactive Switch to simulate offline testing
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Simulate Offline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MastorInk
                    )
                    Switch(
                        checked = isOfflineMode,
                        onCheckedChange = { onToggleOfflineMode() },
                        modifier = Modifier.testTag("offline_toggle_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StatusPendingAmber,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = MastorCreamBorder
                        )
                    )
                }
            }

            // Local cache statistics chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Diary Entries Cached Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MastorCreamBorder.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MastorCopper,
                            modifier = Modifier.size(14.dp)
                        )
                        Column {
                            Text(
                                text = "$cachedEntriesCount Diary Logs",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MastorInk
                            )
                            Text(
                                text = "Cached in Room",
                                fontSize = 9.sp,
                                color = MastorInkMuted
                            )
                        }
                    }
                }

                // Cloud Files Cached Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MastorCreamBorder.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = null,
                            tint = MastorCopper,
                            modifier = Modifier.size(14.dp)
                        )
                        Column {
                            Text(
                                text = "$cachedFilesCount Files Cached",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MastorInk
                            )
                            Text(
                                text = "OneDrive & G-Drive",
                                fontSize = 9.sp,
                                color = MastorInkMuted
                            )
                        }
                    }
                }

                // Pending sync indicator or sync button
                if (pendingSyncCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier
                            .clickable(enabled = !isOfflineMode) { onSyncPendingClick() }
                            .testTag("sync_pending_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(14.dp)
                            )
                            Column {
                                Text(
                                    text = "$pendingSyncCount Pending",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = if (isOfflineMode) "Saved to Room" else "Tap to Sync",
                                    fontSize = 9.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.8f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MastorCreamBorder.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusClaimedGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "100% Synced",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusClaimedGreen
                            )
                        }
                    }
                }
            }
        }
    }
}
