package com.sangusantri.app.data.remote.ayat

import com.sangusantri.app.data.remote.ayat.dto.AyatHariIniItemDto
import com.sangusantri.app.domain.model.AyatHariIniSelection
import java.time.LocalDate

/**
 * Turns published items into selections, dropping anything malformed rather than failing the whole
 * sync.
 *
 * One bad row in the CMS must not cost the reader the other ninety days — the app is offline-first
 * and a rejected sync leaves it on a stale cache. What is *not* tolerated is a plausible-looking
 * reference: surah and ayat bounds are checked here, and the repository additionally refuses to
 * display a reference the local Kemenag dataset cannot resolve, so an out-of-range ayat becomes a
 * blank section rather than a wrong quotation.
 */
object AyatHariIniValidator {
    const val SUPPORTED_SCHEMA_VERSION = 1

    fun validate(items: List<AyatHariIniItemDto>): List<AyatHariIniSelection> =
        items.mapNotNull { item ->
            val date = runCatching { LocalDate.parse(item.date) }.getOrNull()
            when {
                date == null -> null
                item.surah !in SURAH_RANGE -> null
                item.ayat < MIN_AYAT_NUMBER -> null
                else ->
                    AyatHariIniSelection(
                        date = date,
                        surahNumber = item.surah,
                        ayatNumber = item.ayat,
                        theme = item.theme?.trim()?.takeIf(String::isNotEmpty),
                    )
            }
        }

    private val SURAH_RANGE = 1..114
    private const val MIN_AYAT_NUMBER = 1
}
