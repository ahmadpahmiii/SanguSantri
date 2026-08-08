package com.sangusantri.app.data.sync.quran

import android.util.Log
import com.sangusantri.app.data.local.dao.QuranTafsirDao
import com.sangusantri.app.data.local.entity.QuranTafsirEntity
import com.sangusantri.app.data.mapper.toEntity
import com.sangusantri.app.data.remote.quran.QuranValidation
import com.sangusantri.app.data.remote.quran.QuranValidator
import com.sangusantri.app.data.remote.quran.api.QuranApiService
import com.sangusantri.app.data.sync.isRetryableHttpStatus
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

sealed interface QuranTafsirFetchOutcome {
    data class Success(
        val entity: QuranTafsirEntity,
    ) : QuranTafsirFetchOutcome

    data class Failure(
        val retryable: Boolean,
    ) : QuranTafsirFetchOutcome
}

/** On-demand tafsir fetch and cache (QUR-FR-013) — mirrors [QuranSyncManager]'s fetch/validate
 * shape but for one ayat's tafsir rather than the whole 114-surah dataset. */
class QuranTafsirManager
@Inject
constructor(
    private val api: QuranApiService,
    private val tafsirDao: QuranTafsirDao,
) {
    suspend fun getCached(remoteAyatId: Long): QuranTafsirEntity? = tafsirDao.getByRemoteAyatId(remoteAyatId)

    @Suppress("ReturnCount")
    suspend fun fetchAndCache(remoteAyatId: Long): QuranTafsirFetchOutcome {
        val response =
            try {
                api.getTafsir(remoteAyatId)
            } catch (io: IOException) {
                Log.w(TAG, "tafsir fetch failed for ayat $remoteAyatId", io)
                return QuranTafsirFetchOutcome.Failure(retryable = true)
            } catch (malformed: SerializationException) {
                Log.w(TAG, "tafsir fetch malformed for ayat $remoteAyatId", malformed)
                return QuranTafsirFetchOutcome.Failure(retryable = false)
            }
        if (!response.isSuccessful) {
            return QuranTafsirFetchOutcome.Failure(retryable = isRetryableHttpStatus(response.code()))
        }
        val envelope = response.body() ?: return QuranTafsirFetchOutcome.Failure(retryable = false)
        if (QuranValidator.validateEnvelope(envelope.code, envelope.res) is QuranValidation.Invalid) {
            return QuranTafsirFetchOutcome.Failure(retryable = false)
        }
        val dto =
            envelope.data.firstOrNull { it.id == remoteAyatId }
                ?: return QuranTafsirFetchOutcome.Failure(retryable = false)
        val entity = dto.toEntity(cachedAtEpochMillis = System.currentTimeMillis())
        tafsirDao.upsert(entity)
        return QuranTafsirFetchOutcome.Success(entity)
    }

    private companion object {
        const val TAG = "QuranTafsirManager"
    }
}
