package com.elderly.tvassistant

import android.app.Application

/**
 * 应用全局Application类
 * 用于全局初始化操作
 */
class ElderlyTVApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        @Volatile
        private lateinit var instance: ElderlyTVApplication

        fun getInstance(): ElderlyTVApplication = instance
    }
}
