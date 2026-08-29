package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.local.SmsEntity
import com.example.data.model.SmsMessage
import com.example.util.AppConstants
import com.example.util.SmsAnalysisStatus
import com.example.ui.components.AppTabHeader
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
import com.example.ui.theme.OnOceanPrimary
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun formatMessageDate(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val msgCal = Calendar.getInstance().apply { time = date }
    return if (now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)
    ) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    } else if (now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR)) {
        SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
    } else {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }
}

@Composable
fun SmsInboxScreen(
    smsEntities: List<SmsEntity> = emptyList(),
    fallbackMessages: List<SmsMessage> = emptyList(),
    isLoading: Boolean,
    errorMessage: String?,
    autoScanEnabled: Boolean = true,
    onRefresh: () -> Unit,
    onOpenSmsItem: (SmsEntity) -> Unit = {},
    onDismissSms: (Long) -> Unit = {},
    onDismissAllSuspicious: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var hasReadSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasReceiveSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showPermissionExplanationDialog by remember { mutableStateOf(false) }
    var selectedSmsForDetail by remember { mutableStateOf<SmsEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var filterOnlySuspicious by remember { mutableStateOf(false) }

    val multiplePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasReadSmsPermission = permissions[Manifest.permission.READ_SMS] == true
        hasReceiveSmsPermission = permissions[Manifest.permission.RECEIVE_SMS] == true
        if (hasReadSmsPermission) {
            onRefresh()
        }
    }

    LaunchedEffect(hasReadSmsPermission) {
        if (hasReadSmsPermission) {
            onRefresh()
        }
    }

    // Convert fallbackMessages to entities if room entities are currently empty, sorted strictly by timestamp DESC
    val displayedEntities = remember(smsEntities, fallbackMessages) {
        val raw = if (smsEntities.isNotEmpty()) {
            smsEntities
        } else {
            fallbackMessages.map { msg ->
                SmsEntity(
                    id = msg.id,
                    smsId = msg.id,
                    address = msg.address,
                    body = msg.body,
                    timestamp = msg.date,
                    heuristicNeedsScrutiny = msg.heuristicResult.needsScrutiny,
                    heuristicSignals = msg.heuristicResult.matchedSignals.joinToString(AppConstants.SIGNAL_SEPARATOR),
                    status = if (msg.heuristicResult.needsScrutiny) AppConstants.STATUS_WARNING else AppConstants.STATUS_SAFE,
                    openingMessage = if (msg.heuristicResult.needsScrutiny) "Tin nhắn có từ khóa cần chú ý" else "Tin nhắn bình thường",
                    resultJson = "",
                    isDismissed = false
                )
            }
        }
        raw.sortedByDescending { it.timestamp }
    }

    val unDismissedSuspiciousCount = remember(displayedEntities) {
        displayedEntities.count { entity ->
            val parsedStatus = SmsAnalysisStatus.fromString(entity.status)
            !entity.isDismissed && (
                parsedStatus == SmsAnalysisStatus.DANGER ||
                parsedStatus == SmsAnalysisStatus.WARNING ||
                parsedStatus == SmsAnalysisStatus.ANALYZING ||
                parsedStatus == SmsAnalysisStatus.RETRYING ||
                entity.heuristicNeedsScrutiny
            )
        }
    }

    val filteredEntities = remember(displayedEntities, searchQuery, filterOnlySuspicious) {
        displayedEntities.filter { entity ->
            val matchSearch = searchQuery.isBlank() ||
                entity.address.contains(searchQuery, ignoreCase = true) ||
                entity.body.contains(searchQuery, ignoreCase = true)
            val parsedStatus = SmsAnalysisStatus.fromString(entity.status)
            val isSuspicious = !entity.isDismissed && (
                parsedStatus == SmsAnalysisStatus.DANGER ||
                parsedStatus == SmsAnalysisStatus.WARNING ||
                parsedStatus == SmsAnalysisStatus.ANALYZING ||
                parsedStatus == SmsAnalysisStatus.RETRYING ||
                entity.heuristicNeedsScrutiny
            )
            val matchFilter = !filterOnlySuspicious || isSuspicious
            matchSearch && matchFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
            .testTag("screen_sms_inbox")
    ) {
        // UNIFIED HEADER BAR
        AppTabHeader(
            icon = Icons.Default.Message,
            title = "Nhật ký an toàn",
            subtitle = "Lịch sử nhận và quét tin nhắn SMS",
            trailingAction = {
                IconButton(
                    onClick = {
                        if (hasReadSmsPermission) {
                            onRefresh()
                        } else {
                            showPermissionExplanationDialog = true
                        }
                    },
                    modifier = Modifier.testTag("button_refresh_sms")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Tải lại",
                        tint = OceanPrimary
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (!hasReadSmsPermission) {
            // PERMISSION NOT GRANTED VIEW (Educational Rationale Screen)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(OceanPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = OceanPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Bảo vệ tin nhắn SMS tức thời",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "AnTâm.AI giúp ba mẹ tự động nhận diện tin nhắn lừa đảo mạo danh ngân hàng, công an, phạt nguội hoặc chứa đường link nguy hiểm.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp
                        ),
                        color = TextMediumContrast,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Benefit Items
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = LightSurfaceVariant),
                        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = SolidColor(LightOutline))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SafeGreen,
                                    modifier = Modifier.size(18.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Đọc tin nhắn sẵn có: Quét tìm các tin có dấu hiệu đe dọa, link lạ.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                    color = TextHighContrast
                                )
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = OceanPrimary,
                                    modifier = Modifier.size(18.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Cảnh báo SMS mới đến: Tự động phân tích và rung chuông cảnh báo ngay khi nhận tin đáng ngờ.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                    color = TextHighContrast
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showPermissionExplanationDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("button_request_sms_permission"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OceanPrimary,
                            contentColor = OnOceanPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bật bảo vệ tin nhắn SMS",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Realtime Auto Scan Disabled Warning Banner (if user has read permission but disabled realtime scan or denied receive permission)
                if (!hasReceiveSmsPermission || !autoScanEnabled) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable {
                                if (!hasReceiveSmsPermission) {
                                    showPermissionExplanationDialog = true
                                } else {
                                    onOpenSettings()
                                }
                            }
                            .testTag("banner_realtime_sms_disabled"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = LightSurfaceVariant),
                        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = SolidColor(LightOutline))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = OceanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (!hasReceiveSmsPermission) "Chưa bật cảnh báo SMS mới đến. Chạm để cấp quyền bảo vệ tức thời." else "Tự động quét SMS mới đang tắt trong Cài đặt.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = TextMediumContrast,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TextSubtle,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // SMS SEARCH BAR
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Tìm người gửi, nội dung...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                            color = TextSubtle
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = OceanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Xóa tìm kiếm",
                                    tint = TextSubtle,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LightSurface,
                        unfocusedContainerColor = LightSurface,
                        focusedBorderColor = OceanPrimary,
                        unfocusedBorderColor = LightOutline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("input_search_sms")
                )

                // PROMINENT WARNING BANNER IF SUSPICIOUS SMS EXIST
                if (unDismissedSuspiciousCount > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("banner_suspicious_sms_warning"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (filterOnlySuspicious) WarningAmber.copy(alpha = 0.2f) else WarningContainer
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            width = 1.2.dp,
                            brush = SolidColor(WarningBorder)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { filterOnlySuspicious = !filterOnlySuspicious }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "⚠️ $unDismissedSuspiciousCount tin nhắn cần chú ý",
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = OnWarningContainer
                                    )
                                    Text(
                                        text = if (filterOnlySuspicious) "Đang lọc chỉ tin cần chú ý (Chạm để xem tất cả)" else "Chạm để lọc nhanh các tin nhắn này",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                        color = TextMediumContrast
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Action: Mark all as dismissed
                            OutlinedButton(
                                onClick = onDismissAllSuspicious,
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("button_dismiss_all_suspicious"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = OnWarningContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Đã xem tất cả",
                                    modifier = Modifier.size(14.dp),
                                    tint = OnWarningContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Đã xem tất cả",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                // SMS LIST
                if (isLoading && displayedEntities.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = OceanPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Đang đồng bộ và kiểm tra an toàn...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                                color = TextSubtle
                            )
                        }
                    }
                } else if (!errorMessage.isNullOrBlank() && displayedEntities.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Lỗi khi đọc tin nhắn: $errorMessage",
                                color = DangerRed,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(onClick = onRefresh) {
                                Text("Thử lại")
                            }
                        }
                    }
                } else if (filteredEntities.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                tint = TextSubtle,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (filterOnlySuspicious) "Không có tin nhắn nào cần chú ý!" else "Không tìm thấy tin nhắn SMS phù hợp",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = TextSubtle,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("list_sms_messages"),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (filterOnlySuspicious) {
                            item(key = "filter_header") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ĐANG LỌC: CẦN CHÚ Ý (${filteredEntities.size})",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = DangerRed
                                    )
                                    TextButton(
                                        onClick = { filterOnlySuspicious = false },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Text(
                                            text = "Hiện tất cả",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = OceanPrimary
                                        )
                                    }
                                }
                            }
                        }

                        items(filteredEntities, key = { "sms_${it.id}_${it.timestamp}" }) { entity ->
                            SmsEntityCard(
                                entity = entity,
                                onClick = { selectedSmsForDetail = entity },
                                onDismiss = { onDismissSms(entity.id) }
                            )
                        }
                    }
                }
            }
        }

    // DETAIL MODAL FOR SELECTED SMS
    if (selectedSmsForDetail != null) {
        val entity = selectedSmsForDetail!!
        SmsEntityDetailDialog(
            entity = entity,
            onDismiss = { selectedSmsForDetail = null },
            onDismissAlert = {
                onDismissSms(entity.id)
            },
            onViewFullResult = {
                selectedSmsForDetail = null
                onOpenSmsItem(entity)
            }
        )
    }

    // PERMISSION EXPLANATION DIALOG (Friendly Vietnamese explanation before triggering system prompt)
    if (showPermissionExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionExplanationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = OceanPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quyền bảo vệ tin nhắn SMS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Để bảo vệ ba mẹ trước các tin nhắn lừa đảo ngày càng tinh vi, ứng dụng cần được cấp quyền:",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp, fontSize = 13.5.sp),
                        color = TextHighContrast
                    )

                    Text(
                        text = "1. Đọc tin nhắn (READ_SMS): Giúp quét các tin nhắn hiện có để tìm dấu hiệu đe dọa hoặc link độc hại.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
                        color = TextMediumContrast
                    )

                    Text(
                        text = "2. Nhận tin nhắn (RECEIVE_SMS): Tự động phát hiện và cảnh báo ngay khi có tin nhắn lừa đảo mới gửi tới.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
                        color = TextMediumContrast
                    )

                    Text(
                        text = "🔒 Cam kết: Mọi dữ liệu tin nhắn đều được quét an toàn trên thiết bị của bạn, bảo vệ quyền riêng tư tuyệt đối.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OceanPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionExplanationDialog = false
                        val permissionsToRequest = mutableListOf(
                            Manifest.permission.READ_SMS,
                            Manifest.permission.RECEIVE_SMS
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        multiplePermissionLauncher.launch(permissionsToRequest.toTypedArray())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary)
                ) {
                    Text("Đồng ý & Cho phép")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionExplanationDialog = false }) {
                    Text("Để sau")
                }
            }
        )
    }
}

