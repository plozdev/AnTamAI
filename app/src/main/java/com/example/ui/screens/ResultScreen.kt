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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.LightOutlineVariant
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

    // Log raw JSON for developer inspection
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

    // Prepare speech text from opening_message, signals, important_notes and recommended_actions
    val textToRead = remember(result) {
        buildString {
            if (result.openingMessage.isNotBlank()) {
                append(result.openingMessage)
                append(". ")
            }
            if (result.signals.isNotEmpty()) {
                append("Các dấu hiệu chính: ")
                result.signals.forEachIndexed { i, sig ->
                    append("Dấu hiệu ${i + 1}: $sig. ")
                }
            }
            if (result.importantNotes.isNotEmpty()) {
                append("Lưu ý quan trọng: ")
                result.importantNotes.forEach { note ->
                    append("$note. ")
                }
            }
            if (result.recommendedActions.isNotEmpty()) {
                append("Khuyến nghị: ")
                result.recommendedActions.forEach { act ->
                    append("$act. ")
                }
            }
        }
    }

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

    val checkedActions = remember { mutableStateMapOf<Int, Boolean>() }

    // Status Banner 1 dòng theo yêu cầu:
    // DANGER = "LỪA ĐẢO" (Đỏ, icon X/Dangerous)
    // WARNING = "CẦN THẬN TRỌNG" (Vàng/Cam, icon Warning)
    // SAFE = "AN TOÀN" (Xanh lá, icon Check)
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
                modifier = Modifier.clickable { onBackToHome() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = OceanPrimary,
                    modifier = Modifier.size(20.dp)
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

        // ==========================================
        // VÙNG 1: NHẬN DIỆN NHANH (QUICK IDENTIFICATION)
        // ==========================================

        // 1.1 Preview thu nhỏ nội dung người dùng đã gửi
        if (originalText != null || originalImageBitmap != null || originalImageUri != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .testTag("card_input_preview"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 1.dp,
                    brush = SolidColor(LightOutline)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (originalImageBitmap != null) {
                        Image(
                            bitmap = originalImageBitmap.asImageBitmap(),
                            contentDescription = "Ảnh đã phân tích",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ảnh chụp màn hình vừa kiểm tra",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                fontWeight = FontWeight.Bold,
                                color = TextHighContrast
                            )
                            Text(
                                text = "Đã quét và phân tích bằng Gemini Vision",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSubtle
                            )
                        }
                    } else if (originalImageUri != null) {
                        AsyncImage(
                            model = originalImageUri,
                            contentDescription = "Ảnh đã phân tích",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ảnh chụp màn hình vừa kiểm tra",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                fontWeight = FontWeight.Bold,
                                color = TextHighContrast
                            )
                            Text(
                                text = "Đã quét và phân tích bằng Gemini Vision",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSubtle
                            )
                        }
                    } else if (!originalText.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(OceanPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = OceanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Nội dung vừa kiểm tra:",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = TextSubtle
                            )
                            Text(
                                text = "\"$originalText\"",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextHighContrast,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 1.2 Banner trạng thái NGẮN GỌN 1 dòng
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("banner_status"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = statusBgColor),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.5.dp,
                brush = SolidColor(statusBorderColor)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.2f)),
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

        Spacer(modifier = Modifier.height(10.dp))

        // 1.3 Gộp opening_message làm đoạn trấn an DUY NHẤT ngay dưới banner
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

        // 1.4 Nút "🔊 Đọc to" giữ vị trí dễ thấy
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
        // VÙNG 2: LÝ DO (SIGNALS - TỐI ĐA 3 MỤC)
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
        // LƯU Ý QUAN TRỌNG (IMPORTANT NOTES - DẠNG TEXT/BULLET, KHÔNG PHẢI NÚT)
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
        // VÙNG 3: HÀNH ĐỘNG KHUYẾN NGHỊ (RECOMMENDED ACTIONS RENDER THÀNH NÚT BẤM)
        // ==========================================
        Text(
            text = "Hành động đề xuất:",
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
            fontWeight = FontWeight.Bold,
            color = TextHighContrast,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Nút hành động chính theo trạng thái:
            when (status) {
                ScamStatus.DANGER -> {
                    // Nút xoá / dismiss cho luồng thủ công
                    Button(
                        onClick = {
                            Toast.makeText(context, "Đã hiểu và xóa nội dung vừa nhập!", Toast.LENGTH_SHORT).show()
                            onBackToHome()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("button_primary_action_danger"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerRed,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đã hiểu, xoá nội dung đã nhập",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Render các recommended_actions bổ sung thành các nút bấm
                    result.recommendedActions.forEach { action ->
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Khuyến nghị: $action", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                width = 1.dp,
                                brush = SolidColor(DangerRed.copy(alpha = 0.5f))
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = action,
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = DangerRed
                            )
                        }
                    }
                }

                ScamStatus.WARNING -> {
                    // Render các recommended_actions ngắn gọn thành các nút bấm nổi bật
                    if (result.recommendedActions.isNotEmpty()) {
                        result.recommendedActions.forEachIndexed { index, action ->
                            if (index == 0) {
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Hành động: $action", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("button_warning_action_$index"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OceanPrimary,
                                        contentColor = OnOceanPrimary
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = action,
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Hành động: $action", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("button_warning_action_$index"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        width = 1.dp,
                                        brush = SolidColor(OceanPrimary)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = OceanPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = action,
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                                        fontWeight = FontWeight.SemiBold,
                                        color = OceanPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Nút dismiss/clear
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Đã hiểu và hoàn tất kiểm tra!", Toast.LENGTH_SHORT).show()
                            onBackToHome()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("button_dismiss_warning"),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            width = 1.dp,
                            brush = SolidColor(LightOutline)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = TextMediumContrast,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đã hiểu, xoá nội dung đã nhập",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = TextHighContrast
                        )
                    }
                }

                ScamStatus.SAFE, ScamStatus.UNKNOWN -> {
                    Button(
                        onClick = onBackToHome,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("button_primary_action_safe"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OceanPrimary,
                            contentColor = OnOceanPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nội dung an toàn • Đã hiểu",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    result.recommendedActions.forEach { action ->
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, action, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                width = 1.dp,
                                brush = SolidColor(OceanPrimary)
                            )
                        ) {
                            Text(
                                text = action,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = OceanPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3.2 HÀNH ĐỘNG PHỤ: GỌI TỔNG ĐÀI / GỌI NGƯỜI THÂN (Nhỏ gọn, Outlined)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Nút phụ 1: Gọi tổng đài chính thức
            if (!result.officialHotline.isNullOrBlank()) {
                OutlinedButton(
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
                        .height(44.dp)
                        .testTag("button_call_official_hotline"),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = SolidColor(OceanPrimary)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = OceanPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Gọi tổng đài chính thức: ${result.officialHotline}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = OceanPrimary
                    )
                }
            }

            // Nút phụ 2: Gọi người thân
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
                    shape = RoundedCornerShape(10.dp),
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
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        HorizontalDivider(color = LightOutline, thickness = 1.dp)
        Spacer(modifier = Modifier.height(14.dp))

        // 3.3 NÚT "KIỂM TRA NỘI DUNG KHÁC" Ở CUỐI TRANG
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
