package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.repository.IScamAnalysisRepository
import com.example.data.repository.ISmsRepository
import com.example.data.repository.ScamAnalysisRepository
import com.example.data.repository.SmsRepository
import com.example.util.AppConstants
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

        fun enqueue(
            context: Context,
            id: Long,
            address: String,
            body: String,
            timestamp: Long,
            showNotification: Boolean
        ) {
            val inputData = androidx.work.Data.Builder()
                .putLong(KEY_SMS_RECORD_ID, id)
                .putString(KEY_ADDRESS, address)
                .putString(KEY_BODY, body)
                .putLong(KEY_TIMESTAMP, timestamp)
                .putBoolean(KEY_SHOW_NOTIFICATION, showNotification)
                .build()

            val workRequest = androidx.work.OneTimeWorkRequestBuilder<SmsAnalysisWorker>()
                .setInputData(inputData)
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    AppConstants.WORKER_BACKOFF_SECONDS,
                    java.util.concurrent.TimeUnit.SECONDS
                )
                .build()

            androidx.work.WorkManager.getInstance(context.applicationContext).enqueue(workRequest)
        }
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

        try {
            val smsRepository: ISmsRepository = SmsRepository(context)

            // Step 1: Run HeuristicFilter locally
            val heuristicResult = HeuristicFilter.analyze(body)

            // If NOT suspicious: update status to SAFE immediately, DO NOT call Gemini, finish to save quota
            if (!heuristicResult.needsScrutiny) {
                if (recordId > 0) {
                    smsRepository.updateAnalysisResult(
                        id = recordId,
                        status = AppConstants.STATUS_SAFE,
                        openingMessage = "Tin nhắn không có dấu hiệu đáng ngờ.",
                        resultJson = ""
                    )
                }
                return@withContext Result.success()
            }

            // Step 2: Suspicious sign detected -> Call Gemini Flash-Lite via ScamAnalysisRepository
            val repository: IScamAnalysisRepository = ScamAnalysisRepository.getInstance()
            val analysisResult = repository.analyzeSms(body)

            analysisResult.fold(
                onSuccess = { scamResult ->
                    val status = scamResult.status.uppercase()
                    val openingMessage = scamResult.openingMessage
                    val rawJson = scamResult.rawJson

                    if (recordId > 0) {
                        smsRepository.updateAnalysisResult(
                            id = recordId,
                            status = status,
                            openingMessage = openingMessage,
                            resultJson = rawJson
                        )
                    }

                    // If status is DANGER or WARNING and notifications enabled: trigger notification
                    if (showNotification && (status.contains(AppConstants.STATUS_DANGER) || status.contains(AppConstants.STATUS_WARNING))) {
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
                onFailure = { error ->
                    Log.e("AnTamAI", "Gemini SMS analysis failed on attempt $runAttemptCount", error)
                    // If Gemini call fails, check retry count (up to MAX_WORKER_RETRIES)
                    if (runAttemptCount < AppConstants.MAX_WORKER_RETRIES) {
                        if (recordId > 0) {
                            smsRepository.updateAnalysisResult(
                                id = recordId,
                                status = AppConstants.STATUS_RETRYING,
                                openingMessage = "Đang chờ phân tích lại...",
                                resultJson = ""
                            )
                        }
                        Result.retry()
                    } else {
                        // Exceeded max retries: mark as WARNING with clear notice
                        if (recordId > 0) {
                            smsRepository.updateAnalysisResult(
                                id = recordId,
                                status = AppConstants.STATUS_WARNING,
                                openingMessage = "Tin nhắn có từ khóa nghi ngờ. Chưa thể kết nối máy chủ phân tích sâu, vui lòng cẩn trọng không bấm link hay chuyển tiền!",
                                resultJson = ""
                            )
                        }
                        Result.success()
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("AnTamAI", "Unhandled exception in SmsAnalysisWorker", e)
            Result.failure()
        }
    }
}
