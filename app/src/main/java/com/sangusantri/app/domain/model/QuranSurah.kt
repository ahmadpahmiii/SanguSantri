package com.sangusantri.app.domain.model

/** One official LPMQ Kemenag surah record (`docs/engineering/QURAN_API_CONTRACT_DRAFT.md`). */
data class QuranSurah(
    val number: Int,
    val latinName: String,
    val arabicName: String,
    val meaning: String,
    val categoryArabic: String,
    val category: String,
    val ayatCount: Int,
)
