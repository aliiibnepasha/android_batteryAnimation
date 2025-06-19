package com.lowbyte.battery.animation.model

data class IntroItem(
    val title: String? = null,
    val description: String? = null,
    val imageResId: Int? = null,
    val type: SlideType = SlideType.INTRO
)