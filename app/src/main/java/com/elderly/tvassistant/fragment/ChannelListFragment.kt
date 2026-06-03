package com.elderly.tvassistant.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elderly.tvassistant.R
import com.elderly.tvassistant.adapter.ChannelAdapter
import com.elderly.tvassistant.model.Channel

/**
 * 频道列表Fragment
 * 显示所有可用频道的列表
 * 可以嵌入侧边栏或对话框中使用
 */
class ChannelListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private var adapter: ChannelAdapter? = null
    private var onChannelClickListener: ((Channel) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_channel_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recycler_channel_list)
        emptyView = view.findViewById(R.id.tv_empty)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChannelAdapter { channel ->
            onChannelClickListener?.invoke(channel)
        }
        recyclerView.adapter = adapter
    }

    /**
     * 设置频道数据
     */
    fun setChannels(channels: List<Channel>) {
        adapter?.submitList(channels)
        if (channels.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    /**
     * 设置频道点击监听
     */
    fun setOnChannelClickListener(listener: (Channel) -> Unit) {
        onChannelClickListener = listener
    }

    /**
     * 高亮当前播放的频道
     */
    fun setCurrentChannel(channelId: Int) {
        adapter?.setCurrentChannelId(channelId)
    }
}
