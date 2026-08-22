package com.sangusantri.app.data.remote.ayat

import com.sangusantri.app.data.remote.ayat.dto.AyatHariIniItemDto
import com.sangusantri.app.data.remote.ayat.dto.AyatHariIniScheduleDto
import retrofit2.Response
import java.time.LocalDate
import javax.inject.Inject

/**
 * Where the published schedule comes from.
 *
 * An interface with two implementations is normally the kind of thing `CODING_STANDARD.md` warns
 * against, and it exists here for one concrete reason: the CMS endpoint does not exist yet
 * (`docs/product/AYAT_HARI_INI_CMS_BRIEF.md` is the brief for building it). Everything above this
 * line — Room, the sync manager, the repository, Beranda, the widget — is finished and running
 * against [FixtureAyatHariIniRemoteSource]; switching to the real endpoint is one line in
 * `di/AyatHariIniModule`. Delete the fixture and this interface with it once that is done.
 */
interface AyatHariIniRemoteSource {
    suspend fun fetchSchedule(): Response<AyatHariIniScheduleDto>
}

/** The real client. Not yet bound — see the note on [AyatHariIniRemoteSource]. */
class ApiAyatHariIniRemoteSource
@Inject
constructor(
    private val service: AyatHariIniApiService,
) : AyatHariIniRemoteSource {
    override suspend fun fetchSchedule(): Response<AyatHariIniScheduleDto> = service.getSchedule()
}

/**
 * **Development stand-in — must not ship.** Answers as the endpoint will, so the layers above it are
 * exercised for real, but it is not a schedule: it returns the same single entry for every date.
 *
 * It holds exactly one selection, the one the app resolved on the day this was written
 * (QS. Al-Jumu'ah : 1). Filling it with a spread of "good" ayat would be choosing which verses of
 * the Qur'an deserve to be highlighted, and that is precisely the editorial act this whole change
 * exists to move to a person in the CMS — `CLAUDE.md` Content safety forbids the app's authors, and
 * an AI in particular, from making it.
 *
 * Note that even this fixture publishes no scripture: `surah`/`ayat` are a pointer, and the words
 * still come from the local Kemenag dataset.
 */
class FixtureAyatHariIniRemoteSource
@Inject
constructor() : AyatHariIniRemoteSource {
    override suspend fun fetchSchedule(): Response<AyatHariIniScheduleDto> =
        Response.success(
            AyatHariIniScheduleDto(
                schemaVersion = SCHEMA_VERSION,
                items =
                    listOf(
                        AyatHariIniItemDto(
                            date = LocalDate.now().toString(),
                            surah = FIXTURE_SURAH,
                            ayat = FIXTURE_AYAT,
                            theme = null,
                        ),
                    ),
            ),
        )

    private companion object {
        const val SCHEMA_VERSION = 1

        /** QS. Al-Jumu'ah : 1 — what the deterministic selector this replaced resolved to on
         * 2026-08-22, kept so the fixture points at a real, already-displayed ayat rather than one
         * picked here. */
        const val FIXTURE_SURAH = 62
        const val FIXTURE_AYAT = 1
    }
}
