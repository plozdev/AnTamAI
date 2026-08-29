package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.data.repository.SettingsRepository
import com.example.data.repository.SmsRepository
import com.example.worker.SmsAnalysisWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val settingsRepository = SettingsRepository(context)
        if (!settingsRepository.getAutoScanSms()) {
            return
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].originatingAddress ?: "Không rõ"
        val bodyBuilder = StringBuilder()
        var timestamp = System.currentTimeMillis()

        for (message in messages) {
            bodyBuilder.append(message.messageBody ?: "")
            if (message.timestampMillis > 0) {
                timestamp = message.timestampMillis
            }
        }
        val fullBody = bodyBuilder.toString().trim()
        if (fullBody.isBlank()) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val smsRepository = SmsRepository(context)
                val insertedId = smsRepository.insertIncomingSms(
                    sender = sender,
                    body = fullBody,
                    timestamp = timestamp
                )

                SmsAnalysisWorker.enqueue(
                    context = context,
                    id = insertedId,
                    address = sender,
                    body = fullBody,
                    timestamp = timestamp,
                    showNotification = true
                )
            } catch (e: Exception) {
                Log.e("AnTamAI", "Error processing incoming SMS in SmsReceiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
