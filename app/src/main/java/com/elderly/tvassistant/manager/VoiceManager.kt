package com.elderly.tvassistant.manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.elderly.tvassistant.model.Channel

/**
 * 语音识别管理器
 * 封装Android SpeechRecognizer，实现语音换台功能
 * 将识别的文字与频道关键词进行模糊匹配
 */
class VoiceManager(
    private val context: Context,
    private val callback: VoiceCallback
) {

    /** 语音识别回调接口 */
    interface VoiceCallback {
        fun onListeningStart()           // 开始监听（可用于UI反馈）
        fun onResult(channelName: String?) // 识别结果（匹配到的频道名）
        fun onError(error: String)       // 识别错误
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var channelKeywords: Map<String, String> = emptyMap()  // keyword -> channelName

    companion object {
        private const val TAG = "VoiceManager"
    }

    /**
     * 初始化语音识别器
     */
    fun init() {
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createRecognitionListener())
            }
        } catch (e: Exception) {
            Log.e(TAG, "语音识别器初始化失败: ${e.message}")
            callback.onError("语音识别不可用")
        }
    }

    /**
     * 创建语音识别监听器
     */
    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "准备就绪，等待语音输入")
                callback.onListeningStart()
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "检测到语音开始")
            }

            override fun onRmsChanged(rmsDB: Float) {
                // 音量变化回调，可用于UI波形动画
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // 缓冲区数据回调
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "语音结束")
            }

            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "录音错误"
                    SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "录音权限不足"
                    SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
                    SpeechRecognizer.ERROR_NO_MATCH -> "没有识别到内容"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器繁忙"
                    SpeechRecognizer.ERROR_SERVER -> "服务器错误"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "没有检测到语音"
                    else -> "识别失败(错误码: $error)"
                }
                Log.e(TAG, "语音识别错误: $errorMsg")

                // 静默处理，不中断当前播放（避免困扰老人）
                callback.onError(errorMsg)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull() ?: ""
                Log.d(TAG, "识别结果: $spokenText")

                val matchedChannel = matchChannel(spokenText)
                callback.onResult(matchedChannel)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // 部分识别结果（实时识别中间结果）
                val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d(TAG, "部分识别结果: $partial")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // 其他事件
            }
        }
    }

    /**
     * 更新频道关键词库
     * @param channels 当前所有频道列表
     */
    fun updateKeywords(channels: List<Channel>) {
        channelKeywords = buildMap {
            channels.forEach { channel ->
                // 添加频道名称本身作为关键词
                put(channel.name.lowercase(), channel.name)
                // 添加显示名称
                channel.displayName?.let { put(it.lowercase(), channel.name) }
                // 添加所有预设关键词
                channel.keywords.forEach { keyword ->
                    put(keyword.lowercase(), channel.name)
                }
            }
        }
    }

    /**
     * 将识别的文字与频道关键词进行匹配
     * 使用模糊匹配：识别文字中包含关键词即匹配成功
     * @param spokenText 语音识别的文字
     * @return 匹配到的频道名称，未匹配到返回null
     */
    private fun matchChannel(spokenText: String): String? {
        val lowerText = spokenText.lowercase().trim()
        if (lowerText.isEmpty()) return null

        // 遍历关键词，查找匹配
        for ((keyword, channelName) in channelKeywords) {
            if (lowerText.contains(keyword)) {
                Log.d(TAG, "匹配成功: 关键词='$keyword' -> 频道='$channelName'")
                return channelName
            }
        }

        Log.d(TAG, "未匹配到频道: 识别文字='$lowerText'")
        return null
    }

    /**
     * 开始语音监听
     */
    fun startListening() {
        if (speechRecognizer == null) {
            init()
        }

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出频道名称")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "启动语音识别失败: ${e.message}")
            callback.onError("无法启动语音识别")
        }
    }

    /**
     * 停止语音监听
     */
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "停止语音识别失败: ${e.message}")
        }
    }

    /**
     * 销毁语音识别器，释放资源
     */
    fun destroy() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "销毁语音识别器失败: ${e.message}")
        }
        speechRecognizer = null
        channelKeywords = emptyMap()
    }
}
