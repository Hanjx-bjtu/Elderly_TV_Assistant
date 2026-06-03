package com.elderly.tvassistant.manager

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * 语音合成（TTS）管理器
 * 封装TextToSpeech，实现频道切换时的语音播报
 * 针对老年用户，语速稍慢（0.9倍速）
 */
class TTSManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    companion object {
        private const val TAG = "TTSManager"
        private const val DEFAULT_SPEECH_RATE = 0.9f  // 语速稍慢，适合老人
        private const val DEFAULT_PITCH = 1.0f       // 正常音调
    }

    /**
     * 初始化TTS引擎
     * 异步初始化，初始化完成后设置中文语言
     */
    fun init() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                // 设置中文语言
                val result = tts?.setLanguage(Locale.CHINESE)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "中文TTS不支持，尝试使用简体中文")
                    tts?.language = Locale.SIMPLIFIED_CHINESE
                }
                // 设置语速和音调
                tts?.setSpeechRate(DEFAULT_SPEECH_RATE)
                tts?.setPitch(DEFAULT_PITCH)
                Log.d(TAG, "TTS初始化成功")
            } else {
                Log.e(TAG, "TTS初始化失败, status: $status")
                isInitialized = false
            }
        }
    }

    /**
     * 播报文字
     * @param text 要播报的文本
     */
    fun speak(text: String) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS未初始化，无法播报: $text")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_utterance")
        } else {
            @Suppress("DEPRECATION")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    /**
     * 播报频道名称
     * 格式："正在播放 XXX"
     * @param channelName 频道名称
     */
    fun speakChannel(channelName: String) {
        speak("正在播放 $channelName")
    }

    /**
     * 停止当前播报
     */
    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "停止TTS失败: ${e.message}")
        }
    }

    /**
     * 关闭TTS引擎，释放资源
     */
    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "关闭TTS失败: ${e.message}")
        }
        tts = null
        isInitialized = false
    }

    /**
     * TTS是否已初始化
     */
    fun isReady(): Boolean = isInitialized
}
