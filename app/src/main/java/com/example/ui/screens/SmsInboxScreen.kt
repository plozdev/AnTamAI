package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.local.CheckHistoryEntity
import com.example.data.model.SmsMessage
import com.example.ui.components.AppTabHeader
import com.example.ui.theme.DangerBorder
import com.example.ui.theme.DangerContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightOutline
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
import java.util.Date
import java.util.Locale

@Composable
fun SmsInboxScreen(
    messages: List<SmsMessage>,
    isLoading: Boolean,
    errorMessage: String?,
    checkHistory: List<CheckHistoryEntity> = emptyList(),
    onRefresh: () -> Unit,
    onOpenHistoryItem: (CheckHistoryEntity) -> Unit = {},
    onDeleteHistoryItem: (Long) -> Unit = {},
    onClearAllHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedSubTabIndex by remember { mutableIntStateOf(0) }

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

    val suspiciousList = remember(filteredMessages) {
        filteredMessages.filter { it.heuristicResult.needsScrutiny }
    }

    val normalList = remember(filteredMessages) {
        filteredMessages.filter { !it.heuristicResult.needsScrutiny }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
            .testTag("screen_sms_inbox")
    ) {
        // ==========================================
        // UNIFIED HEADER BAR
        // ==========================================
        AppTabHeader(
            icon = Icons.Default.History,
            title = "Nhật ký an toàn",
            subtitle = "Lịch sử quét SMS & kiểm tra thủ công",
            trailingAction = {
                if (selectedSubTabIndex == 0) {
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
            }
        )

        // ==========================================
        // SEGMENTED CONTROL: "SMS" | "Đã kiểm tra tay"
        // ==========================================
        TabRow(
            selectedTabIndex = selectedSubTabIndex,
            containerColor = LightSurfaceVariant,
            contentColor = OceanPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedSubTabIndex]),
                    height = 3.dp,
                    color = OceanPrimary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedSubTabIndex == 0,
                onClick = { selectedSubTabIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedSubTabIndex == 0) OceanPrimary else TextSubtle
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SMS (${messages.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                            fontWeight = if (selectedSubTabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedSubTabIndex == 0) OceanPrimary else TextMediumContrast
                        )
                    }
                },
                modifier = Modifier.testTag("tab_sub_sms")
            )
            Tab(
                selected = selectedSubTabIndex == 1,
                onClick = { selectedSubTabIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedSubTabIndex == 1) OceanPrimary else TextSubtle
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Đã kiểm tra tay (${checkHistory.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                            fontWeight = if (selectedSubTabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedSubTabIndex == 1) OceanPrimary else TextMediumContrast
                        )
                    }
                },
                modifier = Modifier.testTag("tab_sub_manual_history")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (selectedSubTabIndex == 0) {
            // ==========================================
            // SUB-TAB 1: SMS LIST
            // ==========================================
            if (!hasPermission) {
                // PERMISSION NOT GRANTED VIEW
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Cần quyền đọc tin nhắn SMS",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ứng dụng cần quyền đọc tin nhắn SMS để tự động kiểm tra nhanh các dấu hiệu nghi ngờ (như link lạ, mạo danh ngân hàng/công an) ngay trên thiết bị của bạn.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp
                        ),
                        color = TextMediumContrast,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showPermissionRationaleDialog = true },
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
                            text = "Cấp quyền đọc SMS",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
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
                if (suspiciousCount > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { filterOnlySuspicious = !filterOnlySuspicious }
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
                                modifier = Modifier.weight(1f)
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
                                        text = "⚠️ $suspiciousCount tin nhắn cần chú ý",
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
                            Icon(
                                imageVector = if (filterOnlySuspicious) Icons.Default.Close else Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = OnWarningContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // SMS LIST
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = OceanPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Đang quét tin nhắn SMS...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                                color = TextSubtle
                            )
                        }
                    }
                } else if (!errorMessage.isNullOrBlank()) {
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
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(onClick = onRefresh) {
                                Text("Thử lại")
                            }
                        }
                    }
                } else if (filteredMessages.isEmpty()) {
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
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                        // Section 1: Cần chú ý
                        if (suspiciousList.isNotEmpty()) {
                            item(key = "header_suspicious") {
                                Text(
                                    text = "CẦN CHÚ Ý (${suspiciousList.size})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = DangerRed,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }

                            items(suspiciousList, key = { "suspicious_${it.id}" }) { sms ->
                                SmsItemCard(
                                    sms = sms,
                                    onClick = { selectedSmsForDetail = sms }
                                )
                            }
                        }

                        // Section 2: Bình thường (if not filtered)
                        if (!filterOnlySuspicious && normalList.isNotEmpty()) {
                            item(key = "header_normal") {
                                Text(
                                    text = "BÌNH THƯỜNG (${normalList.size})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = OceanPrimary,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                )
                            }

                            items(normalList, key = { "normal_${it.id}" }) { sms ->
                                SmsItemCard(
                                    sms = sms,
                                    onClick = { selectedSmsForDetail = sms }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // SUB-TAB 2: ĐÃ KIỂM TRA TAY (MANUAL CHECK HISTORY)
            // ==========================================
            if (checkHistory.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
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
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = OceanPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Chưa có lịch sử kiểm tra",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Mỗi khi bạn dán tin nhắn hoặc chụp ảnh kiểm tra lừa đảo tại tab 'Kiểm tra', kết quả sẽ được lưu an toàn tại đây để bạn xem lại bất cứ lúc nào.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        ),
                        color = TextSubtle,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("list_manual_check_history"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LỊCH SỬ KIỂM TRA GẦN ĐÂY (${checkHistory.size})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = TextSubtle
                            )

                            TextButton(
                                onClick = onClearAllHistory,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = TextSubtle,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Xóa tất cả",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                    color = TextSubtle
                                )
                            }
                        }
                    }

                    items(checkHistory, key = { it.id }) { item ->
                        HistoryItemCard(
                            item = item,
                            onClick = { onOpenHistoryItem(item) },
                            onDelete = { onDeleteHistoryItem(item.id) }
                        )
                    }
                }
            }
        }
    }

    // DETAIL MODAL FOR SELECTED SMS
    if (selectedSmsForDetail != null) {
        val sms = selectedSmsForDetail!!
        SmsDetailDialog(
            sms = sms,
            onDismiss = { selectedSmsForDetail = null }
        )
    }

    // PERMISSION RATIONALE DIALOG
    if (showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionRationaleDialog = false },
            title = {
                Text(
                    text = "Quyền truy cập SMS an toàn",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "AnTâm.AI cam kết chỉ quét cục bộ trên thiết bị của bạn nhằm cảnh báo sớm các dấu hiệu đáng ngờ. Dữ liệu tin nhắn hoàn toàn không bị gửi ra ngoài nếu bạn không yêu cầu kiểm tra chuyên sâu.",
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
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
                    Text("Đồng ý & Tiếp tục")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationaleDialog = false }) {
                    Text("Để sau")
                }
            }
        )
    }
}

