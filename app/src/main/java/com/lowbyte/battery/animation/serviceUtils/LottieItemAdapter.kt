package com.lowbyte.battery.animation.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.serviceUtils.LottieItem

class LottieItemAdapter(
    private val items: MutableList<LottieItem>,
    private val onRemove: (LottieItem) -> Unit
) : RecyclerView.Adapter<LottieItemAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lottie: LottieAnimationView = view.findViewById(R.id.item_lottie)
        val btnRemove: ImageButton = view.findViewById(R.id.btn_remove_lottie)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lottie_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        Log.d("LottieItemAdapter", "Binding item: ${item.resId}")

        holder.lottie.setAnimation(item.resId)
        holder.lottie.repeatCount = 1000
        holder.lottie.playAnimation()

        holder.btnRemove.setOnClickListener {
            onRemove(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<LottieItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}