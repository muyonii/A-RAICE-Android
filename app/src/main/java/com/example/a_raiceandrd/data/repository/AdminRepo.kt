package com.example.a_raiceandrd.data.repository

import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.data.repository.api.ScanItem
import com.example.a_raiceandrd.model.AdminDashboard
import com.example.a_raiceandrd.model.ChartBar
import com.example.a_raiceandrd.model.DashboardSource
import com.example.a_raiceandrd.model.Outbreak
import com.example.a_raiceandrd.model.Period
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import com.example.a_raiceandrd.model.RecentScan

object AdminRepo {

    private const val MAX_PAGES = 20

    // Loads everything from the API once.
    suspend fun getDashboardSource(): DashboardSource {
        val scans = loadAllScans()
        val map = ApiClient.service.getMap()

        val outbreaks = map.outbreaks.orEmpty().map {
            Outbreak(
                disease = it.disease,
                municipality = it.municipality?.replaceFirstChar { c -> c.uppercase() },
                createdAt = parseDate(it.createdAt)
            )
        }

        val recent = scans.sortedByDescending { it.id }.take(4).map {
            RecentScan(
                id = it.id,
                disease = it.disease?.name,
                farmer = it.farmer?.name ?: "Unknown farmer",
                confidence = ((it.confidence ?: 0.0) * 100).toInt(),
                inOutbreak = it.outbreakId != null
            )
        }

        return DashboardSource(
            totalScans = scans.size,
            confirmedCases = scans.count { it.disease != null },
            farmers = scans.mapNotNull { it.farmer?.id }.distinct().size,
            outbreaks = outbreaks,
            recentScans = recent
        )
    }

    // Works out the cards and the chart for the chosen period (no network).
    fun buildDashboard(source: DashboardSource, period: Period): AdminDashboard {
        val start = periodStart(period)
        val inPeriod = source.outbreaks.filter { it.createdAt?.isBefore(start) == false }

        val byMunicipality = inPeriod
            .filter { it.municipality != null }
            .groupBy { it.municipality!! }
            .map { (name, items) ->
                val topDisease = items.mapNotNull { it.disease }
                    .groupingBy { it }.eachCount()
                    .maxByOrNull { it.value }?.key.orEmpty()
                ChartBar(name, items.size, topDisease)
            }
            .sortedByDescending { it.count }

        val topDisease = inPeriod.mapNotNull { it.disease }
            .groupingBy { it }.eachCount()
            .maxByOrNull { it.value }?.key ?: "None"

        return AdminDashboard(
            totalScans = source.totalScans,
            recentScans = source.recentScans,
            confirmedCases = source.confirmedCases,
            farmersMonitored = source.farmers,
            activeOutbreaks = inPeriod.size,
            municipalitiesAffected = byMunicipality.size,
            topDisease = topDisease,
            chartBars = byMunicipality
        )
    }

    private fun periodStart(period: Period): LocalDate {
        val today = LocalDate.now()
        return when (period) {
            Period.WEEK -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            Period.MONTH -> today.withDayOfMonth(1)
            Period.YEAR -> today.withDayOfYear(1)
        }
    }

    private fun parseDate(text: String?): LocalDate? =
        try { text?.take(10)?.let { LocalDate.parse(it) } } catch (e: Exception) { null }

    private suspend fun loadAllScans(): List<ScanItem> {
        val all = mutableListOf<ScanItem>()
        for (page in 1..MAX_PAGES) {
            val items = ApiClient.service.getScans(page).data.orEmpty()
            if (items.isEmpty()) break
            if (all.isNotEmpty() && items.first().id == all.first().id) break
            all += items
        }
        return all
    }
}