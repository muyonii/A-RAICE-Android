package com.example.a_raiceandrd.ui.home

import androidx.annotation.ColorRes
import com.example.a_raiceandrd.R

enum class ActivityType (@ColorRes val color: Int){
    ALERT(R.color.risk_high),
    WARNING(R.color.risk_moderate),
    OK(R.color.risk_good)
}

/** Row model for Home's Recent Activity. `id` is optional and only used so DiffUtil can tell rows apart. */
data class ActivityItem(
    val title: String,
    val timeAgo: String,
    val type: ActivityType,
    val id: String = "",
    val sortKey: Long = 0L
)