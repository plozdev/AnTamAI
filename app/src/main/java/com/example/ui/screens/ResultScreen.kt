package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.ScamAnalysisResult
import com.example.data.model.ScamStatus
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.LavenderPrimaryContainer
import com.example.ui.theme.LavenderSecondaryContainer
import com.example.ui.theme.OnDangerContainer
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

@Composable
fun ResultScreen(
    result: ScamAnalysisResult,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val status = result.scamStatus
    val (statusTitle, statusSubtitle, statusIcon, statusColor, statusBgColor, statusTextColor) = when (status) {
        ScamStatus.DANGER -> HexStatusTheme(
            title = "CẢNH BÁO: CÓ DẤU HIỆU LỪA ĐẢO RÕ RÀNG",
            subtitle = "Bác tuyệt đối KHÔNG làm theo yêu cầu trong tin nhắn hoặc hình ảnh này!",
            icon = Icons.Default.Dangerous,
            badgeColor = DangerRed,
            containerColor = DangerContainer,
            contentColor = OnDangerContainer
        )
        ScamStatus.WARNING -> HexStatusTheme(
            title = "CẢNH BÁO: CẦN HẾT SỨC CẨN TRỌNG",
            subtitle = "Chưa thể khẳng định an toàn. Bác hãy tự mở ứng dụng ngân hàng để kiểm tra số dư thực tế.",
            icon = Icons.Default.Warning,
            badgeColor = WarningAmber,
            containerColor = WarningContainer,
            contentColor = OnWarningContainer
        )
        ScamStatus.SAFE -> HexStatusTheme(
            title = "ĐÁNH GIÁ: AN TOÀN",
            subtitle = "Không phát hiện dấu hiệu lừa đảo phổ biến.",
            icon = Icons.Default.CheckCircle,
            badgeColor = SafeGreen,
            containerColor = SafeContainer,
            contentColor = OnSafeContainer
        )
        ScamStatus.UNKNOWN -> HexStatusTheme(
            title = "KẾT QUẢ PHÂN TÍCH",
            subtitle = "Hãy đọc kỹ các lưu ý bên dưới trước khi thao tác.",
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            badgeColor = LavenderPrimary,
            containerColor = DarkSurface,
            contentColor = TextHighContrast
        )
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
        // Top Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
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
        }

        // 1. STATUS HEADER CARD (2REM CORNERS, DISTINCTIVE CONTAINER)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = statusBgColor),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.5.dp,
                brush = androidx.compose.ui.graphics.SolidColor(statusColor.copy(alpha = 0.8f))
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = statusTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                    fontWeight = FontWeight.Bold,
                    color = statusTextColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = statusSubtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp, lineHeight = 23.sp),
                    color = statusTextColor.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. OPENING REASSURING MESSAGE
        if (result.openingMessage.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Lời gửi tới bác:",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.Bold,
                        color = LavenderPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.openingMessage,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 26.sp),
                        color = TextHighContrast
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 3. SIGNALS (DẤU HIỆU NHẬN BIẾT)
        if (result.signals.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Các dấu hiệu đáng ngờ nhận diện được:",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    result.signals.forEachIndexed { index, signal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = TextHighContrast
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = signal,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
                                color = TextHighContrast,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 4. RECOMMENDED ACTIONS
        if (result.recommendedActions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(LavenderPrimary.copy(alpha = 0.5f)))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Việc bác cần làm ngay:",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = LavenderPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    result.recommendedActions.forEach { action ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SafeGreen,
                                modifier = Modifier
                                    .size(22.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = action,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
                                color = TextHighContrast,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 5. OFFICIAL HOTLINE IF AVAILABLE
        if (!result.officialHotline.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hotline chính thức của đơn vị:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMediumContrast
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.officialHotline,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                            fontWeight = FontWeight.Bold,
                            color = LavenderPrimary
                        )
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${result.officialHotline}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                clipboardManager.setText(AnnotatedString(result.officialHotline))
                                Toast.makeText(context, "Đã sao chép số hotline: ${result.officialHotline}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LavenderPrimary,
                            contentColor = OnLavenderPrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Gọi hotline")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Gọi ngay", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 6. RAW JSON DISPLAY (SPEC)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_raw_json"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141316)),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
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
                        Text(text = "Sao chép", style = MaterialTheme.typography.labelSmall, color = LavenderPrimary)
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

        Spacer(modifier = Modifier.height(28.dp))

        // Big Bottom Action: Kiểm tra nội dung khác
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

private data class HexStatusTheme(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeColor: Color,
    val containerColor: Color,
    val contentColor: Color
)
