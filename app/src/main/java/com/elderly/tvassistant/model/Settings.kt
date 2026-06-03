package com.elderly.tvassistant.model

/**
 * 设置实体类
 * 用于管理应用设置项的数据结构
 * 实际存储通过SharedPreferences实现
 *
 * @property fontSizeLevel 字体大小级别：1=普通, 2=大, 3=超大
 * @property ttsEnabled 是否开启语音播报
 * @property vibrateEnabled 是否开启震动反馈
 * @property defaultChannelId 启动时默认播放的频道ID
 * @property firstLaunchDone 是否已完成首次引导
 */
data class Setting(
    val fontSizeLevel: Int = FONT_SIZE_EXTRA_LARGE,
    val ttsEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val defaultChannelId: Int = 1,
    val firstLaunchDone: Boolean = false
) {
    companion object {
        const val FONT_SIZE_NORMAL = 1
        const val FONT_SIZE_LARGE = 2
        const val FONT_SIZE_EXTRA_LARGE = 3

        /**
         * 根据字体级别获取字体级别描述文本
         */
        fun getFontSizeText(level: Int): String = when (level) {
            FONT_SIZE_NORMAL -> "普通"
            FONT_SIZE_LARGE -> "大"
            FONT_SIZE_EXTRA_LARGE -> "超大"
            else -> "超大"
        }
    }
}
