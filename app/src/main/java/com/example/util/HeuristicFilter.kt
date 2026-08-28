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
        Pattern.compile("(trong|trước|truoc)\\s*\\d+\\s*(giờ|tiếng|h|phút)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("ngay\\s+lập\\s+tức|ngay\\s+lap\\s+tuc", Pattern.CASE_INSENSITIVE),
        Pattern.compile("khẩn\\s+cấp|khan\\s+cap", Pattern.CASE_INSENSITIVE),
        Pattern.compile("hết\\s+hạn\\s+trong|het\\s+han\\s+trong", Pattern.CASE_INSENSITIVE),
        Pattern.compile("trước\\s+24h|truoc\\s+24h", Pattern.CASE_INSENSITIVE)
    )

    // Các từ khóa chỉ hậu quả đe dọa cụ thể (mất tiền, khóa tài khoản, mất quyền lợi, phạt, đình chỉ)
    private val THREAT_CONSEQUENCES = listOf(
        "khóa tài khoản", "khoa tai khoan", "tài khoản bị khóa", "tai khoan bi khoa",
        "tạm khóa", "tam khoa", "phong tỏa", "phong toa", "ngừng dịch vụ", "ngung dich vu",
        "phạt nguội", "phat nguoi", "xử phạt", "xu phat", "bị phạt", "bi phat",
        "truy cứu", "truy cuu", "khởi tố", "khoi to", "hầu tòa", "hau toa",
        "mất quyền lợi", "mat quyen loi", "mất tiền", "mat tien", "trừ tiền", "tru tien",
        "cưỡng chế", "cuong che", "hủy dịch vụ", "huy dich vu"
    )

    // Các từ khóa yêu cầu hành động tài chính hoặc cung cấp thông tin cá nhân
    private val ACTION_REQUIREMENTS = listOf(
        "chuyển khoản", "chuyen khoan", "chuyển tiền", "chuyen tien", "nạp tiền", "nap tien",
        "thanh toán", "thanh toan", "nộp phạt", "nop phat", "mã otp", "ma otp",
        "mật khẩu", "mat khau", "mã xác thực", "ma xac thuc", "cung cấp cccd", "cung cap cccd",
        "cung cấp thông tin", "cung cap thong tin", "xác thực thông tin", "xac thuc thong tin",
        "nhập thông tin", "nhap thong tin", "số thẻ", "so the", "đăng nhập", "dang nhap"
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

        // 3. Kiểm tra cú pháp hối thúc thời gian:
        // CHỈ gắn cờ khi THỎA MÃN CẢ 2 ĐIỀU KIỆN:
        // (a) Có hậu quả đe dọa cụ thể đi kèm (mất tiền, khóa tài khoản, mất quyền lợi, phạt)
        // VÀ
        // (b) Có yêu cầu hành động tài chính hoặc cung cấp thông tin cá nhân
        var hasUrgencyPattern = false
        for (pattern in URGENCY_PATTERNS) {
            if (pattern.matcher(normalizedBody).find()) {
                hasUrgencyPattern = true
                break
            }
        }

        if (hasUrgencyPattern) {
            val hasThreat = THREAT_CONSEQUENCES.any { normalizedBody.contains(it) }
            val hasActionReq = ACTION_REQUIREMENTS.any { normalizedBody.contains(it) }

            if (hasThreat && hasActionReq) {
                val urgencySignal = "Tạo áp lực thời gian kèm đe dọa hậu quả nghiêm trọng"
                if (!matchedSignals.contains(urgencySignal)) {
                    matchedSignals.add(urgencySignal)
                }
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
