package com.example.a_raiceandrd.ui.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.adapter.ActivityAdapter
import kotlinx.coroutines.launch
import com.example.a_raiceandrd.databinding.FragmentHomeBinding
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.data.repository.api.ApiService
import com.example.a_raiceandrd.util.toActivityItem
import kotlinx.coroutines.delay
import java.util.Locale
import androidx.core.view.isVisible
import kotlinx.coroutines.isActive
import kotlin.coroutines.cancellation.CancellationException
import com.example.a_raiceandrd.util.asDisplayText
import kotlin.collections.firstOrNull
import kotlinx.coroutines.coroutineScope
import com.example.a_raiceandrd.data.repository.api.UserDto
import kotlin.collections.orEmpty
import com.example.a_raiceandrd.data.repository.api.AlertDto
import com.example.a_raiceandrd.data.repository.api.ScanItem
import com.example.a_raiceandrd.data.repository.api.MapResponse
import androidx.core.widget.NestedScrollView
import com.example.a_raiceandrd.ui.setupTopBar
import com.example.a_raiceandrd.ui.showNav
import com.google.android.material.divider.MaterialDividerItemDecoration
import java.text.SimpleDateFormat
import java.util.TimeZone

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val adapter = ActivityAdapter()
    private lateinit var api: ApiService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentHomeBinding.bind(view)

        api = ApiClient.service

        setupTopBar(view, view.findViewById<NestedScrollView>(R.id.homeScroll))

        b.activityList.layoutManager = LinearLayoutManager(requireContext())
        b.activityList.adapter = adapter

        // Name comes from login, so it changes per user.
        b.greetingText.text = greeting(savedName())

        // Thin divider between rows inside the single card (none after the last row).
        b.activityList.addItemDecoration(
            MaterialDividerItemDecoration(
                requireContext(), MaterialDividerItemDecoration.VERTICAL
            ).apply {
                isLastItemDecorated = false
                dividerInsetStart = dp(20)
                dividerInsetEnd = dp(20)
                dividerColor = ContextCompat.getColor(requireContext(), R.color.stroke_default)
            }
        )

        // Show the last known data instantly, then refresh in the background.
        render()

        // Refresh now, then every 30s while the screen is visible ("real-time" updates).
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    load()
                    delay(POLL_MS)
                }
            }
        }
    }

    // Runs one API call, a failure returns null instead of breaking the whole screen.
    private suspend fun <T> safe(name: String, block: suspend () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "$name failed", e)   // 401 here = token expired -> send user to login
        null
    }

    private suspend fun load() = coroutineScope {
        // All four requests run at the same time; each one updates the screen as soon as it finishes.
        launch { safe("user") { api.getUser() }?.let { Cache.user = it; render() } }
        launch { safe("alerts") { api.getAlerts().data.orEmpty() }?.let { Cache.alerts = it; render() } }
        launch { safe("scans") { api.getScans().data.orEmpty() }?.let { Cache.scans = it; render() } }
        launch { safe("map") { api.getMap() }?.let { Cache.map = it; render() } }
    }

    // Draws whatever we currently have (cached or fresh). Safe to call many times.
    private fun render() {
        val binding = _b ?: return
        val user = Cache.user
        val alerts = Cache.alerts
        val scans = Cache.scans
        val map = Cache.map

        // address + name
        if (user != null) {
            val barangay = user.farmerProfile?.barangay?.toTitle()
            val municipality = user.farmerProfile?.municipality?.toTitle()
            binding.userLoc.text = listOfNotNull(barangay, municipality)
                .joinToString(", ").uppercase(Locale.ROOT)
            binding.greetingText.text = greeting(user.name ?: savedName())
        }

        // Notification module: risk ring + latest alert
        if (alerts != null) {
            val latest = alerts.firstOrNull()
            val slug = user?.farmerProfile?.municipality?.trim()
                ?.lowercase(Locale.ROOT)?.replace(" ", "-")
            val severity = map?.municipalities
                ?.firstOrNull { it.slug.equals(slug, ignoreCase = true) }?.severity
                ?: latest?.severity
            applyRisk(severity)

            val disease = latest?.disease.asDisplayText()
            binding.notifTitle.text = when {
                disease != null -> "$disease active nearby"
                latest != null -> latest.message.orEmpty()
                else -> getString(R.string.app_name)
            }
            binding.notifBody.text = latest?.message ?: "No active alerts in your area."
        }

    // Recent activity: newest alerts + scans, never more than MAX_RECENT rows.
    if (alerts != null || scans != null) {
        val items = recentActivity(alerts, scans)
        adapter.submitList(items)
        binding.activityList.isVisible = items.isNotEmpty()
        binding.activityEmpty.isVisible = items.isEmpty()
    }
}

/**
 * Combines alerts and scans into at most MAX_RECENT rows, newest first.
 * show up to 2 alerts + 2 scans (newest scan = highest id)
 */
private fun recentActivity(alerts: List<AlertDto>?, scans: List<ScanItem>?): List<ActivityItem> {
    val alertItems = alerts.orEmpty()
        .map { it.toActivityItem().copy(sortKey = it.createdAt.toMillis()) }
        .sortedByDescending { it.sortKey }

    val scanList = scans.orEmpty()
    val scansHaveTime = scanList.isNotEmpty() && scanList.all { !it.createdAt.isNullOrBlank() }

    val combined = if (scansHaveTime) {
        val scanItems = scanList.map { it.toActivityItem().copy(sortKey = it.createdAt.toMillis()) }
        (alertItems + scanItems).sortedByDescending { it.sortKey }
    } else {
        val scanItems = scanList.sortedByDescending { it.id }.map { it.toActivityItem() }
        val first = alertItems.take(2) + scanItems.take(2)
        first + (alertItems.drop(2) + scanItems.drop(2))
    }

    return combined
        .distinctBy { it.title to it.timeAgo }
        .take(MAX_RECENT)
}

// "2026-10-18T07:40:00.000000Z"; only the first 19 chars are needed.
private fun String?.toMillis(): Long {
    if (this.isNullOrBlank() || length < 19) return 0L
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse(substring(0, 19))?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun applyRisk(severity: String?) {
        val (percent, label, colorRes) = when (severity?.lowercase(Locale.ROOT)) {
            "high" -> Triple(100, "HIGH", R.color.risk_high)
            "medium" -> Triple(66, "MODERATE", R.color.risk_moderate)
            "low" -> Triple(33, "LOW", R.color.risk_good)
            else -> Triple(0, "SAFE", R.color.risk_good)
        }
        val color = ContextCompat.getColor(requireContext(), colorRes)
        b.riskRing.setProgressCompat(percent, true)
        b.riskRing.setIndicatorColor(color)
        b.riskLabel.text = label
        b.riskLabel.setTextColor(color)
    }

    private fun savedName(): String? = ApiClient.userName

    // "balanga" -> "Balanga"
    private fun String.toTitle() =
        replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

    private fun greeting(name: String?) =
        if (name.isNullOrBlank()) "Kumusta!" else "Kumusta, $name"

    override fun onDestroyView() {
        requireView().showNav()
        super.onDestroyView()
        _b = null
    }

    // Last results, kept in memory so reopening Home is instant.
    private object Cache {
        var user: UserDto? = null
        var alerts: List<AlertDto>? = null
        var scans: List<ScanItem>? = null
        var map: MapResponse? = null
    }

    private companion object {
        const val TAG = "HomeFragment"
        const val POLL_MS = 30_000L
        const val MAX_RECENT = 4
    }
}