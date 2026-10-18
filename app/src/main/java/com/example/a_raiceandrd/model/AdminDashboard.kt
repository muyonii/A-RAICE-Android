package com.example.a_raiceandrd.model

import java.time.LocalDate

data class AdminDashboard(
    val totalScans: Int,
    val confirmedCases: Int,
    val farmersMonitored: Int,
    val activeOutbreaks: Int,
    val municipalitiesAffected: Int,
    val topDisease: String,
    val chartBars: List<ChartBar>,
    val recentScans: List<RecentScan>
)

data class RecentScan(
    val disease: String?,
    val farmer: String,
    val confidence: Int,
    val inOutbreak: Boolean
)

data class Outbreak(
    val disease: String?,
    val municipality: String?,
    val createdAt: LocalDate?
)

data class DashboardSource(
    val totalScans: Int,
    val confirmedCases: Int,
    val farmers: Int,
    val outbreaks: List<Outbreak>,
    val recentScans: List<RecentScan>
)

enum class Period { WEEK, MONTH, YEAR }