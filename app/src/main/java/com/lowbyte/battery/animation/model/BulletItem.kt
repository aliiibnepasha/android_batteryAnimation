package com.lowbyte.battery.animation.model

sealed class BulletItem {
    data class Title(val text: String) : BulletItem()
    data class Description(val text: String) : BulletItem()
}