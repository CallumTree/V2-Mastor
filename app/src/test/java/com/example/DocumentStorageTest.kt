package com.example

import com.example.data.entity.LinkedDocument
import com.example.domain.cloud.CloudFileItem
import com.example.domain.cloud.CloudStorageService
import com.example.domain.cloud.StorageProviders
import com.example.domain.boq.GeminiBoqParser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentStorageTest {

    @Test
    fun testProviderAgnosticAbstraction_OneDriveAndGoogleDrive() {
        assertEquals("Microsoft OneDrive", StorageProviders.getDisplayName(StorageProviders.ONEDRIVE))
        assertEquals("Google Drive", StorageProviders.getDisplayName(StorageProviders.GOOGLE_DRIVE))

        val oneDriveFiles = CloudStorageService.getFilesForProvider(StorageProviders.ONEDRIVE)
        val googleDriveFiles = CloudStorageService.getFilesForProvider(StorageProviders.GOOGLE_DRIVE)

        assertTrue(oneDriveFiles.isNotEmpty())
        assertTrue(googleDriveFiles.isNotEmpty())

        val primaryOneDriveFile = oneDriveFiles.first { it.name.endsWith(".xlsx") }
        assertEquals("ONEDRIVE", primaryOneDriveFile.provider)
        assertNotNull(primaryOneDriveFile.sampleContent)

        val primaryGoogleDriveFile = googleDriveFiles.first { it.name.endsWith(".csv") }
        assertEquals("GOOGLE_DRIVE", primaryGoogleDriveFile.provider)
        assertNotNull(primaryGoogleDriveFile.sampleContent)
    }

    @Test
    fun testLinkedDocumentEntityAndCloudFlow() {
        val cloudFile = CloudStorageService.mockOneDriveFiles.first()
        val linkedDoc = CloudStorageService.createLinkedDocumentFromCloudFile(
            cloudFile = cloudFile,
            projectId = "proj_101",
            isPrimaryBoq = true
        )

        assertEquals("proj_101", linkedDoc.projectId)
        assertEquals(StorageProviders.ONEDRIVE, linkedDoc.storageProvider)
        assertEquals("142_Park_Lane_BoQ_v2.xlsx", linkedDoc.fileName)
        assertTrue(linkedDoc.isPrimaryBoq)
        assertNotNull(linkedDoc.contentSnippet)

        // Verify direct passage into Phase 3 parsing pipeline without local upload
        runBlocking {
            val parseResult = GeminiBoqParser.parseBoqText(linkedDoc.contentSnippet!!)
            assertTrue(parseResult.workOrders.isNotEmpty())
            val firstWo = parseResult.workOrders.first()
            assertNotNull(firstWo.description)
            assertTrue(firstWo.scopeElements.isNotEmpty())
        }
    }

    @Test
    fun testNoAiIconographyOrGimmicksInStorageUI() {
        val providerName1 = StorageProviders.getDisplayName(StorageProviders.ONEDRIVE)
        val providerName2 = StorageProviders.getDisplayName(StorageProviders.GOOGLE_DRIVE)

        assertTrue(!providerName1.contains("✨"))
        assertTrue(!providerName1.contains("🤖"))
        assertTrue(!providerName2.contains("✨"))
        assertTrue(!providerName2.contains("🤖"))
    }

    @Test
    fun testFileSearchAndFilteringByFileNameAndProject() {
        val oneDriveFiles = CloudStorageService.getFilesForProvider(StorageProviders.ONEDRIVE)

        // Filter by file name
        val boqResults = oneDriveFiles.filter {
            it.name.lowercase().contains("boq") ||
            (it.associatedProject?.lowercase()?.contains("boq") == true)
        }
        assertTrue(boqResults.isNotEmpty())
        assertTrue(boqResults.any { it.name.contains("BoQ", ignoreCase = true) })

        // Filter by associated project
        val projectResults = oneDriveFiles.filter {
            it.name.lowercase().contains("park lane") ||
            (it.associatedProject?.lowercase()?.contains("park lane") == true)
        }
        assertTrue(projectResults.isNotEmpty())
        assertEquals("142 Park Lane", projectResults.first().associatedProject)
    }

    @Test
    fun testFileDeletionAndRegistration() {
        val initialCount = CloudStorageService.getFilesForProvider(StorageProviders.ONEDRIVE).size
        val newFile = CloudFileItem(
            id = "test_doc_001",
            name = "Test_Document.pdf",
            provider = StorageProviders.ONEDRIVE,
            mimeType = "application/pdf",
            sizeDisplay = "200 KB",
            lastModified = "14 Aug 2026, 12:00",
            cloudPath = "/Contracts/Test/",
            associatedProject = "Test Project"
        )
        CloudStorageService.registerUploadedFile(newFile)
        assertEquals("Test_Document.pdf", newFile.name)
        assertEquals("Test Project", newFile.associatedProject)

        val afterUploadCount = CloudStorageService.getFilesForProvider(StorageProviders.ONEDRIVE).size
        assertEquals(initialCount + 1, afterUploadCount)

        CloudStorageService.deleteFile(newFile.id)

        val afterDeleteCount = CloudStorageService.getFilesForProvider(StorageProviders.ONEDRIVE).size
        assertEquals(initialCount, afterDeleteCount)
    }

    @Test
    fun testRecentFilesTracking_Last5AccessedFiles() {
        CloudStorageService.resetToDefaults()

        val recentFiles = CloudStorageService.getRecentFiles()
        assertEquals(5, recentFiles.size)

        // Verify recent files contain expected initial recent items
        assertTrue(recentFiles.any { it.name.contains("142_Park_Lane_BoQ_v2.xlsx") })
        assertTrue(recentFiles.any { it.name.contains("M&E_Subcontract_Schedule.csv") })

        // Access an existing file (e.g. Kensington Penthouse)
        val kensingtonDoc = CloudFileItem(
            id = "od_file_005",
            name = "Kensington_Heritage_Specification.pdf",
            provider = StorageProviders.ONEDRIVE,
            mimeType = "application/pdf",
            sizeDisplay = "3.8 MB",
            lastModified = "02 Aug 2026, 09:30",
            cloudPath = "/Contracts/Kensington/Specifications/",
            associatedProject = "Kensington Penthouse Restoration"
        )
        CloudStorageService.recordFileAccessed(kensingtonDoc)

        val updatedRecent = CloudStorageService.getRecentFiles()
        assertEquals(5, updatedRecent.size)
        assertEquals("od_file_005", updatedRecent.first().id)
        assertEquals("Kensington_Heritage_Specification.pdf", updatedRecent.first().name)

        // Linking document also records recent access
        val sampleDoc = CloudFileItem(
            id = "new_accessed_doc",
            name = "BoQ_New_Revision_ParkLane.xlsx",
            provider = StorageProviders.ONEDRIVE,
            mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            sizeDisplay = "450 KB",
            lastModified = "16 Aug 2026, 15:00",
            cloudPath = "/Contracts/142_Park_Lane/BoQ/"
        )
        CloudStorageService.createLinkedDocumentFromCloudFile(
            cloudFile = sampleDoc,
            projectId = "proj_101",
            isPrimaryBoq = true
        )

        val afterLinkRecent = CloudStorageService.getRecentFiles()
        assertEquals(5, afterLinkRecent.size)
        assertEquals("new_accessed_doc", afterLinkRecent.first().id)

        // Deleting file removes it from recent files
        CloudStorageService.deleteFile("new_accessed_doc")
        val afterDeleteRecent = CloudStorageService.getRecentFiles()
        assertTrue(afterDeleteRecent.none { it.id == "new_accessed_doc" })
        assertTrue(afterDeleteRecent.size <= 5)
    }
}
