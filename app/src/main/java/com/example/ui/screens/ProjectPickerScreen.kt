package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.entity.Project
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.components.MastorIcon
import com.example.ui.components.MastorTopBar
import com.example.ui.components.MastorWordmark
import com.example.ui.theme.MastorAccentBlue
import com.example.ui.theme.MastorBackgroundLight
import com.example.ui.theme.MastorSlateBorder
import com.example.ui.theme.MastorSlateDark
import com.example.ui.theme.MastorSlateMuted
import com.example.ui.theme.MastorSurfaceLight
import com.example.ui.theme.StatusClaimedGreen

data class PropertyImagePreset(
    val title: String,
    val url: String
)

val DEFAULT_PROPERTY_PRESETS = listOf(
    PropertyImagePreset(
        "Townhouse Refurb",
        "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?q=80&w=800&auto=format&fit=crop"
    ),
    PropertyImagePreset(
        "Commercial Skyscraper",
        "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?q=80&w=800&auto=format&fit=crop"
    ),
    PropertyImagePreset(
        "Luxury Extension",
        "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?q=80&w=800&auto=format&fit=crop"
    ),
    PropertyImagePreset(
        "Modern Office Fitout",
        "https://images.unsplash.com/photo-1497366216548-37526070297c?q=80&w=800&auto=format&fit=crop"
    ),
    PropertyImagePreset(
        "Civil Infrastructure",
        "https://images.unsplash.com/photo-1541888946425-d0fbb186a5b3?q=80&w=800&auto=format&fit=crop"
    )
)

