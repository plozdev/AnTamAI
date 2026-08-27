package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class ScamStatus {
    DANGER,
    WARNING,
    SAFE,
    UNKNOWN
}

@JsonClass(generateAdapter = true)
data class ScamAnalysisResult(
    @param:Json(name = "status") val status: String = "WARNING",
    @param:Json(name = "opening_message") val openingMessage: String = "",
    @param:Json(name = "signals") val signals: List<String> = emptyList(),
    @param:Json(name = "recommended_actions") val recommendedActions: List<String> = emptyList(),
    @param:Json(name = "official_hotline") val officialHotline: String? = null,
    val rawJson: String = ""
) {
    val scamStatus: ScamStatus
        get() = try {
            ScamStatus.valueOf(status.uppercase())
        } catch (_: Exception) {
            ScamStatus.UNKNOWN
        }
}
