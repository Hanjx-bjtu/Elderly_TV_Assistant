package com.elderly.tvassistant.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout

/**
 * 手势检测自定义布局
 * 将屏幕水平方向分为三个区域：
 * - 左侧1/3: 点击触发上一频道
 * - 中间1/3: 点击触发显示/隐藏控制栏
 * - 右侧1/3: 点击触发下一频道
 */
class GestureRelativeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val CLICK_THRESHOLD = 50  // 点击位移阈值（像素）
    }

    /** 区域点击回调接口 */
    interface OnRegionClickListener {
        fun onLeftClick()       // 左侧区域点击
        fun onRightClick()      // 右侧区域点击
        fun onMiddleClick()     // 中间区域点击
    }

    private var listener: OnRegionClickListener? = null
    private var touchStartX = 0f
    private var touchStartY = 0f

    /**
     * 设置区域点击监听器
     */
    fun setOnRegionClickListener(listener: OnRegionClickListener) {
        this.listener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                touchStartY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                val deltaX = Math.abs(event.x - touchStartX)
                val deltaY = Math.abs(event.y - touchStartY)
                // 判定为点击（非滑动）
                if (deltaX < CLICK_THRESHOLD && deltaY < CLICK_THRESHOLD) {
                    val x = event.x
                    val width = width.toFloat()
                    when {
                        x < width / 3 -> listener?.onLeftClick()
                        x > width * 2 / 3 -> listener?.onRightClick()
                        else -> listener?.onMiddleClick()
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
