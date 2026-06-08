package com.elderly.tvassistant.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * 透明触摸拦截层
 * 用于拦截WebView的触摸事件，防止网页内的点击干扰应用手势操作
 * 该View完全透明，位于WebView上层，接收所有触摸事件并传递给父布局处理
 */
class TouchInterceptorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "TouchInterceptorView"
    }

    init {
        // 确保View完全透明
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        // 不绘制任何内容
        setWillNotDraw(true)
    }

    /**
     * 拦截所有触摸事件，防止传递给WebView
     * 返回true表示事件被拦截，不会传递给子View
     */
    fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return true
    }

    /**
     * 不消费触摸事件，让事件传递给父布局（GestureRelativeLayout）处理
     * 返回false表示事件未被消费，会继续传递
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    /**
     * 不绘制任何内容，保持完全透明
     */
    override fun draw(canvas: Canvas) {
        // 不调用super.draw()，避免绘制任何内容
    }

    /**
     * 不绘制任何内容，保持完全透明
     */
    override fun onDraw(canvas: Canvas) {
        // 不调用super.onDraw()，避免绘制任何内容
    }
}