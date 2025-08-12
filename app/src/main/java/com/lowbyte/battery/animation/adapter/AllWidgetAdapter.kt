package com.lowbyte.battery.animation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ItemAllWidgetBinding

class AllWidgetAdapter(
    private val onItemClick: (position: Int, label: String) -> Unit,
    private val headerHeightPx: Int = 0, // optional spacer at top
    private val footerHeightPx: Int = 0  // optional spacer at bottom
) : ListAdapter<String, RecyclerView.ViewHolder>(DiffCallback()) {

    private enum class VT { HEADER, ITEM, FOOTER }

    override fun getItemCount(): Int = super.getItemCount() + 2 // header + footer

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> VT.HEADER.ordinal
        itemCount - 1 -> VT.FOOTER.ordinal
        else -> VT.ITEM.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {

            VT.HEADER.ordinal -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_spacer, parent, false)
                // enforce header height
                val lp = v.layoutParams
                if (lp == null) {
                    v.layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        if (headerHeightPx > 0) headerHeightPx else ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                } else {
                    lp.height = if (headerHeightPx > 0) headerHeightPx else lp.height
                    v.layoutParams = lp
                }
                SpacerVH(v)
            }

            VT.FOOTER.ordinal -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_spacer, parent, false)
                // enforce footer height
                val lp = v.layoutParams
                if (lp == null) {
                    v.layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        if (footerHeightPx > 0) footerHeightPx else ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                } else {
                    lp.height = if (footerHeightPx > 0) footerHeightPx else lp.height
                    v.layoutParams = lp
                }
                SpacerVH(v)
            }

            else -> {
                val binding = ItemAllWidgetBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ItemVH(binding).apply {
                    // keep your original click behavior
                    binding.root.setOnClickListener {
                        val dataIndex = bindingAdapterPosition - 1 // shift by header
                        if (dataIndex in 0 until currentList.size) {
                            onItemClick(dataIndex, getItem(dataIndex))
                        }
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemVH -> {
                val dataIndex = position - 1 // shift by header
                holder.bind(getItem(dataIndex))
            }
            is SpacerVH -> {
                // ensure height sticks
                val desired = when (getItemViewType(position)) {
                    VT.HEADER.ordinal -> headerHeightPx
                    VT.FOOTER.ordinal -> footerHeightPx
                    else -> 0
                }
                if (desired > 0) {
                    val lp = (holder.itemView.layoutParams as? RecyclerView.LayoutParams)
                        ?: RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, desired
                        )
                    lp.height = desired
                    holder.itemView.layoutParams = lp
                    holder.itemView.minimumHeight = desired
                }
            }
        }
    }

    inner class ItemVH(
        private val binding: ItemAllWidgetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String) {
            val context = binding.root.context
            val resId = context.resources.getIdentifier(item, "drawable", context.packageName)

            if (resId != 0) {
                binding.widgetPreview.setImageResource(resId)
            } else {
                binding.widgetPreview.setImageResource(R.drawable.emoji_3) // fallback
            }
        }
    }

    class SpacerVH(view: View) : RecyclerView.ViewHolder(view)

    private class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
}