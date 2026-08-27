package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OceanPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Logo AnTâm.AI",
                            tint = OnOceanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "AnTâm.AI",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        Text(
                            text = "Nhận diện & phòng chống lừa đảo",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextSubtle
                        )
                    }
                }

                // Settings Button
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(LightSurfaceVariant)
                        .testTag("button_open_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Cài đặt số người thân",
                        tint = OceanPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Xin chào bạn, hôm nay bạn cần kiểm tra tin nhắn hay hình ảnh nào?",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
                color = TextMediumContrast,
                fontWeight = FontWeight.Normal
            )

            // Family phone indicator if configured
            if (relativePhone.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SafeContainer)
                        .clickable { onOpenSettings() }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
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
                        text = "Số người thân đã lưu: $relativePhone",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        color = SafeGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // HERO BUTTON 1: CHỤP ẢNH MÀN HÌNH (OCEAN PRIMARY HERO CARD)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showImageSourceSheet = true }
                .testTag("button_take_screenshot"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = OceanPrimary),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Chụp ảnh màn hình",
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Kiểm tra ảnh chụp màn hình",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Biên lai, tin nhắn chat, hóa đơn, website",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION 2: DÁN TIN NHẮN CHỮ
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(LightOutline)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(OceanPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = OnOceanPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Dán tin nhắn chữ",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                fontWeight = FontWeight.Bold,
                                color = TextHighContrast
                            )
                            Text(
                                text = "Sao chép và dán vào ô bên dưới",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = TextSubtle
                            )
                        }
                    }

                    // Quick Paste button (Prominent and clearly sized)
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
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dán",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
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
                        .height(120.dp)
                        .testTag("input_message_text"),
                    placeholder = {
                        Text(
                            text = "Nhập hoặc dán nội dung tin nhắn, số điện thoại lạ, hoặc đường link cần kiểm tra...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = TextSubtle
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = TextHighContrast
                    ),
                    shape = RoundedCornerShape(14.dp),
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
                        modifier = Modifier.size(20.dp)
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

        // TIP & REASSURANCE BANNER
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OceanPrimaryContainer.copy(alpha = 0.5f)),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(OceanPrimary.copy(alpha = 0.2f))
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(OceanPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = OceanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Hệ thống sẽ kiểm tra kỹ mọi đường link và hình ảnh giúp bạn nhận diện dấu hiệu lừa đảo.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                    color = TextHighContrast,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // QUICK SAMPLE CARDS
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Mẫu tin nhắn thử nghiệm phổ biến:",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                color = TextMediumContrast,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

            SampleCard(
                title = "Phạt nguội giao thông giả",
                snippet = "Cục CSGT thông báo: Phương tiện của bạn có biên bản vi phạm giao thông chưa nộp phạt. Vui lòng truy cập csgt-phatnguoi-vn.com trong 24h...",
                onClick = {
                    messageText = "Cục CSGT thông báo: Phương tiện của bạn có biên bản vi phạm giao thông số 84920 chưa nộp phạt. Vui lòng truy cập trang web csgt-phatnguoi-vn.com để tra cứu và đóng phạt trong vòng 24 giờ nếu không tài khoản bằng lái sẽ bị tạm khóa."
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            SampleCard(
                title = "Giả mạo ngân hàng khóa thẻ",
                snippet = "Vietcombank: Tài khoản của quý khách đã bị khóa do phát hiện giao dịch bất thường. Xác minh tại http://vietconbank-online.com...",
                onClick = {
                    messageText = "Thông báo từ ngân hàng Vietcombank: Tài khoản của quý khách đã bị khóa do phát hiện giao dịch bất thường ở nước ngoài. Vui lòng nhấp vào đường link http://vietconbank-online.com/xac-minh để mở lại tài khoản trong 2 giờ."
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            SampleCard(
                title = "Trúng thưởng nhận quà tri ân",
                snippet = "Chúc mừng thuê bao trúng thưởng xe máy SH 150i. Vui lòng chuyển trước phí vận chuyển 1.500.000đ vào tài khoản...",
                onClick = {
                    messageText = "Chúc mừng số thuê bao 090xxx đã may mắn trúng giải Đặc Biệt gồm 01 xe máy SH 150i và phiếu mua sắm 50 triệu từ Tập đoàn Viễn thông. Để nhận thưởng, vui lòng chuyển trước phí vận chuyển 1.500.000đ vào tài khoản chỉ định."
                }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // FOOTER BRANDING
        HorizontalDivider(color = LightOutline, thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "CÔNG NGHỆ GEMINI VISION • AN TOÀN VIỆT NAM",
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
                        .height(52.dp)
                        .testTag("button_pick_gallery"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OceanPrimary,
                        contentColor = OnOceanPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Chọn ảnh chụp màn hình có sẵn",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
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
                        .height(52.dp)
                        .testTag("button_open_camera"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightSurfaceVariant,
                        contentColor = TextHighContrast
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = OceanPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Chụp ảnh màn hình trực tiếp",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
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
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(LightOutline)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                fontWeight = FontWeight.SemiBold,
                color = OceanPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = snippet,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                color = TextMediumContrast,
                maxLines = 2
            )
        }
    }
}
