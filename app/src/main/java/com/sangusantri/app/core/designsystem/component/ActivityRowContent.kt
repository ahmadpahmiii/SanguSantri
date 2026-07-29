package com.sangusantri.app.core.designsystem.component

/** One already-formatted [ActivityRow]'s text content — pre-formatted by the caller (e.g. "Versi 2 · 8 menit"). */
data class ActivityRowContent(
    val primaryText: String,
    val secondaryText: String,
    val trailingText: String,
)
