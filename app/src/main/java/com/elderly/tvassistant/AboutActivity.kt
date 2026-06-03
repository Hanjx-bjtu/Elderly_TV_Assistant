package com.elderly.tvassistant

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * 关于页面Activity
 * 显示APP版本信息、适老化设计理念、帮助联系方式
 */
class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        supportActionBar?.title = "关于"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置版本信息
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName
            findViewById<TextView>(R.id.tv_version).text = "版本 $versionName"
        } catch (e: Exception) {
            findViewById<TextView>(R.id.tv_version).text = "版本 1.0"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
