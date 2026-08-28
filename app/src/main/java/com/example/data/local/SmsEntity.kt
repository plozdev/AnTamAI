package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_entities")
data class SmsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val smsId: Long = 0, // Original ID from system SMS content provider (if available)
    val address: String,
    val body: String,
    val timestamp: Long,
    val heuristicNeedsScrutiny: Boolean = false,
    val heuristicSignals: String = "", // Delimited by "|||"
    val status: String = "SAFE", // "ANALYZING", "SAFE", "WARNING", "DANGER"
    val openingMessage: String = "",
    val resultJson: String = "",
    val isDismissed: Boolean = false
)
