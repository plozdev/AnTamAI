package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ScamAnalysisResult
import com.example.data.model.ScamStatus
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.LavenderPrimaryContainer
import com.example.ui.theme.LavenderSecondaryContainer
import com.example.ui.theme.OnDangerContainer
import com.example.ui.theme.OnLavenderContainer
import com.example.ui.theme.OnLavenderPrimary
import com.example.ui.theme.OnSafeContainer
import com.example.ui.theme.OnWarningContainer
import com.example.ui.theme.SafeContainer
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMediumContrast
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningContainer
import com.example.util.TextToSpeechHelper

@Composable
fun ResultScreen(
    result: ScamAnalysisResult,
    relativePhone: String = "",
    onOpenSettings: () -> Unit = {},
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Text To Speech Helper initialization and lifecycle
    val ttsHelper = remember { TextToSpeechHelper(context) }
    val isSpeaking by ttsHelper.isSpeaking.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    // Keep track of clicked/acknowledged actions
    val checkedActions = remember { mutableStateMapOf<Int, Boolean>() }

    val status = result.scamStatus
    val (statusTitle, statusSubtitle, statusIcon, statusColor, statusBgColor, statusTextColor) = when (status) {
        ScamStatus.DANGER -> BannerTheme(
            title = "CẢNH BÁO NGUY HIỂM: CÓ DẤU HIỆU LỪA ĐẢO",
            subtitle = "Bác tuyệt đối KHÔNG làm theo yêu cầu trong tin nhắn hoặc hình ảnh này!",
            icon = Icons.Default.Dangerous,
            badgeColor = DangerRed,
            containerColor = DangerContainer,
            contentColor = OnDangerContainer
        )
        ScamStatus.WARNING -> BannerTheme(
            title = "CẢNH BÁO: CẦN HẾT SỨC CẨN TRỌNG",
            subtitle = "Chưa thể khẳng định an toàn. Bác hãy tự mở ứng dụng ngân hàng để kiểm tra số dư thực tế.",
            icon = Icons.Default.Warning,
            badgeColor = WarningAmber,
            containerColor = WarningContainer,
            contentColor = OnWarningContainer
        )
        ScamStatus.SAFE -> BannerTheme(
            title = "ĐÁNH GIÁ: AN TOÀN",
            subtitle = "Không phát hiện dấu hiệu lừa đảo phổ biến.",
            icon = Icons.Default.CheckCircle,
            badgeColor = SafeGreen,
            containerColor = SafeContainer,
            contentColor = OnSafeContainer
        )
        ScamStatus.UNKNOWN -> BannerTheme(
            title = "KẾT QUẢ PHÂN TÍCH",
            subtitle = "Hãy đọc kỹ các lưu ý bên dưới trước khi thao tác.",
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            badgeColor = LavenderPrimary,
            containerColor = DarkSurface,
            contentColor = TextHighContrast
        )
    }

    // Prepare speech text from opening_message and signals
    val textToRead = remember(result) {
        buildString {
            if (result.openingMessage.isNotBlank()) {
                append(result.openingMessage)
                append(". ")
            }
            if (result.signals.isNotEmpty()) {
                append("Các dấu hiệu nhận biết gồm: ")
                result.signals.forEachIndexed { i, sig ->
                    append("Dấu hiệu thứ ${i + 1}: $sig. ")
                }
            }
            if (result.recommendedActions.isNotEmpty()) {
                append("Lời khuyên hành động: ")
                result.recommendedActions.forEach { act ->
                    append("$act. ")
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("screen_result"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Navigation row: Back & Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onBackToHome,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("button_back_home")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = LavenderPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Kiểm tra nội dung khác",
                    style = MaterialTheme.typography.labelLarge,
                    color = LavenderPrimary
                )
            }

            OutlinedButton(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("button_open_settings_from_result")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Cài đặt",
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Cài đặt",
                    style = MaterialTheme.typography.labelMedium,
                    color = LavenderPrimary
                )
            }
        }

        // 1. BANNER LỚN ĐỔI MÀU THEO STATUS (DANGER=ĐỎ, WARNING=VÀNG, SAFE=XANH)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("banner_status"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = statusBgColor),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = androidx.compose.ui.graphics.SolidColor(statusColor)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = statusTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 20.sp,
                        lineHeight = 28.sp
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    color = statusTextColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = statusSubtitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                    color = statusTextColor.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. TEXT-TO-SPEECH READ ALOUD BUTTON ("🔊 ĐỌC TO")
        Button(
            onClick = {
                if (isSpeaking) {
                    ttsHelper.stop()
                } else {
                    ttsHelper.speak(textToRead)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .testTag("button_read_aloud"),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSpeaking) DangerContainer else LavenderPrimaryContainer,
                contentColor = if (isSpeaking) OnDangerContainer else OnLavenderContainer
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.5.dp,
                brush = androidx.compose.ui.graphics.SolidColor(if (isSpeaking) DangerRed else LavenderPrimary)
            )
        ) {
            Icon(
                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = if (isSpeaking) "Dừng đọc" else "Đọc to",
                modifier = Modifier.size(26.dp),
                tint = if (isSpeaking) DangerRed else LavenderPrimary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isSpeaking) "⏹️ Đang đọc... Bấm để dừng lại" else "🔊 Đọc to lời dặn cho bác nghe",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. OPENING_MESSAGE LÀM TIÊU ĐỀ LỚN
        if (result.openingMessage.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_opening_message"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.5.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(LavenderPrimary.copy(alpha = 0.6f))
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Lời dặn gửi tới bác:",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.sp,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = LavenderPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.openingMessage,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            lineHeight = 28.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 4. SIGNALS HIỂN THỊ DẠNG DANH SÁCH GẠCH ĐẦU DÒNG, ICON CẢNH BÁO
        if (result.signals.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_signals_list"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(DarkOutline)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Các dấu hiệu đáng ngờ nhận diện được:",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    result.signals.forEachIndexed { index, signal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Cảnh báo",
                                tint = WarningAmber,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = signal,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp
                                ),
                                color = TextHighContrast,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 5. RECOMMENDED_ACTIONS HIỂN THỊ DẠNG NÚT BẤM LỚN
        if (result.recommendedActions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_recommended_actions"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(LavenderPrimary.copy(alpha = 0.5f))
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Việc bác cần làm ngay:",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = LavenderPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    result.recommendedActions.forEachIndexed { index, action ->
                        val isChecked = checkedActions[index] == true

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clickable {
                                    checkedActions[index] = !isChecked
                                    Toast.makeText(
                                        context,
                                        if (!isChecked) "Đã ghi nhớ: $action" else "Đã bỏ chọn",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .testTag("action_button_$index"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChecked) SafeContainer.copy(alpha = 0.7f) else DarkSurfaceVariant
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                width = 1.5.dp,
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (isChecked) SafeGreen else DarkOutline
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isChecked) SafeGreen else LavenderPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = action,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp
                                    ),
                                    fontWeight = FontWeight.Medium,
                                    color = if (isChecked) OnSafeContainer else TextHighContrast,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 6. HOTLINE VÀ GỌI NGƯỜI THÂN (ACTION BUTTONS)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nút "Gọi tổng đài chính thức" nếu official_hotline khác null / không rỗng
            if (!result.officialHotline.isNullOrBlank()) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${result.officialHotline}")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            clipboardManager.setText(AnnotatedString(result.officialHotline))
                            Toast.makeText(context, "Đã sao chép số tổng đài: ${result.officialHotline}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("button_call_official_hotline"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LavenderPrimary,
                        contentColor = OnLavenderPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Gọi tổng đài chính thức (${result.officialHotline})",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Nút "Gọi người thân" nếu đã lưu số trong Settings
            if (relativePhone.isNotBlank()) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$relativePhone")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            clipboardManager.setText(AnnotatedString(relativePhone))
                            Toast.makeText(context, "Đã sao chép số người thân: $relativePhone", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("button_call_relative"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LavenderPrimaryContainer,
                        contentColor = OnLavenderContainer
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = 1.5.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(LavenderPrimary)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FamilyRestroom,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = LavenderPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Gọi người thân / con cháu ($relativePhone)",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Button to prompt configuring relative's phone
                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("button_setup_relative_phone"),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(DarkOutline)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = LavenderPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cài đặt số điện thoại người thân để gọi nhanh",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = LavenderPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 7. RAW JSON DISPLAY
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_raw_json"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141316)),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(DarkOutline)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "JSON thô từ Gemini API:",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = LavenderPrimary
                    )

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(result.rawJson))
                            Toast.makeText(context, "Đã sao chép JSON thô", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy JSON",
                            modifier = Modifier.size(16.dp),
                            tint = LavenderPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sao chép",
                            style = MaterialTheme.typography.labelSmall,
                            color = LavenderPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = result.rawJson.ifBlank { "Không có dữ liệu JSON thô" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    ),
                    color = TextMediumContrast
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 8. BIG BOTTOM ACTION: KIỂM TRA NỘI DUNG KHÁC
        Button(
            onClick = onBackToHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .testTag("button_scan_another"),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LavenderPrimary,
                contentColor = OnLavenderPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Kiểm tra nội dung khác",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private data class BannerTheme(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeColor: Color,
    val containerColor: Color,
    val contentColor: Color
)