@Composable
private fun SmsEntityCard(
    entity: SmsEntity,
    onClick: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    val parsedStatus = SmsAnalysisStatus.fromString(entity.status)
    val isAnalyzing = parsedStatus == SmsAnalysisStatus.ANALYZING
    val isRetrying = parsedStatus == SmsAnalysisStatus.RETRYING
    val isDanger = parsedStatus == SmsAnalysisStatus.DANGER
    val isWarning = parsedStatus == SmsAnalysisStatus.WARNING || entity.heuristicNeedsScrutiny
    val isSuspicious = isDanger || isWarning || isAnalyzing || isRetrying

    val (badgeText, badgeColor, badgeBg) = when {
        isAnalyzing -> Triple("Đang phân tích...", OceanPrimary, OceanPrimaryContainer)
        isRetrying -> Triple("Đang chờ phân tích lại...", WarningAmber, WarningContainer)
        isDanger -> Triple("Nguy hiểm", DangerRed, DangerContainer)
        isWarning -> Triple("Cần chú ý", WarningAmber, WarningContainer)
        else -> Triple("Bình thường", SafeGreen, SafeContainer)
    }

    val formattedTime = remember(entity.timestamp) {
        formatMessageDate(entity.timestamp)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("card_sms_item_${entity.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            width = if (isDanger || isWarning || isRetrying) 1.2.dp else 1.dp,
            brush = SolidColor(
                when {
                    isDanger -> DangerBorder
                    isWarning || isRetrying -> WarningBorder
                    else -> LightOutlineVariant
                }
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isDanger -> DangerContainer
                            isWarning || isRetrying -> WarningContainer
                            isAnalyzing -> OceanPrimaryContainer
                            else -> LightSurfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isAnalyzing) {
                    AnalyzingPulseIndicator()
                } else if (isRetrying) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Đang chờ phân tích lại",
                        tint = WarningAmber,
                        modifier = Modifier.size(22.dp)
                    )
                } else if (isDanger) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(22.dp)
                    )
                } else if (isWarning) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    val initial = entity.address.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "S"
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                        fontWeight = FontWeight.Bold,
                        color = OceanPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Body info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entity.address,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSubtle
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = entity.body,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextMediumContrast,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Status Badge & Dismiss Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isAnalyzing) {
                            AnalyzingBadge()
                        } else if (isRetrying) {
                            RetryingBadge()
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = badgeColor
                                )
                            }
                        }

                        if (entity.isDismissed) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LightSurfaceVariant)
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Đã xem",
                                    tint = TextMediumContrast,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Đã xem",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = TextMediumContrast
                                )
                            }
                        }
                    }

                    // 1-Tap Dismiss button for un-dismissed suspicious items
                    if (!entity.isDismissed && isSuspicious) {
                        OutlinedButton(
                            onClick = onDismiss,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp),
                            modifier = Modifier
                                .height(24.dp)
                                .testTag("button_dismiss_sms_${entity.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextMediumContrast
                            ),
                            border = BorderStroke(0.8.dp, LightOutlineVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Bỏ qua",
                                modifier = Modifier.size(11.dp),
                                tint = TextMediumContrast
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Bỏ qua",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSubtle,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun RetryingBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(WarningContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "Đang chờ phân tích lại...",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = WarningAmber
            )
        }
    }
}

