package com.sangusantri.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey

/**
 * One bottom-navigation destination — deliberately release-scoped, not a placeholder for every
 * roadmap item: only destinations an active release actually ships get built (`00-overview-and-
 * tokens.md`'s navigation table; no Pengingat/Nahwu Quiz/Pesantren/Profil item, ever, per the
 * project's explicit exclusion).
 */
data class RootDestination(
    val key: NavKey,
    val label: String,
    val icon: @Composable (selected: Boolean) -> Unit,
)
