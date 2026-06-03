package com.elderly.tvassistant.manager

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.elderly.tvassistant.receiver.TimerReceiver
import android.util.Log
import androidx.annotation.RequiresPermission

/**
 * 定时管理器
 * 使用AlarmManager实现定时关闭功能
 * 用户可设置15/30/60/90分钟后自动关闭APP
 */
class TimerManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private var pendingIntent: PendingIntent? = null
    private var timerMinutes: Int = 0

    companion object {
        private const val TAG = "TimerManager"
        private const val TIMER_REQUEST_CODE = 1001
    }

    /**
     * 设置定时关闭
     * @param minutes 定时分钟数
     */
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun setTimer(minutes: Int) {
        // 先取消已有定时
        cancelTimer()

        timerMinutes = minutes
        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
        val intent = Intent(context, TimerReceiver::class.java).apply {
            putExtra("TIMER_MINUTES", minutes)
        }

        pendingIntent = createPendingIntent(intent)

        try {
            pendingIntent?.let {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    it
                )
            }
            Log.d(TAG, "定时关闭已设置: $minutes 分钟后")
        } catch (e: SecurityException) {
            Log.e(TAG, "设置定时失败，可能需要权限: ${e.message}")
            // 降级使用set()方法
            pendingIntent?.let { alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, it) }
        }
    }

    /**
     * 创建PendingIntent（兼容不同Android版本）
     */
    private fun createPendingIntent(intent: Intent): PendingIntent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                context,
                TIMER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            @Suppress("DEPRECATION")
            PendingIntent.getBroadcast(
                context,
                TIMER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    /**
     * 取消定时
     */
    fun cancelTimer() {
        pendingIntent?.let {
            alarmManager.cancel(it)
            Log.d(TAG, "定时关闭已取消")
        }
        pendingIntent = null
        timerMinutes = 0
    }

    /**
     * 判断是否已设置定时
     */
    fun isTimerSet(): Boolean = pendingIntent != null

    /**
     * 获取当前定时的分钟数
     */
    fun getTimerMinutes(): Int = timerMinutes
}
