package com.elderly.tvassistant.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.elderly.tvassistant.R

/**
 * 视频播放Fragment
 * 作为WebView视频播放的容器Fragment
 * 可被MainActivity动态加载和管理
 */
class VideoFragment : Fragment() {

    private var webView: WebView? = null
    private lateinit var container: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container = view.findViewById(R.id.video_web_container)
    }

    /**
     * 加载URL
     */
    fun loadUrl(url: String) {
        webView?.loadUrl(url)
    }

    /**
     * 暂停播放
     */
    fun pause() {
        webView?.onPause()
    }

    /**
     * 恢复播放
     */
    fun resume() {
        webView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView?.apply {
            stopLoading()
            loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            clearHistory()
            destroy()
        }
        webView = null
    }
}
