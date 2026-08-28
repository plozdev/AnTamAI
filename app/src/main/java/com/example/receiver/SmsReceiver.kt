package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.AppDatabase
import com.example.data.local.SmsEntity
import com.example.data.repository.SettingsRepository
import com.example.worker.SmsAnalysisWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val settingsRepo = SettingsRepository(context)
        if (!settingsRepo.getAutoScanSms()) {
            return
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].originatingAddress ?: "Không rõ"
        val bodyBuilder = StringBuilder()
        var timestamp = System.currentTimeMillis()

        for (msg in messages) {
            bodyBuilder.append(msg.messageBody ?: "")
            if (msg.timestampMillis > 0) {
                timestamp = msg.timestampMillis
            }
        }
        val fullBody = bodyBuilder.toString().trim()
        if (fullBody.isBlank()) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val newSms = SmsEntity(
                    smsId = 0,
                    address = sender,
                    body = fullBody,
                    timestamp = timestamp,
                    heuristicNeedsScrutiny = true,
                    heuristicSignals = "",
                    status = "ANALYZING",
                    openingMessage = "Đang kiểm tra an toàn...",
                    resultJson = ""
                )
                val insertedId = db.smsDao().insertSms(newSms)

                val inputData = Data.Builder()
                    .putLong(SmsAnalysisWorker.KEY_SMS_RECORD_ID, insertedId)
                    .putString(SmsAnalysisWorker.KEY_ADDRESS, sender)
                    .putString(SmsAnalysisWorker.KEY_BODY, fullBody)
                    .putLong(SmsAnalysisWorker.KEY_TIMESTAMP, timestamp)
                    .putBoolean(SmsAnalysisWorker.KEY_SHOW_NOTIFICATION, true)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<SmsAnalysisWorker>()
                    .setInputData(inputData)
                    .build()

                WorkManager.getInstance(context.applicationContext).enqueue(workRequest)
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}
