package com.sangusantri.app.domain.model

import java.time.LocalDate

/**
 * One quote of the day, as it is displayed.
 *
 * **The text comes from the CMS, and that is a deliberate reversal.** The first version of this
 * feature delivered a bare `(surahNumber, ayatNumber)` reference and read the words out of the
 * locally stored LPMQ Kemenag dataset, so that Kemenag stayed the single source of Qur'an text
 * (ADR 0016 §2). The product owner has since chosen flexibility over that guarantee: an editor
 * writes the quotation by hand, which is what lets the same feature carry a hadith or a plain
 * saying as well as an ayat. ADR 0016's amendment records the trade and its consequence — a typo
 * in the CMS now ships as scripture, and only editorial process catches it.
 *
 * [kind] exists for presentation only. It says how the editor filed the quotation, never what
 * authority it carries; the app must not render it as a claim.
 *
 * [sourceLabel] is what the reader sees as the citation — "QS. Al-Jumu'ah : 1",
 * "HR. Bukhari no. 6114". It is always present, because a quotation nobody can trace is not
 * publishable.
 *
 * [arabic] is null for a quotation that simply is not in Arabic, which `other` may well not be.
 * Every surface here has to handle its absence rather than assume a script line exists.
 */
data class AyatHariIni(
    val kind: QuoteKind,
    val arabic: String?,
    val translation: String,
    val sourceLabel: String,
    val sourceNote: String? = null,
    val theme: String? = null,
)

/** How the editor filed a quotation. Presentation only — see [AyatHariIni]. */
enum class QuoteKind {
    QURAN,
    HADITH,
    OTHER,
    ;

    companion object {
        /** Unknown values become [OTHER] rather than dropping the quote: the field is decorative,
         * and losing a day's reading over a value this app has not been taught yet would be a
         * worse outcome than filing it plainly. */
        fun fromWire(value: String?): QuoteKind = when (value?.lowercase()) {
            "quran" -> QURAN
            "hadith" -> HADITH
            else -> OTHER
        }
    }
}

/**
 * What the CMS publishes for one date.
 *
 * Still a separate type from [AyatHariIni] even though they now carry the same words. This is the
 * editorial decision as synced and stored, keyed by its date; [AyatHariIni] is that decision once
 * a language has been chosen for the reader's device. Keeping them apart is what stops the
 * translation choice from being made silently in three different places.
 */
data class AyatHariIniSelection(
    val date: LocalDate,
    val kind: QuoteKind,
    val arabic: String?,
    val translationId: String,
    val translationEn: String?,
    val sourceLabel: String,
    val sourceNote: String?,
    val theme: String?,
)
