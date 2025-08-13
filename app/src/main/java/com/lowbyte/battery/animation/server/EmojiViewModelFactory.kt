package com.lowbyte.battery.animation.server

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class EmojiViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val checker = NetworkChecker(context)
        val okHttp = NetworkModule.createOkHttp(context, checker)
        val retrofit = NetworkModule.createRetrofit(okHttp)
        val service = NetworkModule.createService(retrofit)
        val repo = EmojiRepository(service)
        @Suppress("UNCHECKED_CAST")
        return EmojiViewModel(repo) as T
    }
}