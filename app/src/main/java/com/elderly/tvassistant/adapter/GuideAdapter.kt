package com.elderly.tvassistant.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elderly.tvassistant.R

class GuideAdapter(
    private val pages: List<GuidePage>
) : RecyclerView.Adapter<GuideAdapter.GuideViewHolder>() {

    data class GuidePage(
        val title: String,
        val description: String,
        val iconRes: Int
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_guide, parent, false)
        return GuideViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int = pages.size

    inner class GuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.iv_guide_icon)
        private val titleView: TextView = itemView.findViewById(R.id.tv_guide_title)
        private val descView: TextView = itemView.findViewById(R.id.tv_guide_desc)

        fun bind(page: GuidePage) {
            iconView.setImageResource(page.iconRes)
            titleView.text = page.title
            descView.text = page.description
        }
    }
}
