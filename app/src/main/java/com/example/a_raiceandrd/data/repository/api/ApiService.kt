package com.example.a_raiceandrd.data.repository.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.Response

interface ApiService {

    @POST("login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("logout")
    suspend fun logout(@Header("Authorization") auth: String)

    @GET("scans")
    suspend fun getScans(@Query("page") page: Int = 1): ScanPage

    @Multipart
    @POST("scans")
    suspend fun submitScan(
        @Part image: MultipartBody.Part,
        @Part("gps_lat") gpsLat: RequestBody? = null,
        @Part("gps_long") gpsLong: RequestBody? = null,
        @Part("farmer_id") farmerId: RequestBody? = null
    ): ScanResponse

    @GET("scans/{id}")
    suspend fun getScan(
        @Path("id") id: Int
    ): ScanResponse

    @GET("user")
    suspend fun getUser(): UserDto

    @GET("alerts")
    suspend fun getAlerts(): AlertPage

    @POST("alerts/{id}/read")
    suspend fun markAlertRead(@Path("id") id: Int): Response<Unit>

    @GET("map")
    suspend fun getMap(): MapResponse

}