package com.lowbyte.battery.animation.server
import retrofit2.HttpException
import java.io.IOException

object ErrorMapper {
    fun map(t: Throwable): String = when (t) {
        is IOException -> "No internet or network error. Please check your connection."
        is HttpException -> "Server error: ${t.code()} ${t.message()}"
        else -> t.message ?: "Unexpected error"
    }
}