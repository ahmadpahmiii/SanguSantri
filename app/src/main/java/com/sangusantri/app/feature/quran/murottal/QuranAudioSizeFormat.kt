package com.sangusantri.app.feature.quran.murottal

import java.util.Locale

/** Byte sizes as the design writes them — "4,2 MB", Indonesian decimal comma. */
internal fun Long.asAudioSize(): String {
    val indonesian = Locale.forLanguageTag("id-ID")
    return when {
        this >= MEGABYTE -> String.format(indonesian, "%.1f MB", toDouble() / MEGABYTE)
        this >= KILOBYTE -> String.format(indonesian, "%.0f kB", toDouble() / KILOBYTE)
        else -> String.format(indonesian, "%d B", this)
    }
}

private const val KILOBYTE = 1024.0
private const val MEGABYTE = 1024.0 * 1024.0
