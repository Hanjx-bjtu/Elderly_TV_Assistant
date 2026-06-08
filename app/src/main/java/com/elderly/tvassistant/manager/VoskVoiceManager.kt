package com.elderly.tvassistant.manager

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class VoskVoiceManager(private val context: Context) {

    interface VoskCallback {
        fun onListeningStart()
        fun onResult(text: String)
        fun onError(error: String)
    }

    interface DownloadCallback {
        fun onProgress(progress: Int)
        fun onComplete()
        fun onError(error: String)
    }

    private var speechService: SpeechService? = null
    private var model: Model? = null
    private var callback: VoskCallback? = null
    private var isListening = false
    private val gson = Gson()
    private var downloadJob: Job? = null

    companion object {
        private const val TAG = "VoskVoiceManager"
        private const val MODEL_DIR_NAME = "vosk-model-small-cn"
        private const val MODEL_DOWNLOAD_URL =
            "https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip"
    }

    val modelDir: File
        get() = File(context.filesDir, MODEL_DIR_NAME)

    fun isModelReady(): Boolean = modelDir.exists() && model != null

    fun isModelDownloaded(): Boolean = modelDir.exists()

    fun isDownloading(): Boolean = downloadJob?.isActive == true

    fun loadModel() {
        if (!modelDir.exists()) return
        try {
            model = Model(modelDir.absolutePath)
            Log.d(TAG, "Vosk模型加载成功")
        } catch (e: Exception) {
            Log.e(TAG, "Vosk模型加载失败: ${e.message}")
            model = null
        }
    }

    fun downloadModel(downloadCallback: DownloadCallback) {
        if (modelDir.exists()) {
            loadModel()
            downloadCallback.onComplete()
            return
        }

        downloadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) { downloadCallback.onProgress(0) }

                val zipFile = File(context.cacheDir, "vosk-model.zip")
                downloadZip(zipFile) { progress ->
                    CoroutineScope(Dispatchers.Main).launch {
                        downloadCallback.onProgress(progress)
                    }
                }

                withContext(Dispatchers.Main) { downloadCallback.onProgress(-1) }
                extractZip(zipFile)
                zipFile.delete()

                loadModel()

                withContext(Dispatchers.Main) { downloadCallback.onComplete() }
            } catch (_: CancellationException) {
                val zipFile = File(context.cacheDir, "vosk-model.zip")
                zipFile.delete()
            } catch (e: Exception) {
                Log.e(TAG, "模型下载失败: ${e.message}")
                withContext(Dispatchers.Main) { downloadCallback.onError(e.message ?: "下载失败") }
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
    }

    private fun downloadZip(zipFile: File, onProgress: (Int) -> Unit) {
        val url = URL(MODEL_DOWNLOAD_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 30000
        connection.readTimeout = 30000
        connection.instanceFollowRedirects = true

        try {
            connection.connect()
            val totalSize = connection.contentLength.toLong()

            connection.inputStream.use { input ->
                FileOutputStream(zipFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L
                    var lastProgress = -1

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (totalSize > 0) {
                            val progress = (totalRead * 100 / totalSize).toInt()
                            if (progress != lastProgress && progress % 5 == 0) {
                                lastProgress = progress
                                onProgress(progress)
                            }
                        }
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun extractZip(zipFile: File) {
        ZipInputStream(zipFile.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val entryName = entry.name
                val relativePath = entryName.substringAfter('/', "")
                if (relativePath.isEmpty()) {
                    entry = zis.nextEntry
                    continue
                }

                val outFile = File(modelDir, relativePath)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fos ->
                        val buf = ByteArray(8192)
                        var len: Int
                        while (zis.read(buf).also { len = it } > 0) {
                            fos.write(buf, 0, len)
                        }
                    }
                }
                entry = zis.nextEntry
            }
        }
        Log.d(TAG, "模型解压完成: ${modelDir.absolutePath}")
    }

    fun startListening(voskCallback: VoskCallback) {
        if (model == null) {
            voskCallback.onError("语音模型未加载")
            return
        }

        callback = voskCallback

        try {
            val recognizer = Recognizer(model, 16000f)
            speechService = SpeechService(recognizer, 16000f)
            speechService?.startListening(object : RecognitionListener {
                override fun onPartialResult(partialResult: String?) {}

                override fun onResult(result: String?) {
                    result?.let { json ->
                        try {
                            val obj = gson.fromJson(json, JsonObject::class.java)
                            val text = obj.get("text")?.asString ?: ""
                            if (text.isNotEmpty()) {
                                Log.d(TAG, "Vosk识别结果: $text")
                                callback?.onResult(text)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "解析识别结果失败: ${e.message}")
                        }
                    }
                }

                override fun onFinalResult(finalResult: String?) {
                    finalResult?.let { json ->
                        try {
                            val obj = gson.fromJson(json, JsonObject::class.java)
                            val text = obj.get("text")?.asString ?: ""
                            if (text.isNotEmpty()) {
                                Log.d(TAG, "Vosk最终识别结果: $text")
                                callback?.onResult(text)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "解析最终结果失败: ${e.message}")
                        }
                    }
                    isListening = false
                }

                override fun onError(exception: Exception?) {
                    Log.e(TAG, "Vosk识别错误: ${exception?.message}")
                    isListening = false
                    callback?.onError("语音识别出错")
                }

                override fun onTimeout() {
                    isListening = false
                    callback?.onError("语音识别超时")
                }
            })
            isListening = true
            callback?.onListeningStart()
        } catch (e: Exception) {
            Log.e(TAG, "启动Vosk识别失败: ${e.message}")
            voskCallback.onError("无法启动语音识别")
        }
    }

    fun stopListening() {
        try {
            speechService?.stop()
            isListening = false
        } catch (e: Exception) {
            Log.e(TAG, "停止Vosk识别失败: ${e.message}")
        }
    }

    fun destroy() {
        try {
            speechService?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "销毁Vosk识别器失败: ${e.message}")
        }
        speechService = null
        model = null
        callback = null
        downloadJob?.cancel()
    }
}