package com.sangusantri.app.data.remote.ayat

import com.sangusantri.app.data.remote.ayat.dto.AyatHariIniScheduleDto
import retrofit2.Response
import javax.inject.Inject

/**
 * The client for the published schedule.
 *
 * A plain class, not an interface with implementations: the interface here only ever existed to
 * let `FixtureAyatHariIniRemoteSource` stand in while the CMS endpoint was being built. The
 * endpoint ships, so both the fixture and the seam are gone — `CODING_STANDARD.md` would call a
 * one-implementation interface unearned, and it was.
 */
class AyatHariIniRemoteSource
@Inject
constructor(
    private val service: AyatHariIniApiService,
) {
    suspend fun fetchSchedule(): Response<AyatHariIniScheduleDto> = service.getSchedule()
}
