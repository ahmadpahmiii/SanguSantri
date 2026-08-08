package com.sangusantri.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One official LPMQ Kemenag surah record (`docs/product/QURAN_PRD.md`, `0.0.6`). */
@Entity(tableName = "quran_surahs")
data class QuranSurahEntity(
    @PrimaryKey val number: Int,
    val latinName: String,
    val arabicName: String,
    val meaning: String,
    val categoryArabic: String,
    val category: String,
    val ayatCount: Int,
)
