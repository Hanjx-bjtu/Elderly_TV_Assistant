package com.elderly.tvassistant.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.elderly.tvassistant.AboutActivity
import com.elderly.tvassistant.BaseActivity
import androidx.lifecycle.lifecycleScope
import com.elderly.tvassistant.R
import com.elderly.tvassistant.database.AppDatabase
import com.elderly.tvassistant.repository.ChannelRepository
import com.elderly.tvassistant.repository.FavoriteRepository
import com.elderly.tvassistant.utils.SharedPrefsHelper
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity() {

    private lateinit var prefsHelper: SharedPrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefsHelper = SharedPrefsHelper(this)

        supportActionBar?.title = "设置"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupSettings()
    }

    private fun setupSettings() {
        setupFontSize()
        setupTTSSwitch()
        setupAutoPlaySwitch()
        setupDefaultChannel()
        setupVibrateSwitch()
        setupNetworkReminderSwitch()
        setupAbout()
    }

    private fun setupFontSize() {
        findViewById<android.view.View>(R.id.layout_font_size).setOnClickListener {
            showFontSizeDialog()
        }
        updateFontSizeSummary()
    }

    private fun setupTTSSwitch() {
        val ttsSwitch = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_tts)
        ttsSwitch.isChecked = prefsHelper.isTTSEnabled
        ttsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsHelper.setTTSEnabled(isChecked)
        }
    }

    private fun setupAutoPlaySwitch() {
        val autoPlaySwitch = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_auto_play)
        autoPlaySwitch.isChecked = prefsHelper.isAutoPlayEnabled
        autoPlaySwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsHelper.setAutoPlayEnabled(isChecked)
        }
    }

    private fun setupDefaultChannel() {
        findViewById<android.view.View>(R.id.layout_default_channel).setOnClickListener {
            showChannelPickerDialog()
        }
        updateDefaultChannelSummary()
    }

    private fun setupVibrateSwitch() {
        val vibrateSwitch = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_vibrate)
        vibrateSwitch.isChecked = prefsHelper.isVibrateEnabled
        vibrateSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsHelper.setVibrateEnabled(isChecked)
        }
    }

    private fun setupNetworkReminderSwitch() {
        val networkSwitch = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_network_reminder)
        networkSwitch.isChecked = prefsHelper.isNetworkReminderEnabled
        networkSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsHelper.setNetworkReminderEnabled(isChecked)
        }
    }

    private fun setupAbout() {
        findViewById<android.view.View>(R.id.layout_about).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    private fun updateFontSizeSummary() {
        val summary = findViewById<android.widget.TextView>(R.id.tv_font_size_value)
        summary.text = prefsHelper.getFontSizeText()
    }

    private fun updateDefaultChannelSummary() {
        val summary = findViewById<android.widget.TextView>(R.id.tv_default_channel_value)
        val database = AppDatabase.getInstance(this)
        val channelRepository = ChannelRepository(database.channelDao())

        lifecycleScope.launch {
            val defaultId = prefsHelper.defaultChannelId
            val channel = channelRepository.getChannelById(defaultId)
            runOnUiThread {
                summary.text = channel?.displayName ?: channel?.name ?: "频道 $defaultId"
            }
        }
    }

    private fun showFontSizeDialog() {
        val sizes = arrayOf("普通", "大", "超大")
        val current = prefsHelper.fontSize - 1
        var fontSizeChanged = false
        AlertDialog.Builder(this)
            .setTitle("字体大小")
            .setSingleChoiceItems(sizes, current) { _, which ->
                val newSize = which + 1
                if (newSize != prefsHelper.fontSize) {
                    prefsHelper.setFontSize(newSize)
                    fontSizeChanged = true
                }
                updateFontSizeSummary()
            }
            .setPositiveButton("确定") { _, _ ->
                if (fontSizeChanged) {
                    recreate()
                }
            }
            .show()
    }

    private fun showChannelPickerDialog() {
        val database = AppDatabase.getInstance(this)
        val channelRepository = ChannelRepository(database.channelDao())

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
                        updateDefaultChannelSummary()
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