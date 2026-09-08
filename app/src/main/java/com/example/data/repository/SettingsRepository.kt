package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _relativePhone = MutableStateFlow(getRelativePhone())
    val relativePhone: StateFlow<String> = _relativePhone.asStateFlow()

    private val _autoReadResult = MutableStateFlow(getAutoReadResult())
    val autoReadResult: StateFlow<Boolean> = _autoReadResult.asStateFlow()

    private val _autoScanSms = MutableStateFlow(getAutoScanSms())
    val autoScanSms: StateFlow<Boolean> = _autoScanSms.asStateFlow()

    fun getRelativePhone(): String {
        return prefs.getString(KEY_RELATIVE_PHONE, "") ?: ""
    }

    fun saveRelativePhone(phone: String) {
        val cleanPhone = phone.trim()
        val committed = prefs.edit().putString(KEY_RELATIVE_PHONE, cleanPhone).commit()
        Log.d("AnTamAI", "SettingsRepository.saveRelativePhone: phone=$cleanPhone, committed=$committed")
        _relativePhone.value = cleanPhone
    }

    fun clearRelativePhone() {
        val committed = prefs.edit().remove(KEY_RELATIVE_PHONE).commit()
        Log.d("AnTamAI", "SettingsRepository.clearRelativePhone: committed=$committed")
        _relativePhone.value = ""
    }

    fun getAutoReadResult(): Boolean {
        val enabled = prefs.getBoolean(KEY_AUTO_READ_RESULT, false)
        return enabled
    }

    fun setAutoReadResult(enabled: Boolean) {
        val committed = prefs.edit().putBoolean(KEY_AUTO_READ_RESULT, enabled).commit()
        Log.d("AnTamAI", "SettingsRepository.setAutoReadResult: enabled=$enabled, committed=$committed")
        _autoReadResult.value = enabled
    }

    fun getAutoScanSms(): Boolean {
        val enabled = prefs.getBoolean(KEY_AUTO_SCAN_SMS, true)
        Log.d("AnTamAI", "SettingsRepository.getAutoScanSms: returning $enabled")
        return enabled
    }

    fun setAutoScanSms(enabled: Boolean) {
        val committed = prefs.edit().putBoolean(KEY_AUTO_SCAN_SMS, enabled).commit()
        Log.d("AnTamAI", "SettingsRepository.setAutoScanSms: enabled=$enabled, committed=$committed")
        _autoScanSms.value = enabled
    }

    companion object {
        private const val PREFS_NAME = "antam_ai_settings_prefs"
        private const val KEY_RELATIVE_PHONE = "relative_phone_number"
        private const val KEY_AUTO_READ_RESULT = "auto_read_result"
        private const val KEY_AUTO_SCAN_SMS = "auto_scan_sms"
    }
}
