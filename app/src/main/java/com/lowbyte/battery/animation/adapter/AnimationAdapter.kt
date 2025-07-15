package com.lowbyte.battery.animation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
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

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.releaseAnimation()
    }

    inner class ViewHolder(
        private val binding: ItemAllAnimationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var lastAnimationName: String? = null

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(position, getItem(position))

                    // Optionally play animation on click
                    binding.lottiAnimation.playAnimation()
                }
            }
        }

        fun bind(item: String) {
            if (item != lastAnimationName) {
                val context = binding.root.context
                val resId = context.resources.getIdentifier(item, "raw", context.packageName)

                if (resId != 0) {
                    binding.lottiAnimation.setAnimation(resId)
                    binding.lottiAnimation.repeatCount = 0 // no looping
                    binding.lottiAnimation.progress = 0f    // reset frame
                    binding.lottiAnimation.pauseAnimation() // do not autoplay
                    lastAnimationName = item
                } else {
                    Log.e("AnimationAdapter", "Lottie resource not found for name: $item")
                    binding.lottiAnimation.cancelAnimation()
                    binding.lottiAnimation.clearAnimation()
                    lastAnimationName = null
                }
            }
        }

        fun releaseAnimation() {
            binding.lottiAnimation.cancelAnimation()
            binding.lottiAnimation.clearAnimation()
            binding.lottiAnimation.progress = 0f
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
}