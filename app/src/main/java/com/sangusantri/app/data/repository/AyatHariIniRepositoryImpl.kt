package com.sangusantri.app.data.repository

import com.sangusantri.app.core.network.ApiResult
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import com.sangusantri.app.data.local.entity.AyatHariIniEntity
import com.sangusantri.app.data.sync.ayat.AyatHariIniSyncManager
import com.sangusantri.app.domain.model.AyatHariIni
import com.sangusantri.app.domain.model.QuoteKind
import com.sangusantri.app.domain.repository.AyatHariIniRepository
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

class AyatHariIniRepositoryImpl @Inject constructor(
    private val database: SanguSantriDatabase,
    private val syncManager: AyatHariIniSyncManager,
) : AyatHariIniRepository {
    private val scheduleDao get() = database.ayatHariIniDao()

    /**
     * Offline-first, in two senses.
     *
     * **It never waits on the network.** Only Room is read here; whether a sync succeeded this
     * launch is not this method's business.
     *
     * **A day with no entry falls back rather than going blank.** If the device rolls into a date
     * the cached window does not cover and the refresh fails, the most recent published quote is
     * shown instead of nothing. That is a deliberate trade: the header is a day or two behind
     * rather than empty, and the app is offline-first everywhere else for the same reason.
     *
     * There is no longer a Kemenag join, and with it went the safety net that used to sit here: a
     * reference the local dataset could not resolve was refused, so a bad schedule produced a blank
     * section rather than a wrong quotation. Under schema version 2 the CMS sends the words
     * themselves and this layer has nothing left to check them against. What is displayed is what
     * an editor typed — see ADR 0016's amendment.
     */
    override suspend fun forDate(date: LocalDate): AyatHariIni? {
        val epochDay = date.toEpochDay()
        val scheduled =
            scheduleDao.getByEpochDay(epochDay) ?: scheduleDao.getLatestOnOrBefore(epochDay)
        return scheduled?.toDomain()
    }

    override suspend fun sync(): ApiResult<Unit> = syncManager.syncIfNeeded()
}

/**
 * Picks the translation for the device's language, falling back to Indonesian.
 *
 * Read at call time rather than cached: the row holds both translations precisely so that a
 * language change is a re-read and never a re-sync.
 */
private fun AyatHariIniEntity.toDomain(): AyatHariIni {
    val prefersEnglish = Locale.getDefault().language == Locale.ENGLISH.language
    return AyatHariIni(
        kind = runCatching { QuoteKind.valueOf(kind) }.getOrDefault(QuoteKind.OTHER),
        arabic = arabic,
        translation = translationEn?.takeIf { prefersEnglish } ?: translationId,
        sourceLabel = sourceLabel,
        sourceNote = sourceNote,
        theme = theme,
    )
}
