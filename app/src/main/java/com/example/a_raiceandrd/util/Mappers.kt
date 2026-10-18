package com.example.a_raiceandrd.util

import com.example.a_raiceandrd.data.repository.api.AlertDto
import com.example.a_raiceandrd.data.repository.api.ScanItem
import com.example.a_raiceandrd.model.AlertItem
import com.example.a_raiceandrd.ui.home.ActivityItem
import com.example.a_raiceandrd.ui.home.ActivityType
import com.google.gson.JsonElement
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.a_raiceandrd.model.AlertLevel

// Reads a display string from either "Orani" or { "name": "Orani" }. */
fun JsonElement?.asDisplayText(): String? {
    if (this == null || isJsonNull) return null
    if (isJsonPrimitive) return asString
    if (isJsonObject) {
        val n = asJsonObject.get("name")
        if (n != null && !n.isJsonNull) return n.asString
    }
    return null
}

/** Reads a slug from { "slug": "orani" }, else builds one from the name ("Orani" -> "orani"). */
fun JsonElement?.asSlug(): String? {
    if (this != null && isJsonObject) {
        val s = asJsonObject.get("slug")
        if (s != null && !s.isJsonNull) return s.asString
    }
    return asDisplayText()?.trim()?.lowercase(Locale.ROOT)?.replace(" ", "-")
}

fun formatTime(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val zone = ZoneId.systemDefault()
        val dt: ZonedDateTime = try {
            OffsetDateTime.parse(iso).atZoneSameInstant(zone)
        } catch (e: Exception) {
            // No offset in the string (e.g. "2026-10-13 07:43:00") -> assume UTC
            LocalDateTime.parse(iso.trim().replace(" ", "T").take(19))
                .atOffset(ZoneOffset.UTC).atZoneSameInstant(zone)
        }
        val day = if (dt.toLocalDate() == LocalDate.now(zone)) "TODAY"
        else dt.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)).uppercase(Locale.ROOT)
        "$day · ${dt.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))}"
    } catch (e: Exception) {
        ""
    }
}

// API severity: none/green, low, medium, high
fun String?.toAlertLevel(): AlertLevel = when (this?.lowercase(Locale.ROOT)) {
    "high" -> AlertLevel.HIGH
    "medium" -> AlertLevel.MODERATE
    else -> AlertLevel.GOOD
}

fun String?.toActivityType(): ActivityType = when (this?.lowercase(Locale.ROOT)) {
    "high" -> ActivityType.ALERT
    "medium" -> ActivityType.WARNING
    else -> ActivityType.OK
}

// ---------- DTO -> UI models ----------

fun AlertDto.toAlertItem() = AlertItem(
    id = (id ?: 0).toLong(),
    title = message.orEmpty(),
    timeLabel = formatTime(createdAt),
    level = severity.toAlertLevel(),
    unread = readAt == null
)

fun AlertDto.toActivityItem() = ActivityItem(
    title = message.orEmpty(),
    timeAgo = formatTime(createdAt),
    type = severity.toActivityType(),
    id = "alert-${id ?: 0}"
)

fun ScanItem.toActivityItem(): ActivityItem {
    val result = disease?.name ?: "No disease detected"
    val pct = confidence?.let { " (${(it * 100).toInt()}%)" }.orEmpty()
    val time = formatTime(createdAt).ifEmpty { "SCAN RESULT" }
    return ActivityItem(
        title = "Scan: $result$pct",
        timeAgo = time,
        type = if (disease == null) ActivityType.OK else ActivityType.WARNING,
        id = "scan-${id ?: 0}"
    )
}