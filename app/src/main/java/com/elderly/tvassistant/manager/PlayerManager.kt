package com.elderly.tvassistant.manager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar

/**
 * 播放器管理器
 * 管理视频播放器的创建、销毁和播放控制
 * 主方案使用WebView加载央视网页播放视频
 * 备选方案使用IjkPlayer播放m3u8流（需额外依赖）
 */
class PlayerManager(
    private val context: Context,
    private val container: FrameLayout
) {

    /** 播放器类型枚举 */
    enum class PlayerType {
        WEB_VIEW,    // WebView方案（主方案）
        IJK_PLAYER   // IjkPlayer方案（备选）
    }

    private var playerType: PlayerType = PlayerType.WEB_VIEW
    private var webView: WebView? = null
    private var currentUrl: String = ""
    private var isPlaying = false
    private var retryCount = 0
    private val maxRetry = 3

    /** 加载进度条 */
    private var progressBar: ProgressBar? = null

    /** 播放器回调 */
    var onPlayStarted: (() -> Unit)? = null
    var onPlayError: ((String) -> Unit)? = null

    /**
     * 初始化播放器
     * @param type 播放器类型
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun init(type: PlayerType) {
        playerType = type
        when (type) {
            PlayerType.WEB_VIEW -> initWebView()
            PlayerType.IJK_PLAYER -> {
                // IjkPlayer需要额外依赖，当前版本使用WebView方案
            }
        }
    }

    /**
     * 初始化WebView播放器
     * 配置WebView以支持视频自动播放和全屏
     */
    private fun initWebView() {
        webView = WebView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_DEFAULT
                allowFileAccess = true
                databaseEnabled = true
                setSupportZoom(false)
            }

            // 设置WebChromeClient以支持视频全屏
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progressBar?.progress = newProgress
                    if (newProgress == 100) {
                        progressBar?.visibility = View.GONE
                    }
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    progressBar?.visibility = View.VISIBLE
                    progressBar?.progress = 0
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // 页面加载完成后尝试自动播放视频
                    view?.evaluateJavascript(
                        "try { var videos = document.querySelectorAll('video'); " +
                                "videos.forEach(function(v) { v.play(); }); } catch(e) {}",
                        null
                    )
                    isPlaying = true
                    onPlayStarted?.invoke()
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && request?.isForMainFrame == true) {
                        handlePlayError("页面加载失败: ${error?.description}")
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    handlePlayError("页面加载失败: $description")
                }
            }
        }

        // 添加加载进度条
        progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // 默认GONE，加载时显示
                visibility = View.GONE
            }
        }

        container.removeAllViews()
        container.addView(webView)
        container.addView(progressBar)
    }

    /**
     * 处理播放错误，支持自动重试
     */
    private fun handlePlayError(errorMsg: String) {
        if (retryCount < maxRetry) {
            retryCount++
            // 延迟1秒后重试
            webView?.postDelayed({
                if (currentUrl.isNotEmpty()) {
                    webView?.loadUrl(currentUrl)
                }
            }, 1000)
        } else {
            isPlaying = false
            onPlayError?.invoke(errorMsg)
        }
    }

    /**
     * 播放指定URL的视频
     * @param url 视频源地址
     */
    fun play(url: String) {
        currentUrl = url
        retryCount = 0
        when (playerType) {
            PlayerType.WEB_VIEW -> {
                webView?.loadUrl(url)
            }
            PlayerType.IJK_PLAYER -> {
                // IjkPlayer播放逻辑（备用方案）
            }
        }
        isPlaying = true
    }

    /**
     * 停止播放
     */
    fun stop() {
        when (playerType) {
            PlayerType.WEB_VIEW -> {
                webView?.loadUrl("about:blank")
            }
            PlayerType.IJK_PLAYER -> {
                // 停止IjkPlayer
            }
        }
        isPlaying = false
    }

    /**
     * 释放所有资源
     * 在Activity/Fragment销毁时调用
     */
    fun release() {
        stop()
        when (playerType) {
            PlayerType.WEB_VIEW -> {
                webView?.apply {
                    stopLoading()
                    loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
                    clearHistory()
                    removeAllViews()
                    (parent as? ViewGroup)?.removeView(this)
                    destroy()
                }
                webView = null
            }
            PlayerType.IJK_PLAYER -> {
                // 释放IjkPlayer资源
            }
        }
        container.removeAllViews()
        progressBar = null
    }

    /**
     * 判断是否正在播放
     */
    fun isCurrentlyPlaying(): Boolean = isPlaying

    /**
     * 获取当前播放的URL
     */
    fun getCurrentUrl(): String = currentUrl

    companion object {
        private const val TAG = "PlayerManager"
    }
}
