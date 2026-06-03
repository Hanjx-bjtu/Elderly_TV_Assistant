package com.elderly.tvassistant.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elderly.tvassistant.R
import com.elderly.tvassistant.model.Channel

/**
 * 频道列表适配器
 * 使用ListAdapter + DiffUtil实现高效列表更新
 * 适配适老化设计：大字体、大点击区域
 */
class ChannelAdapter(
    private val onChannelClick: (Channel) -> Unit
) : ListAdapter<Channel, ChannelAdapter.ChannelViewHolder>(ChannelDiffCallback()) {

    private var currentChannelId: Int = -1

    companion object {
        class ChannelDiffCallback : DiffUtil.ItemCallback<Channel>() {
            override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = getItem(position)
        holder.bind(channel, channel.id == currentChannelId)
    }

    fun setCurrentChannelId(channelId: Int) {
        val oldId = currentChannelId
        currentChannelId = channelId
        if (oldId != channelId) {
            notifyDataSetChanged()
        }
    }

    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.tv_channel_name)
        private val indexText: TextView = itemView.findViewById(R.id.tv_channel_index)

        fun bind(channel: Channel, isCurrentChannel: Boolean) {
            val displayText = channel.displayName ?: channel.name
            nameText.text = displayText

            // 显示序号
            val position = bindingAdapterPosition + 1
            indexText.text = "$position"

            // 高亮当前频道
            if (isCurrentChannel) {
                itemView.setBackgroundResource(R.drawable.bg_channel_selected)
                nameText.setTextColor(itemView.context.getColor(R.color.colorAccent))
            } else {
                itemView.setBackgroundResource(android.R.color.transparent)
                nameText.setTextColor(itemView.context.getColor(R.color.colorTextPrimary))
            }

            // 点击事件
            itemView.setOnClickListener {
                onChannelClick(channel)
            }

            // 确保最小点击区域72dp
            itemView.minimumHeight = (72 * itemView.context.resources.displayMetrics.density).toInt()
        }
    }
}
