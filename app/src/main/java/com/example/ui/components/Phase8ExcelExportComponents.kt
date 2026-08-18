package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.VariationOrder
import com.example.domain.calculation.CalculatedValuation
import com.example.domain.calculation.CalculatedWorkOrder
import com.example.domain.excel.ExcelExportEngine
import com.example.domain.excel.ExcelSheetPreview
import com.example.domain.excel.ExcelWorkbookSnapshot
import com.example.ui.theme.MastorAccentBlue
import com.example.ui.theme.MastorBackgroundLight
import com.example.ui.theme.MastorSlateBorder
import com.example.ui.theme.MastorSlateDark
import com.example.ui.theme.MastorSlateMuted
import com.example.ui.theme.MastorSurfaceLight
import com.example.ui.theme.StatusClaimedBg
import com.example.ui.theme.StatusClaimedGreen
import kotlinx.coroutines.launch

// Excel Branding Accent
val ExcelGreenBrand = Color(0xFF107C41)

/**
 * Phase 8: Excel Export Action Button
 */
@Composable
fun ExportToExcelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Export Excel"
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ExcelGreenBrand.copy(alpha = 0.6f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExcelGreenBrand),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = modifier.testTag("export_to_excel_btn")
    ) {
        Icon(
            imageVector = Icons.Default.GridOn,
            contentDescription = "Export Excel",
            modifier = Modifier.size(14.dp),
            tint = ExcelGreenBrand
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = ExcelGreenBrand
        )
    }
}

/**
 * Phase 8: Confirmation & Preview Modal before generating live snapshot workbook.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcelExportConfirmationModal(
    project: Project,
    calculatedWorkOrders: List<CalculatedWorkOrder>,
    scopeElements: List<ScopeElement>,
    variationOrders: List<VariationOrder>,
    valuation: CalculatedValuation?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var isExportDone by remember { mutableStateOf(false) }

    val snapshot = remember(project, calculatedWorkOrders, scopeElements, variationOrders, valuation) {
        ExcelExportEngine.generateLiveWorkbookSnapshot(
            project = project,
            calculatedWorkOrders = calculatedWorkOrders,
            scopeElements = scopeElements,
            variationOrders = variationOrders,
            valuation = valuation
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MastorSurfaceLight,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Modal Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PHASE 8: EXCEL EXPORT ENGINE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ExcelGreenBrand,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Export V6 Workbook",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ExcelGreenBrand.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = null,
                            tint = ExcelGreenBrand,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "V6 Format",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ExcelGreenBrand
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Snapshot Notice
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MastorBackgroundLight,
                border = BorderStroke(1.dp, MastorSlateBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MastorAccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Live Calculation Snapshot",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorSlateDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "This will generate an un-cached V6 Excel workbook as of right now (${snapshot.timestampDisplay}). It is a one-way export intended for clients, architects, and quantity surveyors.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorSlateMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Workbook Sheets Included (7 Tabs):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MastorSlateDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            // List of sheets to export
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(snapshot.sheets) { sheet ->
                    ExcelSheetPreviewCard(sheet = sheet)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isExportDone) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = StatusClaimedBg,
                    border = BorderStroke(1.dp, StatusClaimedGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusClaimedGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Excel Workbook Generated Successfully (${snapshot.projectName}_V6_Export.xml)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusClaimedGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        isExportDone = true
                        shareOrSaveWorkbook(context, snapshot)
                        Toast.makeText(
                            context,
                            "Exported V6 Workbook for ${project.name}",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExcelGreenBrand),
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("confirm_export_excel_modal_btn")
                ) {
                    Icon(
                        imageVector = if (isExportDone) Icons.Default.Share else Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isExportDone) "Share / Save Workbook" else "Generate Live Export")
                }
            }
        }
    }
}

@Composable
private fun ExcelSheetPreviewCard(sheet: ExcelSheetPreview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
        border = BorderStroke(1.dp, MastorSlateBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (sheet.hideRatesAndCosts) Icons.Default.VisibilityOff else Icons.Default.TableChart,
                        contentDescription = null,
                        tint = if (sheet.hideRatesAndCosts) Color(0xFFD97706) else ExcelGreenBrand,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = sheet.sheetName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateDark
                    )
                }

                if (sheet.hideRatesAndCosts) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "RATES HIDDEN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        text = "${sheet.rowCount} Rows",
                        style = MaterialTheme.typography.labelMedium,
                        color = MastorSlateMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = sheet.description,
                style = MaterialTheme.typography.bodySmall,
                color = MastorSlateMuted
            )

            if (sheet.highlights.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MastorBackgroundLight, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    sheet.highlights.forEach { (label, value) ->
                        Text(
                            text = "$label: $value",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MastorSlateDark
                        )
                    }
                }
            }
        }
    }
}

private fun shareOrSaveWorkbook(context: Context, snapshot: ExcelWorkbookSnapshot) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Mastor V6 Excel Export Live Snapshot for ${snapshot.projectName}:\n\n${snapshot.fullXmlWorkbookContent.take(1000)}...\n\n(Full V6 Workbook Generated)")
        putExtra(Intent.EXTRA_TITLE, "${snapshot.projectName}_V6_Workbook.xml")
        type = "text/xml"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Save or Send V6 Excel Workbook")
    context.startActivity(shareIntent)
}
