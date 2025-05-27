package com.lowbyte.battery.animation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.databinding.ItemCustomeIconsBinding
import com.lowbyte.battery.animation.model.CustomIconGridItem

 class CustomIconGridAdapter(
    private val items: List<CustomIconGridItem>,
    private val onItemClick: (position: Int, label: String) -> Unit
) : RecyclerView.Adapter<CustomIconGridAdapter.GridViewHolder>() {

    inner class GridViewHolder(private val binding: ItemCustomeIconsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CustomIconGridItem, position: Int) {
            binding.customIcon.setImageResource(item.imageRes)
            binding.customIconLabel.text = item.label
            binding.root.setOnClickListener {
                onItemClick(position, item.label)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val binding = ItemCustomeIconsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}