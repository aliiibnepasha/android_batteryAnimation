package com.lowbyte.battery.animation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.RecyclerView
import com.lowbyte.battery.animation.R

class ChildItemAdapter(
    private val items: List<Int>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<ChildItemAdapter.ChildViewHolder>() {

    inner class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(text: Int) {
            val tv = itemView.findViewById<ImageFilterView>(R.id.textChild)
            tv.setImageResource(text)
            itemView.setOnClickListener { onClick(position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_item_rv_item, parent, false)
        return ChildViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}