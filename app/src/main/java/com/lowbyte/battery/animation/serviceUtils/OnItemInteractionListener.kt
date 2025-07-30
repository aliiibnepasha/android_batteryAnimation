package com.lowbyte.battery.animation.serviceUtils

interface OnItemInteractionListener {
    fun onItemSelected(resId: Int?)
    fun onItemCountChanged(items: List<LottieItem>)
}