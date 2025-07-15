package com.lowbyte.battery.animation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ItemAllEmojiBinding

class AllEmojiAdapter(
    private val onItemClick: (position: Int, label: String,isRewardAd:Boolean) -> Unit
) : ListAdapter<String, AllEmojiAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAllEmojiBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ViewHolder(
        private val binding: ItemAllEmojiBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /*  init {
              binding.root.setOnClickListener {
                  onItemClick(adapterPosition, getItem(adapterPosition))
              }
          }*/

        fun bind(item: String, position: Int) {
            val context = binding.root.context
            val resId = context.resources.getIdentifier(item, "drawable", context.packageName)

            binding.root.setOnClickListener {
                if ((position + 1) % 4 == 0) {
                    onItemClick(position, getItem(position),true)
                } else {
                    onItemClick(position, getItem(position),false)
                }

            }

            if ((position + 1) % 4 == 0) {
                binding.watchAdItem.visibility = View.VISIBLE
            } else {
                binding.watchAdItem.visibility = View.INVISIBLE
            }

            if (resId != 0) {
                binding.widgetPreview.setImageResource(resId)
            } else {
                binding.widgetPreview.setImageResource(R.drawable.emoji_2) // fallback image
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
} 