package com.elderly.tvassistant.manager

import android.view.MotionEvent
import android.view.View
import android.util.Log

/**
 * 手势检测管理器
 * 检测屏幕触摸事件，判断点击区域（左/中/右）
 * 左侧点击 → 上一频道
 * 右侧点击 → 下一频道
 * 中间点击 → 显示/隐藏控制栏
 */
class GestureManager(
    private val callback: GestureCallback
) {

    /** 手势回调接口 */
    interface GestureCallback {
        fun onLeftRegionClick()       // 左侧区域点击
        fun onRightRegionClick()      // 右侧区域点击
        fun onMiddleRegionClick()     // 中间区域点击
    }

    private var touchStartX = 0f
    private var touchStartY = 0f

    companion object {
        private const val TAG = "GestureManager"
        private const val CLICK_THRESHOLD = 50  // 点击判定阈值（像素），超过此值视为滑动
    }

    /**
     * 处理触摸事件
     * @param view 触摸的视图
     * @param event 触摸事件
     * @return true表示事件已处理
     */
    fun onTouchEvent(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                touchStartY = event.y
            }
            MotionEvent.ACTION_UP -> {
                val deltaX = Math.abs(event.x - touchStartX)
                val deltaY = Math.abs(event.y - touchStartY)
                // 判断是点击还是滑动（位移小于阈值视为点击）
                if (deltaX < CLICK_THRESHOLD && deltaY < CLICK_THRESHOLD) {
                    val width = view.width.toFloat()
                    val x = event.x
                    handleRegionClick(x, width)
                }
            }
        }
        return true
    }

    /**
     * 判断点击区域并触发回调
     */
    private fun handleRegionClick(x: Float, width: Float) {
        when {
            x < width / 3 -> {
                Log.d(TAG, "左侧区域点击")
                callback.onLeftRegionClick()
            }
            x > width * 2 / 3 -> {
                Log.d(TAG, "右侧区域点击")
                callback.onRightRegionClick()
            }
            else -> {
                Log.d(TAG, "中间区域点击")
                callback.onMiddleRegionClick()
            }
        }
    }
}
