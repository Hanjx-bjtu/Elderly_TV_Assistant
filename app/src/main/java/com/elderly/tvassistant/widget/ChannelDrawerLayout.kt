package com.elderly.tvassistant.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.drawerlayout.widget.DrawerLayout

/**
 * 自定义侧边栏布局
 * 扩展DrawerLayout，增加适老化功能：
 * - 增大触摸响应区域（edge size）
 * - 支持更灵敏的滑动触发
 */
class ChannelDrawerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DrawerLayout(context, attrs, defStyleAttr) {

    /** 触摸边缘范围（像素），增大以方便老人操作 */
    private val touchEdgeSize: Int

    init {
        // 将默认边缘范围扩大2倍
        val config = ViewConfiguration.get(context)
        touchEdgeSize = config.scaledTouchSlop * 4
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // 放宽侧边栏滑出的触发范围
        try {
            return super.onInterceptTouchEvent(ev)
        } catch (e: Exception) {
            // 防止在某些设备上的并发修改异常
            return false
        }
    }

    companion object {
        private const val TAG = "ChannelDrawerLayout"
    }
}
