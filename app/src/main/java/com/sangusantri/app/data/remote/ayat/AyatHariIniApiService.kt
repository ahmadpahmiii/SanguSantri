package com.sangusantri.app.data.remote.ayat

import com.sangusantri.app.data.remote.ayat.dto.AyatHariIniScheduleDto
import retrofit2.Response
import retrofit2.http.GET

/**
 * Read-only, like every other route on the Content API (`../cms/docs/engineering/API.md`): the app
 * never writes a schedule, it only reads what an editor published.
 */
interface AyatHariIniApiService {
    @GET("api/v1/ayat-hari-ini")
    suspend fun getSchedule(): Response<AyatHariIniScheduleDto>
}
