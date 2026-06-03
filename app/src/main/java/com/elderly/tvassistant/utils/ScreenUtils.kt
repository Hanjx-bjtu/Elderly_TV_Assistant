package com.elderly.tvassistant.utils

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager

/**
 * 屏幕适配工具类
 * 提供屏幕尺寸计算和dp/sp转px的工具方法
 * 用于适老化设计中的尺寸适配
 */
class ScreenUtils(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val displayMetrics: DisplayMetrics
        get() {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            return metrics
        }

    /** 屏幕宽度（像素） */
    val screenWidth: Int
        get() = displayMetrics.widthPixels

    /** 屏幕高度（像素） */
    val screenHeight: Int
        get() = displayMetrics.heightPixels

    /** 屏幕密度 */
    val density: Float
        get() = displayMetrics.density

    /**
     * dp转px
     */
    fun dpToPx(dp: Float): Int {
        return (dp * density + 0.5f).toInt()
    }

    /**
     * sp转px
     */
    fun spToPx(sp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp, displayMetrics
        ).toInt()
    }

    /**
     * 获取视频区域高度（屏幕高度的60%）
     */
    fun getVideoAreaHeight(): Int {
        return (screenHeight * 0.6).toInt()
    }

    /**
     * 获取侧边栏宽度（屏幕宽度的75%）
     */
    fun getDrawerWidth(): Int {
        return (screenWidth * 0.75).toInt()
    }
}
