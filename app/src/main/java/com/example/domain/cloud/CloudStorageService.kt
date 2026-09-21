package com.example.domain.cloud

import com.example.data.entity.CachedCloudFile
import com.example.data.entity.LinkedDocument
import com.example.domain.boq.GeminiBoqParser

/**
 * Storage Provider Enum / Constants
 */
object StorageProviders {
    const val ONEDRIVE = "ONEDRIVE"
    const val GOOGLE_DRIVE = "GOOGLE_DRIVE"

    fun getDisplayName(provider: String): String = when (provider) {
        ONEDRIVE -> "Microsoft OneDrive"
        GOOGLE_DRIVE -> "Google Drive"
        else -> provider
    }
}

/**
 * File item in Cloud Storage Browser
 */
data class CloudFileItem(
    val id: String,
    val name: String,
    val provider: String,
    val mimeType: String,
    val sizeDisplay: String,
    val lastModified: String,
    val isFolder: Boolean = false,
    val cloudPath: String = "/",
    val sampleContent: String? = null,
    val associatedProject: String? = null,
    val isCachedOffline: Boolean = true,
    val syncStatus: String = "CACHED_LOCAL"
) {
    fun toCachedCloudFile(projectId: String? = null): CachedCloudFile = CachedCloudFile(
        id = id,
        name = name,
        provider = provider,
        mimeType = mimeType,
        sizeDisplay = sizeDisplay,
        lastModified = lastModified,
        isFolder = isFolder,
        cloudPath = cloudPath,
        sampleContent = sampleContent,
        associatedProject = associatedProject,
        projectId = projectId,
        isOfflineAvailable = true,
        syncStatus = syncStatus
    )
}

fun CachedCloudFile.toCloudFileItem(): CloudFileItem = CloudFileItem(
    id = id,
    name = name,
    provider = provider,
    mimeType = mimeType,
    sizeDisplay = sizeDisplay,
    lastModified = lastModified,
    isFolder = isFolder,
    cloudPath = cloudPath,
    sampleContent = sampleContent,
    associatedProject = associatedProject,
    isCachedOffline = isOfflineAvailable,
    syncStatus = syncStatus
)

/**
 * Phase 7: Cloud Storage Abstraction Service for Microsoft Graph (OneDrive) and Google Drive API.
 */
object CloudStorageService {

    var isOneDriveConnected: Boolean = true
    var oneDriveAccountName: String = "marcus.vance@mayfairheritage.co.uk"

    var isGoogleDriveConnected: Boolean = true
    var googleDriveAccountName: String = "m.vance.qs@gmail.com"
    var googleDriveProjectId: String = "sitemate-5984e"
    var googleDriveProjectNumber: String = "648784146669"
    var googleDriveBrandName: String = "Callum Tree's Apps"
    val googleDriveScopes: List<String> = listOf(
        "https://www.googleapis.com/auth/drive.readonly",
        "https://www.googleapis.com/auth/drive.file"
    )
    var googleDriveLastAuthorized: String = "Today, 14:15"
    var googleDriveTokenStatus: String = "Active & Verified"

    fun authorizeGoogleDrive(email: String = "m.vance.qs@gmail.com") {
        isGoogleDriveConnected = true
        googleDriveAccountName = email
        googleDriveLastAuthorized = "Just now"
        googleDriveTokenStatus = "Active & Verified"
    }

    fun disconnectGoogleDrive() {
        isGoogleDriveConnected = false
        googleDriveTokenStatus = "Revoked"
    }

    // Dynamic file lists in OneDrive and Google Drive
    private val _userOneDriveFiles = mutableListOf<CloudFileItem>()
    private val _userGoogleDriveFiles = mutableListOf<CloudFileItem>()
    private val _deletedFileIds = mutableSetOf<String>()
    private val _recentFiles = mutableListOf<CloudFileItem>()

    init {
        initDefaultRecentFiles()
    }

