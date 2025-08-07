package com.lowbyte.battery.animation.model

import android.widget.ImageView
import android.widget.TextView

data class NavItem(
    val icon: ImageView,
    val text: TextView,
    val selectedIcon: Int,
    val unselectedIcon: Int
)