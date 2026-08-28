package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.example.data.model.SmsMessage
import com.example.util.HeuristicFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsRepository(private val context: Context) {

    suspend fun getInboxMessages(limit: Int = 100): Result<List<SmsMessage>> = withContext(Dispatchers.IO) {
        try {
            val messages = mutableListOf<SmsMessage>()
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
                    val body = if (bodyIndex != -1) it.getString(bodyIndex) ?: "" else ""
                    val date = if (dateIndex != -1) it.getLong(dateIndex) else System.currentTimeMillis()

                    val heuristicResult = HeuristicFilter.analyze(body)

                    messages.add(
                        SmsMessage(
                            id = id,
                            address = address,
                            body = body,
                            date = date,
                            heuristicResult = heuristicResult
                        )
                    )
                    count++
                }
            }

            Result.success(messages)
        } catch (e: SecurityException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
