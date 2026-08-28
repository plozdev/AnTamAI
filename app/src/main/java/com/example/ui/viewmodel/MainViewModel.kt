package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ScamAnalysisResult
import com.example.data.model.SmsMessage
import com.example.data.repository.ScamAnalysisRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.SmsRepository
import com.example.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppTab {
    MESSAGES, // TAB 1 - "Tin nhắn"
    CHECK,    // TAB 2 - "Kiểm tra" (Default)
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

    private val _uiState = MutableStateFlow<UiState>(UiState.Home)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.CHECK)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _smsMessages = MutableStateFlow<List<SmsMessage>>(emptyList())
    val smsMessages: StateFlow<List<SmsMessage>> = _smsMessages.asStateFlow()

    private val _isSmsLoading = MutableStateFlow(false)
    val isSmsLoading: StateFlow<Boolean> = _isSmsLoading.asStateFlow()

    private val _smsError = MutableStateFlow<String?>(null)
    val smsError: StateFlow<String?> = _smsError.asStateFlow()

    val relativePhone: StateFlow<String> = settingsRepository.relativePhone
    val autoReadResult: StateFlow<Boolean> = settingsRepository.autoReadResult

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
            val result = smsRepository.getInboxMessages()
            result.fold(
                onSuccess = { list ->
                    _smsMessages.value = list
                    _isSmsLoading.value = false
                },
                onFailure = { error ->
                    _smsError.value = error.localizedMessage ?: "Không thể đọc tin nhắn"
                    _isSmsLoading.value = false
                }
            )
        }
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

    fun saveRelativePhone(phone: String) {
        settingsRepository.saveRelativePhone(phone)
    }

    fun clearRelativePhone() {
        settingsRepository.clearRelativePhone()
    }

    fun setAutoReadResult(enabled: Boolean) {
        settingsRepository.setAutoReadResult(enabled)
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
