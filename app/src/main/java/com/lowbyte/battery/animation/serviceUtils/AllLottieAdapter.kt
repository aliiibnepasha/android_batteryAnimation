package com.lowbyte.battery.animation.serviceUtils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.lowbyte.battery.animation.databinding.ItemLottieSquareBinding

class AllLottieAdapter(
    private val items: List<Int>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<AllLottieAdapter.LottieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LottieViewHolder {
        val binding = ItemLottieSquareBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LottieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LottieViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class LottieViewHolder(private val binding: ItemLottieSquareBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(rawResId: Int) {
            binding.lottieView.setAnimation(rawResId)
            //binding.lottieView.setAnimationFromUrl("https://abcwebservices.com/uploads/lottie_1.json")
            binding.lottieView.repeatCount = LottieDrawable.INFINITE
            binding.lottieView.playAnimation()
            binding.root.setOnClickListener {
                onItemClick.invoke(rawResId)
            }
        }
    }
}