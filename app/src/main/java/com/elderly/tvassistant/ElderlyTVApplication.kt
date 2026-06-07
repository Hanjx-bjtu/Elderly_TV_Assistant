package com.elderly.tvassistant

import android.app.Application
import android.content.Context
import com.elderly.tvassistant.utils.FontSizeHelper
import com.elderly.tvassistant.utils.SharedPrefsHelper

class ElderlyTVApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun attachBaseContext(base: Context) {
        val prefsHelper = SharedPrefsHelper(base)
        val contextWrapper = FontSizeHelper.wrapContext(base, prefsHelper.fontSize)
        super.attachBaseContext(contextWrapper)
    }

    companion object {
        @Volatile
        private lateinit var instance: ElderlyTVApplication

        fun getInstance(): ElderlyTVApplication = instance
    }
}