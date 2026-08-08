package com.sangusantri.app.data.remote.quran.api

import com.sangusantri.app.data.remote.quran.dto.QuranAyatDto
import com.sangusantri.app.data.remote.quran.dto.QuranEnvelopeDto
import com.sangusantri.app.data.remote.quran.dto.QuranSurahDto
import com.sangusantri.app.data.remote.quran.dto.QuranTafsirDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Official LPMQ Kemenag Quran API (`docs/engineering/QURAN_API_CONTRACT_DRAFT.md`), reached only
 * through the dedicated client built by `di/QuranNetworkModule.kt` — never the shared Firebase
 * Hosting content client (ADR 0016).
 */
interface QuranApiService {
    @GET("surah/local/{first}/{count}")
    suspend fun getSurahs(
        @Path("first") first: Int,
        @Path("count") count: Int,
    ): Response<QuranEnvelopeDto<List<QuranSurahDto>>>

    @GET("ayat/local/{noSurah}")
    suspend fun getAyat(
        @Path("noSurah") surahNumber: Int,
    ): Response<QuranEnvelopeDto<List<QuranAyatDto>>>

    @GET("ayat/local/tafsir/{ayatId}")
    suspend fun getTafsir(
        @Path("ayatId") remoteAyatId: Long,
    ): Response<QuranEnvelopeDto<List<QuranTafsirDto>>>
}
