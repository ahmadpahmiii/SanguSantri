package com.sangusantri.app.domain.model

import java.time.LocalDate

/**
 * One ayat of the day, as it is displayed.
 *
 * **The reference is editorial, the text is not.** Which ayat appears on a given day is decided by
 * a person in the CMS and delivered as a bare `(surahNumber, ayatNumber)` pair; the Arabic and the
 * translation are then read out of the locally stored LPMQ Kemenag dataset. Nothing in this model
 * ever comes from the CMS as scripture, which is what keeps Kemenag the single source of Quran text
 * (ADR 0016 §2) and makes it impossible for a CMS mistake to publish altered Arabic.
 *
 * [theme] is the one genuinely editorial field: a short label the editor may attach ("Sabar",
 * "Syukur"). It is optional and carries no religious claim — it is not a category of the ayat, it
 * is the reason the editor scheduled it.
 */
data class AyatHariIni(
    val surahNumber: Int,
    val surahName: String,
    val ayatNumber: Int,
    val arabicText: String,
    val translation: String,
    val theme: String? = null,
) {
    /** "Ar-Ra'd : 28" — the Indonesian mushaf convention, spaces around the colon. */
    val reference: String get() = "$surahName : $ayatNumber"
}

/**
 * What the CMS actually publishes for one date: a reference and nothing more.
 *
 * Deliberately a separate type from [AyatHariIni]. This is the editorial decision as stored and
 * synced; [AyatHariIni] is that decision after the Kemenag text has been attached to it. Collapsing
 * the two would make it possible to construct a displayable ayat without going through the local
 * dataset.
 */
data class AyatHariIniSelection(
    val date: LocalDate,
    val surahNumber: Int,
    val ayatNumber: Int,
    val theme: String? = null,
)
