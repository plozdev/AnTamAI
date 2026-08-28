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
import com.example.util.HeuristicFilter
import com.example.worker.SmsAnalysisWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.math.abs

class SmsRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val smsDao = db.smsDao()

    fun getAllSmsFlow(): Flow<List<SmsEntity>> = smsDao.getAllSms()

    suspend fun syncInboxMessages(limit: Int = 100): Result<Unit> = withContext(Dispatchers.IO) {
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

            cursor?.use {
                val idIndex = it.getColumnIndex(Telephony.Sms._ID)
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)

                var count = 0
                while (it.moveToNext() && count < limit) {
                    val id = if (idIndex != -1) it.getLong(idIndex) else count.toLong()
                    val address = if (addressIndex != -1) it.getString(addressIndex) ?: "Không rõ" else "Không rõ"
                    val body = if (bodyIndex != -1) it.getString(bodyIndex)?.trim() ?: "" else ""
                    val date = if (dateIndex != -1) it.getLong(dateIndex) else System.currentTimeMillis()

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
            for (msg in rawMessages) {
                val isDuplicate = dedupedMessages.any { existing ->
                    existing.id == msg.id || (
                        existing.address.equals(msg.address, ignoreCase = true) &&
                        existing.body == msg.body &&
                        abs(existing.date - msg.date) < 10_000L
                    )
                }
                if (!isDuplicate) {
                    dedupedMessages.add(msg)
                }
            }

            // Sync with Room
            for (msg in dedupedMessages) {
                val existing = smsDao.findSmsByContent(msg.address, msg.body)
                if (existing == null) {
                    val needsScrutiny = msg.heuristicResult.needsScrutiny
                    val signalsJoined = msg.heuristicResult.matchedSignals.joinToString("|||")

                    val initialStatus = if (needsScrutiny) "ANALYZING" else "SAFE"
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

    suspend fun getInboxMessages(limit: Int = 120): Result<List<SmsMessage>> = withContext(Dispatchers.IO) {
        // Sync first, then map from Room or return direct
        syncInboxMessages(limit)
        try {
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

            val rawMessages = mutableListOf<SmsMessage>()
            cursor?.use {
                val idIndex = it.getColumnIndex(Telephony.Sms._ID)
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)

                var count = 0
                while (it.moveToNext() && count < limit) {
                    val id = if (idIndex != -1) it.getLong(idIndex) else count.toLong()
                    val address = if (addressIndex != -1) it.getString(addressIndex) ?: "Không rõ" else "Không rõ"
                    val body = if (bodyIndex != -1) it.getString(bodyIndex)?.trim() ?: "" else ""
                    val date = if (dateIndex != -1) it.getLong(dateIndex) else System.currentTimeMillis()

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

            Result.success(rawMessages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

