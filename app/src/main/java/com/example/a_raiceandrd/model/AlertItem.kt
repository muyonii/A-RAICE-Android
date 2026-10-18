package com.example.a_raiceandrd.model

    /** Row model for the Alerts screen. `id` is the server's alert id. */
    data class AlertItem(
        val id: Long,
        val title: String,
        val timeLabel: String,
        val level: AlertLevel,
        val unread: Boolean = false
    )