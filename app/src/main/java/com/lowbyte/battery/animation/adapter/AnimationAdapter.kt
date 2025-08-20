package com.lowbyte.battery.animation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.databinding.ItemAllAnimationBinding
import com.lowbyte.battery.animation.utils.AppPreferences

class AnimationAdapter(
    private val onItemClick: (Int, String,isRewardAd:Boolean) -> Unit
) : ListAdapter<String, AnimationAdapter.ViewHolder>(DiffCallback()) {
    private lateinit var preferences: AppPreferences

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAllAnimationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        preferences = AppPreferences.getInstance(parent.context)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position),position)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.releaseAnimation()
    }

    inner class ViewHolder(
        private val binding: ItemAllAnimationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var lastAnimationName: String? = null


        fun bind(item: String,position: Int) {

            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {

                    if (preferences.getBoolean("RewardEarned", false) == false) {
                        val cycleIndex = position % 6 // total cycle length = 2 false + 4 true
                        val isTrue = cycleIndex >= 2  // index 0,1 → false; 2,3,4,5 → true
                        onItemClick(position, getItem(position), isTrue)
                    } else {
                        onItemClick(position, getItem(position),false)
                    }

                    // Optionally play animation on click
                    binding.lottiAnimation.playAnimation()
                }
            }

            val cycleIndex = position % 6 // total cycle length = 2 false + 4 true
            val isTrue = cycleIndex >= 2  // index 0,1 → false; 2,3,4,5 → true
            if (isTrue && preferences.getBoolean("RewardEarned",false) == false) {
                binding.watchAdItem.visibility = View.VISIBLE
            } else {
                binding.watchAdItem.visibility = View.INVISIBLE
            }


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