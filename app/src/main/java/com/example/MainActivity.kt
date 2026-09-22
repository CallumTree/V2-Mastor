package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.Phase2ScopeScreen
import com.example.ui.screens.ProjectPickerScreen
import com.example.ui.theme.MastorTheme
import com.example.ui.viewmodel.Phase1ViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MastorTheme {
                val viewModel: Phase1ViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                if (uiState.selectedProjectId == null || uiState.project == null) {
                    ProjectPickerScreen(
                        projects = uiState.allProjects,
                        onSelectProject = { projectId -> viewModel.selectProject(projectId) },
                        onCreateProject = { name, client, contractRef, address, siteManager, surveyor, contractValue, workType, imageUrl, uplift1, uplift2 ->
                            viewModel.createNewProject(
                                name = name,
                                client = client,
                                contractRef = contractRef,
                                address = address,
                                siteManager = siteManager,
                                surveyor = surveyor,
                                contractValue = contractValue,
                                workType = workType,
                                imageUrl = imageUrl ?: "",
                                uplift1Percent = uplift1,
                                uplift2Percent = uplift2,
                                onCreated = { newId -> viewModel.selectProject(newId) }
                            )
                        }
                    )
                } else {
                    Phase2ScopeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
