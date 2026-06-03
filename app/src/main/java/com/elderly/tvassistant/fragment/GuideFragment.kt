package com.elderly.tvassistant.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.elderly.tvassistant.R

/**
 * 引导页Fragment
 * 单个引导页的内容展示
 * 在GuideActivity的ViewPager2中使用
 */
class GuideFragment : Fragment() {

    private var title: String? = null
    private var description: String? = null
    private var iconRes: Int = 0

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_DESC = "description"
        private const val ARG_ICON = "icon"

        fun newInstance(title: String, description: String, iconRes: Int): GuideFragment {
            return GuideFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_DESC, description)
                    putInt(ARG_ICON, iconRes)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE)
            description = it.getString(ARG_DESC)
            iconRes = it.getInt(ARG_ICON, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guide, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageView>(R.id.iv_guide_icon).setImageResource(iconRes)
        view.findViewById<TextView>(R.id.tv_guide_title).text = title
        view.findViewById<TextView>(R.id.tv_guide_desc).text = description
    }
}
