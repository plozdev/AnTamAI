package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {

    @Query("SELECT * FROM sms_entities ORDER BY timestamp DESC")
    fun getAllSms(): Flow<List<SmsEntity>>

    @Query("SELECT * FROM sms_entities WHERE id = :id LIMIT 1")
    suspend fun getSmsById(id: Long): SmsEntity?

    @Query("SELECT * FROM sms_entities WHERE address = :address AND body = :body LIMIT 1")
    suspend fun findSmsByContent(address: String, body: String): SmsEntity?

    @Query("SELECT * FROM sms_entities WHERE status = 'ANALYZING'")
    suspend fun getAnalyzingSms(): List<SmsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSms(item: SmsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SmsEntity>): List<Long>

    @Update
    suspend fun updateSms(item: SmsEntity)

    @Query("UPDATE sms_entities SET status = :status, openingMessage = :openingMessage, resultJson = :resultJson WHERE id = :id")
    suspend fun updateAnalysisResult(id: Long, status: String, openingMessage: String, resultJson: String)

    @Query("DELETE FROM sms_entities WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM sms_entities")
    suspend fun clearAll()
}
