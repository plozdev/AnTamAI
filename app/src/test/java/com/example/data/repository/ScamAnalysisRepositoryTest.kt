package com.example.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScamAnalysisRepositoryTest {

    private val repository = ScamAnalysisRepository()

    @Test
    fun requireApiKey_blankOrEmpty_throwsIllegalStateException() {
        val emptyException = assertThrows(IllegalStateException::class.java) {
            repository.requireApiKey("")
        }
        assertTrue(emptyException.message?.contains("Chưa cấu hình GEMINI_API_KEY") == true)

        val whitespaceException = assertThrows(IllegalStateException::class.java) {
            repository.requireApiKey("   \t\n  ")
        }
        assertTrue(whitespaceException.message?.contains("Chưa cấu hình GEMINI_API_KEY") == true)
    }

    @Test
    fun requireApiKey_placeholderKey_throwsIllegalStateException() {
        val placeholderException = assertThrows(IllegalStateException::class.java) {
            repository.requireApiKey("MY_GEMINI_API_KEY")
        }
        assertTrue(placeholderException.message?.contains("Chưa cấu hình GEMINI_API_KEY") == true)
    }

    @Test
    fun requireApiKey_validKey_returnsKeySuccessfully() {
        val validKey = "AIzaSyD-sample-valid-gemini-key-123456"
        val resolvedKey = repository.requireApiKey(validKey)
        assertEquals(validKey, resolvedKey)
    }

    @Test
    fun getResolvedSystemPrompt_replacesCurrentDatePlaceholder() {
        val resolvedPrompt = ScamAnalysisRepository.getResolvedSystemPrompt()
        assertFalse("Prompt must not contain unresolved placeholder", resolvedPrompt.contains("{CURRENT_DATE}"))
        val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        assertTrue("Prompt must contain today's date formatted as dd/MM/yyyy", resolvedPrompt.contains("Hôm nay là ngày $todayStr"))
    }
}
