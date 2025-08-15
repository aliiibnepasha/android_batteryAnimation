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
import com.lowbyte.battery.animation.databinding.ItemSmallEmojiBinding

class AllEmojiBatteriesAdapter(
    private val onItemClick: (position: Int, fileItem: FileItem, isRewardAd: Boolean) -> Unit,
    private val categoryName: String ,
    private val folderName: String ,
) : ListAdapter<FileItem, RecyclerView.ViewHolder>(DiffCallback())
{

    private enum class VT { HEADER, ITEM, FOOTER }
    override fun getItemCount(): Int = super.getItemCount()

    override fun getItemViewType(position: Int): Int = VT.ITEM.ordinal


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VT.ITEM.ordinal -> {
                val binding = ItemSmallEmojiBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ItemVH(binding)
            }

            else -> {
                val binding = ItemSmallEmojiBinding.inflate(
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

    inner class ItemVH(private val binding: ItemSmallEmojiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FileItem, dataIndex: Int) {
            val context = binding.root.context
            val isReward = ((dataIndex + 1) % 4 == 0)

            binding.root.setOnClickListener {
                onItemClick(dataIndex, item, isReward)
            }

            binding.watchAdItem.visibility = if (isReward) View.VISIBLE else View.INVISIBLE

            val makeUrl = "https://theswiftvision.com/batteryEmoji/$categoryName/$folderName/${item.name}"

            // Start shimmer
            binding.shimmerLayout.startShimmer()
            binding.shimmerLayout.visibility = View.VISIBLE
            Glide.with(context)
                .load(makeUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.visibility = View.GONE
                        return false // let Glide set the image
                    }
                })
                .into(binding.widgetPreview)
        }

    }

    private class DiffCallback : DiffUtil.ItemCallback<FileItem>() {
        override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem) = oldItem == newItem
        override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem) = oldItem == newItem
    }
}