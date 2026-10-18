package com.example.a_raiceandrd.data.repository.api

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// GET /api/scans  (one page of results)
data class ScanPage(val data: List<ScanItem>?)
data class ScanItem(
    val id: Int,
    val disease: DiseaseRef?,
    val farmer: FarmerRef?,
    val confidence: Double?,
    @SerializedName("outbreak_id") val outbreakId: Int?,
    @SerializedName("created_at") val createdAt: String? = null
)
data class DiseaseRef(val id: Int?, val name: String?)

// GET /api/user  -> farmers get their location inside "farmer_profile"
data class FarmerProfile(
    val municipality: String?,
    val barangay: String?,
    @SerializedName("farm_lat") val farmLat: String?,
    @SerializedName("farm_long") val farmLong: String?
)

// GET /api/user  (municipality/barangay may be a string OR an object, so JsonElement)
data class UserDto(
    val id: Int?,
    val name: String?,
    val role: String?,
    @SerializedName("farmer_profile") val farmerProfile: FarmerProfile?   // null for LGU/admin
)

// GET /api/alerts
data class AlertPage(val data: List<AlertDto>?)
data class AlertDto(
    val id: Int,
    val message: String?,
    val severity: String?,
    val disease: JsonElement?,
    val municipality: JsonElement?,
    @SerializedName("outbreak_id") val outbreakId: Int?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("read_at") val readAt: String?
)

// GET /api/scans/{id} and POST /api/scans
data class Treatment(
    val id: Int,
    val title: String,
    val description: String?,
    val type: String                 // chemical | biological | cultural | organic
)

data class ScanDisease(
    val id: Int?,
    val name: String?,
    val treatments: List<Treatment>?
)

data class ScanResponse(
    val id: Int,
    val status: String?,
    val disease: ScanDisease?,       // can be null
    val confidence: Float?,          // can be null
    @SerializedName("gps_lat") val gpsLat: Double?,
    @SerializedName("gps_lng") val gpsLng: Double?,
    @SerializedName("outbreak_id") val outbreakId: Int?
)

data class FarmerRef(val id: Int?, val name: String?)
// GET /api/map
data class MapResponse(
    val municipalities: List<MapMunicipality>?,
    val outbreaks: List<MapOutbreak>?
)

data class MapMunicipality(
    val slug: String?,
    val severity: String?
)

data class MapOutbreak(
    val id: Int?,
    val disease: String?,
    val municipality: String?,
    @SerializedName("created_at") val createdAt: String?,
    val severity: String?
)