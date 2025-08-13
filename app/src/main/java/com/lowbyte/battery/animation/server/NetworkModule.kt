package com.lowbyte.battery.animation.server

import android.content.Context
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val DEFAULT_BASE = "https://theswiftvision.com/battery_emoji.php/" // not used when @Url is full

    fun createOkHttp(context: Context, networkChecker: NetworkChecker): OkHttpClient {
        val cacheSize = 10L * 1024 * 1024 // 10 MB
        val cache = Cache(context.cacheDir, cacheSize)

        val offlineInterceptor = Interceptor { chain ->
            var request = chain.request()
            if (!networkChecker.isConnected()) {
                // Serve stale cache up to 7 days when offline
                request = request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=${7 * 24 * 60 * 60}")
                    .build()
            }
            val response = chain.proceed(request)
            // Fresh responses: cache for 5 minutes
            response.newBuilder()
                .header("Cache-Control", "public, max-age=${5 * 60}")
                .build()
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(offlineInterceptor)
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun createRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(DEFAULT_BASE)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()


    fun createService(retrofit: Retrofit): EmojiApiService = retrofit.create(EmojiApiService::class.java)
}