@Composable
fun ProjectPickerScreen(
    projects: List<Project>,
    onSelectProject: (String) -> Unit,
    onCreateProject: ((
        name: String,
        client: String,
        contractRef: String,
        address: String,
        siteManager: String,
        surveyor: String,
        contractValue: Double,
        workType: String,
        imageUrl: String,
        uplift1: Double,
        uplift2: Double
    ) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showNewJobDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MastorBackgroundLight,
        topBar = {
            MastorTopBar(
                title = "Project Directory",
                subtitle = "Select an existing job directory or create a new project",
                onMenuClick = null
            ) {
                Surface(
                    color = MastorAccentBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = "${projects.size} Active Jobs",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorAccentBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        floatingActionButton = {
            if (onCreateProject != null) {
                ExtendedFloatingActionButton(
                    onClick = { showNewJobDialog = true },
                    containerColor = MastorAccentBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = "New Job") },
                    text = { Text("New Job", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("add_new_job_fab")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (projects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = MastorSlateMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No active jobs found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MastorSlateDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ New Job' below to create your first construction project.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MastorSlateMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(projects, key = { it.id }) { proj ->
                        ProjectPickerCard(
                            project = proj,
                            onClick = { onSelectProject(proj.id) }
                        )
                    }
                }
            }
        }

        if (showNewJobDialog && onCreateProject != null) {
            NewJobDialog(
                onDismiss = { showNewJobDialog = false },
                onCreate = { name, client, contractRef, address, siteManager, surveyor, contractValue, workType, imageUrl, uplift1, uplift2 ->
                    showNewJobDialog = false
                    onCreateProject(name, client, contractRef, address, siteManager, surveyor, contractValue, workType, imageUrl, uplift1, uplift2)
                }
            )
        }
    }
}

@Composable
fun ProjectPickerCard(
    project: Project,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fallbackBg = DEFAULT_PROPERTY_PRESETS.first().url
    val effectiveImageUrl = project.imageUrl.ifBlank { fallbackBg }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("job_card_${project.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
        border = BorderStroke(1.dp, MastorSlateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Property Picture Snapshot Header with Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(MastorAccentBlue.copy(alpha = 0.8f), MastorSlateDark)
                        )
                    )
            ) {
                AsyncImage(
                    model = effectiveImageUrl,
                    contentDescription = "Property Photo ${project.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark Gradient Gradient Overlay for maximum text contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                // Top Header Badges: Contract Ref & Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MastorAccentBlue.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = project.contractRef,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = StatusClaimedGreen.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = project.status.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Property Title Floating on Image Snapshot
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = project.workType.ifBlank { "Construction Work" },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp
                    )
                }
            }

            // Card Body Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = MastorSlateMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Client: ${project.client}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MastorSlateDark
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MastorSlateMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = project.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorSlateMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MastorSlateMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Site: ${project.siteManager}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MastorSlateMuted
                        )
                    }

                    Text(
                        text = "QS: ${project.surveyor}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MastorSlateMuted
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Financial Summary Bar
                Surface(
                    color = MastorBackgroundLight,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MastorSlateBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CONTRACT VALUE",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MastorSlateMuted
                            )
                            Text(
                                text = MastorCalculationEngine.formatCurrency(project.contractValue),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MastorSlateDark
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CENTRAL MARKUPS",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MastorSlateMuted
                            )
                            Text(
                                text = "+${project.uplift1Percent.toInt()}% / +${project.uplift2Percent.toInt()}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorAccentBlue
                            )
                        }

                        Surface(
                            color = MastorAccentBlue,
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open Job",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewJobDialog(
    onDismiss: () -> Unit,
    onCreate: (
        name: String,
        client: String,
        contractRef: String,
        address: String,
        siteManager: String,
        surveyor: String,
        contractValue: Double,
        workType: String,
        imageUrl: String,
        uplift1: Double,
        uplift2: Double
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var contractRef by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var siteManager by remember { mutableStateOf("Dave Jenkins") }
    var surveyor by remember { mutableStateOf("Eleanor Vance") }
    var contractValueStr by remember { mutableStateOf("350000") }
    var workType by remember { mutableStateOf("Commercial Fitout") }
    var selectedPresetUrl by remember { mutableStateOf(DEFAULT_PROPERTY_PRESETS.first().url) }
    var customImageUrl by remember { mutableStateOf("") }
    var uplift1Str by remember { mutableStateOf("15.0") }
    var uplift2Str by remember { mutableStateOf("5.0") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = MastorSurfaceLight,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NEW CONSTRUCTION JOB",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorAccentBlue,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Create Project Directory",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MastorSlateDark
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MastorSlateMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Property Snapshot Image Selector
                Text(
                    text = "Property Picture Snapshot",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MastorSlateDark
                )
                Text(
                    text = "Select a property visual style for the job card & banner:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MastorSlateMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DEFAULT_PROPERTY_PRESETS) { preset ->
                        val isSelected = selectedPresetUrl == preset.url && customImageUrl.isBlank()
                        Surface(
                            modifier = Modifier
                                .width(120.dp)
                                .height(80.dp)
                                .clickable {
                                    selectedPresetUrl = preset.url
                                    customImageUrl = ""
                                },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MastorAccentBlue else MastorSlateBorder
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = preset.url,
                                    contentDescription = preset.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f))
                                )
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(6.dp)
                                )
                                if (isSelected) {
                                    Surface(
                                        color = MastorAccentBlue,
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = customImageUrl,
                    onValueChange = { customImageUrl = it },
                    label = { Text("Or Custom Property Image URL") },
                    placeholder = { Text("https://example.com/property.jpg") },
                    leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = MastorSlateMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorAccentBlue,
                        unfocusedBorderColor = MastorSlateBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project / Job Name *") },
                    placeholder = { Text("e.g. Canary Wharf Office Refurbishment") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = MastorSlateMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorAccentBlue,
                        unfocusedBorderColor = MastorSlateBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = client,
                        onValueChange = { client = it },
                        label = { Text("Client Name *") },
                        placeholder = { Text("e.g. Canary Group") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = MastorSlateMuted) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorAccentBlue,
                            unfocusedBorderColor = MastorSlateBorder
                        )
                    )

                    OutlinedTextField(
                        value = contractRef,
                        onValueChange = { contractRef = it },
                        label = { Text("Contract Ref *") },
                        placeholder = { Text("e.g. CWG-2026") },
                        leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null, tint = MastorSlateMuted) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorAccentBlue,
                            unfocusedBorderColor = MastorSlateBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Site Address") },
                    placeholder = { Text("10 Upper Bank Street, London E14 5JJ") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MastorSlateMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorAccentBlue,
                        unfocusedBorderColor = MastorSlateBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = contractValueStr,
                        onValueChange = { contractValueStr = it },
                        label = { Text("Contract Value (£)") },
                        leadingIcon = { Text("£", fontWeight = FontWeight.Bold, color = MastorSlateMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorAccentBlue,
                            unfocusedBorderColor = MastorSlateBorder
                        )
                    )

                    OutlinedTextField(
                        value = workType,
                        onValueChange = { workType = it },
                        label = { Text("Work Type") },
                        placeholder = { Text("Fitout / Internal / Extension") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorAccentBlue,
                            unfocusedBorderColor = MastorSlateBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = siteManager,
                        onValueChange = { siteManager = it },
                        label = { Text("Site Manager") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorAccentBlue,
                            unfocusedBorderColor = MastorSlateBorder
                        )
                    )

                    OutlinedTextField(
                        value = surveyor,
                        onValueChange = { surveyor = it },
                        label = { Text("Quantity Surveyor") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorAccentBlue,
                            unfocusedBorderColor = MastorSlateBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = uplift1Str,
                        onValueChange = { uplift1Str = it },
                        label = { Text("Uplift 1 (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorAccentBlue,
                            unfocusedBorderColor = MastorSlateBorder
                        )
                    )

                    OutlinedTextField(
                        value = uplift2Str,
                        onValueChange = { uplift2Str = it },
                        label = { Text("Uplift 2 (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorAccentBlue,
                            unfocusedBorderColor = MastorSlateBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MastorSlateMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val valDbl = contractValueStr.toDoubleOrNull() ?: 250000.0
                            val up1 = uplift1Str.toDoubleOrNull() ?: 15.0
                            val up2 = uplift2Str.toDoubleOrNull() ?: 5.0
                            val finalImgUrl = customImageUrl.ifBlank { selectedPresetUrl }

                            onCreate(
                                name,
                                client,
                                contractRef,
                                address,
                                siteManager,
                                surveyor,
                                valDbl,
                                workType,
                                finalImgUrl,
                                up1,
                                up2
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MastorAccentBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_new_job_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Job", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
