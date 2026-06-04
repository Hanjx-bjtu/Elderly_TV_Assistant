package com.elderly.tvassistant.utils

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * 权限请求工具类
 * 封装运行时权限检查和请求逻辑
 */
class PermissionHelper(private val context: Context) {

    /**
     * 检查是否已授予指定权限
     * @param permission 权限名称
     * @return true表示已授权
     */
    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    /**
     * 检查是否已授予多个权限
     * @param permissions 权限名称列表
     * @return true表示全部已授权
     */
    fun hasAllPermissions(permissions: List<String>): Boolean {
        return permissions.all { hasPermission(it) }
    }

    /**
     * 检查是否可以设置精确闹钟（Android 12+）
     * @return true表示可以设置精确闹钟
     */
    fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmManager.canScheduleExactAlarms()
        }
        return true
    }

    /**
     * 获取精确闹钟权限设置页面Intent
     * @return Intent用于跳转到设置页面
     */
    fun getExactAlarmSettingsIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }
    }

    companion object {
        /** 录音权限 */
        const val PERMISSION_RECORD_AUDIO = android.Manifest.permission.RECORD_AUDIO

        /** 通知权限（Android 13+） */
        const val PERMISSION_POST_NOTIFICATIONS = android.Manifest.permission.POST_NOTIFICATIONS

        /** 精确闹钟权限（Android 12+） */
        const val PERMISSION_SCHEDULE_EXACT_ALARM = android.Manifest.permission.SCHEDULE_EXACT_ALARM
    }
}