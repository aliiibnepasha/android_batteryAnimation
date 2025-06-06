package com.lowbyte.battery.animation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ItemLanguageBinding
import com.lowbyte.battery.animation.model.Language

class LanguageAdapter(
    private val items: List<Language>,
    private val selectedCode: String,
    private val onSelect: (Language) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var selectedPosition = items.indexOfFirst { it.code == selectedCode }

    inner class LanguageViewHolder(val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = items[position]
        val context = holder.itemView.context

        holder.binding.textLanguage.text = language.name
        holder.binding.imageTick.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE
        holder.binding.textLanguage.setTextColor(
            ContextCompat.getColor(
                context,
                if (position == selectedPosition) R.color.blue_app_color else R.color.white
            )
        )

        holder.itemView.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
            onSelect(language)
        }
    }

    override fun getItemCount() = items.size
}