    private fun initDefaultRecentFiles() {
        _recentFiles.clear()
        // Initialize top 5 recently accessed files across cloud storage
        _recentFiles.addAll(
            listOf(
                CloudFileItem(
                    id = "od_file_001",
                    name = "142_Park_Lane_BoQ_v2.xlsx",
                    provider = StorageProviders.ONEDRIVE,
                    mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    sizeDisplay = "142 KB",
                    lastModified = "08 Aug 2026, 14:20",
                    cloudPath = "/Contracts/142_Park_Lane/BoQ/",
                    sampleContent = GeminiBoqParser.SAMPLE_BOQ_1_TEXT,
                    associatedProject = "142 Park Lane"
                ),
                CloudFileItem(
                    id = "gd_file_001",
                    name = "M&E_Subcontract_Schedule.csv",
                    provider = StorageProviders.GOOGLE_DRIVE,
                    mimeType = "text/csv",
                    sizeDisplay = "64 KB",
                    lastModified = "07 Aug 2026, 10:30",
                    cloudPath = "My Drive/Mastor/142 Park Lane/M&E/",
                    sampleContent = GeminiBoqParser.SAMPLE_BOQ_2_TEXT,
                    associatedProject = "142 Park Lane"
                ),
                CloudFileItem(
                    id = "od_file_003",
                    name = "Subcontracts_Schedule_2026.xlsx",
                    provider = StorageProviders.ONEDRIVE,
                    mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    sizeDisplay = "88 KB",
                    lastModified = "04 Aug 2026, 11:15",
                    cloudPath = "/Contracts/142_Park_Lane/Subcontracts/",
                    sampleContent = GeminiBoqParser.SAMPLE_BOQ_2_TEXT,
                    associatedProject = "142 Park Lane"
                ),
                CloudFileItem(
                    id = "gd_file_003",
                    name = "Structural_Calculations_ParkLane.xlsx",
                    provider = StorageProviders.GOOGLE_DRIVE,
                    mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    sizeDisplay = "310 KB",
                    lastModified = "20 Jan 2026, 12:10",
                    cloudPath = "My Drive/Mastor/142 Park Lane/Engineers/",
                    associatedProject = "142 Park Lane"
                ),
                CloudFileItem(
                    id = "od_file_002",
                    name = "Contract_Agreement_CT-2026-991.pdf",
                    provider = StorageProviders.ONEDRIVE,
                    mimeType = "application/pdf",
                    sizeDisplay = "2.4 MB",
                    lastModified = "01 Jan 2026, 09:00",
                    cloudPath = "/Contracts/142_Park_Lane/Legal/",
                    associatedProject = "142 Park Lane"
                )
            )
        )
    }

    fun resetToDefaults() {
        _userOneDriveFiles.clear()
        _userGoogleDriveFiles.clear()
        _deletedFileIds.clear()
        initDefaultRecentFiles()
    }

    /**
     * Record a file as recently accessed, maintaining the last 5 files.
     */
    fun recordFileAccessed(fileItem: CloudFileItem) {
        if (fileItem.isFolder) return
        _recentFiles.removeAll { it.id == fileItem.id }
        _recentFiles.add(0, fileItem)
        if (_recentFiles.size > 5) {
            val trimmed = _recentFiles.take(5)
            _recentFiles.clear()
            _recentFiles.addAll(trimmed)
        }
    }

    /**
     * Get the last 5 accessed files, excluding any deleted files.
     */
    fun getRecentFiles(): List<CloudFileItem> {
        return _recentFiles.filter { it.id !in _deletedFileIds }.take(5)
    }

    fun deleteFile(fileId: String) {
        _deletedFileIds.add(fileId)
        _userOneDriveFiles.removeAll { it.id == fileId }
        _userGoogleDriveFiles.removeAll { it.id == fileId }
        _recentFiles.removeAll { it.id == fileId }
    }

