package com.elderly.tvassistant.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * 语音ViewModel
 * 管理语音识别的状态和结果
 * 将语音识别结果通过LiveData传递给UI层
 */
class VoiceViewModel : ViewModel() {

    /** 语音识别结果（匹配到的频道名称） */
    private val _voiceResult = MutableLiveData<String?>()
    val voiceResult: LiveData<String?> = _voiceResult

    /** 是否正在监听语音 */
    private val _isListening = MutableLiveData<Boolean>()
    val isListening: LiveData<Boolean> = _isListening

    /** 语音识别错误信息 */
    private val _voiceError = MutableLiveData<String?>()
    val voiceError: LiveData<String?> = _voiceError

    /**
     * 设置语音识别结果
     * @param channelName 匹配到的频道名称，null表示未匹配到
     */
    fun setVoiceResult(channelName: String?) {
        _voiceResult.value = channelName
    }

    /**
     * 设置语音监听状态
     * @param listening true表示正在监听
     */
    fun setListening(listening: Boolean) {
        _isListening.value = listening
    }

    /**
     * 设置语音识别错误
     * @param error 错误信息
     */
    fun setVoiceError(error: String?) {
        _voiceError.value = error
    }
}
