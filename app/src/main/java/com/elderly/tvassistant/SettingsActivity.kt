package com.elderly.tvassistant.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.elderly.tvassistant.R
import com.elderly.tvassistant.database.AppDatabase
import com.elderly.tvassistant.model.Channel
import com.elderly.tvassistant.repository.ChannelRepository
import com.elderly.tvassistant.repository.FavoriteRepository
import com.elderly.tvassistant.utils.SharedPrefsHelper
import com.elderly.tvassistant.viewmodel.ChannelViewModel
import kotlinx.coroutines.launch

/**
 * 设置页Activity
 * 提供以下设置项：
 * 1. 字体大小（普通/大/超大）
 * 2. 语音播报开关
 * 3. 震动反馈开关
 * 4. 默认频道设置
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var prefsHelper: SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefsHelper = SharedPrefsHelper(this)

        // 设置标题栏
        supportActionBar?.title = "设置"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupSettings()
    }

    private fun setupSettings() {
        // 字体大小设置
        findViewById<android.view.View>(R.id.layout_font_size).setOnClickListener {
            showFontSizeDialog()
        }
        updateFontSizeSummary()

        // 语音播报开关
        val ttsSwitch = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_tts)
        ttsSwitch.isChecked = prefsHelper.isTTSEnabled
        ttsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsHelper.setTTSEnabled(isChecked)
        }

        // 震动反馈开关
        val vibrateSwitch = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_vibrate)
        vibrateSwitch.isChecked = prefsHelper.isVibrateEnabled
        vibrateSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsHelper.setVibrateEnabled(isChecked)
        }

        // 默认频道设置
        findViewById<android.view.View>(R.id.layout_default_channel).setOnClickListener {
            showChannelPickerDialog()
        }
    }

    private fun updateFontSizeSummary() {
        val summary = findViewById<android.widget.TextView>(R.id.tv_font_size_value)
        summary.text = prefsHelper.getFontSizeText()
    }

    private fun showFontSizeDialog() {
        val sizes = arrayOf("普通", "大", "超大")
        val current = prefsHelper.fontSize - 1
        AlertDialog.Builder(this)
            .setTitle("字体大小")
            .setSingleChoiceItems(sizes, current) { _, which ->
                prefsHelper.setFontSize(which + 1)
                updateFontSizeSummary()
            }
            .setPositiveButton("确定", null)
            .show()
    }

    private fun showChannelPickerDialog() {
        val database = AppDatabase.getInstance(this)
        val channelRepository = ChannelRepository(database.channelDao())
        val favoriteRepository = FavoriteRepository(database.favoriteDao(), database.channelDao())

        lifecycleScope.launch {
            val channels = channelRepository.getAllChannelsList()
            if (channels.isEmpty()) {
                Toast.makeText(this@SettingsActivity, "暂无频道", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val channelNames = channels.map { it.displayName ?: it.name }.toTypedArray()
            val currentDefault = prefsHelper.defaultChannelId
            val currentIndex = channels.indexOfFirst { it.id == currentDefault }.coerceAtLeast(0)

            runOnUiThread {
                AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("选择默认频道")
                    .setSingleChoiceItems(channelNames, currentIndex) { _, which ->
                        val channel = channels[which]
                        prefsHelper.setDefaultChannelId(channel.id)
                        Toast.makeText(
                            this@SettingsActivity,
                            "默认频道已设置为 ${channel.displayName ?: channel.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setPositiveButton("确定", null)
                    .show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
