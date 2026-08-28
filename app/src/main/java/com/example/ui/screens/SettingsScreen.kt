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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import com.example.ui.components.AppTabHeader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DangerBorder
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightOutline
import com.example.ui.theme.LightOutlineVariant
import com.example.ui.theme.LightSurface
import com.example.ui.theme.LightSurfaceVariant
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanPrimaryContainer
import com.example.ui.theme.OnOceanPrimary
import com.example.ui.theme.SafeBorder
import com.example.ui.theme.SafeContainer
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextMediumContrast
import com.example.ui.theme.TextSubtle

@Composable
fun SettingsScreen(
    currentPhone: String,
    autoReadResult: Boolean = false,
    onSavePhone: (String) -> Unit,
    onClearPhone: () -> Unit,
    onToggleAutoRead: (Boolean) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var phoneNumberInput by remember(currentPhone) { mutableStateOf(currentPhone) }
    var saveSuccessMessage by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("screen_settings"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button Row
        // Row(
        //     modifier = Modifier
        //         .fillMaxWidth()
        //         .padding(bottom = 10.dp),
        //     verticalAlignment = Alignment.CenterVertically
        // ) {
        //     OutlinedButton(
        //         onClick = onBack,
        //         shape = RoundedCornerShape(12.dp),
        //         modifier = Modifier.testTag("button_settings_back")
        //     ) {
        //         Icon(
        //             imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        //             contentDescription = "Quay lại",
        //             tint = OceanPrimary,
        //             modifier = Modifier.size(18.dp)
        //         )
        //         Spacer(modifier = Modifier.width(6.dp))
        //         Text(
        //             text = "Quay lại màn hình chính",
        //             style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
        //             color = OceanPrimary
        //         )
        //     }
        // }

        // Title Header
        AppTabHeader(
            icon = Icons.Default.Person,
            title = "Cài đặt ứng dụng",
            subtitle = "Số người thân & tùy chọn hỗ trợ"
        )

        // Auto Read Voice Option Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(LightOutlineVariant)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(OceanPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = OceanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Tự động đọc to kết quả",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold,
                            color = TextHighContrast
                        )
                        Text(
                            text = "Tự động phát giọng đọc giải thích khi có kết quả phân tích",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                            color = TextMediumContrast
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = autoReadResult,
                    onCheckedChange = onToggleAutoRead,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OnOceanPrimary,
                        checkedTrackColor = OceanPrimary,
                        uncheckedThumbColor = TextSubtle,
                        uncheckedTrackColor = LightSurfaceVariant
                    ),
                    modifier = Modifier.testTag("switch_auto_read")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Explanation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(LightOutline)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = OceanPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Lưu số điện thoại của người thân tin cậy để khi phát hiện tin nhắn lạ hoặc có dấu hiệu bất thường, ứng dụng sẽ hiện nút gọi ngay để bạn xác minh nhanh chóng.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
                    color = TextMediumContrast
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Input Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(LightOutlineVariant)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Số điện thoại người thân:",
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = TextHighContrast
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phoneNumberInput,
                    onValueChange = {
                        phoneNumberInput = it
                        saveSuccessMessage = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_relative_phone"),
                    placeholder = {
                        Text(
                            text = "Ví dụ: 0912345678",
                            color = TextSubtle,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = OceanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (phoneNumberInput.isNotEmpty()) {
                            IconButton(onClick = { phoneNumberInput = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Xóa số",
                                    tint = TextSubtle,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (phoneNumberInput.isNotBlank()) {
                                onSavePhone(phoneNumberInput)
                                saveSuccessMessage = true
                                Toast.makeText(context, "Đã lưu số điện thoại thành công!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LightSurfaceVariant,
                        unfocusedContainerColor = LightSurfaceVariant,
                        focusedBorderColor = OceanPrimary,
                        unfocusedBorderColor = LightOutline
                    ),
                    textStyle = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextHighContrast
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Save Button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        if (phoneNumberInput.isNotBlank()) {
                            onSavePhone(phoneNumberInput)
                            saveSuccessMessage = true
                            Toast.makeText(context, "Đã lưu số điện thoại thành công!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Vui lòng nhập số điện thoại hợp lệ", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("button_save_relative_phone"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OceanPrimary,
                        contentColor = OnOceanPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lưu số điện thoại",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Success Message Banner
                if (saveSuccessMessage) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SafeContainer),
                        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(SafeBorder))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SafeGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Đã lưu số điện thoại thành công!",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                fontWeight = FontWeight.Bold,
                                color = SafeGreen
                            )
                        }
                    }
                }
            }
        }

        // Secondary Actions if phone is already set
        if (currentPhone.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Test Call Button
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$currentPhone")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Không thể mở ứng dụng cuộc gọi", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("button_test_call_relative"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightSurfaceVariant,
                        contentColor = TextHighContrast
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = OceanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gọi thử số người thân ($currentPhone)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Delete Phone Button
                OutlinedButton(
                    onClick = {
                        onClearPhone()
                        phoneNumberInput = ""
                        saveSuccessMessage = false
                        Toast.makeText(context, "Đã xóa số điện thoại người thân", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("button_clear_relative_phone"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(DangerBorder)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = DangerRed
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Xóa số người thân đã lưu",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
