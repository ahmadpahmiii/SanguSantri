package com.sangusantri.app.domain.model

/**
 * How a surah's start is introduced in the reader (Beranda/Quran revamp handoff §4).
 *
 * [TENANG] is the default: a centred Arabic surah name, one muted caps line, a short hairline, and
 * the basmalah set in the same Arabic reading face as the text below it. [BAND] keeps the previous
 * three-column metadata band and the basmalah drawable, which review found read "like a stamp from
 * another kitab" — retained as an explicit user choice, not as the default.
 */
enum class QuranSurahHeaderVariant {
    TENANG,
    BAND,
}
