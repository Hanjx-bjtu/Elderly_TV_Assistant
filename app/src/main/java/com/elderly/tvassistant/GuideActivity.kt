package com.elderly.tvassistant.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.elderly.tvassistant.MainActivity
import com.elderly.tvassistant.R
import com.google.android.material.button.MaterialButton

/**
 * 引导页Activity
 * 首次启动时展示3页图文引导，介绍核心操作方法
 * 用户看完后进入主界面
 */
class GuideActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var indicatorLayout: LinearLayout
    private lateinit var skipButton: MaterialButton
    private lateinit var nextButton: MaterialButton

    /** 引导页数据 */
    private val guidePages = listOf(
        GuidePageData(
            title = "左右点击 换台",
            description = "点击屏幕左侧切换到上一个频道，点击右侧切换到下一个频道，简单方便",
            iconRes = R.drawable.ic_swipe
        ),
        GuidePageData(
            title = "语音换台 更方便",
            description = "点击麦克风按钮，说出频道名称，如\"中央一台\"，即可快速切换频道",
            iconRes = R.drawable.ic_voice
        ),
        GuidePageData(
            title = "定时关闭 省心",
            description = "可以设置定时自动关闭，不用担心看太久忘记关电视",
            iconRes = R.drawable.ic_timer
        ),
        GuidePageData(
            title = "大字体 好阅读",
            description = "超大字体设计，清晰易读，专为长辈打造",
            iconRes = R.drawable.ic_font
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        viewPager = findViewById(R.id.view_pager)
        indicatorLayout = findViewById(R.id.layout_indicator)
        skipButton = findViewById(R.id.btn_skip)
        nextButton = findViewById(R.id.btn_next)

        setupViewPager()
        setupButtons()
    }

    private fun setupViewPager() {
        val adapter = GuidePagerAdapter(guidePages)
        viewPager.adapter = adapter

        // 页面切换时更新指示器
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
                if (position == guidePages.size - 1) {
                    nextButton.text = "开始使用"
                } else {
                    nextButton.text = "下一步"
                }
            }
        })

        // 创建指示器点
        createIndicators()
    }

    private fun setupButtons() {
        skipButton.setOnClickListener {
            navigateToMain()
        }

        nextButton.setOnClickListener {
            val nextItem = viewPager.currentItem + 1
            if (nextItem < guidePages.size) {
                viewPager.currentItem = nextItem
            } else {
                navigateToMain()
            }
        }
    }

    private fun createIndicators() {
        indicatorLayout.removeAllViews()
        for (i in guidePages.indices) {
            val dot = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 0, 8, 0)
                }
                setImageDrawable(
                    ContextCompat.getDrawable(
                        this@GuideActivity,
                        if (i == 0) R.drawable.ic_dot_selected else R.drawable.ic_dot_normal
                    )
                )
            }
            indicatorLayout.addView(dot)
        }
    }

    private fun updateIndicators(position: Int) {
        for (i in 0 until indicatorLayout.childCount) {
            val dot = indicatorLayout.getChildAt(i) as ImageView
            dot.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    if (i == position) R.drawable.ic_dot_selected else R.drawable.ic_dot_normal
                )
            )
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    /** 引导页数据类 */
    data class GuidePageData(
        val title: String,
        val description: String,
        val iconRes: Int
    )

    /** 引导页适配器 */
    inner class GuidePagerAdapter(
        private val pages: List<GuidePageData>
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<GuideViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_guide, parent, false)
            return GuideViewHolder(view)
        }

        override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
            holder.bind(pages[position])
        }

        override fun getItemCount(): Int = pages.size
    }

    inner class GuideViewHolder(itemView: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.iv_guide_icon)
        private val titleView: TextView = itemView.findViewById(R.id.tv_guide_title)
        private val descView: TextView = itemView.findViewById(R.id.tv_guide_desc)

        fun bind(page: GuidePageData) {
            iconView.setImageResource(page.iconRes)
            titleView.text = page.title
            descView.text = page.description
        }
    }
}
