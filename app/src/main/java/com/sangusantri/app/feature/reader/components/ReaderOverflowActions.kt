package com.sangusantri.app.feature.reader.components

/** Bundles [ReaderOverflowMenu]'s action callbacks so the function stays under the parameter-count limit. */
data class ReaderOverflowActions(
    val onSwitchMode: () -> Unit,
    val onOpenSettings: () -> Unit,
)
