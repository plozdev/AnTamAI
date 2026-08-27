package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ScamAnalysisResult
import com.example.data.model.ScamStatus
import com.example.data.repository.SettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    @Test
    fun testScamStatusParsing() {
        val dangerResult = ScamAnalysisResult(
            status = "DANGER",
            openingMessage = "Bác bình tĩnh nhé, đây là tin nhắn lừa đảo!",
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
    fun testSettingsRepositorySaveAndClear() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SettingsRepository(context)

        repo.saveRelativePhone("0987654321")
        assertEquals("0987654321", repo.getRelativePhone())

        repo.clearRelativePhone()
        assertEquals("", repo.getRelativePhone())
    }
}
