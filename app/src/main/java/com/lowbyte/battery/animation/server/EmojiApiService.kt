package com.lowbyte.battery.animation.server

import ApiResponse
import retrofit2.http.GET
import retrofit2.http.Url

interface EmojiApiService {
    // Use full URL since your endpoint may be dynamic
    @GET
    suspend fun getAll(@Url fullUrl: String): ApiResponse
}