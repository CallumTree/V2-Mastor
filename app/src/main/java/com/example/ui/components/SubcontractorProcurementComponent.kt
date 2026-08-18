package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ProcurementPackage
import com.example.data.entity.ScopeElement
import com.example.data.entity.Subcontractor
import com.example.data.entity.SubcontractorClaim
import com.example.domain.procurement.TradePackageClassifier
import com.example.ui.components.MastorTopBar
import com.example.ui.theme.MastorAccentBlue
import com.example.ui.theme.MastorSlateDark
import com.example.ui.theme.MastorSlateMuted
import com.example.ui.theme.StatusClaimedGreen
import com.example.ui.theme.StatusFlaggedRed
import com.example.ui.theme.StatusPendingAmber
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val MastorNavy = MastorSlateDark
private val MastorGreen = StatusClaimedGreen
private val MastorGold = StatusPendingAmber
private val MastorRed = StatusFlaggedRed

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.UK)
    return format.format(amount)
}

private fun getTodayString(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.UK)
    return sdf.format(Date())
}

val TRADES_LIST = listOf(
    "Scaffolding",
    "Roofing",
    "Painting",
    "Fencing",
    "Electrical",
    "Plumbing",
    "Joinery",
    "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubcontractorProcurementComponent(
    projectId: String,
    packages: List<ProcurementPackage>,
    subcontractors: List<Subcontractor>,
    allScopeElements: List<ScopeElement>,
    allClaims: List<SubcontractorClaim>,
    onCreatePackage: (trade: String, scopeElementIds: List<String>, dateSent: String) -> Unit,
    onRecordQuote: (packageId: String, subcontractorId: String, quoteAmount: Double, quoteDate: String, notes: String) -> Unit,
    onAwardPackage: (packageId: String, subcontractorId: String?) -> Unit,
    onAddClaim: (packageId: String, claimAmount: Double, claimDate: String, notes: String) -> Unit,
    onUpdateStatus: (packageId: String, status: String) -> Unit,
    onDeletePackage: (packageId: String) -> Unit,
    onCreateSubcontractor: (companyName: String, contactName: String, phone: String, email: String, tradeSpecialism: String, notes: String) -> Unit,
    onDeleteSubcontractor: (id: String) -> Unit,
    onUpdateReviewStatus: (packageId: String, newReviewStatus: String) -> Unit = { _, _ -> },
    onAutoGeneratePackages: () -> Unit = {},
    onMoveScopeLine: (scopeId: String, sourcePackageId: String?, targetPackageId: String) -> Unit = { _, _, _ -> },
    onRemoveScopeLine: (scopeId: String, packageId: String) -> Unit = { _, _ -> },
    onUpdateScopeElement: (element: ScopeElement) -> Unit = {},
    onAddScopeLine: (packageId: String, woRef: String, locationRoom: String, code: String, description: String, qty: Double, units: String, rate: Double) -> Unit = { _, _, _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null
) {
    var selectedTradeFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedReviewFilter by remember { mutableStateOf("All") }

    // Dialog state
    var showBuildPackageDialog by remember { mutableStateOf(false) }
    var showSubcontractorDirectoryDialog by remember { mutableStateOf(false) }
    var viewingEnquiryPackage by remember { mutableStateOf<ProcurementPackage?>(null) }
    var editingPackageDetail by remember { mutableStateOf<ProcurementPackage?>(null) }
    var recordingQuoteForPackage by remember { mutableStateOf<ProcurementPackage?>(null) }
    var managingClaimsForPackage by remember { mutableStateOf<ProcurementPackage?>(null) }
    var showUnclassifiedSection by remember { mutableStateOf(true) }

    // Identify assigned scope element IDs
    val assignedScopeIds = remember(packages) {
        packages.flatMap { pkg ->
            pkg.scopeElementIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }.toSet()
    }

    // Unclassified scope lines pool
    val unclassifiedScopes = remember(allScopeElements, assignedScopeIds) {
        allScopeElements.filter { it.id !in assignedScopeIds }
    }

    // Filtered packages
    val filteredPackages = packages.filter { pkg ->
        val matchesTrade = selectedTradeFilter == "All" || pkg.trade.equals(selectedTradeFilter, ignoreCase = true)
        val matchesStatus = selectedStatusFilter == "All" || pkg.status.equals(selectedStatusFilter, ignoreCase = true)
        val matchesReview = selectedReviewFilter == "All" || pkg.reviewStatus.equals(selectedReviewFilter, ignoreCase = true)
        matchesTrade && matchesStatus && matchesReview
    }

    // Helper map of package -> allowed cost
    val packageAllowedCosts = packages.associateWith { pkg ->
        val linkedIds = pkg.scopeElementIds.split(",").map { it.trim() }
        val scopes = allScopeElements.filter { it.id in linkedIds }
        scopes.sumOf { it.qty * it.rate }
    }

    val totalAllowedBudget = packageAllowedCosts.values.sum()
    val totalQuoted = packages.mapNotNull { it.quoteAmount }.sum()
    val totalClaimedToDate = packages.sumOf { pkg ->
        allClaims.filter { it.packageId == pkg.id }.sumOf { it.claimAmount }
    }
    val totalVariance = totalAllowedBudget - totalQuoted

    // Review status metrics
    val countNotReviewed = packages.count { it.reviewStatus == "Not Reviewed" }
    val countReviewed = packages.count { it.reviewStatus == "Reviewed" }
    val countApproved = packages.count { it.reviewStatus == "Approved for Issue" }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // --- Standardized Mastor Top Bar ---
        MastorTopBar(
            title = "Subcontractor Procurement",
            subtitle = "Trade Package Spec • Subcontractor Bidding • Independent Claims Ledger",
            onMenuClick = onMenuClick
        ) {
            OutlinedButton(
                onClick = { showSubcontractorDirectoryDialog = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MastorNavy)
            ) {
                Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Subcontractors")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { showBuildPackageDialog = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Build Package")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {

        // --- Simplified Compact Procurement Cost Variance Summary ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PROCUREMENT VARIANCE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateMuted,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Independent Subcontractor Ledger",
                        style = MaterialTheme.typography.labelSmall,
                        color = MastorSlateMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Allowed", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                        Text(
                            text = formatCurrency(totalAllowedBudget),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorNavy
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Quoted", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                        Text(
                            text = if (totalQuoted > 0) formatCurrency(totalQuoted) else "—",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorNavy
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Claimed", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                        Text(
                            text = formatCurrency(totalClaimedToDate),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorNavy
                        )
                    }

                    Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.End) {
                        Text("Variance", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                        Text(
                            text = "${if (totalVariance >= 0) "+" else ""}${formatCurrency(totalVariance)} ${if (totalVariance >= 0) "Saving" else "Over"}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (totalVariance >= 0) MastorGreen else MastorRed
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- Package Review & Auto-Generation Bar ---
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Packages:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorNavy
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$countReviewed reviewed • $countNotReviewed pending • ${unclassifiedScopes.size} unassigned",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorSlateMuted
                    )
                }

                Button(
                    onClick = onAutoGeneratePackages,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp), tint = MastorGold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Auto-Generate", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Unclassified Scope Lines Section ---
        if (unclassifiedScopes.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MastorAccentBlue.copy(alpha = 0.06f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MastorAccentBlue.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showUnclassifiedSection = !showUnclassifiedSection },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MastorAccentBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Unclassified Scope Lines (${unclassifiedScopes.size} unassigned)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorNavy
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (showUnclassifiedSection) "Hide" else "Expand Pool",
                                style = MaterialTheme.typography.labelSmall,
                                color = MastorAccentBlue,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = if (showUnclassifiedSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MastorAccentBlue
                            )
                        }
                    }

                    AnimatedVisibility(visible = showUnclassifiedSection) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text(
                                text = "These scope lines have not been assigned to a trade package yet. Click 'Assign to Package' or use Auto-Generate above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorSlateMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier.height(180.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(unclassifiedScopes, key = { it.id }) { scope ->
                                    val suggestedTrade = remember(scope) {
                                        TradePackageClassifier.classifyScopeElement(scope)
                                    }

                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "[${scope.locationRoom}] ${scope.code}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MastorNavy
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = MastorAccentBlue.copy(alpha = 0.12f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "Suggested: $suggestedTrade",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MastorAccentBlue,
                                                            fontSize = 10.sp,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = scope.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MastorNavy,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "Qty: ${scope.qty} ${scope.units} • Allowed Rate: ${formatCurrency(scope.rate)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MastorSlateMuted
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // Quick Assign Dropdown Menu
                                            var showAssignMenu by remember { mutableStateOf(false) }
                                            Box {
                                                OutlinedButton(
                                                    onClick = { showAssignMenu = true },
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text("Assign to Trade", fontSize = 11.sp)
                                                    Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(14.dp))
                                                }

                                                DropdownMenu(
                                                    expanded = showAssignMenu,
                                                    onDismissRequest = { showAssignMenu = false }
                                                ) {
                                                    if (packages.isEmpty()) {
                                                        DropdownMenuItem(
                                                            text = { Text("No Packages Exist (Click Auto-Generate)") },
                                                            onClick = { showAssignMenu = false }
                                                        )
                                                    } else {
                                                        packages.forEach { pkg ->
                                                            DropdownMenuItem(
                                                                text = { Text("${pkg.packageRef} (${pkg.trade})") },
                                                                onClick = {
                                                                    onMoveScopeLine(scope.id, null, pkg.id)
                                                                    showAssignMenu = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // --- Filter Bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Trade Packages (${filteredPackages.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MastorNavy
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Trade Filter
                var showTradeDropdown by remember { mutableStateOf(false) }
                Box {
                    FilterChip(
                        selected = selectedTradeFilter != "All",
                        onClick = { showTradeDropdown = true },
                        label = { Text("Trade: $selectedTradeFilter", fontSize = 11.sp) },
                        trailingIcon = { Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MastorNavy, selectedLabelColor = Color.White)
                    )
                    DropdownMenu(
                        expanded = showTradeDropdown,
                        onDismissRequest = { showTradeDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Trades") },
                            onClick = {
                                selectedTradeFilter = "All"
                                showTradeDropdown = false
                            }
                        )
                        TRADES_LIST.forEach { trade ->
                            DropdownMenuItem(
                                text = { Text(trade) },
                                onClick = {
                                    selectedTradeFilter = trade
                                    showTradeDropdown = false
                                }
                            )
                        }
                    }
                }

                // Review Status Filter
                var showReviewDropdown by remember { mutableStateOf(false) }
                Box {
                    FilterChip(
                        selected = selectedReviewFilter != "All",
                        onClick = { showReviewDropdown = true },
                        label = { Text("Review: $selectedReviewFilter", fontSize = 11.sp) },
                        trailingIcon = { Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MastorNavy, selectedLabelColor = Color.White)
                    )
                    DropdownMenu(
                        expanded = showReviewDropdown,
                        onDismissRequest = { showReviewDropdown = false }
                    ) {
                        val reviewStatuses = listOf("All", "Not Reviewed", "Reviewed", "Approved for Issue")
                        reviewStatuses.forEach { rSt ->
                            DropdownMenuItem(
                                text = { Text(rSt) },
                                onClick = {
                                    selectedReviewFilter = rSt
                                    showReviewDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Package List ---
        if (filteredPackages.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = MastorSlateMuted, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No Procurement Packages Found", style = MaterialTheme.typography.titleMedium, color = MastorNavy)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Click 'Auto-Generate Packages' above to automatically parse scope lines into trade packages, or build one manually.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorSlateMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onAutoGeneratePackages,
                        colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = MastorGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Auto-Generate Trade Packages")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.height(520.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPackages, key = { it.id }) { pkg ->
                    val linkedIds = pkg.scopeElementIds.split(",").map { it.trim() }
                    val linkedScopes = allScopeElements.filter { it.id in linkedIds }
                    val allowedCost = linkedScopes.sumOf { it.qty * it.rate }
                    val sub = subcontractors.find { it.id == pkg.subcontractorId }
                    val claimsForPkg = allClaims.filter { it.packageId == pkg.id }
                    val totalClaimed = claimsForPkg.sumOf { it.claimAmount }

                    ProcurementPackageCard(
                        pkg = pkg,
                        subcontractor = sub,
                        linkedScopes = linkedScopes,
                        allowedCost = allowedCost,
                        totalClaimed = totalClaimed,
                        claimsCount = claimsForPkg.size,
                        onEditPackageDetail = { editingPackageDetail = pkg },
                        onViewEnquiry = { viewingEnquiryPackage = pkg },
                        onRecordQuote = { recordingQuoteForPackage = pkg },
                        onAward = { onAwardPackage(pkg.id, pkg.subcontractorId) },
                        onManageClaims = { managingClaimsForPackage = pkg },
                        onUpdateReviewStatus = { newStatus -> onUpdateReviewStatus(pkg.id, newStatus) },
                        onDelete = { onDeletePackage(pkg.id) }
                    )
                }
            }
        }
    }
    }

    // --- DIALOG: Build Package Dialog ---
    if (showBuildPackageDialog) {
        BuildPackageDialog(
            allScopeElements = allScopeElements,
            existingPackages = packages,
            onDismiss = { showBuildPackageDialog = false },
            onCreate = { trade, scopeElementIds, dateSent ->
                onCreatePackage(trade, scopeElementIds, dateSent)
                showBuildPackageDialog = false
            }
        )
    }

    // --- DIALOG: Full Package Spec & Line Editor Modal ---
    editingPackageDetail?.let { pkg ->
        val currentPkg = packages.find { it.id == pkg.id } ?: pkg
        PackageDetailEditorModal(
            pkg = currentPkg,
            allPackages = packages,
            allScopeElements = allScopeElements,
            subcontractors = subcontractors,
            onDismiss = { editingPackageDetail = null },
            onUpdateReviewStatus = { newRev -> onUpdateReviewStatus(currentPkg.id, newRev) },
            onMoveScopeLine = { scopeId, targetPkgId -> onMoveScopeLine(scopeId, currentPkg.id, targetPkgId) },
            onRemoveScopeLine = { scopeId -> onRemoveScopeLine(scopeId, currentPkg.id) },
            onUpdateScopeElement = onUpdateScopeElement,
            onAddScopeLine = { woRef, locRoom, code, desc, qty, units, rate ->
                onAddScopeLine(currentPkg.id, woRef, locRoom, code, desc, qty, units, rate)
            },
            onExportNoCost = { viewingEnquiryPackage = currentPkg }
        )
    }

    // --- DIALOG: No-Cost Enquiry Export Preview ---
    viewingEnquiryPackage?.let { pkg ->
        val linkedIds = pkg.scopeElementIds.split(",").map { it.trim() }
        val linkedScopes = allScopeElements.filter { it.id in linkedIds }
        val sub = subcontractors.find { it.id == pkg.subcontractorId }

        NoCostEnquiryExportDialog(
            pkg = pkg,
            subcontractor = sub,
            linkedScopes = linkedScopes,
            onDismiss = { viewingEnquiryPackage = null }
        )
    }

    // --- DIALOG: Record Quote Dialog ---
    recordingQuoteForPackage?.let { pkg ->
        RecordQuoteDialog(
            pkg = pkg,
            subcontractors = subcontractors,
            onDismiss = { recordingQuoteForPackage = null },
            onSubmit = { subId, amount, date, notes ->
                onRecordQuote(pkg.id, subId, amount, date, notes)
                recordingQuoteForPackage = null
            }
        )
    }

    // --- DIALOG: Manage Claims Log Dialog ---
    managingClaimsForPackage?.let { pkg ->
        val claimsForPkg = allClaims.filter { it.packageId == pkg.id }
        val sub = subcontractors.find { it.id == pkg.subcontractorId }

        ClaimsLogDialog(
            pkg = pkg,
            subcontractor = sub,
            claims = claimsForPkg,
            onDismiss = { managingClaimsForPackage = null },
            onAddClaim = { amount, date, notes ->
                onAddClaim(pkg.id, amount, date, notes)
            }
        )
    }

    // --- DIALOG: Subcontractors Directory Dialog ---
    if (showSubcontractorDirectoryDialog) {
        SubcontractorDirectoryDialog(
            subcontractors = subcontractors,
            onDismiss = { showSubcontractorDirectoryDialog = false },
            onCreateSubcontractor = onCreateSubcontractor,
            onDeleteSubcontractor = onDeleteSubcontractor
        )
    }
}

// -----------------------------------------------------------------------------
// PACKAGE CARD WITH REVIEW STATUS & LINE ACTIONS
// -----------------------------------------------------------------------------

@Composable
fun ProcurementPackageCard(
    pkg: ProcurementPackage,
    subcontractor: Subcontractor?,
    linkedScopes: List<ScopeElement>,
    allowedCost: Double,
    totalClaimed: Double,
    claimsCount: Int,
    onEditPackageDetail: () -> Unit,
    onViewEnquiry: () -> Unit,
    onRecordQuote: () -> Unit,
    onAward: () -> Unit,
    onManageClaims: () -> Unit,
    onUpdateReviewStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (pkg.status) {
        "Scope Sent" -> MastorAccentBlue
        "Quote Received" -> MastorGold
        "Awarded" -> MastorGreen
        "In Progress" -> MastorAccentBlue
        "Complete" -> MastorSlateDark
        "Declined" -> MastorRed
        else -> MastorSlateMuted
    }

    val (reviewBadgeText, reviewBadgeBg, reviewBadgeFg) = when (pkg.reviewStatus) {
        "Reviewed" -> Triple("Reviewed by QS", MastorGreen.copy(alpha = 0.15f), MastorGreen)
        "Approved for Issue" -> Triple("Approved for Issue", MastorAccentBlue.copy(alpha = 0.15f), MastorAccentBlue)
        else -> Triple("Not Reviewed", MastorGold.copy(alpha = 0.2f), MastorNavy)
    }

    val quotedAmount = pkg.quoteAmount
    val variance = if (quotedAmount != null) allowedCost - quotedAmount else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Ref, Trade, Review Badge, Status, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MastorNavy.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = pkg.packageRef,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MastorNavy,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = pkg.trade,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MastorNavy
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = reviewBadgeBg
                    ) {
                        Text(
                            text = reviewBadgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = reviewBadgeFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = pkg.status.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MastorSlateMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subcontractor & Date info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = MastorSlateMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = subcontractor?.companyName ?: "Unassigned / Tender Package",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (subcontractor != null) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (subcontractor != null) MastorNavy else MastorSlateMuted
                    )
                }

                Text(
                    text = "Sent: ${pkg.dateSent}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MastorSlateMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Cost Comparison Grid ---
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Allowed Cost", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                        Text(
                            text = formatCurrency(allowedCost),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MastorNavy
                        )
                    }

                    Column {
                        Text("Quoted", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                        Text(
                            text = if (quotedAmount != null) formatCurrency(quotedAmount) else "Awaiting Quote",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (quotedAmount != null) MastorNavy else MastorSlateMuted
                        )
                    }

                    Column {
                        Text("Claimed to Date", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                        Text(
                            text = formatCurrency(totalClaimed),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MastorNavy
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Cost Variance", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                        if (variance != null) {
                            Text(
                                text = "${if (variance >= 0) "+" else ""}${formatCurrency(variance)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (variance >= 0) MastorGreen else MastorRed
                            )
                        } else {
                            Text("—", style = MaterialTheme.typography.bodyMedium, color = MastorSlateMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scope preview toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${linkedScopes.size} Scope Elements Covered (Click to Preview)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MastorNavy
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MastorNavy
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    linkedScopes.forEach { sc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "• [${sc.locationRoom}] ${sc.code} - ${sc.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorNavy,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${sc.qty} ${sc.units}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorSlateMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary Action: Review & Edit Package Lines
                Button(
                    onClick = onEditPackageDetail,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Review & Edit Lines", fontSize = 12.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = onViewEnquiry) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("No-Cost Enquiry", fontSize = 12.sp)
                    }

                    if (pkg.status == "Scope Sent" || pkg.status == "Quote Received" || pkg.status == "Draft") {
                        OutlinedButton(
                            onClick = onRecordQuote,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(if (quotedAmount == null) "Record Quote" else "Update Quote", fontSize = 11.sp)
                        }
                    }

                    if (pkg.status == "Quote Received") {
                        Button(
                            onClick = onAward,
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MastorGreen)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Award Package", fontSize = 11.sp)
                        }
                    }

                    if (pkg.status == "Awarded" || pkg.status == "In Progress" || pkg.status == "Complete") {
                        Button(
                            onClick = onManageClaims,
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Claims ($claimsCount)", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DIALOG: FULL PACKAGE SPEC & LINE-BY-LINE EDITOR MODAL (§3 & §4)
// -----------------------------------------------------------------------------

@Composable
fun PackageDetailEditorModal(
    pkg: ProcurementPackage,
    allPackages: List<ProcurementPackage>,
    allScopeElements: List<ScopeElement>,
    subcontractors: List<Subcontractor>,
    onDismiss: () -> Unit,
    onUpdateReviewStatus: (String) -> Unit,
    onMoveScopeLine: (scopeId: String, targetPackageId: String) -> Unit,
    onRemoveScopeLine: (scopeId: String) -> Unit,
    onUpdateScopeElement: (ScopeElement) -> Unit,
    onAddScopeLine: (woRef: String, locationRoom: String, code: String, description: String, qty: Double, units: String, rate: Double) -> Unit,
    onExportNoCost: () -> Unit
) {
    val linkedIds = remember(pkg.scopeElementIds) {
        pkg.scopeElementIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    val linkedScopes = remember(allScopeElements, linkedIds) {
        allScopeElements.filter { it.id in linkedIds }
    }

    // Grouping scope elements by WO Ref & Location/Room (Section 2 layout)
    val groupedScopes = remember(linkedScopes) {
        linkedScopes.groupBy { Pair(it.woRef, it.locationRoom) }
    }

    var editingScopeLine by remember { mutableStateOf<ScopeElement?>(null) }
    var showAddLineDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Rule, contentDescription = null, tint = MastorNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Package Spec Editor: ${pkg.packageRef}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MastorNavy
                        )
                    }

                    // Review Status badge
                    Surface(
                        color = when (pkg.reviewStatus) {
                            "Reviewed" -> MastorGreen.copy(alpha = 0.15f)
                            "Approved for Issue" -> MastorAccentBlue.copy(alpha = 0.15f)
                            else -> MastorGold.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = pkg.reviewStatus,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (pkg.reviewStatus) {
                                "Reviewed" -> MastorGreen
                                "Approved for Issue" -> MastorAccentBlue
                                else -> MastorNavy
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text = "Trade: ${pkg.trade} • ${linkedScopes.size} Scope Lines • Total Allowed: ${formatCurrency(linkedScopes.sumOf { it.qty * it.rate })}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MastorSlateMuted
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Section 4 QS Review Workflow Control Bar
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QS Review Status:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorNavy
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (pkg.reviewStatus != "Reviewed") {
                                OutlinedButton(
                                    onClick = { onUpdateReviewStatus("Reviewed") },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark Reviewed", fontSize = 11.sp)
                                }
                            }

                            if (pkg.reviewStatus != "Approved for Issue") {
                                Button(
                                    onClick = { onUpdateReviewStatus("Approved for Issue") },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MastorAccentBlue)
                                ) {
                                    Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve for Issue", fontSize = 11.sp)
                                }
                            }

                            if (pkg.reviewStatus != "Not Reviewed") {
                                TextButton(onClick = { onUpdateReviewStatus("Not Reviewed") }) {
                                    Text("Reset", fontSize = 11.sp, color = MastorRed)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Package Scope Specifications (Grouped by Order/Room)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)

                    Button(
                        onClick = { showAddLineDialog = true },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Scope Line", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Table of Grouped Lines (Section 2 Specification)
                LazyColumn(
                    modifier = Modifier
                        .height(320.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (groupedScopes.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No lines in this package. Click 'Add Scope Line' or move lines from Unclassified Pool.", style = MaterialTheme.typography.bodySmall, color = MastorSlateMuted)
                            }
                        }
                    } else {
                        groupedScopes.forEach { (woAndRoom, scopeList) ->
                            item {
                                Surface(
                                    color = MastorNavy.copy(alpha = 0.06f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "WORK ORDER / PROPERTY: ${woAndRoom.first} — LOCATION / ROOM: ${woAndRoom.second}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MastorNavy,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            items(scopeList, key = { it.id }) { scope ->
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = scope.code,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MastorNavy
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = scope.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MastorNavy,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Action icons per line (Section 3 Editability)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Edit line icon
                                                IconButton(
                                                    onClick = { editingScopeLine = scope },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit Line", tint = MastorNavy, modifier = Modifier.size(16.dp))
                                                }

                                                // Move line icon
                                                var showMoveMenu by remember { mutableStateOf(false) }
                                                Box {
                                                    IconButton(
                                                        onClick = { showMoveMenu = true },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.DriveFileMove, contentDescription = "Move Line", tint = MastorAccentBlue, modifier = Modifier.size(16.dp))
                                                    }

                                                    DropdownMenu(
                                                        expanded = showMoveMenu,
                                                        onDismissRequest = { showMoveMenu = false }
                                                    ) {
                                                        Text("Move to Package:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                                                        allPackages.filter { it.id != pkg.id }.forEach { target ->
                                                            DropdownMenuItem(
                                                                text = { Text("${target.packageRef} (${target.trade})") },
                                                                onClick = {
                                                                    onMoveScopeLine(scope.id, target.id)
                                                                    showMoveMenu = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }

                                                // Remove line icon
                                                IconButton(
                                                    onClick = { onRemoveScopeLine(scope.id) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.RemoveCircle, contentDescription = "Remove Line", tint = MastorRed, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Measured Qty: ${scope.qty} ${scope.units}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MastorNavy
                                            )
                                            Text(
                                                text = "Allowed Office Rate: ${formatCurrency(scope.rate)} | Allowed Total: ${formatCurrency(scope.qty * scope.rate)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MastorSlateMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onExportNoCost,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export No-Cost Enquiry", fontSize = 12.sp)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)
                ) {
                    Text("Done")
                }
            }
        }
    )

    // Edit Scope Line Dialog
    editingScopeLine?.let { line ->
        var editDesc by remember { mutableStateOf(line.description) }
        var editQty by remember { mutableStateOf(line.qty.toString()) }
        var editUnits by remember { mutableStateOf(line.units) }
        var editLocRoom by remember { mutableStateOf(line.locationRoom) }
        var editRate by remember { mutableStateOf(line.rate.toString()) }

        AlertDialog(
            onDismissRequest = { editingScopeLine = null },
            title = { Text("Edit Package Scope Line (${line.code})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = editQty,
                            onValueChange = { editQty = it },
                            label = { Text("Quantity") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        OutlinedTextField(
                            value = editUnits,
                            onValueChange = { editUnits = it },
                            label = { Text("Units") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editLocRoom,
                        onValueChange = { editLocRoom = it },
                        label = { Text("Location / Room") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editRate,
                        onValueChange = { editRate = it },
                        label = { Text("Internal Rate (£)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedQty = editQty.toDoubleOrNull() ?: line.qty
                        val parsedRate = editRate.toDoubleOrNull() ?: line.rate
                        onUpdateScopeElement(
                            line.copy(
                                description = editDesc,
                                qty = parsedQty,
                                units = editUnits,
                                locationRoom = editLocRoom,
                                rate = parsedRate
                            )
                        )
                        editingScopeLine = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)
                ) {
                    Text("Save Line Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingScopeLine = null }) { Text("Cancel") }
            }
        )
    }

    // Add Scope Line Dialog
    if (showAddLineDialog) {
        var addWoRef by remember { mutableStateOf("WO-01") }
        var addLocRoom by remember { mutableStateOf("General") }
        var addCode by remember { mutableStateOf("SE-" + (100..999).random()) }
        var addDesc by remember { mutableStateOf("") }
        var addQty by remember { mutableStateOf("1.0") }
        var addUnits by remember { mutableStateOf("item") }
        var addRate by remember { mutableStateOf("0.0") }

        AlertDialog(
            onDismissRequest = { showAddLineDialog = false },
            title = { Text("Add Scope Line to ${pkg.packageRef}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = addDesc,
                        onValueChange = { addDesc = it },
                        label = { Text("Line Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = addQty,
                            onValueChange = { addQty = it },
                            label = { Text("Qty") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        OutlinedTextField(
                            value = addUnits,
                            onValueChange = { addUnits = it },
                            label = { Text("Units") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = addWoRef,
                            onValueChange = { addWoRef = it },
                            label = { Text("Work Order Ref") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        OutlinedTextField(
                            value = addLocRoom,
                            onValueChange = { addLocRoom = it },
                            label = { Text("Location/Room") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = addRate,
                        onValueChange = { addRate = it },
                        label = { Text("Internal Allowed Rate (£)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (addDesc.isNotBlank()) {
                            onAddScopeLine(
                                addWoRef,
                                addLocRoom,
                                addCode,
                                addDesc,
                                addQty.toDoubleOrNull() ?: 1.0,
                                addUnits,
                                addRate.toDoubleOrNull() ?: 0.0
                            )
                            showAddLineDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)
                ) {
                    Text("Add to Package")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLineDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// DIALOG: NO-COST ENQUIRY EXPORT PREVIEW (Strict Non-Negotiable Rule)
// -----------------------------------------------------------------------------

@Composable
fun NoCostEnquiryExportDialog(
    pkg: ProcurementPackage,
    subcontractor: Subcontractor?,
    linkedScopes: List<ScopeElement>,
    onDismiss: () -> Unit
) {
    // Grouping scope elements by WO Ref & Location/Room (Section 2 layout)
    val groupedScopes = remember(linkedScopes) {
        linkedScopes.groupBy { Pair(it.woRef, it.locationRoom) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null, tint = MastorNavy)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("No-Cost Enquiry Package Export", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MastorNavy)
                    Text("Tree & Sons Construction Internal Standard", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = MastorGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = MastorNavy, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OFFICE RATES STRICTLY EXCLUDED. Subcontractors price their own work from physical descriptions & quantities only.",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorNavy
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Document Header Preview Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("ENQUIRY REF: ${pkg.packageRef}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MastorNavy)
                        Text("Trade Specialism: ${pkg.trade}", style = MaterialTheme.typography.bodySmall, color = MastorNavy)
                        Text("Date Issued: ${pkg.dateSent}", style = MaterialTheme.typography.bodySmall, color = MastorSlateMuted)
                        if (subcontractor != null) {
                            Text("Recipient Subcontractor: ${subcontractor.companyName} (${subcontractor.contactName})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MastorNavy)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Scope Specifications Table (Structured by Order/Room)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                // Table Header
                Surface(
                    color = MastorNavy,
                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Code", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.weight(0.8f))
                        Text("Description", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.weight(2f))
                        Text("Qty/Unit", style = MaterialTheme.typography.labelSmall, color = Color.White, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        Text("Sub Rate", style = MaterialTheme.typography.labelSmall, color = Color.White, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .height(240.dp)
                        .border(1.dp, MastorNavy, RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                ) {
                    groupedScopes.forEach { (woAndRoom, scopeList) ->
                        item {
                            Surface(color = MastorNavy.copy(alpha = 0.08f), modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Order: ${woAndRoom.first} | Location: ${woAndRoom.second}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorNavy,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        items(scopeList) { sc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sc.code, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MastorNavy, modifier = Modifier.weight(0.8f))
                                Text(sc.description, style = MaterialTheme.typography.bodySmall, color = MastorNavy, modifier = Modifier.weight(2f))
                                Text("${sc.qty} ${sc.units}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                Text("_______", style = MaterialTheme.typography.bodySmall, color = MastorSlateMuted, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                            }
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)
            ) {
                Text("Close Enquiry Preview")
            }
        }
    )
}

// -----------------------------------------------------------------------------
// DIALOG: BUILD PACKAGE WORKFLOW WITH AUTO-MATCHING & NO-COST GUARANTEE
// -----------------------------------------------------------------------------

@Composable
fun BuildPackageDialog(
    allScopeElements: List<ScopeElement>,
    existingPackages: List<ProcurementPackage>,
    onDismiss: () -> Unit,
    onCreate: (trade: String, scopeElementIds: List<String>, dateSent: String) -> Unit
) {
    var selectedTrade by remember { mutableStateOf("Scaffolding") }
    val selectedScopeIds = remember { mutableStateOf(setOf<String>()) }
    var autoMatchedCount by remember { mutableStateOf(0) }
    var unclassifiedCount by remember { mutableStateOf(0) }

    fun runAutoMatch(trade: String) {
        val matched = mutableSetOf<String>()

        allScopeElements.forEach { sc ->
            val classified = TradePackageClassifier.classifyScopeElement(sc)
            if (classified.equals(trade, ignoreCase = true)) {
                matched.add(sc.id)
            }
        }

        selectedScopeIds.value = matched
        autoMatchedCount = matched.size
        unclassifiedCount = allScopeElements.size - matched.size
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Engineering, contentDescription = null, tint = MastorNavy)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Build Trade Procurement Package", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MastorNavy)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select a trade to auto-suggest relevant scope lines or manually check elements. Internal office rates will be excluded from subcontractor enquiry documents.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MastorSlateMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Trade Dropdown Selection
                Text("Package Trade Specialism", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                var expandedTradeMenu by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedTradeMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(selectedTrade, color = MastorNavy, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ExpandMore, contentDescription = null)
                        }
                    }
                    DropdownMenu(expanded = expandedTradeMenu, onDismissRequest = { expandedTradeMenu = false }) {
                        TRADES_LIST.forEach { tr ->
                            DropdownMenuItem(
                                text = { Text(tr) },
                                onClick = {
                                    selectedTrade = tr
                                    expandedTradeMenu = false
                                    runAutoMatch(tr)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Auto-Match Keywords Trigger Button
                Button(
                    onClick = { runAutoMatch(selectedTrade) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MastorNavy.copy(alpha = 0.1f), contentColor = MastorNavy)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp), tint = MastorGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto-Match Trade Keywords")
                }

                if (autoMatchedCount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = MastorGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Auto-selected $autoMatchedCount lines matching '$selectedTrade'. $unclassifiedCount lines unclassified for manual review.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MastorGreen,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Select Scope Lines (${selectedScopeIds.value.size} chosen)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                // Scope List
                LazyColumn(
                    modifier = Modifier
                        .height(240.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                        .padding(4.dp)
                ) {
                    items(allScopeElements, key = { it.id }) { sc ->
                        val isChecked = sc.id in selectedScopeIds.value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) {
                                        selectedScopeIds.value = selectedScopeIds.value - sc.id
                                    } else {
                                        selectedScopeIds.value = selectedScopeIds.value + sc.id
                                    }
                                }
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { check ->
                                    if (check == true) {
                                        selectedScopeIds.value = selectedScopeIds.value + sc.id
                                    } else {
                                        selectedScopeIds.value = selectedScopeIds.value - sc.id
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = MastorNavy)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "[${sc.locationRoom}] ${sc.code} - ${sc.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MastorNavy
                                )
                                Text(
                                    text = "Qty: ${sc.qty} ${sc.units}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MastorSlateMuted
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedScopeIds.value.isNotEmpty()) {
                        onCreate(selectedTrade, selectedScopeIds.value.toList(), getTodayString())
                    }
                },
                enabled = selectedScopeIds.value.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)
            ) {
                Text("Create Enquiry Package")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -----------------------------------------------------------------------------
// DIALOG: RECORD QUOTE
// -----------------------------------------------------------------------------

@Composable
fun RecordQuoteDialog(
    pkg: ProcurementPackage,
    subcontractors: List<Subcontractor>,
    onDismiss: () -> Unit,
    onSubmit: (subcontractorId: String, amount: Double, date: String, notes: String) -> Unit
) {
    var selectedSubId by remember { mutableStateOf(pkg.subcontractorId ?: subcontractors.firstOrNull()?.id ?: "") }
    var quoteAmountStr by remember { mutableStateOf(pkg.quoteAmount?.toString() ?: "") }
    var quoteDateStr by remember { mutableStateOf(pkg.quoteDate ?: getTodayString()) }
    var quoteNotes by remember { mutableStateOf(pkg.quoteNotes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Subcontractor Quote (${pkg.packageRef})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MastorNavy) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Select Subcontractor", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                var expandedSubMenu by remember { mutableStateOf(false) }
                val selectedSub = subcontractors.find { it.id == selectedSubId }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedSubMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(selectedSub?.companyName ?: "Select Subcontractor", color = MastorNavy)
                            Icon(Icons.Default.ExpandMore, contentDescription = null)
                        }
                    }
                    DropdownMenu(expanded = expandedSubMenu, onDismissRequest = { expandedSubMenu = false }) {
                        subcontractors.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text("${sub.companyName} (${sub.contactName})") },
                                onClick = {
                                    selectedSubId = sub.id
                                    expandedSubMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = quoteAmountStr,
                    onValueChange = { quoteAmountStr = it },
                    label = { Text("Lump Sum Quote Amount (£)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = quoteDateStr,
                    onValueChange = { quoteDateStr = it },
                    label = { Text("Date Received") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = quoteNotes,
                    onValueChange = { quoteNotes = it },
                    label = { Text("Notes / Exclusions / Qualifications") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = quoteAmountStr.toDoubleOrNull()
                    if (selectedSubId.isNotEmpty() && amount != null && amount > 0) {
                        onSubmit(selectedSubId, amount, quoteDateStr, quoteNotes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)
            ) {
                Text("Save Subcontractor Quote")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// -----------------------------------------------------------------------------
// DIALOG: CLAIMS LOG (§4 Independent Claims Ledger)
// -----------------------------------------------------------------------------

@Composable
fun ClaimsLogDialog(
    pkg: ProcurementPackage,
    subcontractor: Subcontractor?,
    claims: List<SubcontractorClaim>,
    onDismiss: () -> Unit,
    onAddClaim: (claimAmount: Double, claimDate: String, notes: String) -> Unit
) {
    var showAddClaimForm by remember { mutableStateOf(false) }
    var newClaimAmountStr by remember { mutableStateOf("") }
    var newClaimNotes by remember { mutableStateOf("") }

    val totalClaimed = claims.sumOf { it.claimAmount }
    val quotedAmount = pkg.quoteAmount ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = MastorNavy)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Subcontractor Claims Ledger (${pkg.packageRef})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MastorNavy)
                    Text(subcontractor?.companyName ?: "Subcontractor Package", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Claim Summary Banner
                Surface(
                    color = MastorNavy.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Quoted Value", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                            Text(formatCurrency(quotedAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MastorNavy)
                        }
                        Column {
                            Text("Total Certified", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                            Text(formatCurrency(totalClaimed), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MastorNavy)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Remaining Balance", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                            Text(formatCurrency((quotedAmount - totalClaimed).coerceAtLeast(0.0)), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MastorNavy)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!showAddClaimForm) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Certified Payment Applications (${claims.size})", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { showAddClaimForm = true },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Record Sub Claim", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .height(200.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                    ) {
                        items(claims) { cl ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Claim #${cl.claimNumber} • ${cl.claimDate}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MastorNavy)
                                    if (!cl.notes.isNullOrEmpty()) {
                                        Text(cl.notes, style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                                    }
                                }
                                Text(formatCurrency(cl.claimAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MastorNavy)
                            }
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Record New Subcontractor Payment Claim", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MastorNavy)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newClaimAmountStr,
                            onValueChange = { newClaimAmountStr = it },
                            label = { Text("Certified Claim Amount (£)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = newClaimNotes,
                            onValueChange = { newClaimNotes = it },
                            label = { Text("Notes / Stage Progress Details") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { showAddClaimForm = false }) { Text("Cancel") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val amount = newClaimAmountStr.toDoubleOrNull()
                                    if (amount != null && amount > 0) {
                                        onAddClaim(amount, getTodayString(), newClaimNotes)
                                        showAddClaimForm = false
                                        newClaimAmountStr = ""
                                        newClaimNotes = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)
                            ) {
                                Text("Submit Claim")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)) {
                Text("Close")
            }
        }
    )
}

// -----------------------------------------------------------------------------
// DIALOG: SUBCONTRACTORS DIRECTORY
// -----------------------------------------------------------------------------

@Composable
fun SubcontractorDirectoryDialog(
    subcontractors: List<Subcontractor>,
    onDismiss: () -> Unit,
    onCreateSubcontractor: (companyName: String, contactName: String, phone: String, email: String, tradeSpecialism: String, notes: String) -> Unit,
    onDeleteSubcontractor: (id: String) -> Unit
) {
    var showAddSubForm by remember { mutableStateOf(false) }
    var companyName by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var tradeSpecialism by remember { mutableStateOf("Scaffolding") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Business, contentDescription = null, tint = MastorNavy)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Subcontractor Directory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MastorNavy)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!showAddSubForm) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${subcontractors.size} Approved Subcontractors", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { showAddSubForm = true },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Subcontractor", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .height(260.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                    ) {
                        items(subcontractors) { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sub.companyName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MastorNavy)
                                    Text("Contact: ${sub.contactName} • Specialism: ${sub.tradeSpecialism}", style = MaterialTheme.typography.labelSmall, color = MastorNavy)
                                    Text("Tel: ${sub.phone} | ${sub.email}", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                                }
                                IconButton(onClick = { onDeleteSubcontractor(sub.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MastorSlateMuted, modifier = Modifier.size(18.dp))
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Add Approved Subcontractor", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MastorNavy)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Company Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = contactName,
                            onValueChange = { contactName = it },
                            label = { Text("Contact Person") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Trade Specialism", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        var expandedTradeDropdown by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedTradeDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(tradeSpecialism, color = MastorNavy)
                                    Icon(Icons.Default.ExpandMore, contentDescription = null)
                                }
                            }
                            DropdownMenu(expanded = expandedTradeDropdown, onDismissRequest = { expandedTradeDropdown = false }) {
                                TRADES_LIST.forEach { tr ->
                                    DropdownMenuItem(
                                        text = { Text(tr) },
                                        onClick = {
                                            tradeSpecialism = tr
                                            expandedTradeDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { showAddSubForm = false }) { Text("Cancel") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (companyName.isNotBlank() && contactName.isNotBlank()) {
                                        onCreateSubcontractor(companyName, contactName, phone, email, tradeSpecialism, notes)
                                        showAddSubForm = false
                                        companyName = ""
                                        contactName = ""
                                        phone = ""
                                        email = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)
                            ) {
                                Text("Save Subcontractor")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = MastorNavy)) {
                Text("Close Directory")
            }
        }
    )
}