@Composable
private fun AnalyzingPulseIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .size(18.dp)
            .alpha(alphaAnim),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = OceanPrimary
        )
    }
}

@Composable
private fun AnalyzingBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_alpha"
    )

    Box(
        modifier = Modifier
            .alpha(alphaAnim)
            .clip(RoundedCornerShape(6.dp))
            .background(OceanPrimaryContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(OceanPrimary)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Đang phân tích...",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = OceanPrimary
            )
        }
    }
}

@Composable
private fun SmsEntityDetailDialog(
    entity: SmsEntity,
    onDismiss: () -> Unit,
    onDismissAlert: () -> Unit = {},
    onViewFullResult: () -> Unit
) {
    val parsedStatus = SmsAnalysisStatus.fromString(entity.status)
    val isDanger = parsedStatus == SmsAnalysisStatus.DANGER
    val isWarning = parsedStatus == SmsAnalysisStatus.WARNING || entity.heuristicNeedsScrutiny
    val isAnalyzing = parsedStatus == SmsAnalysisStatus.ANALYZING
    val isRetrying = parsedStatus == SmsAnalysisStatus.RETRYING

    val formattedDate = remember(entity.timestamp) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(entity.timestamp))
    }

    val signalsList = remember(entity.heuristicSignals) {
        if (entity.heuristicSignals.isNotBlank()) {
            entity.heuristicSignals.split(com.example.util.AppConstants.SIGNAL_SEPARATOR).filter { it.isNotBlank() }
        } else emptyList()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = LightSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isDanger -> DangerContainer
                                        isWarning -> WarningContainer
                                        isAnalyzing -> OceanPrimaryContainer
                                        else -> SafeContainer
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isDanger -> Icons.Default.Warning
                                    isWarning -> Icons.Default.Warning
                                    isAnalyzing -> Icons.Default.AccessTime
                                    else -> Icons.Default.CheckCircle
                                },
                                contentDescription = null,
                                tint = when {
                                    isDanger -> DangerRed
                                    isWarning -> WarningAmber
                                    isAnalyzing -> OceanPrimary
                                    else -> SafeGreen
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = entity.address,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                fontWeight = FontWeight.Bold,
                                color = TextHighContrast
                            )
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSubtle
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = TextSubtle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reassuring explanation
                if (entity.openingMessage.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isDanger -> DangerContainer
                                isWarning -> WarningContainer
                                else -> SafeContainer
                            }
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            width = 1.dp,
                            brush = SolidColor(
                                when {
                                    isDanger -> DangerBorder
                                    isWarning -> WarningBorder
                                    else -> SafeBorder
                                }
                            )
                        )
                    ) {
                        Text(
                            text = entity.openingMessage,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
                            fontWeight = FontWeight.Medium,
                            color = when {
                                isDanger -> DangerRed
                                isWarning -> OnWarningContainer
                                else -> SafeGreen
                            },
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Detected suspicious signals
                if (signalsList.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = WarningContainer),
                        border = CardDefaults.outlinedCardBorder().copy(
                            width = 1.dp,
                            brush = SolidColor(WarningBorder)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Dấu hiệu đáng ngờ phát hiện:",
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                                fontWeight = FontWeight.Bold,
                                color = OnWarningContainer
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            signalsList.forEach { signal ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("• ", color = WarningAmber, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = signal,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                        color = TextHighContrast
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Message Body
                Text(
                    text = "Nội dung tin nhắn:",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = TextSubtle
                )
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = SolidColor(LightOutline)
                    )
                ) {
                    Text(
                        text = entity.body,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp
                        ),
                        color = TextHighContrast,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (entity.resultJson.isNotBlank() || isDanger || isWarning) {
                        Button(
                            onClick = onViewFullResult,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDanger) DangerRed else OceanPrimary
                            )
                        ) {
                            Text(
                                text = "Xem phân tích chi tiết & hướng dẫn",
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (!entity.isDismissed && (isDanger || isWarning || entity.heuristicNeedsScrutiny || isRetrying)) {
                        OutlinedButton(
                            onClick = {
                                onDismissAlert()
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("dialog_button_dismiss_sms_${entity.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextMediumContrast
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = TextMediumContrast
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Đánh dấu đã xem / Bỏ qua cảnh báo",
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Đóng",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
