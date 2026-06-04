package com.elderly.tvassistant

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.elderly.tvassistant.database.AppDatabase
import com.elderly.tvassistant.manager.PlayerManager
import com.elderly.tvassistant.manager.TTSManager
import com.elderly.tvassistant.manager.TimerManager
import com.elderly.tvassistant.manager.VoiceManager
import com.elderly.tvassistant.model.Channel
import com.elderly.tvassistant.repository.ChannelRepository
import com.elderly.tvassistant.repository.FavoriteRepository
import com.elderly.tvassistant.utils.PermissionHelper
import com.elderly.tvassistant.utils.SharedPrefsHelper
import com.elderly.tvassistant.viewmodel.ChannelViewModel
import com.elderly.tvassistant.viewmodel.VoiceViewModel
import com.elderly.tvassistant.widget.GestureRelativeLayout

/**
 * 主界面Activity
 * 核心功能：
 * 1. 承载视频播放区域
 * 2. 处理左右区域点击切换频道
 * 3. 管理侧边栏抽屉（频道列表）
 * 4. 协调语音识别、TTS播报、定时关闭等功能
 * 5. 处理返回键退出确认
 */
class MainActivity : AppCompatActivity() {

    // UI组件
    private lateinit var videoContainer: FrameLayout
    private lateinit var currentChannelText: TextView
    private lateinit var voiceBtn: ImageButton
    private lateinit var timerBtn: ImageButton
    private lateinit var favoriteBtn: ImageButton
    private lateinit var menuBtn: ImageButton
    private lateinit var gestureLayout: GestureRelativeLayout

    // 管理器
    private lateinit var playerManager: PlayerManager
    private lateinit var voiceManager: VoiceManager
    private lateinit var ttsManager: TTSManager
    private lateinit var timerManager: TimerManager
    private lateinit var prefsHelper: SharedPrefsHelper
    private lateinit var permissionHelper: PermissionHelper

    // ViewModel
    private lateinit var channelViewModel: ChannelViewModel
    private lateinit var voiceViewModel: VoiceViewModel

    // 数据
    private var channelList: List<Channel> = emptyList()
    private var favoriteChannelList: List<Channel> = emptyList()
    private var currentChannelIndex = 0
    private var currentChannel: Channel? = null
    private var isFirstChannelLoad = true
    private var pendingTimerMinutes: Int = 0

    // 控制栏可见状态
    private var isControlBarVisible = true

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSION_VOICE_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 处理定时关闭退出
        if (intent?.getBooleanExtra("TIMER_OFF", false) == true) {
            finishAffinity()
            return
        }

