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
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.LavenderPrimaryContainer
import com.example.ui.theme.LavenderSecondaryContainer
import com.example.ui.theme.OnLavenderContainer
import com.example.ui.theme.OnLavenderPrimary
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
    val context = LocalContext.current
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
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header with Settings Shortcut
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp)
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
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LavenderPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Logo AnTâm.AI",
                            tint = OnLavenderPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = "AnTâm.AI",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Settings Button (touch target > 48dp)
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .testTag("button_open_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Cài đặt số người thân",
                        tint = LavenderPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Text(
                text = "Con chào bác, hôm nay bác cần con kiểm tra tin nhắn nào ạ?",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 26.sp),
                color = TextMediumContrast,
                fontWeight = FontWeight.Medium
            )

            // Family phone indicator if configured
            if (relativePhone.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SafeContainer.copy(alpha = 0.5f))
                        .clickable { onOpenSettings() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
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
                        text = "Đã lưu số người thân: $relativePhone",
                        style = MaterialTheme.typography.labelMedium,
                        color = SafeGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // HERO BUTTON 1: CHỤP ẢNH MÀN HÌNH (LAVENDER HERO CARD)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showImageSourceSheet = true }
                .testTag("button_take_screenshot"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = LavenderPrimary),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(OnLavenderPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Chụp ảnh màn hình",
                        modifier = Modifier.size(36.dp),
                        tint = LavenderPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Chụp ảnh màn hình",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 23.sp),
                    fontWeight = FontWeight.Bold,
                    color = OnLavenderPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "MỞ CAMERA HOẶC THƯ VIỆN",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = LavenderPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: DÁN TIN NHẮN CHỮ (PROFESSIONAL POLISH CARD WITH 2REM RADIUS)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = LavenderSecondaryContainer),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.5.dp,
                brush = androidx.compose.ui.graphics.SolidColor(DarkOutlineVariant.copy(alpha = 0.6f))
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(OnLavenderContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = OnLavenderPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dán tin nhắn chữ",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                fontWeight = FontWeight.Bold,
                                color = OnLavenderContainer
                            )
                            Text(
                                text = "SAO CHÉP VÀ DÁN VÀO ĐÂY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = TextMediumContrast
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("button_paste_clipboard")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Dán",
                            tint = LavenderPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dán",
                            style = MaterialTheme.typography.labelLarge,
                            color = LavenderPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input box
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("input_message_text"),
                    placeholder = {
                        Text(
                            text = "Nhập hoặc dán nội dung tin nhắn, số điện thoại lạ, hoặc đường link cần kiểm tra vào đây...",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                            color = TextSubtle
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 17.sp,
                        color = TextHighContrast
                    ),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = DarkOutline
                    ),
                    trailingIcon = {
                        if (messageText.isNotEmpty()) {
                            IconButton(onClick = { messageText = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Xóa chữ",
                                    tint = TextMediumContrast
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

                Spacer(modifier = Modifier.height(16.dp))

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
                        .height(56.dp)
                        .testTag("button_analyze_text"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LavenderPrimary,
                        contentColor = OnLavenderPrimary,
                        disabledContainerColor = DarkSurface,
                        disabledContentColor = TextSubtle
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Kiểm tra tin nhắn này ngay",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // TIP & REASSURANCE BANNER
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = LavenderPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Bác đừng lo, con sẽ kiểm tra kỹ mọi đường link và hình ảnh giúp bác nhé.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
                    color = TextHighContrast,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // QUICK SAMPLE CARDS
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Mẫu tin nhắn thử nghiệm phổ biến:",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                color = TextMediumContrast,
                modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
            )

            SampleCard(
                title = "Phạt nguội giao thông giả",
                snippet = "Cục CSGT thông báo: Phương tiện của bạn có biên bản vi phạm giao thông chưa nộp phạt. Vui lòng truy cập csgt-phatnguoi-vn.com trong 24h...",
                onClick = {
                    messageText = "Cục CSGT thông báo: Phương tiện của bạn có biên bản vi phạm giao thông số 84920 chưa nộp phạt. Vui lòng truy cập trang web csgt-phatnguoi-vn.com để tra cứu và đóng phạt trong vòng 24 giờ nếu không tài khoản bằng lái sẽ bị tạm khóa."
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SampleCard(
                title = "Giả mạo ngân hàng khóa thẻ",
                snippet = "Vietcombank: Tài khoản của quý khách đã bị khóa do phát hiện giao dịch bất thường. Xác minh tại http://vietconbank-online.com...",
                onClick = {
                    messageText = "Thông báo từ ngân hàng Vietcombank: Tài khoản của quý khách đã bị khóa do phát hiện giao dịch bất thường ở nước ngoài. Vui lòng nhấp vào đường link http://vietconbank-online.com/xac-minh để mở lại tài khoản trong 2 giờ."
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SampleCard(
                title = "Trúng thưởng nhận quà tri ân",
                snippet = "Chúc mừng thuê bao trúng thưởng xe máy SH 150i. Vui lòng chuyển trước phí vận chuyển 1.500.000đ vào tài khoản...",
                onClick = {
                    messageText = "Chúc mừng số thuê bao 090xxx đã may mắn trúng giải Đặc Biệt gồm 01 xe máy SH 150i và phiếu mua sắm 50 triệu từ Tập đoàn Viễn thông. Để nhận thưởng, vui lòng chuyển trước phí vận chuyển 1.500.000đ vào tài khoản chỉ định."
                }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // FOOTER BRANDING
        HorizontalDivider(color = DarkOutline.copy(alpha = 0.5f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "CÔNG NGHỆ GEMINI VISION • VIỆT NAM",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            ),
            color = TextSubtle.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    // Modal Bottom Sheet for Image Selection (Camera vs Gallery)
    if (showImageSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImageSourceSheet = false },
            sheetState = sheetState,
            containerColor = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Chọn cách gửi ảnh kiểm tra",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextHighContrast
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Option 1: Pick from Gallery / Album
                Button(
                    onClick = {
                        showImageSourceSheet = false
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("button_pick_gallery"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LavenderPrimary,
                        contentColor = OnLavenderPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Chọn ảnh chụp màn hình có sẵn",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Option 2: Take a photo
                Button(
                    onClick = {
                        showImageSourceSheet = false
                        cameraLauncher.launch(null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("button_open_camera"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceVariant,
                        contentColor = TextHighContrast
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Chụp ảnh màn hình trực tiếp",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(DarkOutline.copy(alpha = 0.7f))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.SemiBold,
                color = LavenderPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = snippet,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = TextMediumContrast,
                maxLines = 2
            )
        }
    }
}
