package com.elderly.tvassistant.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
//import com.elderly.tvassistant.MainActivity
//import com.elderly.tvassistant.R
import com.elderly.tvassistant.MainActivity
import kotlin.jvm.java

/**
 * 定时关闭广播接收器
 * 接收AlarmManager发出的定时广播
 * 发送通知提醒用户，并关闭主界面
 */
class TimerReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "TimerReceiver"
        private const val CHANNEL_ID = "timer_channel"
        private const val CHANNEL_NAME = "定时提醒"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "收到定时关闭广播")
        Log.d(TAG, "Intent action: ${intent.action}")
        Log.d(TAG, "Intent extras: ${intent.extras}")

        val minutes = intent.getIntExtra("TIMER_MINUTES", 0)
        val message = if (minutes > 0) {
            "播放时间已到（已播放${minutes}分钟）"
        } else {
            "播放时间已到"
        }

        Log.d(TAG, "定时消息: $message")

        // 发送通知
        sendNotification(context, message)

        // 关闭MainActivity
        val closeIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("TIMER_OFF", true)
        }
        Log.d(TAG, "启动关闭Activity")
        context.startActivity(closeIntent)
    }

    /**
     * 发送定时关闭通知
     */
    private fun sendNotification(context: Context, message: String) {
        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        // 创建通知渠道（Android 8.0+必需）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "定时关闭提醒通知"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 点击通知时打开APP
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0, contentIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("长辈电视助手")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}