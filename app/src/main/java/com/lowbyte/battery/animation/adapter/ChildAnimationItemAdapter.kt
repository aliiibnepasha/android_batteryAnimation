package com.lowbyte.battery.animation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.MainItemRvAnimItemBinding
import com.lowbyte.battery.animation.databinding.MainItemRvItemBinding

class ChildAnimationItemAdapter(
    private val items: List<String>,
    private val onChildItemClick: (Int, String, Int) -> Unit,
    private val parentPosition: Int
) : RecyclerView.Adapter<ChildAnimationItemAdapter.ChildViewHolder>() {

    inner class ChildViewHolder(private val binding: MainItemRvAnimItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(drawableName: String) {
            val context = binding.root.context

            val resId = context.resources.getIdentifier(drawableName, "raw", context.packageName)

            if (resId != 0) {
                binding.animationPreview.setAnimation(resId)
            } else {
                Log.e("AnimationAdapter", "Lottie resource not found for name: $drawableName")
                binding.animationPreview.cancelAnimation()
            }

            binding.root.setOnClickListener {
                onChildItemClick(adapterPosition, drawableName, parentPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val binding = MainItemRvAnimItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChildViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}