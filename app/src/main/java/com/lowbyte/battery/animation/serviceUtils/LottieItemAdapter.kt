
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
    private val items: ArrayList<LottieItem>,
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
        val resId = items[position]
        Log.d("LottieItemAdapter", "onBindViewHolder: $resId")
        holder.lottie.setAnimation(resId.resId)
        holder.lottie.repeatCount = 200
        holder.lottie.playAnimation()
        holder.btnRemove.setOnClickListener {
            onRemove(resId)
        }
    }

    fun updateItems(newItems: List<LottieItem>) {
        items.clear()
        items == newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}
