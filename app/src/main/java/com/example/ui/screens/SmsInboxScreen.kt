package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.model.SmsMessage
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightOutline
import com.example.ui.theme.LightOutlineVariant
import com.example.ui.theme.LightSurface
import com.example.ui.theme.LightSurfaceVariant
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanPrimaryContainer
import com.example.ui.theme.OnOceanPrimary
import com.example.ui.theme.SafeContainer
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMediumContrast
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningBorder
import com.example.ui.theme.WarningContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SmsInboxScreen(
    messages: List<SmsMessage>,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showPermissionRationaleDialog by remember { mutableStateOf(false) }
    var selectedSmsForDetail by remember { mutableStateOf<SmsMessage?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var filterOnlySuspicious by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            onRefresh()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            onRefresh()
        }
    }

    val filteredMessages = remember(messages, searchQuery, filterOnlySuspicious) {
        messages.filter { msg ->
            val matchSearch = searchQuery.isBlank() ||
                msg.address.contains(searchQuery, ignoreCase = true) ||
                msg.body.contains(searchQuery, ignoreCase = true)
            val matchFilter = !filterOnlySuspicious || msg.heuristicResult.needsScrutiny
            matchSearch && matchFilter
        }
    }

    val suspiciousCount = remember(messages) {
        messages.count { it.heuristicResult.needsScrutiny }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
            .testTag("screen_sms_inbox")
    ) {
        // ==========================================
        // HEADER BAR
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OceanPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = null,
                        tint = OceanPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Hộp thư SMS",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast
                    )
                    Text(
                        text = "QUÉT SƠ BỘ TRÊN THIẾT BỊ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = OceanPrimary
                    )
                }
            }

            IconButton(
                onClick = {
                    if (hasPermission) {
                        onRefresh()
                    } else {
                        showPermissionRationaleDialog = true
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

        // ==========================================
        // PERMISSION NOT GRANTED VIEW
        // ==========================================
        if (!hasPermission) {
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
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(OceanPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = null,
                        tint = OceanPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Cho phép đọc tin nhắn SMS",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    fontWeight = FontWeight.Bold,
                    color = TextHighContrast
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = SolidColor(LightOutline)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = OceanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Để giúp bạn và người thân sớm phát hiện các tin nhắn có dấu hiệu bất thường (đòi nợ lạ, trúng thưởng giả, link độc), ứng dụng cần được cấp quyền đọc SMS.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp, lineHeight = 19.sp),
                                color = TextHighContrast
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = SafeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Bảo mật tuyệt đối: Quá trình lọc từ khóa diễn ra hoàn toàn trên điện thoại của bạn, không tải nội dung tin nhắn lên mạng.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
                                color = TextMediumContrast
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.READ_SMS)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("button_request_sms_permission"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OceanPrimary,
                        contentColor = OnOceanPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cấp quyền đọc tin nhắn SMS",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.5.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // ==========================================
            // PERMISSION GRANTED: SHOW LIST & CONTROLS
            // ==========================================

            // SUMMARY CARDS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card Total
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = SolidColor(LightOutline))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(LightSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${messages.size}",
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tổng tin nhắn",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = TextMediumContrast,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Card Suspicious
                Card(
                    modifier = Modifier
                        .weight(1.2f)
                        .clickable { filterOnlySuspicious = !filterOnlySuspicious },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (filterOnlySuspicious) WarningContainer.copy(alpha = 0.5f) else LightSurface
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = SolidColor(if (suspiciousCount > 0) WarningBorder else LightOutline)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (suspiciousCount > 0) WarningContainer else SafeContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$suspiciousCount",
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (suspiciousCount > 0) WarningAmber else SafeGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Cần kiểm tra",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = if (suspiciousCount > 0) WarningAmber else TextMediumContrast,
                                fontWeight = FontWeight.Bold
                            )
                            if (filterOnlySuspicious) {
                                Text(
                                    text = "Đang lọc",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                    color = WarningAmber
                                )
                            }
                        }
                    }
                }
            }

            // SEARCH BAR & FILTER CHIPS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("input_search_sms"),
                    placeholder = {
                        Text(
                            text = "Tìm người gửi hoặc từ khóa...",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = TextSubtle
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSubtle,
                            modifier = Modifier.size(18.dp)
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
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !filterOnlySuspicious,
                        onClick = { filterOnlySuspicious = false },
                        label = { Text("Tất cả (${messages.size})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OceanPrimaryContainer,
                            selectedLabelColor = OceanPrimary
                        )
                    )
                    FilterChip(
                        selected = filterOnlySuspicious,
                        onClick = { filterOnlySuspicious = true },
                        label = { Text("Cần kiểm tra kỹ ($suspiciousCount)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WarningContainer,
                            selectedLabelColor = WarningAmber
                        )
                    )
                }
            }

            // LIST CONTENT
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (isLoading && messages.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = OceanPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Đang đọc danh sách tin nhắn...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = TextMediumContrast
                        )
                    }
                } else if (filteredMessages.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = null,
                            tint = TextSubtle,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotBlank() || filterOnlySuspicious)
                                "Không tìm thấy tin nhắn phù hợp với bộ lọc"
                            else
                                "Hộp thư đến trống",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = TextMediumContrast,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = onRefresh,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = OceanPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tải lại danh sách", color = OceanPrimary, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = filteredMessages,
                            key = { it.id }
                        ) { sms ->
                            SmsItemCard(
                                sms = sms,
                                onClick = { selectedSmsForDetail = sms }
                            )
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // SMS DETAIL DIALOG (Phase A: Display only, no Gemini call)
    // ==========================================
    selectedSmsForDetail?.let { sms ->
        SmsDetailDialog(
            sms = sms,
            onDismiss = { selectedSmsForDetail = null }
        )
    }

    // ==========================================
    // PERMISSION RATIONALE POPUP
    // ==========================================
    if (showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionRationaleDialog = false },
            title = {
                Text(
                    text = "Quyền đọc tin nhắn SMS",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    fontWeight = FontWeight.Bold,
                    color = TextHighContrast
                )
            },
            text = {
                Text(
                    text = "Ứng dụng cần quyền đọc tin nhắn để quét và lọc sơ bộ các tin nhắn có chứa từ khóa lừa đảo hoặc link lạ. Ứng dụng chỉ kiểm tra cục bộ trên máy và không gửi tin nhắn ra ngoài.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp, lineHeight = 19.sp),
                    color = TextMediumContrast
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionRationaleDialog = false
                        permissionLauncher.launch(Manifest.permission.READ_SMS)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary)
                ) {
                    Text("Đồng ý cấp quyền")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationaleDialog = false }) {
                    Text("Để sau", color = TextMediumContrast)
                }
            }
        )
    }
}