    // Mock file system in OneDrive
    val mockOneDriveFiles: List<CloudFileItem>
        get() = (_userOneDriveFiles + listOf(
            CloudFileItem(
                id = "od_file_001",
                name = "142_Park_Lane_BoQ_v2.xlsx",
                provider = StorageProviders.ONEDRIVE,
                mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                sizeDisplay = "142 KB",
                lastModified = "08 Aug 2026, 14:20",
                cloudPath = "/Contracts/142_Park_Lane/BoQ/",
                sampleContent = GeminiBoqParser.SAMPLE_BOQ_1_TEXT,
                associatedProject = "142 Park Lane"
            ),
            CloudFileItem(
                id = "od_file_002",
                name = "Contract_Agreement_CT-2026-991.pdf",
                provider = StorageProviders.ONEDRIVE,
                mimeType = "application/pdf",
                sizeDisplay = "2.4 MB",
                lastModified = "01 Jan 2026, 09:00",
                cloudPath = "/Contracts/142_Park_Lane/Legal/",
                associatedProject = "142 Park Lane"
            ),
            CloudFileItem(
                id = "od_file_003",
                name = "Subcontracts_Schedule_2026.xlsx",
                provider = StorageProviders.ONEDRIVE,
                mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                sizeDisplay = "88 KB",
                lastModified = "04 Aug 2026, 11:15",
                cloudPath = "/Contracts/142_Park_Lane/Subcontracts/",
                sampleContent = GeminiBoqParser.SAMPLE_BOQ_2_TEXT,
                associatedProject = "142 Park Lane"
            ),
            CloudFileItem(
                id = "od_file_004",
                name = "St_Georges_Office_BoQ_Draft.xlsx",
                provider = StorageProviders.ONEDRIVE,
                mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                sizeDisplay = "215 KB",
                lastModified = "06 Aug 2026, 15:40",
                cloudPath = "/Contracts/St_Georges/Commercial_BoQ/",
                sampleContent = GeminiBoqParser.SAMPLE_BOQ_2_TEXT,
                associatedProject = "St. George's Commercial Office Fitout"
            ),
            CloudFileItem(
                id = "od_file_005",
                name = "Kensington_Heritage_Specification.pdf",
                provider = StorageProviders.ONEDRIVE,
                mimeType = "application/pdf",
                sizeDisplay = "3.8 MB",
                lastModified = "02 Aug 2026, 09:30",
                cloudPath = "/Contracts/Kensington/Specifications/",
                associatedProject = "Kensington Penthouse Restoration"
            ),
            CloudFileItem(
                id = "od_folder_001",
                name = "Site_Photos_Feb_2026",
                provider = StorageProviders.ONEDRIVE,
                mimeType = "folder",
                sizeDisplay = "12 Items",
                lastModified = "07 Aug 2026, 16:45",
                isFolder = true,
                cloudPath = "/Contracts/142_Park_Lane/Site_Photos/",
                associatedProject = "142 Park Lane"
            )
        )).filter { it.id !in _deletedFileIds }

    // Mock file system in Google Drive
    val mockGoogleDriveFiles: List<CloudFileItem>
        get() = (_userGoogleDriveFiles + listOf(
            CloudFileItem(
                id = "gd_file_001",
                name = "M&E_Subcontract_Schedule.csv",
                provider = StorageProviders.GOOGLE_DRIVE,
                mimeType = "text/csv",
                sizeDisplay = "64 KB",
                lastModified = "07 Aug 2026, 10:30",
                cloudPath = "My Drive/Mastor/142 Park Lane/M&E/",
                sampleContent = GeminiBoqParser.SAMPLE_BOQ_2_TEXT,
                associatedProject = "142 Park Lane"
            ),
            CloudFileItem(
                id = "gd_file_002",
                name = "Architectural_Drawings_RevC.pdf",
                provider = StorageProviders.GOOGLE_DRIVE,
                mimeType = "application/pdf",
                sizeDisplay = "14.8 MB",
                lastModified = "15 Jan 2026, 16:00",
                cloudPath = "My Drive/Mastor/142 Park Lane/Drawings/",
                associatedProject = "142 Park Lane"
            ),
            CloudFileItem(
                id = "gd_file_003",
                name = "Structural_Calculations_ParkLane.xlsx",
                provider = StorageProviders.GOOGLE_DRIVE,
                mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                sizeDisplay = "310 KB",
                lastModified = "20 Jan 2026, 12:10",
                cloudPath = "My Drive/Mastor/142 Park Lane/Engineers/",
                associatedProject = "142 Park Lane"
            ),
            CloudFileItem(
                id = "gd_file_004",
                name = "HVAC_Schematics_StGeorges.pdf",
                provider = StorageProviders.GOOGLE_DRIVE,
                mimeType = "application/pdf",
                sizeDisplay = "8.2 MB",
                lastModified = "05 Aug 2026, 14:15",
                cloudPath = "My Drive/Mastor/St Georges/HVAC/",
                associatedProject = "St. George's Commercial Office Fitout"
            ),
            CloudFileItem(
                id = "gd_file_005",
                name = "Marble_Tiling_Schedule_Kensington.xlsx",
                provider = StorageProviders.GOOGLE_DRIVE,
                mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                sizeDisplay = "125 KB",
                lastModified = "03 Aug 2026, 11:00",
                cloudPath = "My Drive/Mastor/Kensington/Finishes/",
                sampleContent = GeminiBoqParser.SAMPLE_BOQ_1_TEXT,
                associatedProject = "Kensington Penthouse Restoration"
            ),
            CloudFileItem(
                id = "gd_file_006",
                name = "Site_Safety_RAMS_Statement.pdf",
                provider = StorageProviders.GOOGLE_DRIVE,
                mimeType = "application/pdf",
                sizeDisplay = "1.9 MB",
                lastModified = "06 Aug 2026, 08:30",
                cloudPath = "My Drive/Mastor/142 Park Lane/Safety/",
                sampleContent = "Risk Assessment & Method Statement (RAMS) for Flat 1 structural alterations, load temporary propping, and hot works.",
                associatedProject = "142 Park Lane"
            ),
            CloudFileItem(
                id = "gd_file_007",
                name = "Electrical_Distribution_Layout.pdf",
                provider = StorageProviders.GOOGLE_DRIVE,
                mimeType = "application/pdf",
                sizeDisplay = "5.1 MB",
                lastModified = "07 Aug 2026, 14:00",
                cloudPath = "My Drive/Mastor/142 Park Lane/Drawings/",
                sampleContent = "First & Second Fix Electrical Single Line Diagram and Lighting Circuit Schematic for Mayfair Residential Units.",
                associatedProject = "142 Park Lane"
            ),
            CloudFileItem(
                id = "gd_file_008",
                name = "Fire_Door_FD30S_Certificate_Schedule.xlsx",
                provider = StorageProviders.GOOGLE_DRIVE,
                mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                sizeDisplay = "410 KB",
                lastModified = "08 Aug 2026, 09:45",
                cloudPath = "My Drive/Mastor/142 Park Lane/Compliance/",
                sampleContent = "FD30S and FD60S timber internal door schedule with intumescent seal test certificates and ironmongery specifications.",
                associatedProject = "142 Park Lane"
            ),
            CloudFileItem(
                id = "gd_folder_001",
                name = "Site_Inspections",
                provider = StorageProviders.GOOGLE_DRIVE,
                mimeType = "folder",
                sizeDisplay = "8 Items",
                lastModified = "05 Aug 2026, 09:20",
                isFolder = true,
                cloudPath = "My Drive/Mastor/142 Park Lane/Inspections/",
                associatedProject = "142 Park Lane"
            ),
            CloudFileItem(
                id = "gd_folder_002",
                name = "Drawings_and_Schematics",
                provider = StorageProviders.GOOGLE_DRIVE,
                mimeType = "folder",
                sizeDisplay = "14 Items",
                lastModified = "08 Aug 2026, 16:30",
                isFolder = true,
                cloudPath = "My Drive/Mastor/142 Park Lane/Drawings/",
                associatedProject = "142 Park Lane"
            )
        )).filter { it.id !in _deletedFileIds }

