package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
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
import com.example.util.toSpeechText

@Composable
fun ResultScreen(
    result: ScamAnalysisResult,
    relativePhone: String = "",
    autoReadResult: Boolean = false,
    originalText: String? = null,
    originalImageBitmap: Bitmap? = null,
    originalImageUri: Uri? = null,
    onOpenSettings: () -> Unit = {},
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Log raw JSON for developer inspection in debug mode only
    LaunchedEffect(result) {
        if (com.example.BuildConfig.DEBUG) {
            Log.d("AnTamAI", "=== GEMINI ANALYSIS RESULT ===")
            Log.d("AnTamAI", "Status: ${result.status}")
            Log.d("AnTamAI", "Opening: ${result.openingMessage}")
            Log.d("AnTamAI", "Signals: ${result.signals}")
            Log.d("AnTamAI", "Reminders: ${result.reminders}")
            Log.d("AnTamAI", "Action: ${result.action}")
            Log.d("AnTamAI", "Important Notes: ${result.importantNotes}")
            Log.d("AnTamAI", "Hotline: ${result.officialHotline}")
        }
    }

    // Text To Speech Helper initialization and lifecycle
    val ttsHelper = remember { TextToSpeechHelper(context) }
    val isSpeaking by ttsHelper.isSpeaking.collectAsStateWithLifecycle()

    // Prepare speech text from ScamAnalysisResult extension
    val textToRead = remember(result) { result.toSpeechText() }

    // Auto-read on launch if setting enabled
    LaunchedEffect(Unit) {
        if (autoReadResult && textToRead.isNotBlank()) {
            ttsHelper.speak(textToRead)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    // Status visuals mapping:
    // DANGER = "LỪA ĐẢO" (Đỏ, icon Cancel)
    // WARNING = "CẦN THẬN TRỌNG" (Vàng/Cam, icon Warning)
    // SAFE = "AN TOÀN" (Xanh lá, icon CheckCircle)
    val status = result.scamStatus
    val (statusLabel, statusIcon, statusColor, statusBgColor, statusBorderColor, statusTextColor) = when (status) {
        ScamStatus.DANGER -> StatusVisuals(
            label = "LỪA ĐẢO",
            icon = Icons.Default.Cancel,
            color = DangerRed,
            containerColor = DangerContainer,
            borderColor = DangerBorder,
            textColor = OnDangerContainer
        )
        ScamStatus.WARNING -> StatusVisuals(
            label = "CẦN THẬN TRỌNG",
            icon = Icons.Default.Warning,
            color = WarningAmber,
            containerColor = WarningContainer,
            borderColor = WarningBorder,
            textColor = OnWarningContainer
        )
        ScamStatus.SAFE, ScamStatus.UNKNOWN -> StatusVisuals(
            label = "AN TOÀN",
            icon = Icons.Default.CheckCircle,
            color = SafeGreen,
            containerColor = SafeContainer,
            borderColor = SafeBorder,
            textColor = OnSafeContainer
        )
    }

    // Resolved phone action if available (either from action object or official_hotline)
    val resolvedActionPhone = result.action?.phone?.takeIf { it.isNotBlank() }
        ?: result.officialHotline?.takeIf { it.isNotBlank() }
    val resolvedActionLabel = result.action?.label?.takeIf { it.isNotBlank() }
        ?: if (!resolvedActionPhone.isNullOrBlank()) "Gọi tổng đài chính thức" else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("screen_result"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP BAR: Navigation & Settings icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBackToHome() }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = OceanPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Kết quả kiểm tra",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    fontWeight = FontWeight.Bold,
                    color = TextHighContrast
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LightSurfaceVariant)
                    .testTag("button_open_settings_from_result")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Cài đặt",
                    tint = OceanPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ORIGINAL CONTENT SNIPPET / IMAGE PREVIEW
        if (!originalText.isNullOrBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurfaceVariant),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = SolidColor(LightOutline)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Nội dung vừa kiểm tra:",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                        color = TextSubtle,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = originalText,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                        color = TextMediumContrast,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else if (originalImageBitmap != null || originalImageUri != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurfaceVariant),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = SolidColor(LightOutline)
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (originalImageBitmap != null) {
                        Image(
                            bitmap = originalImageBitmap.asImageBitmap(),
                            contentDescription = "Ảnh đã kiểm tra",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else if (originalImageUri != null) {
                        AsyncImage(
                            model = originalImageUri,
                            contentDescription = "Ảnh đã kiểm tra",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Ảnh chụp / Hóa đơn đã kiểm tra",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        Text(
                            text = "Đã quét và phân tích chi tiết",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = TextSubtle
                        )
                    }
                }
            }
        }

        // ==========================================
        // VÙNG 1: STATUS BANNER 1 DÒNG
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("banner_status_result"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = statusBgColor),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.5.dp,
                brush = SolidColor(statusBorderColor)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 19.sp,
                        letterSpacing = 0.5.sp
                    ),
                    fontWeight = FontWeight.Black,
                    color = statusTextColor
                )
            }
        }

        // ==========================================
        // KHỐI LƯU Ý TÀI CHÍNH (NẾU CÓ CHO ẢNH BIÊN LAI / CHUYỂN KHOẢN)
        // Hiển thị màu xanh dương trung tính, luôn hiện bất kể status
        // ==========================================
        if (result.financialReminder?.show == true) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_financial_reminder"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = OceanPrimaryContainer.copy(alpha = 0.6f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.2.dp,
                    brush = SolidColor(OceanPrimary.copy(alpha = 0.45f))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(OceanPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = OceanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lưu ý tài chính quan trọng:",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold,
                            color = OceanPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val msg1 = result.financialReminder.message1
                        ?: "Mặc dù bức ảnh này trông hoàn toàn bình thường, ba mẹ tuyệt đối chưa giao hàng hay chuyển tiền vội nhé ạ."
                    val msg2 = result.financialReminder.message2
                        ?: "Nguyên tắc vàng: Ba mẹ hãy tự mở ứng dụng ngân hàng của mình lên. Chỉ khi nào thấy số dư thực tế tăng lên thì giao dịch mới thực sự an toàn."

                    Text(
                        text = msg1,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextHighContrast
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = msg2,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = OceanPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // OPENING MESSAGE TRẤN AN
        if (result.openingMessage.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_opening_message"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = SolidColor(statusBorderColor.copy(alpha = 0.6f))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = result.openingMessage,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 14.5.sp,
                            lineHeight = 21.sp
                        ),
                        fontWeight = FontWeight.Medium,
                        color = TextHighContrast
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // READ ALOUD TTS BUTTON
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
                .height(46.dp)
                .testTag("button_read_aloud"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSpeaking) DangerContainer else OceanPrimaryContainer,
                contentColor = if (isSpeaking) OnDangerContainer else OnOceanPrimaryContainer
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = SolidColor(if (isSpeaking) DangerBorder else OceanPrimary.copy(alpha = 0.4f))
            )
        ) {
            Icon(
                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = if (isSpeaking) "Dừng đọc" else "Đọc to",
                modifier = Modifier.size(18.dp),
                tint = if (isSpeaking) DangerRed else OceanPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSpeaking) "⏹️ Đang đọc... Bấm để dừng lại" else "🔊 Đọc to nội dung kết quả",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // VÙNG 2: SIGNALS (DẤU HIỆU NHẬN BIẾT - TỐI ĐA 3 MỤC)
        // ==========================================
        if (result.signals.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_signals_list"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = SolidColor(LightOutline)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Dấu hiệu nhận biết:",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (status == ScamStatus.DANGER) DangerRed else if (status == ScamStatus.WARNING) WarningAmber else OceanPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    result.signals.take(3).forEach { signal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (status == ScamStatus.DANGER) Icons.Default.Cancel else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (status == ScamStatus.DANGER) DangerRed else WarningAmber,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = signal,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                ),
                                color = TextHighContrast,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // ==========================================
        // VÙNG 3: REMINDERS (DANH SÁCH LỜI NHẮC TĨNH VỚI ICON, KHÔNG PHẢI NÚT)
        // ==========================================
        val effectiveReminders = if (result.reminders.isNotEmpty()) {
            result.reminders.take(4)
        } else {
            // Fallback default reminders based on status if empty
            when (status) {
                ScamStatus.DANGER -> listOf(
                    "Tuyệt đối không chuyển tiền theo yêu cầu",
                    "Không cung cấp mật khẩu, mã OTP hoặc thông tin thẻ",
                    "Không bấm vào bất kỳ đường link lạ nào trong tin nhắn",
                    "Xóa tin nhắn hoặc chặn người gửi nếu bị làm phiền"
                )
                ScamStatus.WARNING -> listOf(
                    "Chưa vội giao dịch hay chuyển tiền",
                    "Tự mở app ngân hàng chính thức để kiểm tra số dư thực tế",
                    "Không cung cấp thông tin cá nhân cho người lạ"
                )
                ScamStatus.SAFE, ScamStatus.UNKNOWN -> listOf(
                    "Nội dung chưa thấy dấu hiệu lừa đảo nguy hiểm",
                    "Luôn giữ cảnh giác khi được yêu cầu cung cấp thông tin tài chính"
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_reminders_list"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (status == ScamStatus.DANGER) DangerContainer.copy(alpha = 0.35f)
                else if (status == ScamStatus.WARNING) WarningContainer.copy(alpha = 0.35f)
                else SafeContainer.copy(alpha = 0.35f)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = SolidColor(if (status == ScamStatus.DANGER) DangerBorder else if (status == ScamStatus.WARNING) WarningBorder else SafeBorder)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (status == ScamStatus.DANGER) Icons.Default.Block else Icons.Default.Security,
                        contentDescription = null,
                        tint = if (status == ScamStatus.DANGER) DangerRed else if (status == ScamStatus.WARNING) WarningAmber else SafeGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lời nhắc an toàn quan trọng:",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                effectiveReminders.forEach { reminder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (status == ScamStatus.DANGER) DangerRed.copy(alpha = 0.15f)
                                    else if (status == ScamStatus.WARNING) WarningAmber.copy(alpha = 0.15f)
                                    else SafeGreen.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (status == ScamStatus.DANGER) Icons.Default.Block else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (status == ScamStatus.DANGER) DangerRed else if (status == ScamStatus.WARNING) WarningAmber else SafeGreen,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = reminder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextHighContrast,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ==========================================
        // LƯU Ý QUAN TRỌNG (IMPORTANT NOTES)
        // ==========================================
        if (result.importantNotes.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_important_notes"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = SolidColor(if (status == ScamStatus.DANGER) DangerBorder else if (status == ScamStatus.WARNING) WarningBorder else OceanPrimary.copy(alpha = 0.5f))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = if (status == ScamStatus.DANGER) DangerRed else if (status == ScamStatus.WARNING) WarningAmber else OceanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lưu ý quan trọng:",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    result.importantNotes.forEach { note ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (status == ScamStatus.DANGER) DangerRed else if (status == ScamStatus.WARNING) WarningAmber else OceanPrimary
                            )
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
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

        // ==========================================
        // VÙNG 4: ACTION CHÍNH (1 NÚT TO NỔI BẬT DUY NHẤT KHI CÓ PHONE)
        // ==========================================
        if (!resolvedActionPhone.isNullOrBlank()) {
            val fullActionLabel = if (resolvedActionLabel != null) {
                "$resolvedActionLabel: $resolvedActionPhone"
            } else {
                "Gọi tổng đài: $resolvedActionPhone"
            }

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$resolvedActionPhone")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        clipboardManager.setText(AnnotatedString(resolvedActionPhone))
                        Toast.makeText(context, "Đã sao chép số: $resolvedActionPhone", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("button_primary_action_call"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OceanPrimary,
                    contentColor = OnOceanPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneInTalk,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = fullActionLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.5.sp),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // ==========================================
        // VÙNG 5: NÚT GỌI NGƯỜI THÂN (TỪ SETTINGS - HIỂN THỊ RIÊNG, NHỎ HƠN, BÊN DƯỚI)
        // ==========================================
        if (relativePhone.isNotBlank()) {
            OutlinedButton(
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
                    .height(44.dp)
                    .testTag("button_call_relative"),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = SolidColor(LightOutline)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FamilyRestroom,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = TextMediumContrast
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Gọi hỏi ý kiến người thân: $relativePhone",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = TextHighContrast
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        HorizontalDivider(color = LightOutline, thickness = 1.dp)
        Spacer(modifier = Modifier.height(14.dp))

        // NÚT "KIỂM TRA NỘI DUNG KHÁC" Ở CUỐI TRANG
        Button(
            onClick = onBackToHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("button_scan_another"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LightSurfaceVariant,
                contentColor = TextHighContrast
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = SolidColor(LightOutline)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = OceanPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Kiểm tra nội dung khác",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

private data class StatusVisuals(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val containerColor: Color,
    val borderColor: Color,
    val textColor: Color
)
