package com.sangusantri.app.domain.model

/**
 * Pure policy comparison (ADR 0017), mirroring [com.sangusantri.app.data.content.ContentVersionAction]'s
 * enum-plus-top-level-function shape — no dependencies, so no Hilt use case class.
 */
enum class AppUpdateRequirement {
    NONE,
    FLEXIBLE,
    FORCE,
}

fun decideAppUpdateRequirement(
    installedVersionCode: Int,
    policy: AppUpdatePolicy,
): AppUpdateRequirement =
    when {
        installedVersionCode < policy.minimumVersionCode -> AppUpdateRequirement.FORCE
        installedVersionCode in policy.forceUpdateVersionCodes -> AppUpdateRequirement.FORCE
        else -> AppUpdateRequirement.FLEXIBLE
    }
