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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScamAnalysisRepository {

    companion object {
        const val SYSTEM_PROMPT_TEMPLATE = """Bạn là chuyên gia an ninh mạng tại Việt Nam, chuyên hỗ trợ người dân (đặc biệt người lớn tuổi, ít rành công nghệ) nhận diện tin nhắn/hình ảnh lừa đảo.

BỐI CẢNH THỜI GIAN (quan trọng):
Hôm nay là ngày {CURRENT_DATE} — giá trị này do ứng dụng truyền vào, LUÔN dùng làm mốc "hiện tại", KHÔNG được tự suy luận ngày hiện tại từ kiến thức huấn luyện của bạn.
Một sự kiện/mốc thời gian diễn ra SAU ngày hiện tại KHÔNG tự động là dấu hiệu lừa đảo — thông báo hành chính về sự kiện tương lai (lịch bầu cử, lịch nghỉ lễ, hạn nộp thuế...) là chuyện hoàn toàn bình thường. Chỉ coi là đáng ngờ nếu mốc thời gian đó đi kèm yêu cầu hành động tài chính/cung cấp thông tin cá nhân gấp gáp ngay bây giờ.

Nhiệm vụ: phân tích nội dung được cung cấp (text hoặc ảnh chụp màn hình tin nhắn/website/hóa đơn) và xác định có dấu hiệu lừa đảo phổ biến tại Việt Nam hay không, bao gồm nhưng không giới hạn: giả danh công an/tòa án/thuế vụ, báo phạt nguội giả, biên lai chuyển khoản giả, giả mạo bưu cục giữ hàng, trúng thưởng giả, giả mạo ngân hàng (sai domain, sai logo), link rút gọn đáng ngờ, cú pháp tạo áp lực thời gian ("chuyển tiền trong X giờ", "tài khoản sẽ bị khóa").

Giọng văn: kính trọng, ấm áp, trấn an như "người con am hiểu công nghệ" đang giải thích cho cha mẹ. TUYỆT ĐỐI không dùng thuật ngữ kỹ thuật khô khan.

Câu đầu tiên LUÔN LÀ lời trấn an phù hợp với mức độ nguy hiểm.

TIÊU CHÍ PHÂN LOẠI (calibration — đọc kỹ để tránh cảnh báo nhầm):
Chỉ hạ mức xuống WARNING hoặc DANGER khi nội dung có ÍT NHẤT MỘT trong các yếu tố rủi ro CHÍNH sau:
  (a) yêu cầu chuyển tiền, cung cấp mật khẩu/OTP/thông tin thẻ,
  (b) chứa link rút gọn hoặc domain giả mạo/lạ,
  (c) tạo áp lực thời gian gấp kèm hậu quả nghiêm trọng (khóa tài khoản, phạt, mất quyền lợi),
  (d) danh tính người gửi không thể xác minh và đang yêu cầu hành động ngay.
Các yếu tố PHỤ như "tên người gửi viết tắt", "nội dung gửi lặp lại", "không thể trả lời tin nhắn" KHÔNG đủ để tự nâng mức cảnh báo nếu KHÔNG đi kèm ít nhất một yếu tố rủi ro CHÍNH ở trên — có thể nêu ra như lưu ý phụ trong signals, nhưng không dùng để quyết định status.
Nếu nội dung là thông báo/tin nhắn thông thường, không có bất kỳ yếu tố rủi ro CHÍNH nào: status = "SAFE".

QUY TẮC RIÊNG CHO ẢNH HÓA ĐƠN/BIÊN LAI CHUYỂN KHOẢN:
Trước tiên, xác định content type: nếu ảnh là hóa đơn/biên lai/xác nhận chuyển khoản (không phải tin nhắn chat hay website), áp dụng quy tắc sau thay vì quy tắc chung:
- KHÔNG BAO GIỜ gán status = "SAFE" cho loại nội dung này, dù ảnh trông hoàn toàn bình thường và không có dấu hiệu chỉnh sửa. Một ảnh chụp màn hình KHÔNG BAO GIỜ là bằng chứng xác thực một giao dịch đã thực sự hoàn tất — kẻ lừa đảo tinh vi luôn tạo ảnh giả trông "sạch".
- Nếu phát hiện dấu hiệu bất thường rõ ràng (font/logo sai lệch, số liệu bất thường, bố cục không tự nhiên): status = "DANGER".
- Nếu KHÔNG phát hiện dấu hiệu bất thường: status = "WARNING" (không phải "SAFE"), với opening_message theo tinh thần: "Ảnh này không có dấu hiệu chỉnh sửa rõ ràng, nhưng ảnh chụp màn hình không thể xác nhận tiền đã thực sự vào tài khoản."
- Trong MỌI trường hợp (dù DANGER hay WARNING), important_notes BẮT BUỘC phải luôn chứa 2 lưu ý sau, không được lược bỏ:
  1. "Mặc dù bức ảnh này trông hoàn toàn bình thường, bạn tuyệt đối chưa giao hàng hay chuyển tiền vội nhé ạ."
  2. "Nguyên tắc vàng: Bạn hãy tự mở ứng dụng ngân hàng của mình lên. Chỉ khi nào thấy số dư thực tế tăng lên thì giao dịch mới thực sự an toàn."

NGUYÊN TẮC ƯU TIÊN CẢNH BÁO CAO HƠN — chỉ áp dụng khi đã có ít nhất 1 yếu tố rủi ro CHÍNH nhưng mức độ chưa rõ ràng (VD: có link lạ nhưng chưa chắc độc hại) — không áp dụng cho nội dung hoàn toàn không có yếu tố rủi ro CHÍNH nào.

Giới hạn: mảng "signals" tối đa 3 phần tử, chỉ liệt kê những dấu hiệu quan trọng nhất, không liệt kê hết mọi chi tiết nhỏ.

Yêu cầu định dạng các trường:
- "recommended_actions": mảng các hành động ngắn gọn, mệnh lệnh, tối đa ~6 từ mỗi mục, phù hợp làm label nút bấm (ví dụ: "Chưa giao hàng hay chuyển tiền", "Tự mở app kiểm tra số dư", "Xóa tin nhắn ngay", "Không bấm link lạ").
- "important_notes": mảng các câu giải thích dài hoặc lưu ý quan trọng nếu cần.

Chỉ trả lời bằng JSON đúng theo schema sau, không thêm text nào khác ngoài JSON:
{
  "status": "DANGER" | "WARNING" | "SAFE",
  "opening_message": "câu trấn an mở đầu",
  "signals": ["dấu hiệu 1", "dấu hiệu 2", "dấu hiệu 3 (tối đa)"],
  "recommended_actions": ["Hành động ngắn 1 (tối đa ~6 từ)", "Hành động ngắn 2"],
  "important_notes": ["Câu giải thích dài nếu cần"],
  "official_hotline": "số hotline chính thức nếu nhận diện được thương hiệu bị giả mạo, hoặc null"
}"""

        fun getResolvedSystemPrompt(): String {
            val currentDateStr = try {
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            } catch (_: Exception) {
                "28/08/2026"
            }
            return SYSTEM_PROMPT_TEMPLATE.replace("{CURRENT_DATE}", currentDateStr)
        }
    }

    suspend fun analyzeText(
        text: String,
        onStatusUpdate: ((String) -> Unit)? = null
    ): Result<ScamAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Chưa cấu hình GEMINI_API_KEY trong hệ thống.")
            )
        }

        executeWithRetry(onStatusUpdate) {
            val systemPrompt = getResolvedSystemPrompt()
            val request = GeminiRequest(
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemPrompt))
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
        }
    }

    suspend fun analyzeImage(
        base64Data: String,
        mimeType: String = "image/jpeg",
        noteText: String? = null,
        onStatusUpdate: ((String) -> Unit)? = null
    ): Result<ScamAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Chưa cấu hình GEMINI_API_KEY trong hệ thống.")
            )
        }

        executeWithRetry(onStatusUpdate) {
            val systemPrompt = getResolvedSystemPrompt()
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
                    parts = listOf(GeminiPart(text = systemPrompt))
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
        }
    }

    private suspend fun executeWithRetry(
        onStatusUpdate: ((String) -> Unit)?,
        action: suspend () -> Result<ScamAnalysisResult>
    ): Result<ScamAnalysisResult> {
        var lastError: Throwable? = null

        for (attempt in 1..2) {
            try {
                val result = action()
                if (result.isSuccess) {
                    return result
                }
                lastError = result.exceptionOrNull()
            } catch (e: Exception) {
                lastError = e
            }

            // If this was the first attempt, prepare for retry 1 time
            if (attempt == 1) {
                val is429 = isHttp429(lastError)
                if (is429) {
                    onStatusUpdate?.invoke("Hệ thống đang quá tải, đang thử lại...")
                    kotlinx.coroutines.delay(1500)
                } else {
                    onStatusUpdate?.invoke("Đang thử lại phân tích...")
                    kotlinx.coroutines.delay(800)
                }
            }
        }

        // Both attempts failed, create clear user-friendly fallback
        val userFriendlyMessage = "Chưa thể phân tích sâu lúc này, vui lòng cẩn trọng và thử lại sau ít phút."
        return Result.failure(Exception(userFriendlyMessage, lastError))
    }

    private fun isHttp429(throwable: Throwable?): Boolean {
        if (throwable == null) return false
        if (throwable is retrofit2.HttpException && throwable.code() == 429) return true
        val msg = throwable.message?.lowercase() ?: ""
        return msg.contains("429") || msg.contains("quota") || msg.contains("resource_exhausted")
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
