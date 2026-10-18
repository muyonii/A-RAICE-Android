package com.example.a_raiceandrd.data.repository.api

import com.example.a_raiceandrd.BuildConfig
import com.example.a_raiceandrd.ui.auth.FLogin
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private val BASE_URL: String = BuildConfig.API_BASE_URL

    @Volatile
    var token: String? = null

    @Volatile
    var userName: String? = null

    // Emitted whenever a non-login 401 is received from the server
    val unauthorizedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Optional direct listener callback
    @Volatile
    var onUnauthorized: (() -> Unit)? = null

    // Tolerant Double adapter: handles numbers, numeric strings, and empty strings safely
    private class FlexibleDoubleAdapter : TypeAdapter<Double?>() {
        override fun write(out: JsonWriter, value: Double?) {
            if (value == null) {
                out.nullValue()
            } else {
                out.value(value)
            }
        }

        override fun read(reader: JsonReader): Double? {
            return when (reader.peek()) {
                JsonToken.NULL -> {
                    reader.nextNull()
                    null
                }
                JsonToken.NUMBER -> reader.nextDouble()
                JsonToken.STRING -> {
                    val s = reader.nextString().trim()
                    s.toDoubleOrNull()
                }
                else -> {
                    reader.skipValue()
                    null
                }
            }
        }
    }

    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Double::class.java, FlexibleDoubleAdapter())
        .registerTypeAdapter(java.lang.Double::class.java, FlexibleDoubleAdapter())
        .create()

    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val origRequest = chain.request()
            val requestBuilder = origRequest.newBuilder()
                .header("Accept", "application/json")
            if (origRequest.header("Authorization") == null) {
                token?.let { requestBuilder.header("Authorization", "Bearer $it") }
            }

            val response = chain.proceed(requestBuilder.build())

            if (response.code == 401) {
                val path = origRequest.url.encodedPath
                // Do not trigger global logout for login or logout attempts
                if (!path.endsWith("/login") && !path.endsWith("login") &&
                    !path.endsWith("/logout") && !path.endsWith("logout")) {
                    token = null
                    FLogin.clearSessionLocally()
                    unauthorizedEvents.tryEmit(Unit)
                    onUnauthorized?.invoke()
                }
            }

            response
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val service: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(http)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(ApiService::class.java)
}
