package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.ui.components.AppTabHeader
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightOutline
import com.example.ui.theme.LightOutlineVariant
import com.example.ui.theme.LightSurface
import com.example.ui.theme.LightSurfaceVariant
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanPrimaryContainer
import com.example.ui.theme.OnOceanPrimary
import com.example.ui.theme.OnOceanPrimaryContainer
import com.example.ui.theme.SafeContainer
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMediumContrast
import com.example.ui.theme.TextSubtle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    relativePhone: String = "",
    onOpenSettings: () -> Unit = {},
    onAnalyzeText: (String) -> Unit,
    onAnalyzeImageUri: (Uri) -> Unit,
    onAnalyzeImageBitmap: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var messageText by remember { mutableStateOf("") }
    var showImageSourceSheet by remember { mutableStateOf(false) }
    var isExamplesExpanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onAnalyzeImageUri(uri)
        }
    }

    // Camera Capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            onAnalyzeImageBitmap(bitmap)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("screen_home"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header using unified component
        AppTabHeader(
            icon = Icons.Default.Shield,
            title = "Kiểm tra lừa đảo",
            subtitle = "Chụp ảnh màn hình hoặc dán tin nhắn",
            trailingAction = {
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LightSurfaceVariant)
                        .testTag("button_open_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Cài đặt ứng dụng",
                        tint = OceanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        // Family phone indicator if configured
        if (relativePhone.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SafeContainer)
                    .clickable { onOpenSettings() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = SafeGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Số người thân đã lưu: $relativePhone (Nhấn để sửa)",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    color = SafeGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ACTION CARD 1: CHỤP / CHỌN ẢNH MÀN HÌNH (Đồng nhất visual weight với Card 2)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_check_image"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.2.dp,
                brush = SolidColor(LightOutlineVariant)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OceanPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = OnOceanPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kiểm tra ảnh chụp màn hình",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        Text(
                            text = "Biên lai chuyển khoản, tin nhắn chat, hóa đơn, web",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = TextSubtle
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showImageSourceSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("button_take_screenshot"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OceanPrimary,
                        contentColor = OnOceanPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Chọn ảnh hoặc chụp màn hình",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ACTION CARD 2: DÁN TIN NHẮN CHỮ (Đồng nhất visual weight với Card 1)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_check_text"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.2.dp,
                brush = SolidColor(LightOutlineVariant)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(OceanPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = OnOceanPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dán tin nhắn chữ",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                fontWeight = FontWeight.Bold,
                                color = TextHighContrast
                            )
                            Text(
                                text = "Nội dung tin nhắn, số lạ hoặc đường link",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextSubtle
                            )
                        }
                    }

                    // Quick Paste button
                    OutlinedButton(
                        onClick = {
                            val clipText = clipboardManager.getText()?.text
                            if (!clipText.isNullOrBlank()) {
                                messageText = clipText
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("button_paste_clipboard")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Dán",
                            tint = OceanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Dán",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            color = OceanPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input box
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .testTag("input_message_text"),
                    placeholder = {
                        Text(
                            text = "Nhập hoặc dán nội dung tin nhắn, số điện thoại lạ, hoặc link cần kiểm tra...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = TextSubtle
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = TextHighContrast
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LightSurfaceVariant,
                        unfocusedContainerColor = LightSurfaceVariant,
                        focusedBorderColor = OceanPrimary,
                        unfocusedBorderColor = LightOutline
                    ),
                    trailingIcon = {
                        if (messageText.isNotEmpty()) {
                            IconButton(onClick = { messageText = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Xóa chữ",
                                    tint = TextSubtle,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        if (messageText.isNotBlank()) {
                            onAnalyzeText(messageText)
                        }
                    })
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Submit Button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        if (messageText.isNotBlank()) {
                            onAnalyzeText(messageText)
                        }
                    },
                    enabled = messageText.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("button_analyze_text"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OceanPrimary,
                        contentColor = OnOceanPrimary,
                        disabledContainerColor = LightSurfaceVariant,
                        disabledContentColor = TextSubtle
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kiểm tra tin nhắn này ngay",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // COLLAPSIBLE SAMPLE TEMPLATES (Thu gọn các mẫu thử nghiệm)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExamplesExpanded = !isExamplesExpanded }
                .testTag("card_toggle_examples"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = SolidColor(LightOutline)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(OceanPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = OceanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Xem ví dụ các tin nhắn lừa đảo phổ biến",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = TextHighContrast
                        )
                    }

                    Icon(
                        imageVector = if (isExamplesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExamplesExpanded) "Thu gọn" else "Mở rộng",
                        tint = OceanPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isExamplesExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Bấm vào một mẫu bên dưới để thử nghiệm kiểm tra:",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = TextSubtle
                        )

                        SampleCard(
                            title = "Phạt nguội giao thông giả",
                            snippet = "Cục CSGT thông báo: Phương tiện có biên bản vi phạm giao thông chưa nộp phạt. Nhấn link csgt-phatnguoi-vn.com trong 24h...",
                            onClick = {
                                messageText = "Cục CSGT thông báo: Phương tiện của bạn có biên bản vi phạm giao thông số 84920 chưa nộp phạt. Vui lòng truy cập trang web csgt-phatnguoi-vn.com để tra cứu và đóng phạt trong vòng 24 giờ nếu không tài khoản bằng lái sẽ bị tạm khóa."
                            }
                        )

                        SampleCard(
                            title = "Giả mạo ngân hàng khóa thẻ",
                            snippet = "Vietcombank: Tài khoản của quý khách đã bị khóa. Xác minh tại http://vietconbank-online.com trong 2 giờ...",
                            onClick = {
                                messageText = "Thông báo từ ngân hàng Vietcombank: Tài khoản của quý khách đã bị khóa do phát hiện giao dịch bất thường ở nước ngoài. Vui lòng nhấp vào đường link http://vietconbank-online.com/xac-minh để mở lại tài khoản trong 2 giờ."
                            }
                        )

                        SampleCard(
                            title = "Trúng thưởng tri ân yêu cầu cọc",
                            snippet = "Chúc mừng bạn trúng giải xe SH 150i. Vui lòng chuyển trước phí vận chuyển 1.500.000đ vào tài khoản...",
                            onClick = {
                                messageText = "Chúc mừng số thuê bao 090xxx đã may mắn trúng giải Đặc Biệt gồm 01 xe máy SH 150i và phiếu mua sắm 50 triệu từ Tập đoàn Viễn thông. Để nhận thưởng, vui lòng chuyển trước phí vận chuyển 1.500.000đ vào tài khoản chỉ định."
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // FOOTER BRANDING
        HorizontalDivider(color = LightOutline, thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "BẢO VỆ AN TOÀN TRỰC TUYẾN • CÔNG NGHỆ GEMINI",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            ),
            color = TextSubtle,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    // Modal Bottom Sheet for Image Selection (Camera vs Gallery)
    if (showImageSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImageSourceSheet = false },
            sheetState = sheetState,
            containerColor = LightSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Chọn cách gửi ảnh kiểm tra",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    fontWeight = FontWeight.Bold,
                    color = TextHighContrast
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Option 1: Pick from Gallery / Album
                Button(
                    onClick = {
                        showImageSourceSheet = false
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("button_pick_gallery"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OceanPrimary,
                        contentColor = OnOceanPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Chọn ảnh chụp màn hình có sẵn",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Take a photo
                Button(
                    onClick = {
                        showImageSourceSheet = false
                        cameraLauncher.launch(null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("button_open_camera"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightSurfaceVariant,
                        contentColor = TextHighContrast
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = OceanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Chụp ảnh màn hình trực tiếp",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SampleCard(
    title: String,
    snippet: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurfaceVariant),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = SolidColor(LightOutline)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                fontWeight = FontWeight.SemiBold,
                color = OceanPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = snippet,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = TextMediumContrast,
                maxLines = 2
            )
        }
    }
}
