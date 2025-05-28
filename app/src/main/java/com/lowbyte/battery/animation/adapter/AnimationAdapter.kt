package com.lowbyte.battery.animation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.databinding.ItemAllAnimationBinding

class AnimationAdapter(
    private val onItemClick: (Int, String) -> Unit
) : ListAdapter<String, AnimationAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAllAnimationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemAllAnimationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClick(adapterPosition, getItem(adapterPosition))
            }
        }

        fun bind(item: String) {
            val context = binding.root.context
            val resId = context.resources.getIdentifier(item, "raw", context.packageName)

            if (resId != 0) {
                binding.lottiAnimation.setAnimation(resId)
            } else {
                Log.e("AnimationAdapter", "Lottie resource not found for name: $item")
                binding.lottiAnimation.cancelAnimation()
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
}