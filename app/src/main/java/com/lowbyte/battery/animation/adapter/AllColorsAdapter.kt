package com.lowbyte.battery.animation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.databinding.ItemColorsBinding

class AllColorsAdapter(
    private val onItemClick: (position: Int, colorCode: Int, isRewardAd: Boolean) -> Unit,
) : ListAdapter<Int, RecyclerView.ViewHolder>(DiffCallback())
{

    private enum class VT { HEADER, ITEM, FOOTER }
    override fun getItemCount(): Int = super.getItemCount()

    override fun getItemViewType(position: Int): Int = VT.ITEM.ordinal


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VT.ITEM.ordinal -> {
                val binding = ItemColorsBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ItemVH(binding)
            }

            else -> {
                val binding = ItemColorsBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ItemVH(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemVH -> {
                val dataIndex = position
                holder.bind(getItem(dataIndex), dataIndex)
            }

        }
    }

    inner class ItemVH(private val binding: ItemColorsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(colorCode: Int, dataIndex: Int) {
            // Pick color from list based on position
            binding.widgetPreview.setBackgroundColor(colorCode)

            binding.root.setOnClickListener {
                onItemClick(dataIndex, colorCode, false)
            }
        }
    }


    private class DiffCallback : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
        override fun areContentsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
    }
}