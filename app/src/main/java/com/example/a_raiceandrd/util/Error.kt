package com.example.a_raiceandrd.util

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.Collections
import java.util.WeakHashMap

private val errorBodyCache = Collections.synchronizedMap(WeakHashMap<HttpException, String>())

private fun getCachedErrorBody(e: HttpException): String {
    return errorBodyCache.getOrPut(e) {
        try {
            e.response()?.errorBody()?.string() ?: ""
        } catch (_: Exception) {
            ""
        }
    }
}

fun errorMessage(e: Exception, isLogin: Boolean = false): String = when (e) {
    is HttpException -> when (e.code()) {
        401 -> if (isLogin) "Invalid email or password." else "Session expired. Please log in again."
        403 -> "You don't have access to this."
        404 -> "Not found."
        409 -> "Conflict: This record was already updated or resolved."
        422 -> try {
            val body = getCachedErrorBody(e)
            val json = JSONObject(body)
            if (json.has("errors")) {
                val errorsObj = json.getJSONObject("errors")
                val firstKey = errorsObj.keys().asSequence().firstOrNull()
                if (firstKey != null) {
                    val arr = errorsObj.getJSONArray(firstKey)
                    if (arr.length() > 0) arr.getString(0)
                    else json.optString("message", "Validation failed.")
                } else {
                    json.optString("message", "Validation failed.")
                }
            } else {
                json.optString("message", "Validation failed.")
            }
        } catch (_: Exception) {
            "Invalid image or data."
        }
        429 -> "Too many attempts. Wait a minute and retry."
        else -> "Server error (${e.code()})."
    }
    is SocketTimeoutException -> "The server is taking too long (the AI model may be waking up). Try again."
    is IOException -> "No internet connection."
    else -> "Something went wrong. Please try again."
}

fun parseFieldErrors(e: Exception): Map<String, String> {
    if (e !is HttpException || e.code() != 422) return emptyMap()
    val errorBody = getCachedErrorBody(e)
    if (errorBody.isBlank()) return emptyMap()

    val result = mutableMapOf<String, String>()
    try {
        val root = JSONObject(errorBody)
        if (root.has("errors")) {
            val errorsObj = root.getJSONObject("errors")
            for (key in errorsObj.keys()) {
                val array = errorsObj.optJSONArray(key)
                if (array != null && array.length() > 0) {
                    result[key] = array.getString(0)
                } else {
                    val str = errorsObj.optString(key)
                    if (str.isNotBlank()) result[key] = str
                }
            }
        }
        if (result.isEmpty() && root.has("message")) {
            result["message"] = root.getString("message")
        }
    } catch (_: Exception) {
        // Ignore parse error
    }
    return result
}