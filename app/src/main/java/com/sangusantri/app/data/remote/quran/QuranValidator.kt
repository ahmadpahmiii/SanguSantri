package com.sangusantri.app.data.remote.quran

import com.sangusantri.app.data.remote.quran.QuranValidator.validateAyatForSurah
import com.sangusantri.app.data.remote.quran.dto.QuranAyatDto
import com.sangusantri.app.data.remote.quran.dto.QuranSurahDto

sealed interface QuranValidation {
    data object Valid : QuranValidation

    data class Invalid(
        val reason: String,
    ) : QuranValidation
}

/**
 * Pure structural validation of LPMQ Kemenag responses, run before any candidate dataset is
 * committed to Room (QUR-FR-003). This is technical validation only — it never repairs, merges, or
 * invents religious content; an invalid candidate is rejected wholesale by the caller
 * ([com.sangusantri.app.data.sync.quran.QuranSyncManager]), never partially accepted.
 *
 * The observed Surah 114 example arrived out of numeric order (ayat 2 before ayat 1,
 * `docs/engineering/QURAN_API_CONTRACT_DRAFT.md`) — [validateAyatForSurah] deliberately validates
 * the *set* of ayat numbers against the expected `1..jmlAyat` range regardless of array order; the
 * caller is responsible for sorting the already-validated list before persistence.
 */
object QuranValidator {
    const val EXPECTED_SURAH_COUNT = 114
    private const val ENVELOPE_SUCCESS_CODE = 200
    private const val ENVELOPE_SUCCESS_RES = "success"

    fun validateEnvelope(
        code: Int,
        res: String,
    ): QuranValidation =
        if (code == ENVELOPE_SUCCESS_CODE && res == ENVELOPE_SUCCESS_RES) {
            QuranValidation.Valid
        } else {
            QuranValidation.Invalid("unsuccessful envelope (code=$code, res=$res)")
        }

    @Suppress("ReturnCount")
    fun validateSurahList(surahs: List<QuranSurahDto>): QuranValidation {
        if (surahs.size != EXPECTED_SURAH_COUNT) {
            return QuranValidation.Invalid("expected $EXPECTED_SURAH_COUNT surahs, got ${surahs.size}")
        }
        val ids = surahs.map { it.id }
        if (ids.distinct().size != ids.size) return QuranValidation.Invalid("duplicate surah id")
        if (ids.sorted() != (1..EXPECTED_SURAH_COUNT).toList()) {
            return QuranValidation.Invalid("surah ids are not exactly 1..$EXPECTED_SURAH_COUNT")
        }
        val invalidSurah = surahs.firstOrNull { it.jumlahAyat <= 0 || it.nama.isBlank() || it.arabic.isBlank() }
        if (invalidSurah != null) return QuranValidation.Invalid("surah ${invalidSurah.id} metadata is incomplete")
        return QuranValidation.Valid
    }

    @Suppress("ReturnCount")
    fun validateAyatForSurah(
        surah: QuranSurahDto,
        ayats: List<QuranAyatDto>,
    ): QuranValidation {
        val wrongSurah = ayats.firstOrNull { it.surah != surah.id }
        if (wrongSurah != null) {
            return QuranValidation.Invalid(
                "ayat ${wrongSurah.id} belongs to surah ${wrongSurah.surah}, expected ${surah.id}",
            )
        }
        val ayatNumbers = ayats.map { it.ayat }
        if (ayatNumbers.distinct().size != ayatNumbers.size) {
            return QuranValidation.Invalid("duplicate ayat number in surah ${surah.id}")
        }
        if (ayatNumbers.sorted() != (1..surah.jumlahAyat).toList()) {
            return QuranValidation.Invalid(
                "surah ${surah.id} ayat numbers are not exactly 1..${surah.jumlahAyat}, got ${ayatNumbers.sorted()}",
            )
        }
        val remoteIds = ayats.map { it.id }
        if (remoteIds.distinct().size != remoteIds.size) {
            return QuranValidation.Invalid("duplicate remote ayat id within surah ${surah.id}")
        }
        return ayats.firstNotNullOfOrNull(::validateAyatFields) ?: QuranValidation.Valid
    }

    private fun validateAyatFields(ayat: QuranAyatDto): QuranValidation? {
        val identity = "${ayat.surah}:${ayat.ayat}"
        return when {
            ayat.teksMsiUsmani.isBlank() -> QuranValidation.Invalid("ayat $identity arabic text is blank")
            ayat.terjemah.isBlank() -> QuranValidation.Invalid("ayat $identity translation is blank")
            ayat.juz !in 1..30 -> QuranValidation.Invalid("ayat $identity juz must be within 1..30")
            ayat.halaman <= 0 -> QuranValidation.Invalid("ayat $identity halaman must be positive")
            else -> null
        }
    }

    /** Cross-surah uniqueness [validateAyatForSurah] cannot see on its own: remote ayat ids must be
     * unique across the whole 114-surah candidate, not merely within one surah. */
    fun validateGlobalUniqueness(allAyats: List<QuranAyatDto>): QuranValidation {
        val remoteIds = allAyats.map { it.id }
        return if (remoteIds.distinct().size != remoteIds.size) {
            QuranValidation.Invalid("duplicate remote ayat id across surahs")
        } else {
            QuranValidation.Valid
        }
    }
}
