package com.example.util

import android.util.Log
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
        } catch (e: Exception) {
            Log.w("AnTamAI", "Failed to parse ScamAnalysisResult from JSON: $json", e)
            null
        }
    }

    fun toJson(result: ScamAnalysisResult): String {
        return try {
            scamResultAdapter.toJson(result)
        } catch (e: Exception) {
            Log.w("AnTamAI", "Failed to serialize ScamAnalysisResult to JSON", e)
            ""
        }
    }
}
