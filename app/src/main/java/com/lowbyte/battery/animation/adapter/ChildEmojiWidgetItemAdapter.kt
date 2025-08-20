package com.lowbyte.battery.animation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.MainItemRvItemBinding
import com.lowbyte.battery.animation.utils.AppPreferences

class ChildEmojiWidgetItemAdapter(
    private val items: List<String>,
    private val onChildItemClick: (Int, String, Int, isRewardAd: Boolean) -> Unit,
    private val parentPosition: Int
) : RecyclerView.Adapter<ChildEmojiWidgetItemAdapter.ChildViewHolder>() {
    private lateinit var preferences: AppPreferences

    inner class ChildViewHolder(private val binding: MainItemRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(drawableName: String, position: Int) {
            val context = binding.root.context
            val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

            if (resId != 0) {
                binding.widgetPreview.setImageResource(resId)
            } else {
                binding.widgetPreview.setImageResource(R.drawable.emoji_4)
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
        val binding = MainItemRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        preferences = AppPreferences.getInstance(parent.context)

        return ChildViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}
