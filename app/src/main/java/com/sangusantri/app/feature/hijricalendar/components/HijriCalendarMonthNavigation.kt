package com.sangusantri.app.feature.hijricalendar.components

/** [canGoToPrevious]/[canGoToNext] gate [HijriCalendarMonthHeader]'s prev/next arrow buttons;
 * [onPrevious]/[onNext] fire on tap. Bundled into one parameter (with [HijriCalendarMonthHeader]
 * also taking the whole month rather than its fields separately) to keep that composable's own
 * parameter count under detekt's threshold. */
data class HijriCalendarMonthNavigation(
    val canGoToPrevious: Boolean,
    val canGoToNext: Boolean,
    val onPrevious: () -> Unit,
    val onNext: () -> Unit,
)
