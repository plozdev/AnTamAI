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
data class ActionItem(
    @param:Json(name = "label") val label: String? = null,
    @param:Json(name = "phone") val phone: String? = null
)

@JsonClass(generateAdapter = true)
data class ScamAnalysisResult(
    @param:Json(name = "status") val status: String = "WARNING",
    @param:Json(name = "opening_message") val openingMessage: String = "",
    @param:Json(name = "signals") val signals: List<String> = emptyList(),
    @param:Json(name = "reminders") val reminders: List<String> = emptyList(),
    @param:Json(name = "action") val action: ActionItem? = null,
    @param:Json(name = "important_notes") val importantNotes: List<String> = emptyList(),
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
