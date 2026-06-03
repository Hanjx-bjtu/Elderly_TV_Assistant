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
import com.elderly.tvassistant.adapter.FavoriteAdapter
import com.elderly.tvassistant.model.Channel

/**
 * 收藏Fragment
 * 显示用户收藏的频道列表
 */
class FavoriteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private var adapter: FavoriteAdapter? = null
    private var onChannelClickListener: ((Channel) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recycler_favorite_list)
        emptyView = view.findViewById(R.id.tv_empty_favorite)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FavoriteAdapter { channel ->
            onChannelClickListener?.invoke(channel)
        }
        recyclerView.adapter = adapter
    }

    /**
     * 设置收藏频道数据
     */
    fun setFavorites(channels: List<Channel>) {
        adapter?.submitList(channels)
        if (channels.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    fun setOnChannelClickListener(listener: (Channel) -> Unit) {
        onChannelClickListener = listener
    }
}
