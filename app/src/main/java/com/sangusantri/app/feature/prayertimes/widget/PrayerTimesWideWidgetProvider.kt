package com.sangusantri.app.feature.prayertimes.widget

/**
 * The horizontal band offered as a second entry in the widget picker: a 4x1 that lands as the city
 * line, a hairline and the six prayers across.
 *
 * Deliberately empty. It exists only because a `<receiver>` needs its own class — the picker shows
 * one entry per receiver, and the two entries differ solely in what
 * `res/xml/prayer_times_wide_widget_info.xml` declares (default cells, resize envelope, label).
 * [PrayerTimesWidgetProvider] already lays itself out from the reported size, so this shape needs
 * no rendering code of its own, and resizing either entry still gives the arrangement that fits.
 *
 * Not annotated `@AndroidEntryPoint`: the annotation on the superclass is what generates the
 * injecting base, and it injects this subclass's inherited `repository` field just the same.
 */
class PrayerTimesWideWidgetProvider : PrayerTimesWidgetProvider()
