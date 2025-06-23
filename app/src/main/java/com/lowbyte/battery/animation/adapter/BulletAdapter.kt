package com.lowbyte.battery.animation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.model.BulletItem

class BulletAdapter(private val items: List<BulletItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TITLE = 0
        private const val TYPE_DESC = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is BulletItem.Title -> TYPE_TITLE
            is BulletItem.Description -> TYPE_DESC
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_TITLE) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bullet_title, parent, false)
            TitleViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bullet_description, parent, false)
            DescViewHolder(view)
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is BulletItem.Title -> (holder as TitleViewHolder).bind(item)
            is BulletItem.Description -> (holder as DescViewHolder).bind(item)
        }
    }

    class TitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.textViewTitle)
        fun bind(item: BulletItem.Title) {
            textView.text = item.text
        }
    }

    class DescViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.textViewDesc)
        fun bind(item: BulletItem.Description) {
            textView.text = item.text
        }
    }
}