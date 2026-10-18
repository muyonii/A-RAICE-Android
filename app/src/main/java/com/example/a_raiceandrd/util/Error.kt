package com.example.a_raiceandrd.util

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

fun errorMessage(e: Exception): String = when (e) {
    is HttpException -> when (e.code()) {
        401 -> "Session expired. Please log in again."
        403 -> "You don't have access to this."
        404 -> "Not found."
        422 -> try {
            JSONObject(e.response()?.errorBody()?.string() ?: "").getString("message")
        } catch (_: Exception) { "Invalid image or data." }
        429 -> "Too many attempts. Wait a minute and retry."
        else -> "Server error (${e.code()})."
    }
    is SocketTimeoutException -> "The server is taking too long (the AI model may be waking up). Try again."
    is IOException -> "No internet connection."
    else -> e.message ?: "Something went wrong."
}