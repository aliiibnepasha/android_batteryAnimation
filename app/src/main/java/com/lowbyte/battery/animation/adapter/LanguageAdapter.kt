package com.lowbyte.battery.animation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.R

class LanguageAdapter(
    private val items: List<String>,
    private val onSelect: (String) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var selectedPosition = -1

    inner class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text = itemView.findViewById<TextView>(R.id.textLanguage)
        val tick = itemView.findViewById<ImageView>(R.id.imageTick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language, parent, false)
        return LanguageViewHolder(view)
    }



    override fun onBindViewHolder(holder: LanguageViewHolder, position1: Int) {
        val language = items[position1]
        holder.text.text = language
        holder.tick.visibility = if (position1 == selectedPosition) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = holder.getAdapterPosition()
            notifyItemChanged(previous)
            notifyItemChanged(position1)
            onSelect(language)
        }
    }


    override fun getItemCount() = items.size
}