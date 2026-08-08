package com.sangusantri.app.data.remote.quran

import com.sangusantri.app.data.remote.quran.dto.QuranAyatDto
import com.sangusantri.app.data.remote.quran.dto.QuranSurahDto
import org.junit.Assert.assertTrue
import org.junit.Test

class QuranValidatorTest {
    @Test
    fun validEnvelopePasses() {
        assertTrue(QuranValidator.validateEnvelope(code = 200, res = "success") is QuranValidation.Valid)
    }

    @Test
    fun wrongCodeEnvelopeIsRejected() {
        assertInvalid(QuranValidator.validateEnvelope(code = 500, res = "success"))
    }

    @Test
    fun wrongResEnvelopeIsRejected() {
        assertInvalid(QuranValidator.validateEnvelope(code = 200, res = "error"))
    }

    @Test
    fun completeSurahListPasses() {
        assertTrue(QuranValidator.validateSurahList(fullSurahList()) is QuranValidation.Valid)
    }

    @Test
    fun surahListWithFewerThan114SurahsIsRejected() {
        assertInvalid(QuranValidator.validateSurahList(fullSurahList().dropLast(1)))
    }

    @Test
    fun surahListWithDuplicateIdIsRejected() {
        val list = fullSurahList().toMutableList()
        list[1] = list[1].copy(id = list[0].id)
        assertInvalid(QuranValidator.validateSurahList(list))
    }

    @Test
    fun surahListMissingASurahNumberIsRejected() {
        val list = fullSurahList().toMutableList()
        // Replace surah 50 with a duplicate of surah 1, so the id set is no longer exactly 1..114.
        list[49] = list[49].copy(id = 1)
        assertInvalid(QuranValidator.validateSurahList(list))
    }

    /** The supplied Surah 114 response arrived as ayat 2, 1, 3, 4, 5, 6 —
     * `docs/engineering/QURAN_API_CONTRACT_DRAFT.md`. Validation must accept this: canonical order
     * is derived by the caller sorting on the numeric `ayat` field, never trusted from array order. */
    @Test
    fun outOfOrderSurah114AyatPasses() {
        val surah114 = surah(id = 114, jumlahAyat = 6)
        val outOfOrderAyat =
            listOf(2, 1, 3, 4, 5, 6).map { number -> ayat(surah = 114, ayat = number, remoteId = 6230L + number) }

        val result = QuranValidator.validateAyatForSurah(surah114, outOfOrderAyat)

        assertTrue(result is QuranValidation.Valid)
    }

    @Test
    fun missingAyatNumberIsRejected() {
        val surah114 = surah(id = 114, jumlahAyat = 6)
        val missingAyat4 =
            listOf(1, 2, 3, 5, 6).map { number -> ayat(surah = 114, ayat = number, remoteId = 6230L + number) }

        assertInvalid(QuranValidator.validateAyatForSurah(surah114, missingAyat4))
    }

    @Test
    fun duplicateAyatNumberIsRejected() {
        val surah114 = surah(id = 114, jumlahAyat = 6)
        val duplicateAyat2 =
            listOf(1, 2, 2, 4, 5, 6).map { number -> ayat(surah = 114, ayat = number, remoteId = 6230L + number) }

        assertInvalid(QuranValidator.validateAyatForSurah(surah114, duplicateAyat2))
    }

    @Test
    fun ayatBelongingToWrongSurahIsRejected() {
        val surah114 = surah(id = 114, jumlahAyat = 6)
        val ayats =
            listOf(1, 2, 3, 4, 5).map { number -> ayat(surah = 114, ayat = number, remoteId = 6230L + number) } +
                ayat(surah = 113, ayat = 6, remoteId = 6236L)

        assertInvalid(QuranValidator.validateAyatForSurah(surah114, ayats))
    }

    @Test
    fun duplicateRemoteIdWithinSurahIsRejected() {
        val surah114 = surah(id = 114, jumlahAyat = 6)
        val ayats =
            listOf(1, 2, 3, 4, 5).map { number -> ayat(surah = 114, ayat = number, remoteId = 6230L + number) } +
                ayat(surah = 114, ayat = 6, remoteId = 6231L)

        assertInvalid(QuranValidator.validateAyatForSurah(surah114, ayats))
    }

    @Test
    fun blankArabicTextIsRejected() {
        val surah114 = surah(id = 114, jumlahAyat = 1)
        val ayats = listOf(ayat(surah = 114, ayat = 1, remoteId = 6231L, teksMsiUsmani = " "))

        assertInvalid(QuranValidator.validateAyatForSurah(surah114, ayats))
    }

    @Test
    fun blankTranslationIsRejected() {
        val surah114 = surah(id = 114, jumlahAyat = 1)
        val ayats = listOf(ayat(surah = 114, ayat = 1, remoteId = 6231L, terjemah = " "))

        assertInvalid(QuranValidator.validateAyatForSurah(surah114, ayats))
    }

    @Test
    fun nonPositiveJuzIsRejected() {
        val surah114 = surah(id = 114, jumlahAyat = 1)
        val ayats = listOf(ayat(surah = 114, ayat = 1, remoteId = 6231L, juz = 0))

        assertInvalid(QuranValidator.validateAyatForSurah(surah114, ayats))
    }

    @Test
    fun nonPositiveHalamanIsRejected() {
        val surah114 = surah(id = 114, jumlahAyat = 1)
        val ayats = listOf(ayat(surah = 114, ayat = 1, remoteId = 6231L, halaman = 0))

        assertInvalid(QuranValidator.validateAyatForSurah(surah114, ayats))
    }

    @Test
    fun globallyUniqueRemoteIdsPass() {
        val allAyats = (1L..10L).map { id -> ayat(surah = 1, ayat = id.toInt(), remoteId = id) }

        assertTrue(QuranValidator.validateGlobalUniqueness(allAyats) is QuranValidation.Valid)
    }

    @Test
    fun duplicateRemoteIdAcrossSurahsIsRejected() {
        val allAyats =
            listOf(
                ayat(surah = 1, ayat = 1, remoteId = 1L),
                ayat(surah = 2, ayat = 1, remoteId = 1L),
            )

        assertInvalid(QuranValidator.validateGlobalUniqueness(allAyats))
    }

    private fun assertInvalid(result: QuranValidation) {
        assertTrue(result is QuranValidation.Invalid)
    }

    private fun fullSurahList(): List<QuranSurahDto> = (1..QuranValidator.EXPECTED_SURAH_COUNT).map { id -> surah(id) }

    private fun surah(
        id: Int,
        jumlahAyat: Int = 6,
    ) = QuranSurahDto(
        id = id,
        nama = "[FIXTURE] Surah $id",
        arabic = "[FIXTURE-AR]",
        arti = "[FIXTURE]",
        kategoriAr = "[FIXTURE-AR]",
        kategori = "Madaniyyah",
        jumlahAyat = jumlahAyat,
    )

    @Suppress("LongParameterList")
    private fun ayat(
        surah: Int,
        ayat: Int,
        remoteId: Long,
        juz: Int = 30,
        halaman: Int = 604,
        teksMsiUsmani: String = "[FIXTURE-AR]",
        terjemah: String = "[FIXTURE]",
    ) = QuranAyatDto(
        id = remoteId,
        surah = surah,
        ayat = ayat,
        juz = juz,
        halaman = halaman,
        teksMsiUsmani = teksMsiUsmani,
        teksGundul = "[FIXTURE-AR]",
        teksLatin = "[FIXTURE-LATIN]",
        keterangan = "",
        terjemah = terjemah,
        noFoot = "",
        teksFoot = "",
    )
}
