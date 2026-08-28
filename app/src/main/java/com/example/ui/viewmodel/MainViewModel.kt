package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CheckHistoryEntity
import com.example.data.local.SmsEntity
import com.example.data.model.ScamAnalysisResult
import com.example.data.model.SmsMessage
import com.example.data.remote.ApiClient
import com.example.data.repository.CheckHistoryRepository
import com.example.data.repository.ScamAnalysisRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.SmsRepository
import com.example.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    MESSAGES, // TAB 1 - "Nhật ký" (Default Main Tab)
    CHECK,    // TAB 2 - "Kiểm tra"
    SETTINGS  // TAB 3 - "Cài đặt"
}

sealed interface UiState {
    data object Home : UiState
    data object Settings : UiState
    data class Analyzing(val message: String = "Đang phân tích...") : UiState
    data class Result(
        val data: ScamAnalysisResult,
        val originalText: String? = null,
        val originalImageBitmap: Bitmap? = null,
        val originalImageUri: Uri? = null
    ) : UiState
    data class Error(val errorMessage: String) : UiState
}

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: ScamAnalysisRepository = ScamAnalysisRepository()
    private val settingsRepository: SettingsRepository = SettingsRepository(application)
    private val smsRepository: SmsRepository = SmsRepository(application)
    private val database = AppDatabase.getInstance(application)
    private val historyRepository = CheckHistoryRepository(database.checkHistoryDao())

    private val _uiState = MutableStateFlow<UiState>(UiState.Home)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.MESSAGES)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    val smsEntities: StateFlow<List<SmsEntity>> = smsRepository.getAllSmsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _smsMessages = MutableStateFlow<List<SmsMessage>>(emptyList())
    val smsMessages: StateFlow<List<SmsMessage>> = _smsMessages.asStateFlow()

    private val _isSmsLoading = MutableStateFlow(false)
    val isSmsLoading: StateFlow<Boolean> = _isSmsLoading.asStateFlow()

    private val _smsError = MutableStateFlow<String?>(null)
    val smsError: StateFlow<String?> = _smsError.asStateFlow()

    val checkHistory: StateFlow<List<CheckHistoryEntity>> = historyRepository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val relativePhone: StateFlow<String> = settingsRepository.relativePhone
    val autoReadResult: StateFlow<Boolean> = settingsRepository.autoReadResult
    val autoScanSms: StateFlow<Boolean> = settingsRepository.autoScanSms

    private var lastAnalyzedAction: (() -> Unit)? = null

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
        if (_uiState.value !is UiState.Home) {
            _uiState.value = UiState.Home
        }
    }

    fun loadSmsMessages() {
        viewModelScope.launch {
            _isSmsLoading.value = true
            _smsError.value = null
            val syncResult = smsRepository.syncInboxMessages()
            syncResult.fold(
                onSuccess = {
                    val messagesResult = smsRepository.getInboxMessages()
                    messagesResult.onSuccess { _smsMessages.value = it }
                    _isSmsLoading.value = false
                },
                onFailure = { error ->
                    _smsError.value = error.localizedMessage ?: "Không thể đọc tin nhắn"
                    _isSmsLoading.value = false
                }
            )
        }
    }

    fun openSmsEntity(entity: SmsEntity) {
        val parsedResult = try {
            if (entity.resultJson.isNotBlank()) {
                val adapter = ApiClient.moshi.adapter(ScamAnalysisResult::class.java)
                adapter.fromJson(entity.resultJson)
            } else null
        } catch (_: Exception) {
            null
        } ?: ScamAnalysisResult(
            status = entity.status,
            openingMessage = if (entity.openingMessage.isNotBlank()) entity.openingMessage else if (entity.status == "SAFE") "Tin nhắn an toàn" else "Tin nhắn đáng ngờ",
            signals = if (entity.heuristicSignals.isNotBlank()) entity.heuristicSignals.split("|||") else emptyList(),
            reminders = emptyList()
        )

        _uiState.value = UiState.Result(
            data = parsedResult,
            originalText = entity.body
        )
    }

    fun showResultFromNotification(
        originalText: String?,
        resultJson: String?,
        sender: String?
    ) {
        val parsedResult = try {
            if (!resultJson.isNullOrBlank()) {
                val adapter = ApiClient.moshi.adapter(ScamAnalysisResult::class.java)
                adapter.fromJson(resultJson)
            } else null
        } catch (_: Exception) {
            null
        } ?: ScamAnalysisResult(
            status = "DANGER",
            openingMessage = "Phát hiện tin nhắn có dấu hiệu bất thường từ $sender",
            signals = emptyList(),
            reminders = emptyList()
        )

        _uiState.value = UiState.Result(
            data = parsedResult,
            originalText = originalText
        )
    }

    fun analyzeText(text: String) {
        if (text.isBlank()) return
        lastAnalyzedAction = { analyzeText(text) }
        _uiState.value = UiState.Analyzing("Đang phân tích nội dung tin nhắn...")

        viewModelScope.launch {
            val result = repository.analyzeText(text.trim()) { msg ->
                _uiState.value = UiState.Analyzing(msg)
            }
            result.fold(
                onSuccess = { scamResult ->
                    viewModelScope.launch {
                        historyRepository.saveCheckHistory(
                            contentType = "TEXT",
                            contentPreview = text.trim(),
                            status = scamResult.status,
                            openingMessage = scamResult.openingMessage,
                            resultJson = scamResult.rawJson
                        )
                    }
                    _uiState.value = UiState.Result(
                        data = scamResult,
                        originalText = text.trim()
                    )
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(
                        error.localizedMessage ?: "Có lỗi xảy ra trong quá trình kiểm tra. Vui lòng thử lại!"
                    )
                }
            )
        }
    }

    fun analyzeImageUri(context: Context, uri: Uri, note: String? = null) {
        lastAnalyzedAction = { analyzeImageUri(context, uri, note) }
        _uiState.value = UiState.Analyzing("Đang phân tích ảnh chụp màn hình...")

        viewModelScope.launch {
            val base64 = ImageUtils.uriToBase64(context, uri)
            if (base64 == null) {
                _uiState.value = UiState.Error("Không thể đọc hình ảnh đã chọn. Vui lòng thử lại với ảnh khác.")
                return@launch
            }

            val result = repository.analyzeImage(base64, "image/jpeg", note) { msg ->
                _uiState.value = UiState.Analyzing(msg)
            }
            result.fold(
                onSuccess = { scamResult ->
                    viewModelScope.launch {
                        historyRepository.saveCheckHistory(
                            contentType = "IMAGE",
                            contentPreview = if (!note.isNullOrBlank()) note else "Ảnh chụp màn hình / Hóa đơn",
                            status = scamResult.status,
                            openingMessage = scamResult.openingMessage,
                            resultJson = scamResult.rawJson
                        )
                    }
                    _uiState.value = UiState.Result(
                        data = scamResult,
                        originalImageUri = uri
                    )
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(
                        error.localizedMessage ?: "Có lỗi xảy ra khi phân tích hình ảnh. Vui lòng thử lại!"
                    )
                }
            )
        }
    }

    fun analyzeImageBitmap(bitmap: Bitmap, note: String? = null) {
        lastAnalyzedAction = { analyzeImageBitmap(bitmap, note) }
        _uiState.value = UiState.Analyzing("Đang phân tích ảnh chụp...")

        viewModelScope.launch {
            val base64 = ImageUtils.bitmapToBase64(bitmap)
            val result = repository.analyzeImage(base64, "image/jpeg", note) { msg ->
                _uiState.value = UiState.Analyzing(msg)
            }
            result.fold(
                onSuccess = { scamResult ->
                    viewModelScope.launch {
                        historyRepository.saveCheckHistory(
                            contentType = "IMAGE",
                            contentPreview = if (!note.isNullOrBlank()) note else "Ảnh chụp trực tiếp từ camera",
                            status = scamResult.status,
                            openingMessage = scamResult.openingMessage,
                            resultJson = scamResult.rawJson
                        )
                    }
                    _uiState.value = UiState.Result(
                        data = scamResult,
                        originalImageBitmap = bitmap
                    )
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(
                        error.localizedMessage ?: "Có lỗi xảy ra khi phân tích hình ảnh. Vui lòng thử lại!"
                    )
                }
            )
        }
    }

    fun openHistoryItem(item: CheckHistoryEntity) {
        val result = try {
            if (item.resultJson.isNotBlank()) {
                val adapter = ApiClient.moshi.adapter(ScamAnalysisResult::class.java)
                adapter.fromJson(item.resultJson)
            } else null
        } catch (_: Exception) {
            null
        } ?: ScamAnalysisResult(
            status = item.status,
            openingMessage = item.openingMessage,
            signals = emptyList(),
            reminders = emptyList()
        )

        _uiState.value = UiState.Result(
            data = result,
            originalText = if (item.contentType == "TEXT") item.contentPreview else null
        )
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            historyRepository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAll()
        }
    }

    fun saveRelativePhone(phone: String) {
        settingsRepository.saveRelativePhone(phone)
    }

    fun clearRelativePhone() {
        settingsRepository.clearRelativePhone()
    }

    fun setAutoReadResult(enabled: Boolean) {
        settingsRepository.setAutoReadResult(enabled)
    }

    fun setAutoScanSms(enabled: Boolean) {
        settingsRepository.setAutoScanSms(enabled)
    }

    fun openSettings() {
        _uiState.value = UiState.Settings
    }

    fun resetToHome() {
        _uiState.value = UiState.Home
    }

    fun retry() {
        lastAnalyzedAction?.invoke() ?: run {
            _uiState.value = UiState.Home
        }
    }
}
