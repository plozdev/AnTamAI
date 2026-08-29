package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.local.CheckHistoryEntity
import com.example.data.repository.ScamAnalysisRepository
import com.example.util.HeuristicFilter
import com.example.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsAnalysisWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_SMS_RECORD_ID = "key_sms_record_id"
        const val KEY_ADDRESS = "key_address"
        const val KEY_BODY = "key_body"
        const val KEY_TIMESTAMP = "key_timestamp"
        const val KEY_SHOW_NOTIFICATION = "key_show_notification"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val recordId = inputData.getLong(KEY_SMS_RECORD_ID, -1L)
        val address = inputData.getString(KEY_ADDRESS) ?: "Không rõ"
        val body = inputData.getString(KEY_BODY) ?: ""
        val timestamp = inputData.getLong(KEY_TIMESTAMP, System.currentTimeMillis())
        val showNotification = inputData.getBoolean(KEY_SHOW_NOTIFICATION, true)

        if (body.isBlank()) {
            return@withContext Result.success()
        }

        val db = AppDatabase.getInstance(context)
        val smsDao = db.smsDao()
        val historyDao = db.checkHistoryDao()

        // Step 1: Run HeuristicFilter locally
        val heuristicResult = HeuristicFilter.analyze(body)

        // If NOT suspicious: update status to SAFE immediately, DO NOT call Gemini, finish to save quota
        if (!heuristicResult.needsScrutiny) {
            if (recordId > 0) {
                smsDao.updateAnalysisResult(
                    id = recordId,
                    status = "SAFE",
                    openingMessage = "Tin nhắn không có dấu hiệu đáng ngờ.",
                    resultJson = ""
                )
            }
            return@withContext Result.success()
        }

        // Step 2: Suspicious sign detected -> Call Gemini Flash-Lite via ScamAnalysisRepository
        val repository = ScamAnalysisRepository()
        val analysisResult = repository.analyzeSms(body)

        analysisResult.fold(
            onSuccess = { scamResult ->
                val status = scamResult.status.uppercase()
                val openingMessage = scamResult.openingMessage
                val rawJson = scamResult.rawJson

                if (recordId > 0) {
                    smsDao.updateAnalysisResult(
                        id = recordId,
                        status = status,
                        openingMessage = openingMessage,
                        resultJson = rawJson
                    )
                }

                // Also save to history so it is safely archived
                try {
                    historyDao.insertHistory(
                        CheckHistoryEntity(
                            timestamp = timestamp,
                            contentType = "SMS",
                            contentPreview = if (body.length > 80) body.take(80) + "..." else body,
                            status = status,
                            openingMessage = openingMessage,
                            resultJson = rawJson
                        )
                    )
                } catch (_: Exception) { }

                // If status is DANGER or WARNING and notifications enabled: trigger notification
                if (showNotification && (status.contains("DANGER") || status.contains("WARNING"))) {
                    NotificationHelper.showScamAlertNotification(
                        context = context,
                        smsRecordId = recordId,
                        address = address,
                        body = body,
                        status = status,
                        openingMessage = openingMessage,
                        resultJson = rawJson
                    )
                }

                Result.success()
            },
            onFailure = { _ ->
                // If Gemini call fails, check retry count (up to 3 retries)
                if (runAttemptCount < 3) {
                    if (recordId > 0) {
                        smsDao.updateAnalysisResult(
                            id = recordId,
                            status = "RETRYING",
                            openingMessage = "Đang chờ phân tích lại...",
                            resultJson = ""
                        )
                    }
                    Result.retry()
                } else {
                    // Exceeded max retries: mark as WARNING with clear notice
                    if (recordId > 0) {
                        smsDao.updateAnalysisResult(
                            id = recordId,
                            status = "WARNING",
                            openingMessage = "Tin nhắn có từ khóa nghi ngờ. Chưa thể kết nối máy chủ phân tích sâu, vui lòng cẩn trọng không bấm link hay chuyển tiền!",
                            resultJson = ""
                        )
                    }
                    Result.success()
                }
            }
        )
    }
}
