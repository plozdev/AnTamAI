package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AnalyzingScreen
import com.example.ui.screens.ErrorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AnTamTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnTamTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) { innerPadding ->
                    MainAppContent(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val relativePhone by viewModel.relativePhone.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    when (val state = uiState) {
        is UiState.Home -> {
            HomeScreen(
                relativePhone = relativePhone,
                onOpenSettings = { viewModel.openSettings() },
                onAnalyzeText = { text -> viewModel.analyzeText(text) },
                onAnalyzeImageUri = { uri -> viewModel.analyzeImageUri(context, uri) },
                onAnalyzeImageBitmap = { bitmap -> viewModel.analyzeImageBitmap(bitmap) },
                modifier = modifier
            )
        }
        is UiState.Settings -> {
            SettingsScreen(
                currentPhone = relativePhone,
                onSavePhone = { phone -> viewModel.saveRelativePhone(phone) },
                onClearPhone = { viewModel.clearRelativePhone() },
                onBack = { viewModel.resetToHome() },
                modifier = modifier
            )
        }
        is UiState.Analyzing -> {
            AnalyzingScreen(
                message = state.message,
                modifier = modifier
            )
        }
        is UiState.Result -> {
            ResultScreen(
                result = state.data,
                relativePhone = relativePhone,
                onOpenSettings = { viewModel.openSettings() },
                onBackToHome = { viewModel.resetToHome() },
                modifier = modifier
            )
        }
        is UiState.Error -> {
            ErrorScreen(
                errorMessage = state.errorMessage,
                onRetry = { viewModel.retry() },
                onBackToHome = { viewModel.resetToHome() },
                modifier = modifier
            )
        }
    }
}
