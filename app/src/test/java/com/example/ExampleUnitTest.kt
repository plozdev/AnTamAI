package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ScamAnalysisResult
import com.example.data.model.ScamStatus
import com.example.data.repository.ScamAnalysisRepository
import com.example.data.repository.SettingsRepository
import com.example.util.HeuristicFilter
import com.example.ui.screens.buildShareText
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
            reminders = listOf("Không chuyển tiền", "Mở app ngân hàng kiểm tra"),
            officialHotline = "1900545413"
        )
        assertEquals(ScamStatus.DANGER, dangerResult.scamStatus)
        assertEquals("1900545413", dangerResult.officialHotline)
        assertEquals(2, dangerResult.signals.size)

        val warningResult = ScamAnalysisResult(
            status = "WARNING",
            openingMessage = "Ảnh hóa đơn này chưa xác minh được",
            signals = listOf("Ảnh chụp màn hình có thể làm giả"),
            reminders = listOf("Chưa giao hàng", "Kiểm tra số dư thực tế")
        )
        assertEquals(ScamStatus.WARNING, warningResult.scamStatus)
    }

    @Test
    fun testHeuristicFilterViettelAndDataPlusCases() {
        // Case 1: 195 Viettel promo - should NOT flag
        val viettelSms = "[TB] Uu dai dac biet: Dang ky goi cuoc 4G ST15K chi 15.000d/3 ngay co 3GB data toc do cao. Soan ST15K gui 195."
        val viettelResult = HeuristicFilter.analyze(viettelSms)
        assertFalse("Viettel promo should not be flagged", viettelResult.needsScrutiny)

        // Case 2: 9598 Data Plus invite - should NOT flag
        val dataPlusSms = "Moi quy khach tham gia chuong trinh Data Plus nhan 5GB luu luong mien phi trong 24 gio toi. Chi tiet LH 18008098."
        val dataPlusResult = HeuristicFilter.analyze(dataPlusSms)
        assertFalse("Data Plus promo without threats should not be flagged", dataPlusResult.needsScrutiny)

        // Case 3: Real scam with bank impersonation and threat & link
        val bankScamSms = "Vietcombank: Tai khoan cua quy khach da bi tam khoa do vi pham. Vui long dang nhap https://vcb-security.top trong 2 gio de xac thuc tranh bi mat tien vinh vien."
        val scamResult = HeuristicFilter.analyze(bankScamSms)
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

    @Test
    fun testSettingsToggleOnOffCycle() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo1 = SettingsRepository(context)

        // Turn OFF auto scan
        repo1.setAutoScanSms(false)
        assertFalse(repo1.getAutoScanSms())
        assertFalse(repo1.autoScanSms.value)

        // Read from fresh repository instance (simulating fresh process/broadcast)
        val repo2 = SettingsRepository(context)
        assertFalse(repo2.getAutoScanSms())

        // Turn back ON
        repo2.setAutoScanSms(true)
        assertTrue(repo2.getAutoScanSms())
        assertTrue(repo2.autoScanSms.value)

        // Verify fresh instance picks up ON immediately
        val repo3 = SettingsRepository(context)
        assertTrue(repo3.getAutoScanSms())

        // Test AutoRead toggle cycle
        repo1.setAutoReadResult(false)
        assertFalse(repo1.getAutoReadResult())
        repo1.setAutoReadResult(true)
        assertTrue(repo1.getAutoReadResult())
    }

    @Test
    fun testBuildShareTextForStatuses() {
        val danger = ScamAnalysisResult(
            status = "DANGER",
            openingMessage = "Bạn cẩn thận nhé, đây là bẫy lừa đảo mạo danh ngân hàng",
            signals = listOf("Đường link giả mạo vietcombank"),
            reminders = listOf("Không click link")
        )
        val dangerShare = buildShareText(danger)
        assertTrue(dangerShare.startsWith("🚨 AnTâm.AI CẢNH BÁO: Bạn cẩn thận nhé, đây là bẫy lừa đảo mạo danh ngân hàng"))
        assertTrue(dangerShare.contains("Dấu hiệu: Đường link giả mạo vietcombank"))
        assertTrue(dangerShare.endsWith("Kiểm tra tin nhắn nghi ngờ miễn phí tại AnTâm.AI"))

        val warning = ScamAnalysisResult(
            status = "WARNING",
            openingMessage = "Tin nhắn có dấu hiệu bất thường cần xác minh thêm",
            signals = listOf("Yêu cầu cung cấp thông tin cá nhân"),
            reminders = listOf("Gọi điện xác minh")
        )
        val warningShare = buildShareText(warning)
        assertTrue(warningShare.startsWith("⚠️ AnTâm.AI lưu ý: Tin nhắn có dấu hiệu bất thường cần xác minh thêm"))
        assertTrue(warningShare.contains("Dấu hiệu: Yêu cầu cung cấp thông tin cá nhân"))
        assertTrue(warningShare.endsWith("Kiểm tra tin nhắn nghi ngờ miễn phí tại AnTâm.AI"))

        val safe = ScamAnalysisResult(
            status = "SAFE",
            openingMessage = "Tin nhắn quảng cáo thông thường từ tổng đài chính thức",
            signals = listOf("Cú pháp đăng ký gói cước Viettel hợp lệ"),
            reminders = emptyList()
        )
        val safeShare = buildShareText(safe)
        assertTrue(safeShare.startsWith("✅ AnTâm.AI xác nhận AN TOÀN: Tin nhắn quảng cáo thông thường từ tổng đài chính thức"))
        assertTrue(safeShare.contains("Dấu hiệu: Cú pháp đăng ký gói cước Viettel hợp lệ"))
        assertTrue(safeShare.endsWith("Kiểm tra tin nhắn nghi ngờ miễn phí tại AnTâm.AI"))
    }
}
