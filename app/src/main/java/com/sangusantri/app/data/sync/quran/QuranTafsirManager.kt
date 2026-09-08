package com.sangusantri.app.data.sync.quran

import com.sangusantri.app.core.network.ApiResult
import com.sangusantri.app.core.network.map
import com.sangusantri.app.core.network.safeApiCall
import com.sangusantri.app.core.network.unwrap
import com.sangusantri.app.core.network.validate
import com.sangusantri.app.core.validation.Validation
import com.sangusantri.app.data.local.dao.QuranTafsirDao
import com.sangusantri.app.data.local.entity.QuranTafsirEntity
import com.sangusantri.app.data.mapper.toEntity
import com.sangusantri.app.data.remote.quran.api.QuranApiService
import com.sangusantri.app.data.remote.quran.dto.QuranTafsirDto
import javax.inject.Inject

/**
 * On-demand tafsir fetch and cache (QUR-FR-013) — one ayat's tafsir rather than
 * [QuranSyncManager]'s whole 114-surah dataset, but the same fetch/unwrap/validate/persist shape.
 *
 * The endpoint answers with a *list*, so "the ayat I asked for is not in it" is a validation
 * failure like any other, rather than a special case each caller has to know about. The envelope
 * check that used to be spelled out here is now [unwrap], shared with every other Kemenag call.
 */
class QuranTafsirManager @Inject constructor(private val api: QuranApiService, private val tafsirDao: QuranTafsirDao) {
    suspend fun getCached(remoteAyatId: Long): QuranTafsirEntity? = tafsirDao.getByRemoteAyatId(remoteAyatId)

    suspend fun fetchAndCache(remoteAyatId: Long): ApiResult<QuranTafsirEntity> {
        val source = "tafsir $remoteAyatId"
        val result =
            safeApiCall(source) { api.getTafsir(remoteAyatId) }
                .unwrap(source)
                .validate(source) { tafsirs -> validate(tafsirs, remoteAyatId) }
                .map { tafsirs ->
                    tafsirs.first { it.id == remoteAyatId }.toEntity(cachedAtEpochMillis = System.currentTimeMillis())
                }

        if (result is ApiResult.Success) tafsirDao.upsert(result.data)
        return result
    }

    /** Blank text counts as absent, not as an empty tafsir: caching it would leave the reader
     * looking at a blank sheet with no way to ask again. */
    private fun validate(
        tafsirs: List<QuranTafsirDto>,
        remoteAyatId: Long,
    ): Validation {
        val match = tafsirs.firstOrNull { it.id == remoteAyatId }
        return Validation.of(
            when {
                match == null -> "response carried no tafsir for ayat $remoteAyatId"
                match.teks.isBlank() || match.tahlili.isBlank() -> "tafsir is blank"
                else -> null
            },
        )
    }
}
