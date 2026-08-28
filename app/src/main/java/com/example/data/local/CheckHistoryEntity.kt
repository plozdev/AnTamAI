package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_history")
data class CheckHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val contentType: String, // "TEXT" or "IMAGE"
    val contentPreview: String,
    val status: String, // "DANGER", "WARNING", "SAFE"
    val openingMessage: String = "",
    val resultJson: String = ""
)
