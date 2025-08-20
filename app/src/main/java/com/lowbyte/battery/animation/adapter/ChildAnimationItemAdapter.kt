package com.lowbyte.battery.animation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.databinding.MainItemRvAnimItemBinding
import com.lowbyte.battery.animation.utils.AppPreferences

class ChildAnimationItemAdapter(
    private val items: List<String>,
    private val onChildItemClick: (Int, String, Int, isRewardAd: Boolean) -> Unit,
    private val parentPosition: Int
) : RecyclerView.Adapter<ChildAnimationItemAdapter.ChildViewHolder>() {
    private lateinit var preferences: AppPreferences

    inner class ChildViewHolder(private val binding: MainItemRvAnimItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(drawableName: String, position: Int) {
            val context = binding.root.context

            val resId = context.resources.getIdentifier(drawableName, "raw", context.packageName)

            if (resId != 0) {
                binding.animationPreview.setAnimation(resId)
            } else {
                Log.e("AnimationAdapter", "Lottie resource not found for name: $drawableName")
                binding.animationPreview.cancelAnimation()
            }
            val cycleIndex = position % 4 // total cycle length = 2 false + 4 true
            val isTrue = cycleIndex < 2  // index 0,1 → false; 2,3,4,5 → true
            if (isTrue && preferences.getBoolean("RewardEarned", false) == false) {
                binding.watchAdItem.visibility = View.VISIBLE
            } else {
                binding.watchAdItem.visibility = View.INVISIBLE
            }
            binding.root.setOnClickListener {
                if (preferences.getBoolean("RewardEarned", false) == false) {
                    val cycleIndex = position % 4 // total cycle length = 2 false + 4 true
                    val isTrue = cycleIndex < 2  // index 0,1 → false; 2,3,4,5 → true
                    onChildItemClick(position, drawableName, parentPosition, isTrue)
                } else {
                    onChildItemClick(position, drawableName, parentPosition, false)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val binding = MainItemRvAnimItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        preferences = AppPreferences.getInstance(parent.context)

        return ChildViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size
}