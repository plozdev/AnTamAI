package com.example.util

import com.example.data.model.ActionItem
import com.example.data.model.FinancialReminder
import com.example.data.model.ScamAnalysisResult
import com.example.data.model.ScamStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonUtilsTest {

    @Test
    fun parseScamAnalysisResult_nullOrBlankInput_returnsNull() {
        assertNull(JsonUtils.parseScamAnalysisResult(null))
        assertNull(JsonUtils.parseScamAnalysisResult(""))
        assertNull(JsonUtils.parseScamAnalysisResult("   \n\t  "))
    }

    @Test
    fun parseScamAnalysisResult_validJson_parsesCorrectly() {
        val json = """
            {
              "status": "DANGER",
              "opening_message": "Ba mẹ bình tĩnh nhé, đây là tin nhắn lừa đảo mạo danh!",
              "signals": ["Tên miền lạ", "Yêu cầu cung cấp OTP", "Đe dọa khóa tài khoản"],
              "reminders": ["Tuyệt đối không bấm link", "Không cung cấp mã OTP"],
              "action": {
                "label": "Gọi tổng đài Vietcombank",
                "phone": "1900545413"
              },
              "important_notes": ["Ngân hàng không bao giờ yêu cầu gửi OTP qua tin nhắn."],
              "official_hotline": "1900545413",
              "financial_reminder": {
                "show": true,
                "message_1": "Mặc dù bức ảnh này trông hoàn toàn bình thường, ba mẹ tuyệt đối chưa giao hàng hay chuyển tiền vội nhé ạ.",
                "message_2": "Nguyên tắc vàng: Ba mẹ hãy tự mở ứng dụng ngân hàng của mình lên."
              }
            }
        """.trimIndent()

        val result = JsonUtils.parseScamAnalysisResult(json)
        assertNotNull(result)
        assertEquals("DANGER", result?.status)
        assertEquals(ScamStatus.DANGER, result?.scamStatus)
        assertEquals("Ba mẹ bình tĩnh nhé, đây là tin nhắn lừa đảo mạo danh!", result?.openingMessage)
        assertEquals(3, result?.signals?.size)
        assertEquals(2, result?.reminders?.size)
        assertEquals("Gọi tổng đài Vietcombank", result?.action?.label)
        assertEquals("1900545413", result?.action?.phone)
        assertEquals("1900545413", result?.officialHotline)
        assertTrue(result?.financialReminder?.show == true)
        assertEquals("Mặc dù bức ảnh này trông hoàn toàn bình thường, ba mẹ tuyệt đối chưa giao hàng hay chuyển tiền vội nhé ạ.", result?.financialReminder?.message1)
    }

    @Test
    fun parseScamAnalysisResult_jsonWrappedInMarkdownCodeBlock_parsesSuccessfully() {
        val markdownJson = """
            ```json
            {
              "status": "SAFE",
              "opening_message": "Tin nhắn này an toàn ba mẹ nhé!",
              "signals": [],
              "reminders": ["Không cần thao tác gì thêm"],
              "action": null,
              "important_notes": [],
              "official_hotline": null,
              "financial_reminder": null
            }
            ```
        """.trimIndent()

        val result = JsonUtils.parseScamAnalysisResult(markdownJson)
        assertNotNull(result)
        assertEquals("SAFE", result?.status)
        assertEquals(ScamStatus.SAFE, result?.scamStatus)
        assertEquals("Tin nhắn này an toàn ba mẹ nhé!", result?.openingMessage)
        assertNull(result?.action)
        assertNull(result?.financialReminder)
    }

    @Test
    fun parseScamAnalysisResult_malformedOrCorruptedJson_returnsNullGracefully() {
        val malformedJson = "{ \"status\": \"DANGER\", \"opening_message\": "
        val result = JsonUtils.parseScamAnalysisResult(malformedJson)
        assertNull("Malformed JSON must safely return null instead of crashing", result)

        val nonJsonString = "Xin chao day la tin nhan khong phai JSON"
        val nonJsonResult = JsonUtils.parseScamAnalysisResult(nonJsonString)
        assertNull("Plain text must safely return null", nonJsonResult)
    }

    @Test
    fun toJson_and_roundTrip_worksProperly() {
        val original = ScamAnalysisResult(
            status = "WARNING",
            openingMessage = "Cần thận trọng kiểm tra lại",
            signals = listOf("Số lạ"),
            reminders = listOf("Không chuyển tiền vội"),
            action = ActionItem(label = "Hỏi người thân", phone = "0900000000"),
            financialReminder = FinancialReminder(show = true, message1 = "Lưu ý 1", message2 = "Lưu ý 2")
        )

        val json = JsonUtils.toJson(original)
        assertTrue(json.isNotBlank())
        assertTrue(json.contains("WARNING"))
        assertTrue(json.contains("Cần thận trọng"))

        val parsedBack = JsonUtils.parseScamAnalysisResult(json)
        assertNotNull(parsedBack)
        assertEquals(original.status, parsedBack?.status)
        assertEquals(original.openingMessage, parsedBack?.openingMessage)
        assertEquals(original.action?.label, parsedBack?.action?.label)
        assertEquals(original.action?.phone, parsedBack?.action?.phone)
        assertEquals(original.financialReminder?.show, parsedBack?.financialReminder?.show)
    }
}
