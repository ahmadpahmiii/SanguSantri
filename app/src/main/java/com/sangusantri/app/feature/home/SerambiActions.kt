package com.sangusantri.app.feature.home

/** [SerambiScreen]'s parameter-less navigation actions, bundled to keep the composable's own
 * parameter list short — `onContentSelected` stays separate since it carries a content id. */
data class SerambiActions(
    val onSetelanClick: () -> Unit,
    val onAboutClick: () -> Unit,
    val onPengingatClick: () -> Unit,
    val onBelajarClick: () -> Unit,
    val onQuranClick: () -> Unit,
)
