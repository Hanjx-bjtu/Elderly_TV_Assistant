package com.elderly.tvassistant.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elderly.tvassistant.R
import com.elderly.tvassistant.model.Channel


class FavoriteAdapter(
    private val onChannelClick: (Channel) -> Unit
) : ListAdapter<Channel, FavoriteAdapter.FavoriteViewHolder>(FavoriteDiffCallback()) {

    companion object {
        class FavoriteDiffCallback : DiffUtil.ItemCallback<Channel>() {
            override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val channel = getItem(position)
        holder.bind(channel)
    }

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.tv_favorite_name)
        private val iconView: ImageView = itemView.findViewById(R.id.iv_favorite_icon)

        fun bind(channel: Channel) {
            val displayText = channel.displayName ?: channel.name
            nameText.text = displayText

            // 设置收藏星标图标
            iconView.setImageResource(R.drawable.ic_star_filled)

            itemView.setOnClickListener {
                onChannelClick(channel)
            }

            // 确保最小点击区域72dp
            itemView.minimumHeight = (72 * itemView.context.resources.displayMetrics.density).toInt()
        }
    }
}
