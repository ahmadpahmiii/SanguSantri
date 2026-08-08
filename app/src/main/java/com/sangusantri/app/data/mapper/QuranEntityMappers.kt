package com.sangusantri.app.data.mapper

import com.sangusantri.app.data.local.entity.QuranBookmarkEntity
import com.sangusantri.app.data.local.entity.QuranReadingSessionEntity
import com.sangusantri.app.data.local.entity.QuranReadingStateEntity
import com.sangusantri.app.data.local.entity.QuranSurahEntity
import com.sangusantri.app.data.local.entity.QuranTafsirEntity
import com.sangusantri.app.data.local.entity.QuranVerseEntity
import com.sangusantri.app.data.remote.quran.dto.QuranAyatDto
import com.sangusantri.app.data.remote.quran.dto.QuranSurahDto
import com.sangusantri.app.data.remote.quran.dto.QuranTafsirDto
import com.sangusantri.app.domain.model.QuranBookmark
import com.sangusantri.app.domain.model.QuranReadingSession
import com.sangusantri.app.domain.model.QuranReadingState
import com.sangusantri.app.domain.model.QuranSurah
import com.sangusantri.app.domain.model.QuranTafsir
import com.sangusantri.app.domain.model.QuranVerse

// Wire-to-entity mappers preserve every official source field exactly (`CLAUDE.md` Content
// safety) — the API's Latin `teks` transliteration is the one field intentionally not carried
// across (QUR-FR-009).

fun QuranSurahDto.toEntity(): QuranSurahEntity =
    QuranSurahEntity(
        number = id,
        latinName = nama,
        arabicName = arabic,
        meaning = arti,
        categoryArabic = kategoriAr,
        category = kategori,
        ayatCount = jumlahAyat,
    )

fun QuranAyatDto.toEntity(): QuranVerseEntity =
    QuranVerseEntity(
        surahNumber = surah,
        ayatNumber = ayat,
        remoteId = id,
        juz = juz,
        page = halaman,
        arabicText = teksMsiUsmani,
        arabicTextNoHarakat = teksGundul,
        translation = terjemah,
        note = keterangan,
        footnoteNumber = noFoot,
        footnoteText = teksFoot,
    )

fun QuranTafsirDto.toEntity(cachedAtEpochMillis: Long): QuranTafsirEntity =
    QuranTafsirEntity(
        remoteAyatId = id,
        ringkas = teks,
        tahlili = tahlili,
        cachedAtEpochMillis = cachedAtEpochMillis,
    )

fun QuranSurahEntity.toDomain(): QuranSurah =
    QuranSurah(
        number = number,
        latinName = latinName,
        arabicName = arabicName,
        meaning = meaning,
        categoryArabic = categoryArabic,
        category = category,
        ayatCount = ayatCount,
    )

fun QuranVerseEntity.toDomain(): QuranVerse =
    QuranVerse(
        surahNumber = surahNumber,
        ayatNumber = ayatNumber,
        remoteId = remoteId,
        juz = juz,
        page = page,
        arabicText = arabicText,
        arabicTextNoHarakat = arabicTextNoHarakat,
        translation = translation,
        note = note,
        footnoteNumber = footnoteNumber,
        footnoteText = footnoteText,
    )

fun QuranTafsirEntity.toDomain(): QuranTafsir =
    QuranTafsir(
        remoteAyatId = remoteAyatId,
        ringkas = ringkas,
        tahlili = tahlili,
        cachedAtEpochMillis = cachedAtEpochMillis,
    )

fun QuranBookmarkEntity.toDomain(): QuranBookmark =
    QuranBookmark(
        surahNumber = surahNumber,
        ayatNumber = ayatNumber,
        createdAtEpochMillis = createdAtEpochMillis,
    )

fun QuranReadingStateEntity.toDomain(): QuranReadingState =
    QuranReadingState(
        surahNumber = surahNumber,
        ayatNumber = ayatNumber,
        page = page,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )

fun QuranReadingSessionEntity.toDomain(): QuranReadingSession =
    QuranReadingSession(
        id = id,
        surahNumber = surahNumber,
        startAyat = startAyat,
        endAyat = endAyat,
        readAtEpochMillis = readAtEpochMillis,
    )
