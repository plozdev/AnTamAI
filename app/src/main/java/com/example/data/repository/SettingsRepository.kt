package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
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
        prefs.edit().putString(KEY_RELATIVE_PHONE, cleanPhone).apply()
        _relativePhone.value = cleanPhone
    }

    fun clearRelativePhone() {
        prefs.edit().remove(KEY_RELATIVE_PHONE).apply()
        _relativePhone.value = ""
    }

    fun getAutoReadResult(): Boolean {
        return prefs.getBoolean(KEY_AUTO_READ_RESULT, false)
    }

    fun setAutoReadResult(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_READ_RESULT, enabled).apply()
        _autoReadResult.value = enabled
    }

    fun getAutoScanSms(): Boolean {
        return prefs.getBoolean(KEY_AUTO_SCAN_SMS, true)
    }

    fun setAutoScanSms(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SCAN_SMS, enabled).apply()
        _autoScanSms.value = enabled
    }

    companion object {
        private const val PREFS_NAME = "antam_ai_settings_prefs"
        private const val KEY_RELATIVE_PHONE = "relative_phone_number"
        private const val KEY_AUTO_READ_RESULT = "auto_read_result"
        private const val KEY_AUTO_SCAN_SMS = "auto_scan_sms"
    }
}
