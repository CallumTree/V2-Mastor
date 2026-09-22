package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.entity.Project
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.components.MastorIcon
import com.example.ui.components.MastorTopBar
import com.example.ui.components.MastorWordmark
import com.example.ui.illustrations.MastorProjectHeroCard
import com.example.ui.theme.*
import java.io.File

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
        containerColor = MastorCream,
        topBar = {
            MastorTopBar(
                title = "Project Directory",
                subtitle = "Select an existing job directory or create a new project",
                onMenuClick = null
            ) {
                Surface(
                    color = MastorCopper.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = "${projects.size} Active Jobs",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorCopper,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        floatingActionButton = {
            if (onCreateProject != null) {
                ExtendedFloatingActionButton(
                    onClick = { showNewJobDialog = true },
                    containerColor = MastorCopper,
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
                            tint = MastorInkMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No active jobs found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MastorInk
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ New Job' below to create your first construction project.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MastorInkMuted
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
                        MastorProjectHeroCard(
                            projectId = proj.id,
                            projectName = proj.name,
                            projectType = proj.workType,
                            clientName = proj.client,
                            contractValue = proj.contractValue,
                            status = proj.status,
                            contractRef = proj.contractRef,
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
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var contractRef by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var siteManager by remember { mutableStateOf("Dave Jenkins") }
    var surveyor by remember { mutableStateOf("Eleanor Vance") }
    var contractValueStr by remember { mutableStateOf("350000") }
    var workType by remember { mutableStateOf("Commercial Fitout") }
    var siteImageUriString by remember { mutableStateOf("") }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var uplift1Str by remember { mutableStateOf("15.0") }
    var uplift2Str by remember { mutableStateOf("5.0") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            uploadError = null
            siteImageUriString = copyUriToInternalStorage(context, uri)
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            uploadError = null
            siteImageUriString = tempCameraUri.toString()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            uploadError = null
            launchCameraForSite(context, { tempCameraUri = it }, takePictureLauncher, { uploadError = it })
        } else {
            uploadError = "Camera permission is required to capture site photo."
        }
    }

    fun openCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            uploadError = null
            launchCameraForSite(context, { tempCameraUri = it }, takePictureLauncher, { uploadError = it })
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = MastorCharcoalMid,
            border = BorderStroke(1.dp, MastorCharcoalLight),
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
                        BracketLabel(
                            text = "NEW CONSTRUCTION JOB",
                            color = MastorCopper
                        )
                        Text(
                            text = "Create Project Directory",
                            style = MastorHeadline.copy(color = MastorCreamText)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MastorCreamMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Site Picture Upload Area
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Site Picture",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorInk
                        )
                        Text(
                            text = "Upload a photo of the project or site location",
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorInkMuted
                        )
                    }
                    if (siteImageUriString.isNotBlank()) {
                        Surface(
                            color = StatusClaimedBg,
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(1.dp, StatusClaimedGreen.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = StatusClaimedGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PHOTO READY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusClaimedGreen
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (uploadError != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = uploadError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (siteImageUriString.isBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("upload_site_picture_area"),
                        shape = RoundedCornerShape(14.dp),
                        color = MastorCopper.copy(alpha = 0.03f),
                        border = BorderStroke(1.5.dp, MastorCopper.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                color = MastorCopper.copy(alpha = 0.12f),
                                shape = CircleShape,
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = "Upload site picture",
                                        tint = MastorCopper,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Upload Site Picture",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MastorInk
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Tap anywhere to select photo from device gallery or camera",
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorInkMuted
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MastorCopper),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("choose_site_photo_btn")
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Choose Photo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }

                                OutlinedButton(
                                    onClick = { openCamera() },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, MastorCreamBorder),
                                    modifier = Modifier.testTag("take_site_photo_btn")
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MastorInk, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Take Photo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MastorInk)
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("site_picture_preview_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MastorCreamDark),
                        border = BorderStroke(1.dp, MastorCreamBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                AsyncImage(
                                    model = siteImageUriString,
                                    contentDescription = "Uploaded Site Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, MastorCreamBorder)
                                    ) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MastorCopper, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Change", fontSize = 12.sp, color = MastorCopper)
                                    }

                                    OutlinedButton(
                                        onClick = { openCamera() },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, MastorCreamBorder)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MastorInk, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Take New", fontSize = 12.sp, color = MastorInk)
                                    }
                                }

                                TextButton(
                                    onClick = { siteImageUriString = "" }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remove", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project / Job Name *") },
                    placeholder = { Text("e.g. Canary Wharf Office Refurbishment") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = MastorInkMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorCopper,
                        unfocusedBorderColor = MastorCreamBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = client,
                        onValueChange = { client = it },
                        label = { Text("Client Name *") },
                        placeholder = { Text("e.g. Canary Group") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = MastorInkMuted) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
                        )
                    )

                    OutlinedTextField(
                        value = contractRef,
                        onValueChange = { contractRef = it },
                        label = { Text("Contract Ref *") },
                        placeholder = { Text("e.g. CWG-2026") },
                        leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null, tint = MastorInkMuted) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Site Address") },
                    placeholder = { Text("10 Upper Bank Street, London E14 5JJ") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MastorInkMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorCopper,
                        unfocusedBorderColor = MastorCreamBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = contractValueStr,
                        onValueChange = { contractValueStr = it },
                        label = { Text("Contract Value (£)") },
                        leadingIcon = { Text("£", fontWeight = FontWeight.Bold, color = MastorInkMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
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
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
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
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
                        )
                    )

                    OutlinedTextField(
                        value = surveyor,
                        onValueChange = { surveyor = it },
                        label = { Text("Quantity Surveyor") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
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
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
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
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpaceMD),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MastorSecondaryButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    MastorPrimaryButton(
                        text = "Create Job",
                        onClick = {
                            val valDbl = contractValueStr.toDoubleOrNull() ?: 250000.0
                            val up1 = uplift1Str.toDoubleOrNull() ?: 15.0
                            val up2 = uplift2Str.toDoubleOrNull() ?: 5.0
                            val finalImgUrl = siteImageUriString

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
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_new_job_btn")
                    )
                }
            }
        }
    }
}

private fun copyUriToInternalStorage(context: Context, sourceUri: Uri): String {
    return try {
        val dir = File(context.filesDir, "site_photos").apply { mkdirs() }
        val file = File(dir, "site_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        Uri.fromFile(file).toString()
    } catch (e: Exception) {
        sourceUri.toString()
    }
}

private fun launchCameraForSite(
    context: Context,
    onUriCreated: (Uri) -> Unit,
    launcher: androidx.activity.result.ActivityResultLauncher<Uri>,
    onError: (String) -> Unit
) {
    try {
        val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
        val imageFile = File.createTempFile("site_snap_${System.currentTimeMillis()}_", ".jpg", imagesDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
        onUriCreated(uri)
        launcher.launch(uri)
    } catch (e: Exception) {
        onError("Camera unavailable: ${e.message}")
    }
}
