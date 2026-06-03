package com.elderly.tvassistant

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.elderly.tvassistant.database.AppDatabase
import com.elderly.tvassistant.utils.ChannelDataLoader
import com.elderly.tvassistant.utils.SharedPrefsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.elderly.tvassistant.R
import com.elderly.tvassistant.activity.GuideActivity
import com.elderly.tvassistant.activity.MainActivity
import kotlin.jvm.java

/**
 * 启动页Activity
 * 功能：
 * 1. 展示启动画面（2秒）
 * 2. 初始化本地数据库（首次启动时加载预设频道数据）
 * 3. 判断是否首次启动，决定跳转引导页或主页
 */
class SplashActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SplashActivity"
        private const val SPLASH_DELAY = 2000L  // 启动页展示时长（毫秒）
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var prefsHelper: SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        prefsHelper = SharedPrefsHelper(this)

        // 初始化数据库并加载预设数据
        initDatabase()
    }

    /**
     * 异步初始化数据库
     * 使用协程在IO线程中进行数据库操作
     */
    private fun initDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = AppDatabase.getInstance(this@SplashActivity)

                // 检查是否需要初始化预设数据
                val channelCount = database.channelDao().getChannelCount()
                if (channelCount == 0) {
                    Log.d(TAG, "数据库为空，加载预设频道数据")
                    loadPresetChannels(database)
                } else {
                    Log.d(TAG, "数据库已有 $channelCount 个频道")
                }

                // 切回主线程跳转页面
                withContext(Dispatchers.Main) {
                    checkFirstLaunch()
                }
            } catch (e: Exception) {
                Log.e(TAG, "数据库初始化失败: ${e.message}")
                // 即使失败也要跳转，使用默认频道兜底
                withContext(Dispatchers.Main) {
                    checkFirstLaunch()
                }
            }
        }
    }

    /**
     * 从assets加载预设频道数据到数据库
     */
    private suspend fun loadPresetChannels(database: AppDatabase) {
        try {
            val channels = ChannelDataLoader.loadFromAssets(this@SplashActivity)
            if (channels.isNotEmpty()) {
                database.channelDao().insertAllChannels(channels)
                Log.d(TAG, "成功加载 ${channels.size} 个预设频道")
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载预设频道失败: ${e.message}")
            // ChannelDataLoader内部已有兜底方案
        }
    }

    /**
     * 检查是否首次启动，决定跳转目标
     */
    private fun checkFirstLaunch() {
        val isFirstLaunch = prefsHelper.isFirstLaunch()
        val intent = if (isFirstLaunch) {
            Log.d(TAG, "首次启动，跳转引导页")
            Intent(this, GuideActivity::class.java)
        } else {
            Log.d(TAG, "非首次启动，跳转主界面")
            Intent(this, MainActivity::class.java)
        }

        handler.postDelayed({
            startActivity(intent)
            // 标记首次启动已完成
            prefsHelper.setFirstLaunchDone()
            finish()
            // 过渡动画
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, SPLASH_DELAY)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
