package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.forLanguageTag("vi-VN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to Vietnamese generic or default locale
                tts?.setLanguage(Locale.forLanguageTag("vi"))
            }
            tts?.setSpeechRate(0.92f) // slightly slower for elderly clarity
            tts?.setPitch(1.0f)
            isInitialized = true

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    Log.e("TTS", "Error code: $errorCode")
                }
            })
        } else {
            Log.e("TTS", "Init failed")
        }
    }

    fun speak(text: String) {
        if (!isInitialized || text.isBlank()) return
        stop()
        _isSpeaking.value = true
        val utteranceId = "ANTAM_SPEECH_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        _isSpeaking.value = false
    }
}

fun com.example.data.model.ScamAnalysisResult.toSpeechText(): String = buildString {
    if (openingMessage.isNotBlank()) {
        append(openingMessage)
        append(". ")
    }
    if (financialReminder?.show == true) {
        append("Lưu ý tài chính: ")
        financialReminder.message1?.let { append("$it ") }
        financialReminder.message2?.let { append("$it ") }
    }
    if (signals.isNotEmpty()) {
        append("Các dấu hiệu chính: ")
        signals.forEachIndexed { i, sig ->
            append("Dấu hiệu ${i + 1}: $sig. ")
        }
    }
    if (reminders.isNotEmpty()) {
        append("Lời nhắc an toàn: ")
        reminders.forEach { reminder ->
            append("$reminder. ")
        }
    }
    if (importantNotes.isNotEmpty()) {
        append("Lưu ý quan trọng: ")
        importantNotes.forEach { note ->
            append("$note. ")
        }
    }
}
