package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.Phase2ScopeScreen
import com.example.ui.theme.MastorTheme
import com.example.ui.viewmodel.Phase1ViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MastorTheme {
                val viewModel: Phase1ViewModel = viewModel()
                Phase2ScopeScreen(viewModel = viewModel)
            }
        }
    }
}
