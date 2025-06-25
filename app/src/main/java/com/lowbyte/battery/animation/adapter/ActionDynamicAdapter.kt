package com.lowbyte.battery.animation.adapter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.databinding.ItemActionDynamicBinding
import com.lowbyte.battery.animation.databinding.ItemActionScrollBinding
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

data class ActionDynamicItem(val label: String, val actionName: String)

class ActionDynamicAdapter(
    private val items: List<ActionDynamicItem>,
    private val onItemClick: (position: Int, label: String) -> Unit
) : RecyclerView.Adapter<ActionDynamicAdapter.ActionScrollViewHolder>() {
    private lateinit var preferences: AppPreferences

    inner class ActionScrollViewHolder(val binding: ItemActionDynamicBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ActionDynamicItem, position: Int) {
            binding.enableDynamicFeature.isChecked = preferences.getBoolean(item.actionName, false) == true
            binding.titleDynamicFeature.text = item.label
            binding.root.setOnClickListener {

            }
            binding.enableDynamicFeature.setOnCheckedChangeListener { _, isChecked ->

                preferences.setBoolean(item.actionName,isChecked)
                onItemClick(position, item.label)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionScrollViewHolder {
        val binding = ItemActionDynamicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        preferences = AppPreferences.getInstance(parent.context)
        return ActionScrollViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActionScrollViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size
}