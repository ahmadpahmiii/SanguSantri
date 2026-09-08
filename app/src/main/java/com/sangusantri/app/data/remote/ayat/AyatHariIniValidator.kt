package com.sangusantri.app.data.remote.ayat

import com.sangusantri.app.data.remote.ayat.dto.AyatHariIniItemDto
import com.sangusantri.app.domain.model.AyatHariIniSelection
import com.sangusantri.app.domain.model.QuoteKind
import java.time.LocalDate

/**
 * Turns published items into selections, dropping anything malformed rather than failing the whole
 * sync.
 *
 * One bad row in the CMS must not cost the reader the other ninety days — the app is offline-first
 * and a rejected sync leaves it on a stale cache.
 *
 * What is checked has changed with schema version 2, and it is worth being explicit about what can
 * no longer be checked at all. Version 1 published a surah and an ayat number, so the app could
 * verify the reference against its own Kemenag dataset and refuse anything that did not resolve —
 * a wrong reference could never become wrong scripture. The text now arrives as text, and no
 * amount of validation here can tell a correct ayat from a mistyped one. So this checks the two
 * things that remain checkable: a date the app can actually file the quote under, and the presence
 * of the two fields nothing can be displayed without.
 */
object AyatHariIniValidator {
    const val SUPPORTED_SCHEMA_VERSION = 2

    fun validate(items: List<AyatHariIniItemDto>): List<AyatHariIniSelection> = items.mapNotNull { item ->
        val date = runCatching { LocalDate.parse(item.date) }.getOrNull()
        val translationId = item.translation.id.trim()
        val sourceLabel = item.sourceLabel.trim()
        when {
            date == null -> null
            // The base-language translation is the one field every surface renders.
            translationId.isEmpty() -> null
            // A quotation with no citation is exactly what the CMS's NOT NULL constraint
            // exists to prevent; refusing it here too means a hand-edited row cannot slip
            // an uncited hadith onto a reader's home screen.
            sourceLabel.isEmpty() -> null
            else ->
                AyatHariIniSelection(
                    date = date,
                    kind = QuoteKind.fromWire(item.kind),
                    arabic = item.arabic?.trim()?.takeIf(String::isNotEmpty),
                    translationId = translationId,
                    translationEn =
                        item.translation.en
                            ?.trim()
                            ?.takeIf(String::isNotEmpty),
                    sourceLabel = sourceLabel,
                    sourceNote = item.sourceNote?.trim()?.takeIf(String::isNotEmpty),
                    theme = item.theme?.trim()?.takeIf(String::isNotEmpty),
                )
        }
    }
}
