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
            try {
                if (com.example.BuildConfig.DEBUG) {
                    Log.w("AnTamAI", "Failed to parse ScamAnalysisResult from JSON: $json", e)
                } else {
                    Log.w("AnTamAI", "Failed to parse ScamAnalysisResult", e)
                }
            } catch (_: Throwable) {
                // Ignore log errors in pure JVM unit test environment
            }
            null
        }
    }

    fun toJson(result: ScamAnalysisResult): String {
        return try {
            scamResultAdapter.toJson(result)
        } catch (e: Exception) {
            try {
                Log.w("AnTamAI", "Failed to serialize ScamAnalysisResult to JSON", e)
            } catch (_: Throwable) {
                // Ignore log errors in pure JVM unit test environment
            }
            ""
        }
    }
}
