package com.example.a_raiceandrd.data.repository.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // PUBLIC
    @POST("login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @GET("municipalities")
    suspend fun getMunicipalities(): List<MunicipalityDto>

    @GET("diseases")
    suspend fun getDiseases(): List<DiseaseDto>

    @GET("diseases/{id}")
    suspend fun getDisease(@Path("id") id: Int): DiseaseDto

    // AUTHENTICATED
    @POST("logout")
    suspend fun logout(): Response<Unit>

    @GET("user")
    suspend fun getUser(): UserDto

    @PATCH("user")
    suspend fun updateUser(@Body body: UpdateUserRequest): UserDto

    @PUT("user/password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): MessageResponse

    @GET("scans")
    suspend fun getScans(
        @Query("page") page: Int? = null,
        @Query("farmer_id") farmerId: Int? = null,
        @Query("municipality") municipality: String? = null,
        @Query("disease_id") diseaseId: Int? = null,
        @Query("status") status: String? = null
    ): Page<ScanDto>

    @GET("scans/{id}")
    suspend fun getScan(@Path("id") id: Int): ScanDto

    @Multipart
    @POST("scans")
    suspend fun submitScan(
        @Part image: MultipartBody.Part,
        @Part("gps_lat") gpsLat: RequestBody? = null,
        @Part("gps_long") gpsLong: RequestBody? = null,
        @Part("farmer_id") farmerId: RequestBody? = null
    ): ScanDto

    @GET("alerts")
    suspend fun getAlerts(
        @Query("unread") unread: Int? = null,
        @Query("page") page: Int? = null
    ): Page<AlertDto>

    @POST("alerts/{id}/read")
    suspend fun markAlertRead(@Path("id") id: Int): Response<Unit>

    @POST("alerts/broadcast")
    suspend fun broadcastAlert(@Body body: BroadcastAlertRequest): BroadcastAlertResponse

    @GET("map")
    suspend fun getMap(): MapResponseDto

    // STAFF ONLY
    @GET("outbreaks")
    suspend fun getOutbreaks(
        @Query("page") page: Int? = null,
        @Query("status") status: String? = null
    ): Page<OutbreakDto>

    @GET("outbreaks/{id}")
    suspend fun getOutbreak(@Path("id") id: Int): OutbreakDto

    @PATCH("outbreaks/{id}/close")
    suspend fun closeOutbreak(@Path("id") id: Int): MessageResponse

    @GET("reports")
    suspend fun getReports(
        @Query("page") page: Int? = null
    ): Page<ReportDto>

    @POST("reports")
    suspend fun createReport(@Body body: CreateReportRequest): ReportDto

    @GET("reports/{id}")
    suspend fun getReport(@Path("id") id: Int): ReportDto
}