package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ScamAnalysisResult
import com.example.data.model.ScamStatus
import com.example.ui.theme.DangerBorder
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightOutline
import com.example.ui.theme.LightSurface
import com.example.ui.theme.LightSurfaceVariant
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanPrimaryContainer
import com.example.ui.theme.OnDangerContainer
import com.example.ui.theme.OnOceanPrimary
import com.example.ui.theme.OnOceanPrimaryContainer
import com.example.ui.theme.OnSafeContainer
import com.example.ui.theme.OnWarningContainer
import com.example.ui.theme.SafeBorder
import com.example.ui.theme.SafeContainer
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMediumContrast
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningBorder
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

    // Log raw JSON for debugging as requested by user
    LaunchedEffect(result) {
        Log.d("AnTamAI", "=== GEMINI ANALYSIS RESULT ===")
        Log.d("AnTamAI", "Status: ${result.status}")
        Log.d("AnTamAI", "Opening: ${result.openingMessage}")
        Log.d("AnTamAI", "Signals: ${result.signals}")
        Log.d("AnTamAI", "Actions: ${result.recommendedActions}")
        Log.d("AnTamAI", "Hotline: ${result.officialHotline}")
        Log.d("AnTamAI", "Raw JSON: ${result.rawJson}")
    }

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
    val (statusTitle, statusSubtitle, statusIcon, statusColor, statusBgColor, statusBorderColor, statusTextColor) = when (status) {
        ScamStatus.DANGER -> BannerTheme(
            title = "CẢNH BÁO: CÓ DẤU HIỆU LỪA ĐẢO",
            subtitle = "Bạn tuyệt đối KHÔNG làm theo yêu cầu trong tin nhắn hoặc hình ảnh này!",
            icon = Icons.Default.Dangerous,
            badgeColor = DangerRed,
            containerColor = DangerContainer,
            borderColor = DangerBorder,
            contentColor = OnDangerContainer
        )
        ScamStatus.WARNING -> BannerTheme(
            title = "CẢNH BÁO: CẦN HẾT SỨC CẨN TRỌNG",
            subtitle = "Chưa thể khẳng định an toàn. Bạn hãy tự mở ứng dụng ngân hàng để kiểm tra số dư thực tế.",
            icon = Icons.Default.Warning,
            badgeColor = WarningAmber,
            containerColor = WarningContainer,
            borderColor = WarningBorder,
            contentColor = OnWarningContainer
        )
        ScamStatus.SAFE -> BannerTheme(
            title = "ĐÁNH GIÁ: AN TOÀN",
            subtitle = "Không phát hiện dấu hiệu lừa đảo phổ biến.",
            icon = Icons.Default.CheckCircle,
            badgeColor = SafeGreen,
            containerColor = SafeContainer,
            borderColor = SafeBorder,
            contentColor = OnSafeContainer
        )
        ScamStatus.UNKNOWN -> BannerTheme(
            title = "KẾT QUẢ PHÂN TÍCH",
            subtitle = "Hãy đọc kỹ các lưu ý bên dưới trước khi thao tác.",
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            badgeColor = OceanPrimary,
            containerColor = LightSurfaceVariant,
            borderColor = LightOutline,
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
                    append("Dấu hiệu ${i + 1}: $sig. ")
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
            .background(LightBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("screen_result"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Navigation row: Back & Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onBackToHome,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("button_back_home")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = OceanPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Kiểm tra nội dung khác",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                    color = OceanPrimary
                )
            }

            OutlinedButton(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("button_open_settings_from_result")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Cài đặt",
                    tint = OceanPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Cài đặt",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                    color = OceanPrimary
                )
            }
        }

        // 1. BANNER LỚN ĐỔI MÀU THEO STATUS (DANGER=ĐỎ, WARNING=VÀNG, SAFE=XANH)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("banner_status"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = statusBgColor),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.5.dp,
                brush = androidx.compose.ui.graphics.SolidColor(statusBorderColor)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = statusTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        lineHeight = 24.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = statusTextColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = statusSubtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    ),
                    color = statusTextColor.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                .height(48.dp)
                .testTag("button_read_aloud"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSpeaking) DangerContainer else OceanPrimaryContainer,
                contentColor = if (isSpeaking) OnDangerContainer else OnOceanPrimaryContainer
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(if (isSpeaking) DangerBorder else OceanPrimary.copy(alpha = 0.4f))
            )
        ) {
            Icon(
                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = if (isSpeaking) "Dừng đọc" else "Đọc to",
                modifier = Modifier.size(20.dp),
                tint = if (isSpeaking) DangerRed else OceanPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSpeaking) "⏹️ Đang đọc... Bấm để dừng lại" else "🔊 Đọc to nội dung cảnh báo",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. OPENING_MESSAGE LÀM TIÊU ĐỀ LỚN
        if (result.openingMessage.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_opening_message"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(OceanPrimary.copy(alpha = 0.3f))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Lời khuyên dành cho bạn:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = OceanPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = result.openingMessage,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = TextHighContrast
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. SIGNALS HIỂN THỊ DẠNG DANH SÁCH GẠCH ĐẦU DÒNG, ICON CẢNH BÁO
        if (result.signals.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_signals_list"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(LightOutline)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Các dấu hiệu nhận diện được:",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    result.signals.forEach { signal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Cảnh báo",
                                tint = WarningAmber,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 3.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = signal,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                ),
                                color = TextHighContrast,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 5. RECOMMENDED_ACTIONS HIỂN THỊ DẠNG NÚT BẤM
        if (result.recommendedActions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_recommended_actions"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(LightOutline)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hành động khuyến nghị:",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                        fontWeight = FontWeight.Bold,
                        color = OceanPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    result.recommendedActions.forEachIndexed { index, action ->
                        val isChecked = checkedActions[index] == true

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    checkedActions[index] = !isChecked
                                    Toast.makeText(
                                        context,
                                        if (!isChecked) "Đã ghi nhớ: $action" else "Đã bỏ chọn",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .testTag("action_button_$index"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChecked) SafeContainer else LightSurfaceVariant
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                width = 1.dp,
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (isChecked) SafeBorder else LightOutline
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isChecked) SafeGreen else OceanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = action,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
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

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 6. HOTLINE VÀ GỌI NGƯỜI THÂN
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Nút "Gọi tổng đài chính thức" nếu official_hotline khác null
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
                        .height(50.dp)
                        .testTag("button_call_official_hotline"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OceanPrimary,
                        contentColor = OnOceanPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gọi tổng đài chính thức (${result.officialHotline})",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
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
                        .height(50.dp)
                        .testTag("button_call_relative"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OceanPrimaryContainer,
                        contentColor = OnOceanPrimaryContainer
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(OceanPrimary.copy(alpha = 0.5f))
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FamilyRestroom,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = OceanPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gọi người thân ($relativePhone)",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Button to prompt configuring relative's phone
                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("button_setup_relative_phone"),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(LightOutline)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = OceanPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cài đặt số điện thoại người thân để gọi nhanh",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = OceanPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. BIG BOTTOM ACTION: KIỂM TRA NỘI DUNG KHÁC
        Button(
            onClick = onBackToHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("button_scan_another"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OceanPrimary,
                contentColor = OnOceanPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Kiểm tra nội dung khác",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private data class BannerTheme(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeColor: Color,
    val containerColor: Color,
    val borderColor: Color,
    val contentColor: Color
)
