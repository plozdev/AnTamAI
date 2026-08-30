package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HeuristicFilterTest {

    @Test
    fun analyze_emptyOrBlank_returnsNotSuspicious() {
        val emptyResult = HeuristicFilter.analyze("")
        assertFalse(emptyResult.needsScrutiny)
        assertEquals("Bình thường", emptyResult.summary)
        assertTrue(emptyResult.matchedSignals.isEmpty())

        val blankResult = HeuristicFilter.analyze("    \n\t  ")
        assertFalse(blankResult.needsScrutiny)
        assertEquals("Bình thường", blankResult.summary)
    }

    @Test
    fun analyze_telecomPromotionsWithTimeWindow_doesNotFlagFalsePositive() {
        // Case 1: Viettel 195 promotion with "3 ngay"
        val viettelPromo = "[TB] Uu dai dac biet: Dang ky goi cuoc 4G ST15K chi 15.000d/3 ngay co 3GB data toc do cao. Soan ST15K gui 195."
        val viettelResult = HeuristicFilter.analyze(viettelPromo)
        assertFalse("Viettel promo should pass as normal (not flagged)", viettelResult.needsScrutiny)
        assertEquals("Bình thường", viettelResult.summary)

        // Case 2: 9598 Data Plus invitation with "trong 24 gio toi"
        val dataPlusPromo = "Moi quy khach tham gia chuong trinh Data Plus nhan 5GB luu luong mien phi trong 24 gio toi. Chi tiet LH 18008098."
        val dataPlusResult = HeuristicFilter.analyze(dataPlusPromo)
        assertFalse("Data Plus promo with 'trong 24 gio' without threats should pass as normal", dataPlusResult.needsScrutiny)
        assertEquals("Bình thường", dataPlusResult.summary)

        // Case 3: Vinaphone / Mobifone promo with time limit
        val vinaPromo = "Vinaphone tang 20% gia tri the nap cho thue bao trong 2 ngay 15-16/08. Chi tiet lh 18001091."
        val vinaResult = HeuristicFilter.analyze(vinaPromo)
        assertFalse("Telecom top-up promo should pass as normal", vinaResult.needsScrutiny)
        assertEquals("Bình thường", vinaResult.summary)

        // Case 4: Normal daily conversation with time reference
        val normalChat = "Mai 9 gio sang nho mang tai lieu hop qua phong minh nhe, trong 2 tieng nua gui file qua mail giup minh."
        val chatResult = HeuristicFilter.analyze(normalChat)
        assertFalse("Normal conversation with 'trong 2 tieng' should pass as normal", chatResult.needsScrutiny)
        assertEquals("Bình thường", chatResult.summary)
    }

    @Test
    fun analyze_threatsWithActionRequirementsAndUrgency_flagsAsSuspicious() {
        // Case 1: Phishing bank with account lock threat + action requirement + urgency window
        val bankThreatSms = "Vietcombank: Tai khoan cua quy khach da bi tam khoa do vi pham. Vui long dang nhap de xac thuc thong tin trong 2 gio de tranh bi khoa tai khoan vinh vien."
        val bankThreatResult = HeuristicFilter.analyze(bankThreatSms)
        assertTrue("Bank scam with lock threat and action requirement must be flagged", bankThreatResult.needsScrutiny)
        assertEquals("Cần kiểm tra kỹ", bankThreatResult.summary)
        assertTrue(bankThreatResult.matchedSignals.isNotEmpty())

        // Case 2: Threatening police impersonation + demand transfer
        val policeThreatSms = "Bo Cong An thong bao: Ong/Ba co lenh bat tam giam vi lien quan du an rua tien. Yeu cau chuyen khoan tien vao tai khoan tam giu trong 24h de phuc vu dieu tra."
        val policeResult = HeuristicFilter.analyze(policeThreatSms)
        assertTrue("Police impersonation with money transfer demand must be flagged", policeResult.needsScrutiny)
        assertEquals("Cần kiểm tra kỹ", policeResult.summary)
    }

    @Test
    fun analyze_shortLinkDomains_flagsAsSuspicious() {
        val linkSms = "Nhan qua tri an tu Shopee tai link: https://bit.ly/nhanqua2026 ngay hom nay."
        val linkResult = HeuristicFilter.analyze(linkSms)
        assertTrue("Short link domain must be flagged", linkResult.needsScrutiny)
        assertTrue(linkResult.matchedSignals.any { it.contains("link rút gọn") || it.contains("bit.ly") })

        val suspiciousTopDomainSms = "Kiem tra don hang cua ban tai https://giaohang-nhanh.top"
        val domainResult = HeuristicFilter.analyze(suspiciousTopDomainSms)
        assertTrue(".top domain must be flagged", domainResult.needsScrutiny)
    }

    @Test
    fun analyze_scamKeywords_flagsAsSuspicious() {
        val otpScam = "Ma OTP cua ban la 582914. Nhan vien ngan hang dang yeu cau cung cap ma xac thuc de nang cap the."
        val otpResult = HeuristicFilter.analyze(otpScam)
        assertTrue("OTP / verification keyword should be flagged", otpResult.needsScrutiny)

        val prizeScam = "Chuc mung quy khach da trung thuong xe SH Mode tu chuong trinh quay so may man tri an khach hang."
        val prizeResult = HeuristicFilter.analyze(prizeScam)
        assertTrue("Lottery / prize scam should be flagged", prizeResult.needsScrutiny)
    }
}
