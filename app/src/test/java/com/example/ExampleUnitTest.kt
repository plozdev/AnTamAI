package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ScamAnalysisResult
import com.example.data.model.ScamStatus
import com.example.data.repository.ScamAnalysisRepository
import com.example.data.repository.SettingsRepository
import com.example.util.HeuristicFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    @Test
    fun testScamStatusParsing() {
        val dangerResult = ScamAnalysisResult(
            status = "DANGER",
            openingMessage = "Bạn bình tĩnh nhé, đây là tin nhắn lừa đảo!",
            signals = listOf("Link sai lệch", "Thúc ép thời gian"),
            recommendedActions = listOf("Không chuyển tiền", "Mở app ngân hàng kiểm tra"),
            officialHotline = "1900545413"
        )
        assertEquals(ScamStatus.DANGER, dangerResult.scamStatus)
        assertEquals("1900545413", dangerResult.officialHotline)
        assertEquals(2, dangerResult.signals.size)

        val warningResult = ScamAnalysisResult(
            status = "WARNING",
            openingMessage = "Ảnh hóa đơn này chưa xác minh được",
            signals = listOf("Ảnh chụp màn hình có thể làm giả"),
            recommendedActions = listOf("Chưa giao hàng", "Kiểm tra số dư thực tế")
        )
        assertEquals(ScamStatus.WARNING, warningResult.scamStatus)
    }

    @Test
    fun testHeuristicFilterViettelAndDataPlusCases() {
        // Case 1: 195 Viettel promo - should NOT flag
        val viettelSms = "[TB] Uu dai dac biet: Dang ky goi cuoc 4G ST15K chi 15.000d/3 ngay co 3GB data toc do cao. Soan ST15K gui 195."
        val viettelResult = HeuristicFilter.analyze("195", viettelSms)
        assertFalse("Viettel promo should not be flagged", viettelResult.needsScrutiny)

        // Case 2: 9598 Data Plus invite - should NOT flag
        val dataPlusSms = "Moi quy khach tham gia chuong trinh Data Plus nhan 5GB luu luong mien phi trong 24 gio toi. Chi tiet LH 18008098."
        val dataPlusResult = HeuristicFilter.analyze("9598", dataPlusSms)
        assertFalse("Data Plus promo without threats should not be flagged", dataPlusResult.needsScrutiny)

        // Case 3: Real scam with bank impersonation and threat & link
        val bankScamSms = "Vietcombank: Tai khoan cua quy khach da bi tam khoa do vi pham. Vui long dang nhap https://vcb-security.top trong 2 gio de xac thuc tranh bi mat tien vinh vien."
        val scamResult = HeuristicFilter.analyze("+84901234567", bankScamSms)
        assertTrue("Scam SMS with phishing link and threat should be flagged", scamResult.needsScrutiny)
    }

    @Test
    fun testDateInjectionInSystemPrompt() {
        val resolvedPrompt = ScamAnalysisRepository.getResolvedSystemPrompt()
        assertFalse(resolvedPrompt.contains("{CURRENT_DATE}"))
        val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        assertTrue(resolvedPrompt.contains("Hôm nay là ngày $todayStr"))
    }

    @Test
    fun testSettingsRepositorySaveAndClear() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SettingsRepository(context)

        repo.saveRelativePhone("0987654321")
        assertEquals("0987654321", repo.getRelativePhone())

        repo.clearRelativePhone()
        assertEquals("", repo.getRelativePhone())

        repo.setAutoReadResult(true)
        assertTrue(repo.getAutoReadResult())

        repo.setAutoReadResult(false)
        assertFalse(repo.getAutoReadResult())

        repo.setAutoScanSms(false)
        assertFalse(repo.getAutoScanSms())

        repo.setAutoScanSms(true)
        assertTrue(repo.getAutoScanSms())
    }
}
