package com.lowbyte.battery.animation.model

import androidx.annotation.Keep

@Keep
data class ModelSubscription(
    val duration: String,
    val label: String,
    var price: String,
    var productId: String
)
