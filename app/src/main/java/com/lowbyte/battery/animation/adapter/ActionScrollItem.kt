package com.lowbyte.battery.animation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.databinding.ItemActionScrollBinding

data class ActionScrollItem(val label: String)

class ActionScrollAdapter(
    private val items: List<ActionScrollItem>
) : RecyclerView.Adapter<ActionScrollAdapter.ActionScrollViewHolder>() {

    inner class ActionScrollViewHolder(val binding: ItemActionScrollBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionScrollViewHolder {
        val binding = ItemActionScrollBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActionScrollViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActionScrollViewHolder, position: Int) {
        holder.binding.actionScrollLabel.text = items[position].label
    }

    override fun getItemCount() = items.size
}