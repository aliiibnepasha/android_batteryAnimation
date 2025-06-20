package com.lowbyte.battery.animation.main.intro

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
  private val fragments = listOf(
    SlideOneFragment.newInstance("Welcome to Page One"),
    SlideTwoFragment.newInstance("Here's Page Two"),
    SlideThreeFragment.newInstance("This is the Final Page")
  )

  override fun getItemCount() = fragments.size
  override fun createFragment(pos: Int) = fragments[pos]
}