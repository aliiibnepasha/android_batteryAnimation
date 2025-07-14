package com.lowbyte.battery.animation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.main.IntroSlideFragment
import com.lowbyte.battery.animation.main.intro.NativeAdFragment
import com.lowbyte.battery.animation.model.IntroItem
import com.lowbyte.battery.animation.model.SlideType

class IntroAdapter(
    fa: FragmentActivity,
    private val items: List<IntroItem>
) : FragmentStateAdapter(fa){
    override fun getItemCount() = items.size

    override fun createFragment(position: Int): Fragment {
        val item = items[position]
        return when (item.type) {
            SlideType.NATIVE_AD -> NativeAdFragment()
            SlideType.INTRO -> IntroSlideFragment.newInstance(
                item.title.orEmpty(),
                item.description.orEmpty(),
                item.imageResId ?: 0
            )
        }


    }
    }