    fun registerUploadedFile(fileItem: CloudFileItem) {
        if (fileItem.provider == StorageProviders.ONEDRIVE) {
            _userOneDriveFiles.add(0, fileItem)
        } else {
            _userGoogleDriveFiles.add(0, fileItem)
        }
        recordFileAccessed(fileItem)
    }

    fun getFilesForProvider(provider: String): List<CloudFileItem> = when (provider) {
        StorageProviders.ONEDRIVE -> mockOneDriveFiles
        StorageProviders.GOOGLE_DRIVE -> mockGoogleDriveFiles
        else -> emptyList()
    }

    fun createLinkedDocumentFromCloudFile(
        cloudFile: CloudFileItem,
        projectId: String,
        isPrimaryBoq: Boolean = true,
        workOrderRef: String? = null,
        docCategory: String = if (isPrimaryBoq) "BoQ / Specification" else "Project Documentation"
    ): LinkedDocument {
        recordFileAccessed(cloudFile)
        val nowDisplay = "09 Aug 2026, 10:45 AM"
        return LinkedDocument(
            id = "doc_${cloudFile.id}_${System.currentTimeMillis() % 10000}",
            projectId = projectId,
            storageProvider = cloudFile.provider,
            fileId = cloudFile.id,
            fileName = cloudFile.name,
            mimeType = cloudFile.mimeType,
            fileSizeDisplay = cloudFile.sizeDisplay,
            lastSyncedAt = nowDisplay,
            isPrimaryBoq = isPrimaryBoq,
            cloudPath = cloudFile.cloudPath,
            contentSnippet = cloudFile.sampleContent ?: GeminiBoqParser.SAMPLE_BOQ_1_TEXT,
            fullContent = cloudFile.sampleContent ?: GeminiBoqParser.SAMPLE_BOQ_1_TEXT,
            workOrderRef = workOrderRef,
            docCategory = docCategory
        )
    }

    fun createLinkedDocumentForWorkOrder(
        cloudFile: CloudFileItem,
        projectId: String,
        workOrderRef: String,
        docCategory: String = "Project Documentation"
    ): LinkedDocument {
        return createLinkedDocumentFromCloudFile(
            cloudFile = cloudFile,
            projectId = projectId,
            isPrimaryBoq = false,
            workOrderRef = workOrderRef,
            docCategory = docCategory
        )
    }
}
