package com.lowbyte.battery.animation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.databinding.MainItemRvBinding
import com.lowbyte.battery.animation.databinding.MainItemTitleBinding
import com.lowbyte.battery.animation.model.MultiViewItem

class MultiViewAdapter(
    private val data: List<MultiViewItem>,
    private val onChildItemClick: (Int, String, Int) -> Unit,
    private val onChildViewAllClick: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TITLE = 0
        private const val TYPE_EMOJI_OR_WIDGET_LIST = 1
        private const val TYPE_ANIMATION_LIST = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is MultiViewItem.TitleItem -> TYPE_TITLE
            is MultiViewItem.ListEmojiOrWidgetItem -> TYPE_EMOJI_OR_WIDGET_LIST
            is MultiViewItem.ListAnimationItem -> TYPE_ANIMATION_LIST
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TITLE -> {
                val binding = MainItemTitleBinding.inflate(inflater, parent, false)
                TitleViewHolder(binding)
            }
            TYPE_EMOJI_OR_WIDGET_LIST -> {
                val binding = MainItemRvBinding.inflate(inflater, parent, false)
                HorizontalEmojiOrWidgetListViewHolder(binding)
            }

            TYPE_ANIMATION_LIST -> {
                val binding = MainItemRvBinding.inflate(inflater, parent, false)
                HorizontalAnimationListViewHolder(binding)
            }


            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = data[position]) {
            is MultiViewItem.TitleItem -> (holder as TitleViewHolder).bind(item, position)
            is MultiViewItem.ListEmojiOrWidgetItem -> (holder as HorizontalEmojiOrWidgetListViewHolder).bind(item.items, position)
            is MultiViewItem.ListAnimationItem -> (holder as HorizontalAnimationListViewHolder).bind(item.items, position)
        }
    }

    inner class TitleViewHolder(private val binding: MainItemTitleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MultiViewItem.TitleItem, position: Int) {
            binding.textLeft.text = item.title
            binding.textRight.setOnClickListener {
                onChildViewAllClick.invoke(position)
            }
        }
    }

    inner class HorizontalEmojiOrWidgetListViewHolder(private val binding: MainItemRvBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(items: List<String>, parentPosition: Int) {
            binding.recyclerChild.layoutManager = LinearLayoutManager(binding.root.context, RecyclerView.HORIZONTAL, false)
            binding.recyclerChild.adapter = ChildEmojiWidgetItemAdapter(items, onChildItemClick, parentPosition)
        }
    }

      inner class HorizontalAnimationListViewHolder(private val binding: MainItemRvBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(items: List<String>, parentPosition: Int) {
            binding.recyclerChild.layoutManager = LinearLayoutManager(binding.root.context, RecyclerView.HORIZONTAL, false)
            binding.recyclerChild.adapter = ChildAnimationItemAdapter(items, onChildItemClick, parentPosition)
        }
    }




}