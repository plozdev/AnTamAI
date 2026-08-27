package com.example.data.repository

import com.example.BuildConfig
import com.example.data.model.GeminiContent
import com.example.data.model.GeminiGenerationConfig
import com.example.data.model.GeminiInlineData
import com.example.data.model.GeminiPart
import com.example.data.model.GeminiRequest
import com.example.data.model.ScamAnalysisResult
import com.example.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScamAnalysisRepository {

    companion object {
        const val SYSTEM_PROMPT = """Bạn là chuyên gia an ninh mạng tại Việt Nam, chuyên hỗ trợ người dân nhận diện tin nhắn/hình ảnh lừa đảo.

Nhiệm vụ: phân tích nội dung được cung cấp (text hoặc ảnh chụp màn hình tin nhắn/website/hóa đơn) và xác định có dấu hiệu lừa đảo phổ biến tại Việt Nam hay không, bao gồm nhưng không giới hạn: giả danh công an/tòa án/thuế vụ, báo phạt nguội giả, biên lai chuyển khoản giả, giả mạo bưu cục giữ hàng, trúng thưởng giả, giả mạo ngân hàng (sai domain, sai logo), link rút gọn đáng ngờ, cú pháp tạo áp lực thời gian ("chuyển tiền trong X giờ", "tài khoản sẽ bị khóa").

Giọng văn: ân cần, rõ ràng, trấn an như một người bạn am hiểu công nghệ đang giải thích cho bạn. TUYỆT ĐỐI không dùng thuật ngữ kỹ thuật khô khan (vd: không nói "domain không khớp", mà nói "địa chỉ web ngân hàng thật là ...vietcombank.com.vn, còn link này viết sai thành ...vietconbank, đây là dấu hiệu giả mạo").

Xưng hô: dùng danh xưng "bạn" (không dùng ba mẹ, bác hay chú).

Câu đầu tiên LUÔN LÀ lời trấn an phù hợp với mức độ nguy hiểm.

QUY TẮC RIÊNG CHO ẢNH HÓA ĐƠN/BIÊN LAI CHUYỂN KHOẢN:
Trước tiên, xác định content type: nếu ảnh là hóa đơn/biên lai/xác nhận chuyển khoản (không phải tin nhắn chat hay website), áp dụng quy tắc sau thay vì quy tắc chung:
- KHÔNG BAO GIỜ gán status = "SAFE" cho loại nội dung này, dù ảnh trông hoàn toàn bình thường và không có dấu hiệu chỉnh sửa. Một ảnh chụp màn hình KHÔNG BAO GIỜ là bằng chứng xác thực một giao dịch đã thực sự hoàn tất — kẻ lừa đảo tinh vi luôn tạo ảnh giả trông "sạch".
- Nếu phát hiện dấu hiệu bất thường rõ ràng (font/logo sai lệch, số liệu bất thường, bố cục không tự nhiên): status = "DANGER".
- Nếu KHÔNG phát hiện dấu hiệu bất thường: status = "WARNING" (không phải "SAFE"), với opening_message theo tinh thần: "Ảnh này không có dấu hiệu chỉnh sửa rõ ràng, nhưng ảnh chụp màn hình không thể xác nhận tiền đã thực sự vào tài khoản."
- Trong MỌI trường hợp (dù DANGER hay WARNING), recommended_actions BẮT BUỘC phải luôn chứa 2 hành động sau, không được lược bỏ:
  1. "Mặc dù bức ảnh này trông hoàn toàn bình thường, bạn tuyệt đối chưa giao hàng hay chuyển tiền vội nhé ạ."
  2. "Nguyên tắc vàng: Bạn hãy tự mở ứng dụng ngân hàng của mình lên. Chỉ khi nào thấy số dư thực tế tăng lên thì giao dịch mới thực sự an toàn."

NGUYÊN TẮC CHUNG (áp dụng cho mọi loại nội dung, không riêng biên lai):
Khi không chắc chắn giữa 2 mức độ, LUÔN chọn mức cảnh báo cao hơn (ưu tiên WARNING hơn SAFE, ưu tiên DANGER hơn WARNING nếu có bất kỳ tín hiệu đáng ngờ nào, dù nhỏ). False positive (cảnh báo nhầm nội dung an toàn) ít gây hại hơn nhiều so với false negative (bỏ sót lừa đảo thật).

Chỉ trả lời bằng JSON đúng theo schema sau, không thêm text nào khác ngoài JSON:
{
  "status": "DANGER" | "WARNING" | "SAFE",
  "opening_message": "câu trấn an mở đầu",
  "signals": ["dấu hiệu 1 bằng ngôn ngữ đời thường", "dấu hiệu 2", "dấu hiệu 3"],
  "recommended_actions": ["hành động cụ thể 1", "hành động cụ thể 2"],
  "official_hotline": "số hotline chính thức nếu nhận diện được thương hiệu bị giả mạo, hoặc null"
}"""
    }

    suspend fun analyzeText(text: String): Result<ScamAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(
                    IllegalStateException("Chưa cấu hình GEMINI_API_KEY trong hệ thống.")
                )
            }

            val request = GeminiRequest(
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = SYSTEM_PROMPT))
                ),
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = "Hãy phân tích nội dung sau để kiểm tra dấu hiệu lừa đảo:\n\n$text")
                        )
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    responseMimeType = "application/json"
                )
            )

            val response = ApiClient.geminiService.generateContent(apiKey, request)
            parseGeminiResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeImage(
        base64Data: String,
        mimeType: String = "image/jpeg",
        noteText: String? = null
    ): Result<ScamAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(
                    IllegalStateException("Chưa cấu hình GEMINI_API_KEY trong hệ thống.")
                )
            }

            val parts = mutableListOf<GeminiPart>()
            val promptText = if (!noteText.isNullOrBlank()) {
                "Hãy kiểm tra bức ảnh này (kèm ghi chú: \"$noteText\") để nhận diện dấu hiệu lừa đảo hoặc biên lai giả."
            } else {
                "Hãy kiểm tra bức ảnh này để nhận diện dấu hiệu lừa đảo hoặc biên lai chuyển khoản giả mạo."
            }

            parts.add(GeminiPart(text = promptText))
            parts.add(
                GeminiPart(
                    inlineData = GeminiInlineData(
                        mimeType = mimeType,
                        data = base64Data
                    )
                )
            )

            val request = GeminiRequest(
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = SYSTEM_PROMPT))
                ),
                contents = listOf(
                    GeminiContent(parts = parts)
                ),
                generationConfig = GeminiGenerationConfig(
                    responseMimeType = "application/json"
                )
            )

            val response = ApiClient.geminiService.generateContent(apiKey, request)
            parseGeminiResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseGeminiResponse(response: com.example.data.model.GeminiResponse): Result<ScamAnalysisResult> {
        val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: return Result.failure(IllegalStateException(response.error?.message ?: "Không nhận được phản hồi từ AI"))

        val cleanedJson = cleanJsonString(rawText)
        return try {
            val adapter = ApiClient.moshi.adapter(ScamAnalysisResult::class.java)
            val parsed = adapter.fromJson(cleanedJson)
            if (parsed != null) {
                Result.success(parsed.copy(rawJson = cleanedJson))
            } else {
                Result.failure(IllegalStateException("Không thể đọc định dạng dữ liệu phản hồi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun cleanJsonString(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json").trim()
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```").trim()
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```").trim()
        }
        return text
    }
}
