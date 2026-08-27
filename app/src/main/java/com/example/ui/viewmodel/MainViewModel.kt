package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ScamAnalysisResult
import com.example.data.repository.ScamAnalysisRepository
import com.example.data.repository.SettingsRepository
import com.example.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState {
    data object Home : UiState
    data object Settings : UiState
    data class Analyzing(val message: String = "Đang phân tích...") : UiState
    data class Result(val data: ScamAnalysisResult) : UiState
    data class Error(val errorMessage: String) : UiState
}

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: ScamAnalysisRepository = ScamAnalysisRepository()
    private val settingsRepository: SettingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow<UiState>(UiState.Home)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val relativePhone: StateFlow<String> = settingsRepository.relativePhone

    private var lastAnalyzedAction: (() -> Unit)? = null

    fun analyzeText(text: String) {
        if (text.isBlank()) return
        lastAnalyzedAction = { analyzeText(text) }
        _uiState.value = UiState.Analyzing("Đang phân tích nội dung tin nhắn...")

        viewModelScope.launch {
            val result = repository.analyzeText(text.trim())
            result.fold(
                onSuccess = { scamResult ->
                    _uiState.value = UiState.Result(scamResult)
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

            val result = repository.analyzeImage(base64, "image/jpeg", note)
            result.fold(
                onSuccess = { scamResult ->
                    _uiState.value = UiState.Result(scamResult)
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
            val result = repository.analyzeImage(base64, "image/jpeg", note)
            result.fold(
                onSuccess = { scamResult ->
                    _uiState.value = UiState.Result(scamResult)
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
