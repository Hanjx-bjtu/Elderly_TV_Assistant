package com.elderly.tvassistant.utils

import android.content.Context
import android.content.pm.PackageManager
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

    companion object {
        /** 录音权限 */
        const val PERMISSION_RECORD_AUDIO = android.Manifest.permission.RECORD_AUDIO

        /** 通知权限（Android 13+） */
        const val PERMISSION_POST_NOTIFICATIONS = android.Manifest.permission.POST_NOTIFICATIONS
    }
}
