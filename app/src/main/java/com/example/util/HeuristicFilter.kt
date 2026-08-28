package com.example.util

import com.example.data.model.HeuristicResult
import java.util.Locale
import java.util.regex.Pattern

object HeuristicFilter {

    private val KEYWORD_SIGNALS = listOf(
        // Tài chính & Giao dịch
        "chuyển khoản" to "Yêu cầu chuyển khoản hoặc giao dịch tiền",
        "chuyen khoan" to "Yêu cầu chuyển khoản",
        "hoàn tiền" to "Đề cập hoàn tiền / hoàn thuế",
        "hoan tien" to "Đề cập hoàn tiền",
        "hoàn thuế" to "Đề cập hoàn thuế",
        "nạp tiền" to "Yêu cầu nạp tiền",
        "vay tiền" to "Mời chào vay vốn / tiền nhanh",
        "lãi suất cao" to "Mời gọi đầu tư lãi suất cao",
        "việc nhẹ lương cao" to "Tuyển dụng việc nhẹ lương cao",

        // Tài khoản & Đe dọa
        "tài khoản bị khóa" to "Cảnh báo tài khoản bị khóa / tạm ngừng",
        "tai khoan bi khoa" to "Cảnh báo tài khoản bị khóa",
        "khóa tài khoản" to "Cảnh báo khóa tài khoản",
        "khoa tai khoan" to "Cảnh báo khóa tài khoản",
        "tạm khóa" to "Thông báo tạm khóa dịch vụ",
        "phong tỏa" to "Đe dọa phong tỏa tài sản/tài khoản",

        // Giả danh cơ quan & Phạt
        "công an" to "Nhắc tới cơ quan Công an",
        "cong an" to "Nhắc tới cơ quan Công an",
        "tòa án" to "Nhắc tới Tòa án / Viện kiểm sát",
        "phạt nguội" to "Thông báo phạt nguội giao thông",
        "phat nguoi" to "Thông báo phạt nguội",
        "bưu cục" to "Thông báo bưu cục / giữ kiện hàng",
        "thu hồi bưu phẩm" to "Thông báo giữ / thu hồi bưu phẩm",
        "truy cứu" to "Đe dọa xử lý pháp luật / truy cứu",

        // Trúng thưởng & Quà tặng
        "trúng thưởng" to "Thông báo trúng thưởng / nhận quà",
        "trung thuong" to "Thông báo trúng thưởng",
        "nhận quà" to "Mời nhận quà tri ân",
        "tri ân khách hàng" to "Mời nhận quà tri ân khách hàng",
        "quay số may mắn" to "Thông báo trúng thưởng may mắn",

        // OTP & Bảo mật
        "mã otp" to "Đề cập đến mã OTP / mã xác thực",
        "ma otp" to "Đề cập đến mã OTP",
        "mã xác thực" to "Yêu cầu cung cấp mã xác thực",
        "mật khẩu" to "Đề cập đến mật khẩu ngân hàng/dịch vụ"
    )

    private val SHORT_LINK_DOMAINS = listOf(
        "bit.ly", "tinyurl.com", "t.co", "is.gd", "cutt.ly",
        "shorturl.at", "rebrand.ly", "ow.ly", "gg.gg", "s.id",
        ".xyz", ".top", ".vip", ".cc", ".icu", ".site", ".tk", ".ml", ".ga"
    )

    private val URGENCY_PATTERNS = listOf(
        Pattern.compile("(trong|trước|truoc)\\s*\\d+\\s*(giờ|tiếng|h|phút|ngày)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("ngay\\s+lập\\s+tức|ngay\\s+lap\\s+tuc", Pattern.CASE_INSENSITIVE),
        Pattern.compile("khẩn\\s+cấp|khan\\s+cap", Pattern.CASE_INSENSITIVE),
        Pattern.compile("hết\\s+hạn\\s+trong|het\\s+han\\s+trong", Pattern.CASE_INSENSITIVE),
        Pattern.compile("trước\\s+24h|truoc\\s+24h", Pattern.CASE_INSENSITIVE)
    )

    /**
     * Phân tích nội dung tin nhắn SMS thuần cục bộ (không gọi mạng/API)
     */
    fun analyze(body: String): HeuristicResult {
        if (body.isBlank()) {
            return HeuristicResult(needsScrutiny = false, matchedSignals = emptyList())
        }

        val normalizedBody = body.lowercase(Locale.ROOT)
        val matchedSignals = mutableListOf<String>()

        // 1. Kiểm tra từ khóa đáng ngờ
        for ((keyword, description) in KEYWORD_SIGNALS) {
            if (normalizedBody.contains(keyword)) {
                if (!matchedSignals.contains(description)) {
                    matchedSignals.add(description)
                }
            }
        }

        // 2. Kiểm tra link rút gọn hoặc domain lạ
        for (domain in SHORT_LINK_DOMAINS) {
            if (normalizedBody.contains(domain)) {
                val linkSignal = "Chứa link rút gọn / tên miền lạ ($domain)"
                if (!matchedSignals.contains(linkSignal)) {
                    matchedSignals.add(linkSignal)
                }
                break
            }
        }

        // 3. Kiểm tra cú pháp hối thúc thời gian
        for (pattern in URGENCY_PATTERNS) {
            if (pattern.matcher(normalizedBody).find()) {
                val urgencySignal = "Tạo áp lực thời gian gấp gáp"
                if (!matchedSignals.contains(urgencySignal)) {
                    matchedSignals.add(urgencySignal)
                }
                break
            }
        }

        val needsScrutiny = matchedSignals.isNotEmpty()

        return HeuristicResult(
            needsScrutiny = needsScrutiny,
            matchedSignals = matchedSignals.take(4),
            summary = if (needsScrutiny) "Cần kiểm tra kỹ" else "Bình thường"
        )
    }
}
