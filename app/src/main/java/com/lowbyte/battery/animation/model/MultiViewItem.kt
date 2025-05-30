package com.lowbyte.battery.animation.model

sealed class MultiViewItem {
    data class TitleItem(val title: String) : MultiViewItem()
    data class ListEmojiOrWidgetItem(val items: List<String>) : MultiViewItem()
    data class ListAnimationItem(val items: List<String>) : MultiViewItem()
}