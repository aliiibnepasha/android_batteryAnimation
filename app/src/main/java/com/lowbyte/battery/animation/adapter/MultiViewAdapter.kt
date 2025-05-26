package com.lowbyte.battery.animation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.model.MultiViewItem

class MultiViewAdapter(
    private val data: List<MultiViewItem>,
    private val onChildItemClick: (Int) -> Unit,
    private val onChildViewAllClick: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TITLE = 0
        private const val TYPE_LIST = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is MultiViewItem.TitleItem -> TYPE_TITLE
            is MultiViewItem.ListItem -> TYPE_LIST
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TITLE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.main_item_title, parent, false)
                TitleViewHolder(view)
            }
            TYPE_LIST -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.main_item_rv, parent, false)
                HorizontalListViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = data[position]) {
            is MultiViewItem.TitleItem -> (holder as TitleViewHolder).bind(item,position)
            is MultiViewItem.ListItem -> (holder as HorizontalListViewHolder).bind(item.items)
        }
    }

    inner class TitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: MultiViewItem.TitleItem, position: Int) {
            itemView.findViewById<TextView>(R.id.textLeft).text = item.title
            itemView.findViewById<TextView>(R.id.textRight).setOnClickListener {
                onChildViewAllClick.invoke(position)
            }
        }
    }

    inner class HorizontalListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerChild)

        fun bind(items: List<Int>) {
            recyclerView.layoutManager = LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)
            recyclerView.adapter = ChildItemAdapter(items, onChildItemClick)
        }
    }
}