package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.example.data.model.SmsMessage
import com.example.util.HeuristicFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class SmsRepository(private val context: Context) {

    suspend fun getInboxMessages(limit: Int = 120): Result<List<SmsMessage>> = withContext(Dispatchers.IO) {
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

            // Deduplication logic:
            // 1. Remove duplicate ID entries
            // 2. Remove identical (address, body) if timestamps are within 10 seconds of each other
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

            Result.success(dedupedMessages)
        } catch (e: SecurityException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
