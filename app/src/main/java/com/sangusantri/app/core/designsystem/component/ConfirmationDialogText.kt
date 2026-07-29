package com.sangusantri.app.core.designsystem.component

/** Bundles [ConfirmationDialog]'s four text values so the function stays under the parameter-count
 * limit (mirrors [com.sangusantri.app.feature.guidedreader.ConfirmDialogText]'s established shape). */
data class ConfirmationDialogText(
    val title: String,
    val message: String,
    val confirmLabel: String,
    val cancelLabel: String,
)
