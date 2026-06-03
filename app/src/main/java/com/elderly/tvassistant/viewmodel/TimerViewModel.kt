package com.elderly.tvassistant.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * 定时ViewModel
 * 管理定时关闭的状态和剩余时间
 */
class TimerViewModel : ViewModel() {

    /** 定时剩余时间（秒），-1表示未设置定时 */
    private val _timerRemaining = MutableLiveData<Long>()
    val timerRemaining: LiveData<Long> = _timerRemaining

    /** 定时是否已设置 */
    private val _isTimerSet = MutableLiveData<Boolean>()
    val isTimerSet: LiveData<Boolean> = _isTimerSet

    /**
     * 更新定时剩余时间
     * @param seconds 剩余秒数
     */
    fun updateRemainingTime(seconds: Long) {
        _timerRemaining.value = seconds
    }

    /**
     * 设置定时状态
     * @param isSet true表示定时已设置
     */
    fun setTimerStatus(isSet: Boolean) {
        _isTimerSet.value = isSet
        if (!isSet) {
            _timerRemaining.value = -1L
        }
    }
}
