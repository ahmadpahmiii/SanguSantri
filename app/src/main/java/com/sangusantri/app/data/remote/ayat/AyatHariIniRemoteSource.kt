package com.sangusantri.app.data.remote.ayat

import com.sangusantri.app.core.network.ApiResult
import com.sangusantri.app.core.network.safeApiCall
import com.sangusantri.app.core.network.validate
import com.sangusantri.app.core.validation.Validation
import com.sangusantri.app.data.remote.ayat.dto.AyatHariIniScheduleDto
import javax.inject.Inject

/**
 * The client for the published schedule.
 *
 * A plain class, not an interface with implementations: the interface here only ever existed to
 * let `FixtureAyatHariIniRemoteSource` stand in while the CMS endpoint was being built. The
 * endpoint ships, so both the fixture and the seam are gone — `CODING_STANDARD.md` would call a
 * one-implementation interface unearned, and it was.
 */
class AyatHariIniRemoteSource @Inject constructor(private val service: AyatHariIniApiService) {
    /**
     * The published schedule, fetched and schema-checked.
     *
     * An unknown schema version is [ApiResult.MalformedResponse] and therefore never retried: the
     * CMS has moved on without the app, and only a released update fixes it. The caller keeps its
     * existing cache, so a reader sees yesterday's quote continue rather than an empty header —
     * which is what made the version 1 → 2 change safe to ship on either side first.
     */
    suspend fun fetchSchedule(): ApiResult<AyatHariIniScheduleDto> =
        safeApiCall(SOURCE) { service.getSchedule() }.validate(SOURCE) { body ->
            Validation.of(
                "unsupported schemaVersion ${body.schemaVersion}"
                    .takeIf { body.schemaVersion != AyatHariIniValidator.SUPPORTED_SCHEMA_VERSION },
            )
        }

    private companion object {
        const val SOURCE = "ayat-hari-ini"
    }
}
