package com.sangusantri.app.domain.model

/** In-app update policy (ADR 0017), sourced from the Firebase Remote Config `in_app_update` parameter. */
data class AppUpdatePolicy(
    val minimumVersionCode: Int,
    val forceUpdateVersionCodes: Set<Int>,
)
