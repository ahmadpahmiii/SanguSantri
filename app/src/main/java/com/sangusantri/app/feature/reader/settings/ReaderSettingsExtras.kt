package com.sangusantri.app.feature.reader.settings

/**
 * The reader-specific extra sections [ReaderSettingsSheet] can show on top of the appearance
 * controls every reader shares. Bundled rather than passed as two more parameters so the sheet
 * stays under the shared-parameter-count limit as further readers reuse it.
 *
 * At most one is ever non-null in practice — a reader is Guided or Sholawat, not both — but they
 * are independent fields rather than a sealed choice, because nothing about the sheet's layout
 * would break if some future reader wanted both.
 */
data class ReaderSettingsExtras(
    val progressionMode: ProgressionModeControl? = null,
    val sholawatLayout: SholawatLayoutControl? = null,
)