@Composable
private fun HistoryItemCard(
    item: CheckHistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val statusUpper = item.status.uppercase()
    val (statusLabel, statusColor, statusBgColor, statusBorderColor) = when {
        statusUpper.contains("DANGER") -> Quad(
            "LỪA ĐẢO",
            DangerRed,
            DangerContainer,
            DangerBorder
        )
        statusUpper.contains("WARNING") -> Quad(
            "CẦN THẬN TRỌNG",
            WarningAmber,
            WarningContainer,
            WarningBorder
        )
        else -> Quad(
            "AN TOÀN",
            SafeGreen,
            SafeContainer,
            SafeBorder
        )
    }

    val formattedDate = remember(item.timestamp) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(item.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("history_item_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = SolidColor(LightOutline)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (item.contentType == "IMAGE") OceanPrimaryContainer else LightSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.contentType == "IMAGE") Icons.Default.Image else Icons.Default.TextFields,
                    contentDescription = null,
                    tint = if (item.contentType == "IMAGE") OceanPrimary else TextMediumContrast,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusBgColor)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = statusColor
                        )
                    }

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSubtle
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.contentPreview,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = TextHighContrast,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Xóa mục này",
                    tint = TextSubtle,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SmsItemCard(
    sms: SmsMessage,
    onClick: () -> Unit
) {
    val isSuspicious = sms.heuristicResult.needsScrutiny
    val formattedDate = remember(sms.date) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(sms.date))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("sms_card_${sms.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSuspicious) WarningContainer.copy(alpha = 0.35f) else LightSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = SolidColor(if (isSuspicious) WarningBorder else LightOutline)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
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
                            .background(if (isSuspicious) WarningAmber.copy(alpha = 0.2f) else OceanPrimaryContainer),
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
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = TextHighContrast,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isSuspicious) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(WarningAmber.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⚠️ Cần chú ý",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = WarningAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = sms.body,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = TextMediumContrast,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

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
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSubtle
                    )
                }

                Text(
                    text = "Xem chi tiết →",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = OceanPrimary
                )
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
    val formattedDate = remember(sms.date) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(sms.date))
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
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSuspicious) WarningContainer else OceanPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSuspicious) Icons.Default.Warning else Icons.Default.Message,
                                contentDescription = null,
                                tint = if (isSuspicious) WarningAmber else OceanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = sms.address,
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

                // Heuristic signals if suspicious
                if (isSuspicious && sms.heuristicResult.matchedSignals.isNotEmpty()) {
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
                                text = "Dấu hiệu nghi ngờ phát hiện trên máy:",
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                                fontWeight = FontWeight.Bold,
                                color = OnWarningContainer
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            sms.heuristicResult.matchedSignals.forEach { signal ->
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
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Full SMS Body
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
                        text = sms.body,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp
                        ),
                        color = TextHighContrast,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary)
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

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
