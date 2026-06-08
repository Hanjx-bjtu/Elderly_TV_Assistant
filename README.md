# 长辈电视助手 (Elderly TV Assistant)

一款专为老年用户设计的 Android 电视直播应用，通过极简交互、语音控制和适老化设计，让长辈轻松观看电视直播。

## 功能特性

### 极简换台
- **左右点击换台**：点击屏幕左侧切上一频道，右侧切下一频道，无需复杂操作
- **语音换台**：说出频道名称（如"中央一台"），即可快速切换，支持在线/离线语音识别
- **收藏频道**：常看的频道一键收藏，快速访问

### 语音识别（待实现）
- **在线识别**：优先使用系统 SpeechRecognizer（支持 Google、华为、小米、三星、讯飞等引擎）
- **离线识别**：基于 [Vosk](https://alphacephei.com/vosk/) 的离线中文语音模型（约 42MB），无网络也可使用
- **Intent 回退**：当系统语音服务不可用时，自动回退到 Intent 方式调用第三方语音应用

### 语音播报 (TTS)
- 切换频道时自动播报频道名称（"正在播放 中央一台"）
- 语速 0.9 倍，适合老年用户聆听
- 可在设置中开关

### 定时关闭
- 支持 15/30/60/90 分钟定时自动关闭
- 基于 AlarmManager 精确定时，省电可靠
- 看电视不怕忘记关

### 适老化设计
- **超大字体**：支持普通/大/超大三级字体缩放，全局生效
- **大按钮**：所有操作按钮尺寸加大，易于点击
- **首次引导**：首次启动展示图文引导页，介绍核心操作方法
- **震动反馈**：换台时提供震动提示，操作更有确认感

### 频道管理
- 预置 CCTV 1-13 频道（综合、财经、综艺、中文国际、体育、电影、国防军事、电视剧、纪录、科教、戏曲、新闻）
- 频道数据本地持久化（Room 数据库）
- 支持自定义默认频道和启动自动播放

## 应用截图

| 启动页 | 引导页 | 主界面 |
|:---:|:---:|:---:|
| Splash | Guide | Main |

| 频道列表 | 设置页 | 关于页 |
|:---:|:---:|:---:|
| Channels | Settings | About |

## 🏗️ 技术架构

### 整体架构
```
app/
├── src/main/
│   ├── java/com/elderly/tvassistant/
│   │   ├── MainActivity.kt          # 主界面：视频播放 + 频道切换
│   │   ├── SplashActivity.kt        # 启动页：数据库初始化 + 跳转判断
│   │   ├── AboutActivity.kt         # 关于页面
│   │   ├── BaseActivity.kt          # Activity 基类
│   │   ├── ElderlyTVApplication.kt  # Application：全局字体缩放
│   │   ├── activity/
│   │   │   ├── GuideActivity.kt     # 首次引导页（ViewPager2）
│   │   │   └── SettingsActivity.kt  # 设置页面
│   │   ├── adapter/                 # RecyclerView 适配器
│   │   ├── database/                # Room 数据库（Channel + Favorite）
│   │   ├── fragment/                # Fragment（频道列表、收藏、视频、引导）
│   │   ├── manager/                 # 核心业务管理器
│   │   │   ├── PlayerManager.kt     # WebView 视频播放器
│   │   │   ├── VoiceManager.kt      # 语音识别（在线 + Vosk 离线）
│   │   │   ├── VoskVoiceManager.kt  # Vosk 离线语音引擎
│   │   │   ├── TTSManager.kt        # 语音合成播报
│   │   │   ├── GestureManager.kt    # 手势区域检测
│   │   │   └── TimerManager.kt      # 定时关闭管理
│   │   ├── model/                   # 数据模型（Channel, Favorite, Setting）
│   │   ├── repository/              # 数据仓库层
│   │   ├── viewmodel/               # ViewModel（Channel, Voice, Timer）
│   │   ├── widget/                  # 自定义控件
│   │   │   ├── GestureRelativeLayout.kt  # 手势区域点击布局
│   │   │   ├── ChannelDrawerLayout.kt    # 频道侧边栏
│   │   │   └── TouchInterceptorView.kt   # 触摸拦截（防止 WebView 窃取焦点）
│   │   └── utils/                   # 工具类
│   │       ├── SharedPrefsHelper.kt # SharedPreferences 封装
│   │       ├── FontSizeHelper.kt    # 全局字体缩放
│   │       ├── ChannelDataLoader.kt # 预设频道数据加载
│   │       ├── PermissionHelper.kt  # 权限检查工具
│   │       └── ScreenUtils.kt      # 屏幕工具
│   ├── assets/
│   │   ├── channels.json            # 预置频道数据
│   │   └── Example_CCTV_page.html   # CCTV 页面示例
│   └── res/                         # 布局、图片、字符串等资源
```

### 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| 最低 SDK | Android 7.0 (API 24) |
| 目标 SDK | Android 16 (API 36) |
| 架构 | MVVM (ViewModel + LiveData + Repository) |
| 数据库 | Room (SQLite) |
| 视频播放 | WebView (加载央视网页直播) |
| 语音识别 | Android SpeechRecognizer + Vosk 离线引擎 |
| 语音合成 | Android TextToSpeech |
| 异步 | Kotlin Coroutines |
| 序列化 | Gson |
| 图片加载 | Glide |
| UI 组件 | AndroidX, Material Design, ViewPager2, RecyclerView |

## 🚀 构建与运行

### 环境要求
- Android Studio Hedgehog 或更高版本
- JDK 11+
- Android SDK，compileSdk 36
- Gradle 9.3.1

### 构建步骤

1. **克隆仓库**
   ```bash
   git clone https://github.com/Hanjx-bjtu/Elderly_TV_Assistant.git
   cd Elderly_TV_Assistant
   ```

2. **打开项目**
   - 用 Android Studio 打开项目根目录
   - 等待 Gradle 同步完成

3. **运行应用**
   - 连接 Android 设备或启动模拟器（API 24+）
   - 点击 Run 按钮或执行：
     ```bash
     ./gradlew assembleDebug
     ```

4. **生成 Release APK**
   ```bash
   ./gradlew assembleRelease
   ```
   输出路径：`app/build/outputs/apk/release/`

## 核心模块说明

### 语音识别流程（内层，外层尚未表现）
```
用户点击麦克风按钮
       │
       ▼
  检查录音权限 ──(拒绝)──▶ 引导用户去设置开启
       │
    (已授权)
       │
       ▼
  查找系统语音服务 ──(找到)──▶ 使用 SpeechRecognizer 在线识别
       │
    (未找到)
       │
       ▼
  Vosk 模型已下载？ ──(是)──▶ 使用 Vosk 离线识别
       │
      (否)
       │
       ▼
  Intent 识别可用？ ──(是)──▶ 调用第三方语音应用
       │
      (否)
       │
       ▼
  提示下载 Vosk 离线模型（42MB）
```

### 视频播放方案
采用 WebView 加载央视官网移动端直播页面，通过注入 JavaScript 隐藏页面非视频元素，实现纯净播放体验。WebView 触摸事件被上层 `TouchInterceptorView` 拦截，防止误触网页元素。

### 数据存储
- **Room 数据库**：频道信息（`channel_table`）和收藏记录（`favorite_table`）
- **SharedPreferences**：用户设置（字体大小、TTS 开关、默认频道、首次启动标记等）

## 权限说明

| 权限 | 用途 |
|------|------|
| `INTERNET` | 加载直播视频流和网页 |
| `ACCESS_NETWORK_STATE` | 检测网络连接状态 |
| `RECORD_AUDIO` | 语音识别换台 |
| `VIBRATE` | 换台震动反馈 |
| `SCHEDULE_EXACT_ALARM` | 定时关闭功能 |
| `FOREGROUND_SERVICE` | 前台服务 |
| `POST_NOTIFICATIONS` | 通知权限 (Android 13+) |

## License

本项目仅供学习和研究使用。直播源来自央视官网公开页面，版权归原作者所有。