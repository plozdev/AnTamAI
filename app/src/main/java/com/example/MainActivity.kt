package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AnalyzingScreen
import com.example.ui.screens.ErrorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmsInboxScreen
import com.example.ui.theme.AnTamTheme
import com.example.ui.theme.LightSurface
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanPrimaryContainer
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMediumContrast
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    companion object {
        const val EXTRA_OPEN_SMS_ID = "com.example.EXTRA_OPEN_SMS_ID"
        const val EXTRA_SENDER = "com.example.EXTRA_SENDER"
        const val EXTRA_ORIGINAL_TEXT = "com.example.EXTRA_ORIGINAL_TEXT"
        const val EXTRA_RESULT_JSON = "com.example.EXTRA_RESULT_JSON"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            AnTamTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val originalText = intent.getStringExtra(EXTRA_ORIGINAL_TEXT)
        val resultJson = intent.getStringExtra(EXTRA_RESULT_JSON)
        val sender = intent.getStringExtra(EXTRA_SENDER)

        if (!originalText.isNullOrBlank() || !resultJson.isNullOrBlank()) {
            viewModel.showResultFromNotification(originalText, resultJson, sender)
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val relativePhone by viewModel.relativePhone.collectAsStateWithLifecycle()
    val autoReadResult by viewModel.autoReadResult.collectAsStateWithLifecycle()
    val autoScanSms by viewModel.autoScanSms.collectAsStateWithLifecycle()
    val smsEntities by viewModel.smsEntities.collectAsStateWithLifecycle()
    val smsMessages by viewModel.smsMessages.collectAsStateWithLifecycle()
    val isSmsLoading by viewModel.isSmsLoading.collectAsStateWithLifecycle()
    val smsError by viewModel.smsError.collectAsStateWithLifecycle()
    val checkHistory by viewModel.checkHistory.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Ensure back press on Result, Settings, Analyzing or Error returns to Home (Messages tab)
    BackHandler(enabled = uiState !is UiState.Home || currentTab != AppTab.MESSAGES) {
        if (uiState !is UiState.Home) {
            viewModel.resetToHome()
        } else if (currentTab != AppTab.MESSAGES) {
            viewModel.selectTab(AppTab.MESSAGES)
        }
    }

    when (val state = uiState) {
        is UiState.Home -> {
            Scaffold(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier
                            .shadow(elevation = 8.dp)
                            .testTag("bottom_nav_bar"),
                        containerColor = LightSurface,
                        contentColor = TextHighContrast,
                        tonalElevation = 4.dp
                    ) {
                        // TAB 1: Nhật ký (Mặc định)
                        NavigationBarItem(
                            selected = currentTab == AppTab.MESSAGES,
                            onClick = { viewModel.selectTab(AppTab.MESSAGES) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == AppTab.MESSAGES) Icons.Filled.Message else Icons.Outlined.Message,
                                    contentDescription = "Nhật ký",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Nhật ký",
                                    fontSize = 12.sp,
                                    fontWeight = if (currentTab == AppTab.MESSAGES) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OceanPrimary,
                                selectedTextColor = OceanPrimary,
                                indicatorColor = OceanPrimaryContainer,
                                unselectedIconColor = TextMediumContrast,
                                unselectedTextColor = TextMediumContrast
                            ),
                            modifier = Modifier.testTag("tab_messages")
                        )

                        // TAB 2: Kiểm tra
                        NavigationBarItem(
                            selected = currentTab == AppTab.CHECK,
                            onClick = { viewModel.selectTab(AppTab.CHECK) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == AppTab.CHECK) Icons.Filled.Security else Icons.Outlined.Security,
                                    contentDescription = "Kiểm tra",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Kiểm tra",
                                    fontSize = 12.sp,
                                    fontWeight = if (currentTab == AppTab.CHECK) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OceanPrimary,
                                selectedTextColor = OceanPrimary,
                                indicatorColor = OceanPrimaryContainer,
                                unselectedIconColor = TextMediumContrast,
                                unselectedTextColor = TextMediumContrast
                            ),
                            modifier = Modifier.testTag("tab_check")
                        )

                        // TAB 3: Cài đặt
                        NavigationBarItem(
                            selected = currentTab == AppTab.SETTINGS,
                            onClick = { viewModel.selectTab(AppTab.SETTINGS) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == AppTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Cài đặt",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Cài đặt",
                                    fontSize = 12.sp,
                                    fontWeight = if (currentTab == AppTab.SETTINGS) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OceanPrimary,
                                selectedTextColor = OceanPrimary,
                                indicatorColor = OceanPrimaryContainer,
                                unselectedIconColor = TextMediumContrast,
                                unselectedTextColor = TextMediumContrast
                            ),
                            modifier = Modifier.testTag("tab_settings")
                        )
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (currentTab) {
                        AppTab.MESSAGES -> {
                            SmsInboxScreen(
                                smsEntities = smsEntities,
                                fallbackMessages = smsMessages,
                                isLoading = isSmsLoading,
                                errorMessage = smsError,
                                checkHistory = checkHistory,
                                autoScanEnabled = autoScanSms,
                                onRefresh = { viewModel.loadSmsMessages() },
                                onOpenSmsItem = { entity -> viewModel.openSmsEntity(entity) },
                                onDismissSms = { id -> viewModel.dismissSms(id) },
                                onDismissAllSuspicious = { viewModel.dismissAllSuspicious() },
                                onOpenHistoryItem = { item -> viewModel.openHistoryItem(item) },
                                onDeleteHistoryItem = { id -> viewModel.deleteHistoryItem(id) },
                                onClearAllHistory = { viewModel.clearAllHistory() },
                                onOpenSettings = { viewModel.selectTab(AppTab.SETTINGS) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        AppTab.CHECK -> {
                            HomeScreen(
                                relativePhone = relativePhone,
                                onOpenSettings = { viewModel.selectTab(AppTab.SETTINGS) },
                                onAnalyzeText = { text -> viewModel.analyzeText(text) },
                                onAnalyzeImageUri = { uri -> viewModel.analyzeImageUri(context, uri) },
                                onAnalyzeImageBitmap = { bitmap -> viewModel.analyzeImageBitmap(bitmap) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        AppTab.SETTINGS -> {
                            SettingsScreen(
                                currentPhone = relativePhone,
                                autoReadResult = autoReadResult,
                                autoScanSms = autoScanSms,
                                onSavePhone = { phone -> viewModel.saveRelativePhone(phone) },
                                onClearPhone = { viewModel.clearRelativePhone() },
                                onToggleAutoRead = { enabled -> viewModel.setAutoReadResult(enabled) },
                                onToggleAutoScan = { enabled -> viewModel.setAutoScanSms(enabled) },
                                onBack = { viewModel.selectTab(AppTab.MESSAGES) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
        is UiState.Settings -> {
            SettingsScreen(
                currentPhone = relativePhone,
                autoReadResult = autoReadResult,
                autoScanSms = autoScanSms,
                onSavePhone = { phone -> viewModel.saveRelativePhone(phone) },
                onClearPhone = { viewModel.clearRelativePhone() },
                onToggleAutoRead = { enabled -> viewModel.setAutoReadResult(enabled) },
                onToggleAutoScan = { enabled -> viewModel.setAutoScanSms(enabled) },
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
                autoReadResult = autoReadResult,
                originalText = state.originalText,
                originalImageBitmap = state.originalImageBitmap,
                originalImageUri = state.originalImageUri,
                onOpenSettings = { viewModel.selectTab(AppTab.SETTINGS) },
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
