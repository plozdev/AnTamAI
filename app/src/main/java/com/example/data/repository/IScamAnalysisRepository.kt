package com.example.data.repository

import com.example.data.model.ScamAnalysisResult

interface IScamAnalysisRepository {
    suspend fun analyzeSms(
        smsBody: String,
        onStatusUpdate: ((String) -> Unit)? = null
    ): Result<ScamAnalysisResult>

    suspend fun analyzeText(
        text: String,
        model: String = ScamAnalysisRepository.MODEL_FLASH,
        onStatusUpdate: ((String) -> Unit)? = null
    ): Result<ScamAnalysisResult>

    suspend fun analyzeImage(
        base64Data: String,
        mimeType: String = "image/jpeg",
        noteText: String? = null,
        model: String = ScamAnalysisRepository.MODEL_FLASH,
        onStatusUpdate: ((String) -> Unit)? = null
    ): Result<ScamAnalysisResult>
}