        initViews()
        initManagers()
        initViewModels()
        setupObservers()
        setupGesture()
        setupBottomButtons()
        setupMenuButton()
    }

    /**
     * 初始化UI组件引用
     */
    private fun initViews() {
        videoContainer = findViewById(R.id.video_container)
        currentChannelText = findViewById(R.id.tv_current_channel)
        voiceBtn = findViewById(R.id.btn_voice)
        timerBtn = findViewById(R.id.btn_timer)
        favoriteBtn = findViewById(R.id.btn_favorite)
        menuBtn = findViewById(R.id.btn_menu)
        gestureLayout = findViewById(R.id.gesture_layout)
    }

    /**
     * 初始化各管理器
     */
    private fun initManagers() {
        // 播放器管理器
        playerManager = PlayerManager(this, videoContainer)
        playerManager.init(PlayerManager.PlayerType.WEB_VIEW)
        playerManager.onPlayError = { error ->
            runOnUiThread {
                Toast.makeText(this, "播放失败，请稍后重试", Toast.LENGTH_SHORT).show()
            }
        }

        // TTS管理器
        ttsManager = TTSManager(this)
        ttsManager.init()

        // 定时管理器
        timerManager = TimerManager(this)

        // 偏好设置
        prefsHelper = SharedPrefsHelper(this)

        // 权限工具
        permissionHelper = PermissionHelper(this)
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModels() {
        // 初始化数据库和Repository
        val database = AppDatabase.getInstance(this)
        val channelRepository = ChannelRepository(database.channelDao())
        val favoriteRepository = FavoriteRepository(database.favoriteDao(), database.channelDao())

        // 创建ViewModel
        channelViewModel = ViewModelProvider(
            this,
            ChannelViewModel.ChannelViewModelFactory(channelRepository, favoriteRepository)
        )[ChannelViewModel::class.java]
        voiceViewModel = ViewModelProvider(this)[VoiceViewModel::class.java]

        // 初始化语音管理器
        voiceManager = VoiceManager(this, object : VoiceManager.VoiceCallback {
            override fun onListeningStart() {
                runOnUiThread {
                    voiceBtn.isSelected = true
                    Toast.makeText(this@MainActivity, "正在听，请说频道名称...", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResult(channelName: String?) {
                runOnUiThread {
                    voiceBtn.isSelected = false
                    voiceViewModel.setVoiceResult(channelName)
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    voiceBtn.isSelected = false
                    // 静默处理，避免困扰老人
                }
            }
        })
        voiceManager.init()
    }

    /**
     * 设置LiveData观察者
     */
    private fun setupObservers() {
        // 观察频道列表
        channelViewModel.allChannels.observe(this) { channels ->
            channelList = channels
            if (channels.isNotEmpty()) {
                // 更新语音关键词库
                voiceManager.updateKeywords(channels)

                // 首次加载时播放默认频道
                if (isFirstChannelLoad) {
                    isFirstChannelLoad = false
                    val defaultChannelId = prefsHelper.defaultChannelId
                    val defaultChannel = channels.find { it.id == defaultChannelId } ?: channels.first()
                    switchToChannel(defaultChannel)
                }
            }
        }

        // 观察收藏状态
        channelViewModel.isFavorite.observe(this) { isFavorite ->
            favoriteBtn.isSelected = isFavorite
            favoriteBtn.contentDescription = if (isFavorite) "取消收藏" else "添加收藏"
        }

        // 观察收藏频道列表
        channelViewModel.favoriteChannels.observe(this) { favorites ->
            favoriteChannelList = favorites
        }

        // 观察语音识别结果
        voiceViewModel.voiceResult.observe(this) { channelName ->
            channelName?.let { name ->
                val targetChannel = channelList.find { channel ->
                    channel.name.contains(name, ignoreCase = true) ||
                            channel.displayName?.contains(name, ignoreCase = true) == true ||
                            channel.keywords.any { keyword ->
                                keyword.contains(name, ignoreCase = true)
                            }
                }
                targetChannel?.let { channel ->
                    switchToChannel(channel)
                    showToast("已切换到 $name")
                } ?: showToast("没有找到该频道")
            }
        }

        // 观察语音监听状态
        voiceViewModel.isListening.observe(this) { isListening ->
            voiceBtn.isSelected = isListening
        }
    }

    /**
     * 设置手势检测
     */
    private fun setupGesture() {
        gestureLayout.setOnRegionClickListener(object : GestureRelativeLayout.OnRegionClickListener {
            override fun onLeftClick() {
                switchToPrevChannel()
                vibrate()
            }

            override fun onRightClick() {
                switchToNextChannel()
                vibrate()
            }

            override fun onMiddleClick() {
                toggleControlBar()
            }
        })
    }

    /**
     * 设置底部按钮点击事件
     */
    private fun setupBottomButtons() {
        voiceBtn.setOnClickListener { startVoiceRecognition() }
        timerBtn.setOnClickListener { showTimerDialog() }
        favoriteBtn.setOnClickListener { toggleFavorite() }
    }

    /**
     * 设置菜单按钮（打开侧边栏）
     */
    private fun setupMenuButton() {
        menuBtn.setOnClickListener {
            showMainMenuDialog()
        }
    }

    /**
     * 显示主菜单对话框
     */
    private fun showMainMenuDialog() {
        val menuItems = arrayOf("查看收藏", "所有频道", "取消")
        AlertDialog.Builder(this)
            .setTitle("菜单")
            .setItems(menuItems) { _, which ->
                when (which) {
                    0 -> showFavoriteListDialog()
                    1 -> showChannelListDialog()
                }
            }
            .show()
    }

    /**
     * 显示收藏列表对话框
     */
    private fun showFavoriteListDialog() {
        if (favoriteChannelList.isEmpty()) {
            showToast("暂无收藏频道")
            return
        }
        val channelNames = favoriteChannelList.map { it.displayName ?: it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("我的收藏")
            .setItems(channelNames) { _, which ->
                switchToChannel(favoriteChannelList[which])
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 切换到上一个频道
     */
    fun switchToPrevChannel() {
        if (channelList.isEmpty()) return
        currentChannelIndex = (currentChannelIndex - 1 + channelList.size) % channelList.size
        switchToChannel(channelList[currentChannelIndex])
    }

    /**
     * 切换到下一个频道
     */
    fun switchToNextChannel() {
        if (channelList.isEmpty()) return
        currentChannelIndex = (currentChannelIndex + 1) % channelList.size
        switchToChannel(channelList[currentChannelIndex])
    }

    /**
     * 切换到指定频道
     */
    fun switchToChannel(channel: Channel) {
        currentChannel = channel
        currentChannelIndex = channelList.indexOf(channel).coerceAtLeast(0)

        // 更新UI
        val displayText = channel.displayName ?: channel.name
        currentChannelText.text = displayText

        // 检查收藏状态
        channelViewModel.checkFavorite(channel.id)

        // 播放视频
        playerManager.play(channel.url)

        // TTS播报
        if (prefsHelper.isTTSEnabled) {
            ttsManager.speakChannel(displayText)
        }
    }

    /**
     * 启动语音识别
     */
    private fun startVoiceRecognition() {
        if (permissionHelper.hasPermission(Manifest.permission.RECORD_AUDIO)) {
            voiceManager.startListening()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSION_VOICE_CODE
            )
        }
    }

    /**
     * 显示定时关闭选择对话框
     */
    private fun showTimerDialog() {
        val minutes = arrayOf("1分钟", "30分钟", "60分钟", "90分钟", "取消定时")
        AlertDialog.Builder(this)
            .setTitle("定时关闭")
            .setItems(minutes)  { _, which ->
                when (which) {
                    0 -> setTimer(1)
                    1 -> setTimer(30)
                    2 -> setTimer(60)
                    3 -> setTimer(90)
                    4 -> cancelTimer()
                }
            }
            .show()
    }

    /**
     * 设置定时关闭
     */
    @SuppressLint("ScheduleExactAlarm")
    private fun setTimer(minutes: Int) {
        pendingTimerMinutes = minutes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (permissionHelper.canScheduleExactAlarms()) {
                executeSetTimer(minutes)
            } else {
                showExactAlarmPermissionDialog()
            }
        } else {
            executeSetTimer(minutes)
        }
    }

    /**
     * 显示精确闹钟权限请求对话框
     */
    private fun showExactAlarmPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要精确闹钟权限")
            .setMessage("为了确保定时功能正常工作，需要授予精确闹钟权限。\n\n点击确定前往设置页面开启权限。")
            .setPositiveButton("确定") { _, _ ->
                val intent = permissionHelper.getExactAlarmSettingsIntent()
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    showToast("无法打开设置页面")
                }
            }
            .setNegativeButton("取消") { _, _ ->
                pendingTimerMinutes = 0
                showToast("定时功能需要精确闹钟权限")
            }
            .setCancelable(false)
            .show()
    }

    /**
     * 执行设置定时关闭
     */
    @SuppressLint("ScheduleExactAlarm")
    private fun executeSetTimer(minutes: Int) {
        timerManager.setTimer(minutes)
        timerBtn.isSelected = true
        showToast("已设置${minutes}分钟后自动关闭")
    }

    /**
     * 取消定时关闭
     */
    private fun cancelTimer() {
        timerManager.cancelTimer()
        timerBtn.isSelected = false
        pendingTimerMinutes = 0
        showToast("已取消定时关闭")
    }

    /**
     * 切换收藏状态
     */
    private fun toggleFavorite() {
        currentChannel?.let { channel ->
            channelViewModel.toggleFavorite(channel.id)
            val isFav = !favoriteBtn.isSelected
            showToast(if (isFav) "已添加收藏" else "已取消收藏")
        }
    }

    /**
     * 显示频道列表对话框
     */
    private fun showChannelListDialog() {
        if (channelList.isEmpty()) {
            showToast("暂无频道")
            return
        }
        val channelNames = channelList.map { it.displayName ?: it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("选择频道")
            .setItems(channelNames) { _, which ->
                switchToChannel(channelList[which])
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 切换控制栏显示/隐藏
     */
    private fun toggleControlBar() {
        isControlBarVisible = !isControlBarVisible
        val visibility = if (isControlBarVisible) View.VISIBLE else View.GONE
        currentChannelText.visibility = visibility
        voiceBtn.visibility = visibility
        timerBtn.visibility = visibility
        favoriteBtn.visibility = visibility
        menuBtn.visibility = visibility
    }

    /**
     * 震动反馈
     */
    private fun vibrate() {
        if (prefsHelper.isVibrateEnabled) {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    }

    /**
     * 显示提示信息
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 返回键退出确认
     */
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("退出")
            .setMessage("确定要退出长辈电视助手吗？")
            .setPositiveButton("确定") { _, _ -> finishAffinity() }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // 页面恢复时恢复播放
        if (currentChannel != null && !playerManager.isCurrentlyPlaying()) {
            playerManager.play(currentChannel!!.url)
        }

        // 检查是否有待处理的定时设置
        if (pendingTimerMinutes > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (permissionHelper.canScheduleExactAlarms()) {
                executeSetTimer(pendingTimerMinutes)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // 页面暂停时停止语音监听
        voiceManager.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.release()
        voiceManager.destroy()
        ttsManager.shutdown()
        timerManager.cancelTimer()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_VOICE_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    voiceManager.startListening()
                } else {
                    showToast("需要录音权限才能使用语音功能")
                }
            }
        }
    }
}