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
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScamAnalysisRepository : IScamAnalysisRepository {

    companion object {
        @Volatile
        private var instance: ScamAnalysisRepository? = null

        fun getInstance(): ScamAnalysisRepository {
            return instance ?: synchronized(this) {
                instance ?: ScamAnalysisRepository().also { instance = it }
            }
        }

        // Global rate limiter: maximum concurrent Gemini API requests across entire app
        val geminiRateLimiter = Semaphore(com.example.util.AppConstants.GEMINI_MAX_CONCURRENT_REQUESTS)

        // Models: Flash-Lite for fast SMS scanning (Tab 1), Flash for manual check & multimodal images (Tab 2)
        const val MODEL_FLASH_LITE = "gemini-3.1-flash-lite-preview"
        const val MODEL_FLASH = "gemini-3.5-flash"

        const val SYSTEM_PROMPT_TEMPLATE = """Bạn là chuyên gia an ninh mạng tại Việt Nam, chuyên hỗ trợ người dùng nhận diện tin nhắn/hình ảnh lừa đảo.

BỐI CẢNH THỜI GIAN (quan trọng):
Hôm nay là ngày {CURRENT_DATE} — giá trị này do ứng dụng truyền vào, LUÔN dùng làm mốc "hiện tại", KHÔNG được tự suy luận ngày hiện tại từ kiến thức huấn luyện của bạn.
Một sự kiện/mốc thời gian diễn ra SAU ngày hiện tại KHÔNG tự động là dấu hiệu lừa đảo — thông báo hành chính về sự kiện tương lai (lịch bầu cử, lịch nghỉ lễ, hạn nộp thuế...) là chuyện hoàn toàn bình thường. Chỉ coi là đáng ngờ nếu mốc thời gian đó đi kèm yêu cầu hành động tài chính/cung cấp thông tin cá nhân gấp gáp ngay bây giờ.

Nhiệm vụ: phân tích nội dung được cung cấp (text hoặc ảnh chụp màn hình tin nhắn/website/hóa đơn) và xác định có dấu hiệu lừa đảo phổ biến tại Việt Nam hay không, bao gồm nhưng không giới hạn: giả danh công an/tòa án/thuế vụ, báo phạt nguội giả, biên lai chuyển khoản giả, giả mạo bưu cục giữ hàng, trúng thưởng giả, giả mạo ngân hàng (sai domain, sai logo), link rút gọn đáng ngờ, cú pháp tạo áp lực thời gian ("chuyển tiền trong X giờ", "tài khoản sẽ bị khóa").

Giọng văn: ân cần, gần gũi, trấn an, xưng hô là "bạn" (ví dụ: "Bạn bình tĩnh nhé...", "Bạn tuyệt đối không..."). TUYỆT ĐỐI không dùng thuật ngữ kỹ thuật khô khan.

Câu đầu tiên LUÔN LÀ lời trấn an phù hợp với mức độ nguy hiểm.

TIÊU CHÍ PHÂN LOẠI (calibration — đọc kỹ để tránh cảnh báo nhầm):
Chỉ hạ mức xuống WARNING hoặc DANGER khi nội dung có ÍT NHẤT MỘT trong các yếu tố rủi ro CHÍNH sau:
  (a) yêu cầu chuyển tiền, cung cấp mật khẩu/OTP/thông tin thẻ,
  (b) chứa link rút gọn hoặc domain giả mạo/lạ,
  (c) tạo áp lực thời gian gấp kèm hậu quả nghiêm trọng (khóa tài khoản, phạt, mất quyền lợi),
  (d) danh tính người gửi không thể xác minh và đang yêu cầu hành động ngay.
Các yếu tố PHỤ như "tên người gửi viết tắt", "nội dung gửi lặp lại", "không thể trả lời tin nhắn" KHÔNG đủ để tự nâng mức cảnh báo nếu KHÔNG đi kèm ít nhất một yếu tố rủi ro CHÍNH ở trên — có thể nêu ra như lưu ý phụ trong signals, nhưng không dùng để quyết định status.
Nếu nội dung là thông báo/tin nhắn thông thường, không có bất kỳ yếu tố rủi ro CHÍNH nào: status = "SAFE".

QUY TẮC RIÊNG CHO ẢNH BIÊN LAI/CHUYỂN KHOẢN:
Trước tiên, xác định content type: nếu ảnh là hóa đơn/biên lai/xác nhận chuyển khoản, áp dụng các quy tắc sau:
- Nếu KHÔNG phát hiện dấu hiệu bất thường: status = "SAFE"
- Nếu phát hiện dấu hiệu bất thường CỤ THỂ, RÕ RÀNG (sai font/logo, số liệu toán học không khớp, artifact chỉnh sửa rõ rệt): status = "DANGER"
- Nếu chỉ có tín hiệu MƠ HỒ, không chắc chắn (định dạng lạ nhưng không rõ là giả hay do quy ước UI của ngân hàng): status = "WARNING", KHÔNG được đẩy thẳng lên DANGER
- QUAN TRỌNG: tên người nhận dạng mã định danh dài, có chuỗi ký tự/số ghép lại (VD: "Ph Truong Dh Fpt Tai Tphcm_14002939479") là quy ước hiển thị BÌNH THƯỜNG của các app ngân hàng Việt Nam (Vietcombank, Techcombank...) khi chuyển khoản liên ngân hàng — TUYỆT ĐỐI KHÔNG coi đây là bằng chứng giả mạo, trừ khi có bằng chứng khác cụ thể hơn đi kèm.

NGUYÊN TẮC ƯU TIÊN CẢNH BÁO CAO HƠN — chỉ áp dụng khi đã có ít nhất 1 yếu tố rủi ro CHÍNH nhưng mức độ chưa rõ ràng (VD: có link lạ nhưng chưa chắc độc hại) — không áp dụng cho nội dung hoàn toàn không có yếu tố rủi ro CHÍNH nào.

Giới hạn: mảng "signals" tối đa 3 phần tử, chỉ liệt kê những dấu hiệu quan trọng nhất, không liệt kê hết mọi chi tiết nhỏ.

QUY ĐỊNH ĐỊNH DẠNG:
- "reminders": Mảng các lời nhắc KHÔNG cần hành động thật (không làm gì, không bấm link, không chuyển tiền, không cung cấp OTP/mật khẩu, xóa tin nhắn/chặn số...), tối đa 4 mục, câu ngắn gọn, dứt khoát.
- "action": Chỉ điền khi có số điện thoại thật để gọi (hotline chính thức của cơ quan/ngân hàng/doanh nghiệp bị giả mạo hoặc tổng đài hỗ trợ). Nếu không có số điện thoại cụ thể, BẮT BUỘC để action = null. Cấu trúc: { "label": "Gọi tổng đài chính thức" | "Gọi hotline hỗ trợ", "phone": "số điện thoại" } hoặc null.
- "important_notes": mảng các câu giải thích dài hoặc lưu ý quan trọng nếu cần.
- "official_hotline": số hotline chính thức nếu nhận diện được thương hiệu/cơ quan bị giả mạo, hoặc null.
- "financial_reminder": object BẮT BUỘC LUÔN XUẤT HIỆN cho mọi ảnh biên lai/chuyển khoản (KHÔNG phụ thuộc status DANGER/WARNING/SAFE). Với nội dung khác (không phải ảnh biên lai/chuyển khoản) thì để null:
  {
    "show": true,
    "message_1": "Mặc dù bức ảnh này trông hoàn toàn bình thường, bạn tuyệt đối chưa giao hàng hay chuyển tiền vội nhé.",
    "message_2": "Nguyên tắc vàng: Bạn hãy tự mở ứng dụng ngân hàng của mình lên. Chỉ khi nào thấy số dư thực tế tăng lên thì giao dịch mới thực sự an toàn."
  }

Chỉ trả lời bằng JSON đúng theo schema sau, không thêm text nào khác ngoài JSON:
{
  "status": "DANGER" | "WARNING" | "SAFE",
  "opening_message": "câu trấn an mở đầu",
  "signals": ["dấu hiệu 1", "dấu hiệu 2", "dấu hiệu 3 (tối đa)"],
  "reminders": ["Không chuyển tiền", "Không cung cấp thông tin cá nhân", "Không bấm vào đường link lạ"],
  "action": { "label": "Gọi tổng đài chính thức", "phone": "1900xxxx" } hoặc null,
  "important_notes": ["Câu giải thích dài nếu cần"],
  "official_hotline": "số hotline chính thức nếu nhận diện được, hoặc null",
  "financial_reminder": {
    "show": true,
    "message_1": "Mặc dù bức ảnh này trông hoàn toàn bình thường, bạn tuyệt đối chưa giao hàng hay chuyển tiền vội nhé.",
    "message_2": "Nguyên tắc vàng: Bạn hãy tự mở ứng dụng ngân hàng của mình lên. Chỉ khi nào thấy số dư thực tế tăng lên thì giao dịch mới thực sự an toàn."
  } hoặc null
}"""

        fun getResolvedSystemPrompt(): String {
            val currentDateStr = try {
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            } catch (_: Exception) {
                java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
            }
            return SYSTEM_PROMPT_TEMPLATE.replace("{CURRENT_DATE}", currentDateStr)
        }
    }

    override suspend fun analyzeSms(
        smsBody: String,
        onStatusUpdate: ((String) -> Unit)?
    ): Result<ScamAnalysisResult> {
        // Tab 1 (SMS analysis): Use Flash-Lite for fast, lightweight scam screening
        return analyzeText(
            text = smsBody,
            model = MODEL_FLASH_LITE,
            onStatusUpdate = onStatusUpdate
        )
    }

    override suspend fun analyzeText(
        text: String,
        model: String,
        onStatusUpdate: ((String) -> Unit)?
    ): Result<ScamAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = try {
            requireApiKey()
        } catch (e: Exception) {
            return@withContext Result.failure(e)
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

            val response = ApiClient.geminiService.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )
            parseGeminiResponse(response)
        }
    }

    override suspend fun analyzeImage(
        base64Data: String,
        mimeType: String,
        noteText: String?,
        model: String,
        onStatusUpdate: ((String) -> Unit)?
    ): Result<ScamAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = try {
            requireApiKey()
        } catch (e: Exception) {
            return@withContext Result.failure(e)
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

            val response = ApiClient.geminiService.generateContent(
                model = model,
                apiKey = apiKey,
                request = request
            )
            parseGeminiResponse(response)
        }
    }

    private suspend fun executeWithRetry(
        onStatusUpdate: ((String) -> Unit)?,
        action: suspend () -> Result<ScamAnalysisResult>
    ): Result<ScamAnalysisResult> = geminiRateLimiter.withPermit {
        var lastError: Throwable? = null

        for (attempt in 1..2) {
            try {
                val result = action()
                if (result.isSuccess) {
                    return@withPermit result
                }
                lastError = result.exceptionOrNull()
                android.util.Log.e("AnTamAI", "Gemini API call attempt $attempt failed", lastError)
            } catch (e: Exception) {
                lastError = e
                android.util.Log.e("AnTamAI", "Gemini API call attempt $attempt exception", e)
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
        Result.failure(Exception(userFriendlyMessage, lastError))
    }

    internal fun requireApiKey(apiKey: String = BuildConfig.GEMINI_API_KEY): String {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Chưa cấu hình GEMINI_API_KEY trong hệ thống.")
        }
        return apiKey
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

        val parsed = com.example.util.JsonUtils.parseScamAnalysisResult(rawText)
        return if (parsed != null) {
            Result.success(parsed)
        } else {
            Result.failure(IllegalStateException("Không thể đọc định dạng dữ liệu phản hồi"))
        }
    }
}
