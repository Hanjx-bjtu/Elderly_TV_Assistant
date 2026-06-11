package com.elderly.tvassistant.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.elderly.tvassistant.model.Channel

class VoiceManager(
    private val context: Context,
    private val callback: VoiceCallback
) {

    interface VoiceCallback {
        fun onListeningStart()
        fun onResult(channelName: String?)
        fun onError(error: String)
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var channelKeywords: Map<String, String> = emptyMap()
    private var discoveredService: ComponentName? = null
    private var useXfyun = false
    private val xfyunManager: XfyunVoiceManager = XfyunVoiceManager(context)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var readyForSpeechTimeout: Runnable? = null
    private var isReadyForSpeechFired = false

    companion object {
        private const val TAG = "VoiceManager"
        private const val READY_FOR_SPEECH_TIMEOUT_MS = 3000L
        private val FAKE_SERVICE_KEYWORDS = listOf("fake", "stub", "dummy", "noop")
        private val KNOWN_SERVICES = listOf(
            ComponentName(
                "com.google.android.googlequicksearchbox",
                "com.google.android.voicesearch.serviceapi.GoogleRecognitionService"
            ),
            ComponentName(
                "com.huawei.vassistant",
                "com.huawei.vassistant.recognition.VoiceRecognitionService"
            ),
            ComponentName(
                "com.xiaomi.voiceassistant",
                "com.xiaomi.voiceassistant.MivoiceRecognitionService"
            ),
            ComponentName(
                "com.samsung.android.bixby.agent",
                "com.samsung.android.voiceservice.VoiceRecognitionService"
            ),
            ComponentName(
                "com.iflytek.speechcloud",
                "com.iflytek.speechcloud.RecognitionService"
            )
        )
    }

    private fun isFakeService(component: ComponentName): Boolean {
        val className = component.className.lowercase()
        val packageName = component.packageName.lowercase()
        for (keyword in FAKE_SERVICE_KEYWORDS) {
            if (className.contains(keyword) || packageName.contains(keyword)) {
                Log.w(TAG, "发现假语音识别服务: $component (匹配关键词: $keyword)")
                return true
            }
        }
        return false
    }

    private fun findRecognitionService(): ComponentName? {
        try {
            val intent = Intent("android.speech.RecognitionService")
            val services = context.packageManager.queryIntentServices(intent, 0)
            for (resolveInfo in services) {
                val serviceInfo = resolveInfo.serviceInfo
                val component = ComponentName(serviceInfo.packageName, serviceInfo.name)
                Log.d(TAG, "发现语音识别服务(查询): $component")
                if (!isFakeService(component)) {
                    return component
                }
                Log.d(TAG, "跳过假语音识别服务: $component")
            }
        } catch (e: Exception) {
            Log.w(TAG, "查询语音识别服务失败: ${e.message}")
        }

        for (service in KNOWN_SERVICES) {
            try {
                context.packageManager.getServiceInfo(service, 0)
                Log.d(TAG, "发现语音识别服务(已知): $service")
                if (!isFakeService(service)) {
                    return service
                }
                Log.d(TAG, "跳过假语音识别服务(已知): $service")
            } catch (_: Exception) {
            }
        }

        return null
    }

    fun init() {
        Log.d(TAG, "init: 开始初始化，isRecognitionAvailable=${SpeechRecognizer.isRecognitionAvailable(context)}")

        val realService = findRecognitionService()

        if (realService != null) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context, realService).apply {
                    setRecognitionListener(createRecognitionListener())
                }
                discoveredService = realService
                useXfyun = false
                Log.d(TAG, "init: 使用发现的语音识别服务: $realService")
                return
            } catch (e: Exception) {
                Log.e(TAG, "语音识别器创建失败: ${e.message}")
            }
        }

        if (xfyunManager.isAvailable()) {
            useXfyun = true
            Log.d(TAG, "init: 未发现真实系统识别服务，使用科大讯飞语音识别")
            return
        }

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createRecognitionListener())
                }
                useXfyun = false
                Log.d(TAG, "init: 科大讯飞不可用，尝试系统默认语音识别服务（可能不可靠）")
                return
            } catch (e: Exception) {
                Log.e(TAG, "默认语音识别器创建失败: ${e.message}")
            }
        }

        Log.w(TAG, "init: 所有语音识别方式均不可用")
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "准备就绪，等待语音输入")
                isReadyForSpeechFired = true
                cancelReadyForSpeechTimeout()
                callback.onListeningStart()
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "检测到语音开始")
            }

            override fun onRmsChanged(rmsDB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

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
                callback.onError(errorMsg)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull() ?: ""
                Log.d(TAG, "识别结果: $spokenText")
                val matchedChannel = matchChannel(spokenText)
                callback.onResult(matchedChannel)
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun updateKeywords(channels: List<Channel>) {
        channelKeywords = buildMap {
            channels.forEach { channel ->
                put(channel.name.lowercase(), channel.name)
                channel.displayName?.let { put(it.lowercase(), channel.name) }
                channel.keywords.forEach { keyword ->
                    put(keyword.lowercase(), channel.name)
                }
            }
        }
    }

    private fun matchChannel(spokenText: String): String? {
        val lowerText = spokenText.lowercase().trim()
        if (lowerText.isEmpty()) return null

        for ((keyword, channelName) in channelKeywords) {
            if (lowerText.contains(keyword)) {
                Log.d(TAG, "匹配成功: 关键词='$keyword' -> 频道='$channelName'")
                return channelName
            }
        }

        Log.d(TAG, "未匹配到频道: 识别文字='$lowerText'")
        return null
    }

    fun isAvailable(): Boolean {
        val realService = findRecognitionService()
        val hasRealSystemService = realService != null
        val xfyunReady = xfyunManager.isAvailable()
        val result = hasRealSystemService || xfyunReady
        Log.d(TAG, "isAvailable: hasRealSystem=$hasRealSystemService, xfyun=$xfyunReady, result=$result")
        return result
    }

    fun isXfyunAvailable(): Boolean = xfyunManager.isAvailable()

    fun isIntentRecognitionAvailable(): Boolean {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        return intent.resolveActivity(context.packageManager) != null
    }

    fun matchSpokenText(spokenText: String): String? {
        return matchChannel(spokenText)
    }

    fun createRecognizerIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出频道名称")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }
    }

    private fun cancelReadyForSpeechTimeout() {
        readyForSpeechTimeout?.let {
            mainHandler.removeCallbacks(it)
            readyForSpeechTimeout = null
        }
    }

    private fun scheduleReadyForSpeechTimeout() {
        cancelReadyForSpeechTimeout()
        isReadyForSpeechFired = false
        readyForSpeechTimeout = Runnable {
            if (!isReadyForSpeechFired) {
                Log.w(TAG, "onReadyForSpeech超时未触发，语音识别服务可能不可用")
                callback.onError("语音识别服务未响应，请尝试其他方式")
            }
        }
        mainHandler.postDelayed(readyForSpeechTimeout!!, READY_FOR_SPEECH_TIMEOUT_MS)
    }

    fun startListening() {
        if (useXfyun && xfyunManager.isAvailable()) {
            xfyunManager.startListening(object : XfyunVoiceManager.XfyunCallback {
                override fun onListeningStart() {
                    callback.onListeningStart()
                }

                override fun onResult(text: String) {
                    val matchedChannel = matchChannel(text)
                    callback.onResult(matchedChannel)
                }

                override fun onError(error: String) {
                    callback.onError(error)
                }
            })
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context) && discoveredService == null) {
            Log.e(TAG, "该设备不支持语音识别服务")
            callback.onError("该设备不支持语音识别服务")
            return
        }

        if (speechRecognizer == null) {
            init()
        }

        if (speechRecognizer == null) {
            if (xfyunManager.isAvailable()) {
                useXfyun = true
                xfyunManager.startListening(object : XfyunVoiceManager.XfyunCallback {
                    override fun onListeningStart() {
                        callback.onListeningStart()
                    }

                    override fun onResult(text: String) {
                        val matchedChannel = matchChannel(text)
                        callback.onResult(matchedChannel)
                    }

                    override fun onError(error: String) {
                        callback.onError(error)
                    }
                })
                return
            }
            Log.e(TAG, "语音识别器初始化失败，无法启动")
            callback.onError("语音识别不可用，请检查设备是否支持")
            return
        }

        try {
            speechRecognizer?.cancel()
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出频道名称")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }
            speechRecognizer?.startListening(intent)
            scheduleReadyForSpeechTimeout()
        } catch (e: Exception) {
            Log.e(TAG, "启动语音识别失败: ${e.message}")
            callback.onError("无法启动语音识别")
        }
    }

    fun stopListening() {
        cancelReadyForSpeechTimeout()
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "停止语音识别失败: ${e.message}")
        }
        xfyunManager.stopListening()
    }

    fun destroy() {
        cancelReadyForSpeechTimeout()
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "销毁语音识别器失败: ${e.message}")
        }
        speechRecognizer = null
        channelKeywords = emptyMap()
        xfyunManager.destroy()
    }
}