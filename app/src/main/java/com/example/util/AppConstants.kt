package com.example.util

object AppConstants {
    const val SIGNAL_SEPARATOR = "|||"
    const val DEFAULT_SMS_FETCH_LIMIT = 100
    const val NETWORK_TIMEOUT_SECONDS = 15L
    const val WORKER_BACKOFF_SECONDS = 30L
    const val MAX_WORKER_RETRIES = 3
    const val GEMINI_MAX_CONCURRENT_REQUESTS = 2

    // Status strings
    const val STATUS_SAFE = "SAFE"
    const val STATUS_WARNING = "WARNING"
    const val STATUS_DANGER = "DANGER"
    const val STATUS_ANALYZING = "ANALYZING"
    const val STATUS_RETRYING = "RETRYING"
}

enum class SmsAnalysisStatus {
    SAFE,
    WARNING,
    DANGER,
    ANALYZING,
    RETRYING;

    companion object {
        fun fromString(status: String?): SmsAnalysisStatus {
            val upper = status?.uppercase() ?: return SAFE
            return when {
                upper.contains("DANGER") -> DANGER
                upper.contains("RETRYING") -> RETRYING
                upper.contains("ANALYZING") -> ANALYZING
                upper.contains("WARNING") -> WARNING
                else -> SAFE
            }
        }
    }
}

enum class ContentType {
    SMS,
    TEXT,
    IMAGE
}
