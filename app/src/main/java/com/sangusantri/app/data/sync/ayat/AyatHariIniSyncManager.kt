package com.sangusantri.app.data.sync.ayat

import com.sangusantri.app.data.local.dao.AyatHariIniDao
import com.sangusantri.app.data.local.entity.AyatHariIniEntity
import com.sangusantri.app.data.remote.ayat.AyatHariIniRemoteSource
import com.sangusantri.app.data.remote.ayat.AyatHariIniValidator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

/**
 * Pulls the published schedule into Room.
 *
 * **Cheap to call, so callers do not have to be clever.** Beranda asks on every launch; this returns
 * immediately when Room already holds today's entry, so a normal day costs no request at all. The
 * mutex covers the case where Beranda and the widget both ask within the same second of a cold
 * start.
 *
 * **A failed fetch costs nothing that was already there.** Room is written only on success, so a
 * launch with no network leaves the cached window untouched and the reader keeps whatever the last
 * successful sync delivered.
 *
 * The staleness this accepts is deliberate: once today is cached, an editor's later change to
 * *today's* entry is not picked up until tomorrow. Chasing it would mean fetching on every launch
 * for a value that changes once a day. Scheduling is meant to be done ahead of time, and the brief
 * says so.
 */
class AyatHariIniSyncManager
@Inject
constructor(
    private val remoteSource: AyatHariIniRemoteSource,
    private val dao: AyatHariIniDao,
) {
    private val mutex = Mutex()

    suspend fun syncIfNeeded(today: LocalDate = LocalDate.now()): Result<Unit> =
        mutex.withLock {
            if (dao.getByEpochDay(today.toEpochDay()) != null) {
                Result.success(Unit)
            } else {
                sync(today)
            }
        }

    private suspend fun sync(today: LocalDate): Result<Unit> =
        runCatching {
            val response = remoteSource.fetchSchedule()
            val body = response.body()
            if (!response.isSuccessful || body == null) {
                throw IOException("ayat-hari-ini schedule unavailable (HTTP ${response.code()})")
            }
            // An unknown schema version means the CMS moved on without the app. Keeping the existing
            // cache is the safe outcome: a reader sees yesterday's schedule continue rather than an
            // empty header, and an app update fixes it.
            if (body.schemaVersion != AyatHariIniValidator.SUPPORTED_SCHEMA_VERSION) {
                throw IOException("unsupported ayat-hari-ini schemaVersion ${body.schemaVersion}")
            }
            val entries =
                AyatHariIniValidator.validate(body.items).map { selection ->
                    AyatHariIniEntity(
                        epochDay = selection.date.toEpochDay(),
                        surahNumber = selection.surahNumber,
                        ayatNumber = selection.ayatNumber,
                        theme = selection.theme,
                    )
                }
            dao.replaceFrom(entries, pruneBeforeEpochDay = today.minusDays(RETENTION_DAYS).toEpochDay())
        }

    private companion object {
        /** How much of the past is kept. Not for display — Beranda only ever asks for today — but
         * as the material the offline fallback reads when a new day arrives with no network. One
         * row per day makes this free. */
        const val RETENTION_DAYS = 90L
    }
}
