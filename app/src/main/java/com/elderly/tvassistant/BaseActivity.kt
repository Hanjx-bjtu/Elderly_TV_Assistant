package com.elderly.tvassistant

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.elderly.tvassistant.utils.FontSizeHelper
import com.elderly.tvassistant.utils.SharedPrefsHelper

open class BaseActivity : AppCompatActivity() {

    private var appliedFontScale: Float = 1.0f

    override fun attachBaseContext(newBase: Context) {
        val prefsHelper = SharedPrefsHelper(newBase)
        appliedFontScale = FontSizeHelper.getFontScale(prefsHelper.fontSize)
        val contextWrapper = FontSizeHelper.wrapContext(newBase, prefsHelper.fontSize)
        super.attachBaseContext(contextWrapper)
    }

    override fun onResume() {
        super.onResume()
        val prefsHelper = SharedPrefsHelper(this)
        val newFontScale = FontSizeHelper.getFontScale(prefsHelper.fontSize)
        if (newFontScale != appliedFontScale) {
            appliedFontScale = newFontScale
            recreate()
        }
    }
}