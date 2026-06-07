package com.elderly.tvassistant.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences工具类
 * 管理应用的本地偏好设置
 * 包括首次启动标记、字体大小、开关设置等
 */
class SharedPrefsHelper(context: Context) {

    companion object {
        private const val PREF_NAME = "elderly_tv_prefs"

        // 首次启动
        private const val KEY_FIRST_LAUNCH = "first_launch"

        // 字体大小
        private const val KEY_FONT_SIZE = "font_size"
        const val FONT_SIZE_NORMAL = 1
        const val FONT_SIZE_LARGE = 2
        const val FONT_SIZE_EXTRA_LARGE = 3

        // 语音播报
        private const val KEY_TTS_ENABLED = "tts_enabled"

        // 震动反馈
        private const val KEY_VIBRATE_ENABLED = "vibrate_enabled"

        // 默认频道
        private const val KEY_DEFAULT_CHANNEL = "default_channel"

        // 自动播放
        private const val KEY_AUTO_PLAY = "auto_play"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // ==================== 首次启动 ====================

    /** 判断是否首次启动 */
    fun isFirstLaunch(): Boolean = prefs.getBoolean(KEY_FIRST_LAUNCH, true)

    /** 标记首次启动已完成 */
    fun setFirstLaunchDone() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    // ==================== 字体大小 ====================

    /** 获取字体大小级别 */
    val fontSize: Int
        get() = prefs.getInt(KEY_FONT_SIZE, FONT_SIZE_EXTRA_LARGE)

    /** 设置字体大小级别 */
    fun setFontSize(size: Int) {
        prefs.edit().putInt(KEY_FONT_SIZE, size).apply()
    }

    /** 获取字体大小文本描述 */
    fun getFontSizeText(): String = when (fontSize) {
        FONT_SIZE_NORMAL -> "普通"
        FONT_SIZE_LARGE -> "大"
        FONT_SIZE_EXTRA_LARGE -> "超大"
        else -> "超大"
    }

    // ==================== 语音播报 ====================

    /** 语音播报是否开启 */
    val isTTSEnabled: Boolean
        get() = prefs.getBoolean(KEY_TTS_ENABLED, true)

    /** 设置语音播报开关 */
    fun setTTSEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TTS_ENABLED, enabled).apply()
    }

    // ==================== 震动反馈 ====================

    /** 震动反馈是否开启 */
    val isVibrateEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATE_ENABLED, true)

    /** 设置震动反馈开关 */
    fun setVibrateEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATE_ENABLED, enabled).apply()
    }

    // ==================== 默认频道 ====================

    /** 获取默认频道ID */
    val defaultChannelId: Int
        get() = prefs.getInt(KEY_DEFAULT_CHANNEL, 1)

    /** 设置默认频道ID */
    fun setDefaultChannelId(channelId: Int) {
        prefs.edit().putInt(KEY_DEFAULT_CHANNEL, channelId).apply()
    }

    // ==================== 自动播放 ====================

    /** 启动时是否自动播放 */
    val isAutoPlayEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_PLAY, true)

    /** 设置自动播放开关 */
    fun setAutoPlayEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_PLAY, enabled).apply()
    }
}