package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    private const val CHANNEL_ID = "antam_scam_alerts_channel"
    private const val CHANNEL_NAME = "Cảnh báo tin nhắn lừa đảo"
    private const val CHANNEL_DESC = "Cảnh báo khẩn cấp khi phát hiện tin nhắn có dấu hiệu lừa đảo hoặc link độc hại"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showScamAlertNotification(
        context: Context,
        smsRecordId: Long,
        address: String,
        body: String,
        status: String,
        openingMessage: String,
        resultJson: String
    ) {
        createNotificationChannel(context)

        val isDanger = status.uppercase().contains("DANGER")
        val title = if (isDanger) {
            "🚨 Cảnh báo lừa đảo từ: $address"
        } else {
            "⚠️ Chú ý: Tin nhắn đáng ngờ từ $address"
        }

        val displayMessage = if (openingMessage.isNotBlank()) {
            openingMessage
        } else {
            "Phát hiện dấu hiệu bất thường trong tin nhắn. Bạn chạm vào đây để xem chi tiết nhé!"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_OPEN_SMS_ID, smsRecordId)
            putExtra(MainActivity.EXTRA_SENDER, address)
            putExtra(MainActivity.EXTRA_ORIGINAL_TEXT, body)
            putExtra(MainActivity.EXTRA_RESULT_JSON, resultJson)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            smsRecordId.toInt().takeIf { it != 0 } ?: (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val publicNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(if (isDanger) "🚨 Cảnh báo an toàn SMS" else "⚠️ Lưu ý tin nhắn SMS")
            .setContentText("Phát hiện nội dung đáng ngờ. Mở khóa thiết bị để xem hướng dẫn an toàn.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(displayMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$displayMessage\n\nTin nhắn từ: $address\nNội dung: \"$body\""))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (smsRecordId.takeIf { it > 0 } ?: System.currentTimeMillis()).toInt()
        notificationManager.notify(notificationId, notification)
    }
}
