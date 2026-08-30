package com.example.data.repository

import com.example.data.local.SmsEntity
import com.example.data.model.SmsMessage
import com.example.util.AppConstants
import kotlinx.coroutines.flow.Flow

interface ISmsRepository {
    fun getAllSmsFlow(): Flow<List<SmsEntity>>
    suspend fun syncInboxMessages(limit: Int = AppConstants.DEFAULT_SMS_FETCH_LIMIT): Result<Unit>
    suspend fun setDismissed(id: Long, isDismissed: Boolean)
    suspend fun dismissAllSuspicious()
    suspend fun getInboxMessages(limit: Int = AppConstants.DEFAULT_SMS_FETCH_LIMIT): Result<List<SmsMessage>>
    suspend fun insertIncomingSms(sender: String, body: String, timestamp: Long): Long
    suspend fun updateAnalysisResult(id: Long, status: String, openingMessage: String, resultJson: String)
    fun getDangerousMessageCountFlow(): Flow<Int>
}
