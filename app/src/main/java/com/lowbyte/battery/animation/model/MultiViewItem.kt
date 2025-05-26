package com.lowbyte.battery.animation.model

import androidx.constraintlayout.utils.widget.ImageFilterView

sealed class MultiViewItem {
    data class TitleItem(val title: String) : MultiViewItem()
    data class ListItem(val items: List<String>) : MultiViewItem()
}