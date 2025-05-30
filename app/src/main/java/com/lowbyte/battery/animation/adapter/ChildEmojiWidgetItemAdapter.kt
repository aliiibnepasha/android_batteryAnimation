package com.lowbyte.battery.animation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.MainItemRvItemBinding

class ChildEmojiWidgetItemAdapter(
    private val items: List<String>,
    private val onChildItemClick: (Int, String, Int) -> Unit,
    private val parentPosition: Int
) : RecyclerView.Adapter<ChildEmojiWidgetItemAdapter.ChildViewHolder>() {

    inner class ChildViewHolder(private val binding: MainItemRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(drawableName: String) {
            val context = binding.root.context
            val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

            if (resId != 0) {
                binding.widgetPreview.setImageResource(resId)
            } else {
                binding.widgetPreview.setImageResource(R.drawable.emoji_4)
            }


            binding.root.setOnClickListener {
                onChildItemClick(adapterPosition, drawableName, parentPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val binding = MainItemRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChildViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
