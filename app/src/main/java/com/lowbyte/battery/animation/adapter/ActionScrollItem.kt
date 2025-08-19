package com.lowbyte.battery.animation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ItemActionScrollBinding

data class ActionScrollItem(val label: String, val actionName: String)

class ActionScrollAdapter(
    private val items: List<ActionScrollItem>,
    private val onItemClick: (position: Int, label: String) -> Unit
) : RecyclerView.Adapter<ActionScrollAdapter.ActionScrollViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION  // Store selected item index

    inner class ActionScrollViewHolder(val binding: ItemActionScrollBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ActionScrollItem, position: Int) {
            binding.actionScrollLabel.text = item.label

            // ✅ Only one radio can be checked
            binding.actionScrollLabel.isChecked = (position == selectedPosition)

            // ✅ Change text color based on selection
            val colorRes =
                if (binding.actionScrollLabel.isChecked) R.color.gradientStart // purple selected
                else R.color.titleSubColor // gray unselected

            binding.actionScrollLabel.setTextColor(
                binding.root.context.getColor(colorRes)
            )

            // ✅ Handle click
            binding.actionScrollLabel.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position

                // refresh UI for both old and new selection
                if (previousPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousPosition)
                }
                notifyItemChanged(selectedPosition)

                onItemClick(position, item.label)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionScrollViewHolder {
        val binding = ItemActionScrollBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActionScrollViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActionScrollViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size
}
