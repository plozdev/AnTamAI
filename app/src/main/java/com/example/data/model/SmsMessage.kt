package com.example.data.model

data class HeuristicResult(
    val needsScrutiny: Boolean,
    val matchedSignals: List<String> = emptyList(),
    val summary: String = if (needsScrutiny) "Có từ khóa đáng ngờ" else "Bình thường"
)

data class SmsMessage(
    val id: Long,
    val address: String,
    val body: String,
    val date: Long,
    val heuristicResult: HeuristicResult
)
