package com.example.util

import com.example.data.model.ScamAnalysisResult
import com.example.data.remote.ApiClient
import com.squareup.moshi.JsonAdapter

object JsonUtils {
    private val scamResultAdapter: JsonAdapter<ScamAnalysisResult> by lazy {
        ApiClient.moshi.adapter(ScamAnalysisResult::class.java)
    }

    fun parseScamAnalysisResult(json: String?): ScamAnalysisResult? {
        if (json.isNullOrBlank()) return null
        return try {
            val cleanedJson = json.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val parsed = scamResultAdapter.fromJson(cleanedJson)
            parsed?.copy(rawJson = cleanedJson)
        } catch (_: Exception) {
            null
        }
    }

    fun toJson(result: ScamAnalysisResult): String {
        return try {
            scamResultAdapter.toJson(result)
        } catch (_: Exception) {
            ""
        }
    }
}
