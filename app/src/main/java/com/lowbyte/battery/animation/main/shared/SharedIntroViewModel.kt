package com.lowbyte.battery.animation.main.shared

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedIntroViewModel : ViewModel() {
    val childEvent = MutableLiveData<String>()
}