@Composable
private fun SmsItemCard(
    sms: SmsMessage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSuspicious = sms.heuristicResult.needsScrutiny
    val formattedDate = remember(sms.date) {
        try {
            val sdf = SimpleDateFormat("HH:mm • dd/MM", Locale.getDefault())
            sdf.format(Date(sms.date))
        } catch (_: Exception) {
            ""
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("card_sms_item_${sms.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = SolidColor(if (isSuspicious) WarningBorder else LightOutlineVariant)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Sender + Time + Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isSuspicious) WarningContainer else OceanPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSuspicious) Icons.Default.Warning else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isSuspicious) WarningAmber else OceanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = sms.address,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Badge trạng thái Heuristic
                if (isSuspicious) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarningContainer)
                            .border(1.dp, WarningBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Cần kiểm tra kỹ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = WarningAmber
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(LightSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Bình thường",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextSubtle
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body preview (1-2 lines)
            Text(
                text = sms.body.ifBlank { "(Tin nhắn không có nội dung văn bản)" },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = TextHighContrast,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Timestamp & Match hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = TextSubtle,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = TextSubtle
                    )
                }

                if (isSuspicious && sms.heuristicResult.matchedSignals.isNotEmpty()) {
                    Text(
                        text = "• ${sms.heuristicResult.matchedSignals.first()}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = WarningAmber,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SmsDetailDialog(
    sms: SmsMessage,
    onDismiss: () -> Unit
) {
    val isSuspicious = sms.heuristicResult.needsScrutiny
    val formattedDateTime = remember(sms.date) {
        try {
            val sdf = SimpleDateFormat("HH:mm:ss • dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(sms.date))
        } catch (_: Exception) {
            ""
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = LightSurface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSuspicious) WarningContainer else OceanPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSuspicious) Icons.Default.Warning else Icons.Default.Message,
                                contentDescription = null,
                                tint = if (isSuspicious) WarningAmber else OceanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Chi tiết tin nhắn",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                fontWeight = FontWeight.Bold,
                                color = TextHighContrast
                            )
                            Text(
                                text = sms.address,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                color = OceanPrimary,
                                fontWeight = FontWeight.SemiBold
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

                // Time info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = TextSubtle,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Thời gian gửi: $formattedDateTime",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = TextMediumContrast
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Full Message Content Box
                Text(
                    text = "Nội dung tin nhắn:",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    fontWeight = FontWeight.Bold,
                    color = TextMediumContrast
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightSurfaceVariant)
                        .border(1.dp, LightOutline, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = sms.body.ifBlank { "(Tin nhắn rỗng)" },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        color = TextHighContrast
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Heuristic analysis section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSuspicious) WarningContainer.copy(alpha = 0.4f) else SafeContainer.copy(alpha = 0.4f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = SolidColor(if (isSuspicious) WarningBorder else LightOutline)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSuspicious) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isSuspicious) WarningAmber else SafeGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSuspicious) "Dấu hiệu phát hiện sơ bộ:" else "Đánh giá sơ bộ:",
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (isSuspicious) WarningAmber else SafeGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (isSuspicious) {
                            sms.heuristicResult.matchedSignals.forEach { signal ->
                                Text(
                                    text = "• $signal",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 17.sp),
                                    color = TextHighContrast,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Không tìm thấy từ khóa đáng ngờ trong bộ lọc sơ bộ.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                color = TextMediumContrast
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "(*) Lưu ý: Đây là đánh giá từ bộ lọc từ khóa cục bộ trên máy, chưa qua phân tích chuyên sâu của AI.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                lineHeight = 15.sp
                            ),
                            color = TextSubtle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OceanPrimary,
                        contentColor = OnOceanPrimary
                    )
                ) {
                    Text(
                        text = "Đóng",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
