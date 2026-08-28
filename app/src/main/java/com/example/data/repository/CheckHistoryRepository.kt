package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.CheckHistoryDao
import com.example.data.local.CheckHistoryEntity
import kotlinx.coroutines.flow.Flow

class CheckHistoryRepository(private val dao: CheckHistoryDao) {

    val allHistory: Flow<List<CheckHistoryEntity>> = dao.getAllHistory()

    suspend fun saveCheckHistory(
        contentType: String,
        contentPreview: String,
        status: String,
        openingMessage: String = "",
        resultJson: String = ""
    ): Long {
        val entity = CheckHistoryEntity(
            contentType = contentType,
            contentPreview = contentPreview,
            status = status,
            openingMessage = openingMessage,
            resultJson = resultJson,
            timestamp = System.currentTimeMillis()
        )
        return dao.insertHistory(entity)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
