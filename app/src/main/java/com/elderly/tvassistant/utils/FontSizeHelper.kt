package com.elderly.tvassistant.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import com.elderly.tvassistant.utils.SharedPrefsHelper.Companion.FONT_SIZE_EXTRA_LARGE
import com.elderly.tvassistant.utils.SharedPrefsHelper.Companion.FONT_SIZE_LARGE
import com.elderly.tvassistant.utils.SharedPrefsHelper.Companion.FONT_SIZE_NORMAL

object FontSizeHelper {

    fun getFontScale(fontSizeLevel: Int): Float = when (fontSizeLevel) {
        FONT_SIZE_NORMAL -> 0.8f
        FONT_SIZE_LARGE -> 1.0f
        FONT_SIZE_EXTRA_LARGE -> 1.2f
        else -> 1.2f
    }

    fun wrapContext(context: Context, fontSizeLevel: Int): ContextWrapper {
        val fontScale = getFontScale(fontSizeLevel)
        val config = Configuration(context.resources.configuration)
        config.fontScale = fontScale
        val newContext = context.createConfigurationContext(config)
        return ContextWrapper(newContext)
    }
}