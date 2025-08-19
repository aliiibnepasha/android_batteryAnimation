package com.lowbyte.battery.animation.adapter

import FileItem
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ItemAllEmojiBinding
import com.lowbyte.battery.animation.model.IconItem

class AllIconsAdapter(
    private val onItemClick: (position: Int, fileItem: IconItem, isRewardAd: Boolean) -> Unit,
    private val headerHeightPx: Int = 0,
    private val footerHeightPx: Int = 0,
) : ListAdapter<IconItem, RecyclerView.ViewHolder>(DiffCallback())
{

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

                if (headerHeightPx > 0) {
                    val lp = v.layoutParams
                    if (lp == null) {
                        v.layoutParams = RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            headerHeightPx
                        )
                    } else {
                        lp.height = headerHeightPx
                        v.layoutParams = lp
                    }
                }
                SpacerVH(v) // you can also make a dedicated HeaderVH
            }

            VT.FOOTER.ordinal -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_spacer, parent, false)

                if (headerHeightPx > 0) {
                    val lp = v.layoutParams
                    if (lp == null) {
                        v.layoutParams = RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            footerHeightPx
                        )
                    } else {
                        lp.height = footerHeightPx
                        v.layoutParams = lp
                    }
                }
                SpacerVH(v) // or FooterVH if different handling
            }

            else -> {
                val binding = ItemAllEmojiBinding.inflate(
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
                // shift by 1 because of header
                val dataIndex = position - 1
                holder.bind(getItem(dataIndex), dataIndex)
            }

            is SpacerVH -> { /* nothing */
            }
        }
    }

    inner class ItemVH(private val binding: ItemAllEmojiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: IconItem, dataIndex: Int) {
            val context = binding.root.context

            val isReward = ((dataIndex + 1) % 4 == 0)

            binding.root.setOnClickListener {
                onItemClick(dataIndex, item, isReward)
            }

            binding.watchAdItem.visibility = if (isReward) View.VISIBLE else View.INVISIBLE

            binding.shimmerLayout.startShimmer()
            binding.shimmerLayout.visibility = View.VISIBLE

        }

    }

    class SpacerVH(view: View) : RecyclerView.ViewHolder(view)

    private class DiffCallback : DiffUtil.ItemCallback<IconItem>() {
        override fun areItemsTheSame(oldItem: IconItem, newItem: IconItem) = oldItem == newItem
        override fun areContentsTheSame(oldItem: IconItem, newItem: IconItem) = oldItem == newItem
    }
}