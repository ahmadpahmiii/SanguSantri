package com.sangusantri.app.feature.reader.components

/** Bundles [ReaderOverflowMenu]'s three action callbacks so the function stays under the parameter-count limit. */
data class ReaderOverflowActions(
    val onSwitchMode: () -> Unit,
    val onOpenTableOfContents: () -> Unit,
    val onOpenSettings: () -> Unit,
)
