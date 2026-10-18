package com.example.a_raiceandrd.data.repository.api

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// Generic paginated container matching Laravel pagination
data class Page<T>(
    val data: List<T>? = null,
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("last_page") val lastPage: Int? = null,
    @SerializedName("next_page_url") val nextPageUrl: String? = null,
    val total: Int? = null
)

// ---------- User & Auth Models ----------

data class LoginRequest(
    val email: String? = null,
    val password: String? = null
)

data class RegisterRequest(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    @SerializedName("password_confirmation") val passwordConfirmation: String? = null,
    val barangay: String? = null,
    val municipality: String? = null,
    @SerializedName("contact_number") val contactNumber: String? = null,
    @SerializedName("farm_lat") val farmLat: Double? = null,
    @SerializedName("farm_long") val farmLong: Double? = null
)

data class FarmerProfileDto(
    val municipality: String? = null,
    val barangay: String? = null,
    @SerializedName("farm_lat") val farmLat: Double? = null,
    @SerializedName("farm_long") val farmLong: Double? = null,
    @SerializedName("contact_number") val contactNumber: String? = null
)

typealias FarmerProfile = FarmerProfileDto

data class UserDto(
    val id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null,
    @SerializedName("farmer_profile") val farmerProfile: FarmerProfileDto? = null
)

typealias ApiUser = UserDto

data class AuthResponse(
    val token: String? = null,
    val user: UserDto? = null
)

typealias LoginResponse = AuthResponse

data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
    val barangay: String? = null,
    val municipality: String? = null,
    @SerializedName("contact_number") val contactNumber: String? = null,
    @SerializedName("farm_lat") val farmLat: Double? = null,
    @SerializedName("farm_long") val farmLong: Double? = null
)

data class ChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String? = null,
    val password: String? = null,
    @SerializedName("password_confirmation") val passwordConfirmation: String? = null
)

// ---------- Municipalities & Diseases ----------

data class MunicipalityDto(
    val name: String? = null,
    val slug: String? = null,
    val value: String? = null,
    val psgc: String? = null
)

data class TreatmentDto(
    val id: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val type: String? = null // chemical | biological | cultural | organic
)

typealias Treatment = TreatmentDto

data class DiseaseDto(
    val id: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val causes: String? = null,
    val symptoms: String? = null,
    @SerializedName("prevention_tips") val preventionTips: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("image_path") val imagePath: String? = null,
    @SerializedName("treatments_count") val treatmentsCount: Int? = null,
    val history: String? = null,
    val sources: String? = null,
    val treatments: List<TreatmentDto>? = null
)

typealias DiseaseRef = DiseaseDto
typealias ScanDisease = DiseaseDto

// ---------- Scans ----------

data class TopDiseaseDto(
    @SerializedName("disease_id") val diseaseId: Int? = null,
    @SerializedName("disease_name") val diseaseName: String? = null,
    val confidence: Double? = null
)

data class OutbreakRefDto(
    val id: Int? = null,
    val municipality: String? = null,
    val status: String? = null
)

data class FarmerRefDto(
    val id: Int? = null,
    val name: String? = null,
    @SerializedName("contact_number") val contactNumber: String? = null
)

typealias FarmerRef = FarmerRefDto

data class ScanDto(
    val id: Int? = null,
    val status: String? = null, // processing | completed | failed | unknown
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    val disease: DiseaseDto? = null,
    val confidence: Double? = null,
    @SerializedName("top_3") val top3: List<TopDiseaseDto>? = null,
    @SerializedName("gps_lat") val gpsLat: Double? = null,
    @SerializedName("gps_lng") val gpsLng: Double? = null,
    @SerializedName("outbreak_id") val outbreakId: Int? = null,
    val farmer: FarmerRefDto? = null,
    val outbreak: OutbreakRefDto? = null
)

typealias ScanItem = ScanDto
typealias ScanResponse = ScanDto
typealias ScanPage = Page<ScanDto>

// ---------- Alerts ----------

data class AlertDto(
    val id: Int? = null,
    val message: String? = null,
    val severity: String? = null, // low | medium | high
    val disease: JsonElement? = null,
    val municipality: JsonElement? = null,
    @SerializedName("outbreak_id") val outbreakId: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("read_at") val readAt: String? = null
)

typealias AlertPage = Page<AlertDto>

data class BroadcastAlertRequest(
    val message: String? = null,
    val severity: String? = null,
    val municipality: String? = null,
    @SerializedName("outbreak_id") val outbreakId: Int? = null
)

data class BroadcastAlertDto(
    val id: Int? = null,
    val message: String? = null,
    val severity: String? = null,
    @SerializedName("outbreak_id") val outbreakId: Int? = null,
    val municipality: String? = null,
    @SerializedName("recipients_count") val recipientsCount: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class BroadcastAlertResponse(
    val message: String? = null,
    val alert: BroadcastAlertDto? = null
)

// ---------- Map ----------

data class MapMunicipalityDto(
    val slug: String? = null,
    val severity: String? = null // null | low | medium | high
)

typealias MapMunicipality = MapMunicipalityDto

data class MapOutbreakDto(
    val id: Int? = null,
    val disease: String? = null,
    val municipality: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    val severity: String? = null
)

typealias MapOutbreak = MapOutbreakDto

data class MapFarmScanDto(
    val id: Int? = null,
    val disease: String? = null,
    @SerializedName("scan_date") val scanDate: String? = null
)

data class MapFarmDto(
    val id: Int? = null,
    val farmer: FarmerRefDto? = null,
    val barangay: String? = null,
    val municipality: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val scan: MapFarmScanDto? = null
)

data class MapResponseDto(
    val municipalities: List<MapMunicipalityDto>? = null,
    val outbreaks: List<MapOutbreakDto>? = null,
    val farms: List<MapFarmDto>? = null
)

typealias MapResponse = MapResponseDto

// ---------- Outbreaks (Staff) ----------

data class UserRefDto(
    val id: Int? = null,
    val name: String? = null
)

data class OutbreakDto(
    val id: Int? = null,
    val disease: DiseaseDto? = null,
    val municipality: String? = null,
    val status: String? = null, // active | resolved | unknown
    val severity: String? = null,
    @SerializedName("scans_count") val scansCount: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("closed_at") val closedAt: String? = null,
    @SerializedName("closed_by") val closedBy: UserRefDto? = null
)

// ---------- Reports (Staff) ----------

data class ReportSummaryPerDiseaseDto(
    val disease: String? = null,
    val count: Int? = null
)

data class ReportSummaryDto(
    @SerializedName("total_scans") val totalScans: Int? = null,
    @SerializedName("confirmed_cases") val confirmedCases: Int? = null,
    @SerializedName("per_disease") val perDisease: List<ReportSummaryPerDiseaseDto>? = null,
    @SerializedName("top_disease") val topDisease: String? = null
)

data class ReportDto(
    val id: Int? = null,
    val municipality: String? = null,
    @SerializedName("range_start") val rangeStart: String? = null,
    @SerializedName("range_end") val rangeEnd: String? = null,
    @SerializedName("summary_data") val summaryData: ReportSummaryDto? = null,
    @SerializedName("generated_by") val generatedBy: UserRefDto? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class CreateReportRequest(
    val municipality: String? = null,
    @SerializedName("range_start") val rangeStart: String? = null,
    @SerializedName("range_end") val rangeEnd: String? = null,
    @SerializedName("disease_id") val diseaseId: Int? = null
)

// ---------- Common ----------

data class MessageResponse(
    val message: String? = null
)