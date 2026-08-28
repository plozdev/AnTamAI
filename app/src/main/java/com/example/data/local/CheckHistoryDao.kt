package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckHistoryDao {

    @Query("SELECT * FROM check_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CheckHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: CheckHistoryEntity): Long

    @Query("DELETE FROM check_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM check_history")
    suspend fun clearAll()
}
