package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.AppDatabase
import com.example.data.local.SmsEntity
import com.example.data.model.HeuristicResult
import com.example.data.model.SmsMessage
import com.example.util.AppConstants
import com.example.util.HeuristicFilter
import com.example.worker.SmsAnalysisWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class SmsRepository(private val context: Context) : ISmsRepository {

    private val database = AppDatabase.getInstance(context)
    private val smsDao = database.smsDao()

    override fun getAllSmsFlow(): Flow<List<SmsEntity>> = smsDao.getAllSms()

    override suspend fun syncInboxMessages(limit: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val rawMessages = mutableListOf<SmsMessage>()
            val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
            )

            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use { activeCursor ->
                val idIndex = activeCursor.getColumnIndex(Telephony.Sms._ID)
                val addressIndex = activeCursor.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = activeCursor.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = activeCursor.getColumnIndex(Telephony.Sms.DATE)

                var count = 0
                while (activeCursor.moveToNext() && count < limit) {
                    val id = if (idIndex != -1) activeCursor.getLong(idIndex) else count.toLong()
                    val address = if (addressIndex != -1) activeCursor.getString(addressIndex) ?: "Không rõ" else "Không rõ"
                    val body = if (bodyIndex != -1) activeCursor.getString(bodyIndex)?.trim() ?: "" else ""
                    val date = if (dateIndex != -1) activeCursor.getLong(dateIndex) else System.currentTimeMillis()

                    if (body.isNotBlank()) {
                        val heuristicResult = HeuristicFilter.analyze(body)
                        rawMessages.add(
                            SmsMessage(
                                id = id,
                                address = address.trim(),
                                body = body,
                                date = date,
                                heuristicResult = heuristicResult
                            )
                        )
                        count++
                    }
                }
            }

            // Deduplication logic
            val dedupedMessages = mutableListOf<SmsMessage>()
            for (message in rawMessages) {
                val isDuplicate = dedupedMessages.any { existing ->
                    existing.id == message.id || (
                        existing.address.equals(message.address, ignoreCase = true) &&
                        existing.body == message.body &&
                        abs(existing.date - message.date) < 10_000L
                    )
                }
                if (!isDuplicate) {
                    dedupedMessages.add(message)
                }
            }

            // Sync with Room
            for (msg in dedupedMessages) {
                val existing = smsDao.findSmsByContent(msg.address, msg.body)
                if (existing == null) {
                    val needsScrutiny = msg.heuristicResult.needsScrutiny
                    val signalsJoined = msg.heuristicResult.matchedSignals.joinToString(AppConstants.SIGNAL_SEPARATOR)

                    val initialStatus = if (needsScrutiny) AppConstants.STATUS_ANALYZING else AppConstants.STATUS_SAFE
                    val initialOpening = if (needsScrutiny) "Đang phân tích bảo mật..." else "Tin nhắn không có dấu hiệu đáng ngờ."

                    val newEntity = SmsEntity(
                        smsId = msg.id,
                        address = msg.address,
                        body = msg.body,
                        timestamp = msg.date,
                        heuristicNeedsScrutiny = needsScrutiny,
                        heuristicSignals = signalsJoined,
                        status = initialStatus,
                        openingMessage = initialOpening,
                        resultJson = ""
                    )

                    val insertedId = smsDao.insertSms(newEntity)

                    // If flagged as needing scrutiny, enqueue WorkManager for deep Gemini analysis
                    if (needsScrutiny) {
                        val inputData = Data.Builder()
                            .putLong(SmsAnalysisWorker.KEY_SMS_RECORD_ID, insertedId)
                            .putString(SmsAnalysisWorker.KEY_ADDRESS, msg.address)
                            .putString(SmsAnalysisWorker.KEY_BODY, msg.body)
                            .putLong(SmsAnalysisWorker.KEY_TIMESTAMP, msg.date)
                            .putBoolean(SmsAnalysisWorker.KEY_SHOW_NOTIFICATION, false)
                            .build()

                        val workRequest = OneTimeWorkRequestBuilder<SmsAnalysisWorker>()
                            .setInputData(inputData)
                            .setBackoffCriteria(
                                androidx.work.BackoffPolicy.EXPONENTIAL,
                                AppConstants.WORKER_BACKOFF_SECONDS,
                                TimeUnit.SECONDS
                            )
                            .build()

                        WorkManager.getInstance(context.applicationContext).enqueue(workRequest)
                    }
                }
            }

            Result.success(Unit)
        } catch (e: SecurityException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setDismissed(id: Long, isDismissed: Boolean) = withContext(Dispatchers.IO) {
        smsDao.setDismissed(id, isDismissed)
    }

    override suspend fun dismissAllSuspicious() = withContext(Dispatchers.IO) {
        smsDao.dismissAllSuspicious()
    }

    override suspend fun getInboxMessages(limit: Int): Result<List<SmsMessage>> = withContext(Dispatchers.IO) {
        val syncResult = syncInboxMessages(limit)
        if (syncResult.isFailure) {
            return@withContext Result.failure(syncResult.exceptionOrNull() ?: Exception("Lỗi đồng bộ SMS"))
        }
        try {
            val entities = smsDao.getAllSmsDirect()
            val mapped = entities.map { entity ->
                SmsMessage(
                    id = entity.smsId,
                    address = entity.address,
                    body = entity.body,
                    date = entity.timestamp,
                    heuristicResult = HeuristicResult(
                        needsScrutiny = entity.heuristicNeedsScrutiny,
                        matchedSignals = if (entity.heuristicSignals.isNotBlank()) entity.heuristicSignals.split(AppConstants.SIGNAL_SEPARATOR) else emptyList()
                    )
                )
            }
            Result.success(mapped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun insertIncomingSms(sender: String, body: String, timestamp: Long): Long = withContext(Dispatchers.IO) {
        val newSms = SmsEntity(
            smsId = 0,
            address = sender,
            body = body,
            timestamp = timestamp,
            heuristicNeedsScrutiny = true,
            heuristicSignals = "",
            status = AppConstants.STATUS_ANALYZING,
            openingMessage = "Đang kiểm tra an toàn...",
            resultJson = ""
        )
        smsDao.insertSms(newSms)
    }

    override suspend fun updateAnalysisResult(id: Long, status: String, openingMessage: String, resultJson: String) = withContext(Dispatchers.IO) {
        smsDao.updateAnalysisResult(id, status, openingMessage, resultJson)
    }
}

