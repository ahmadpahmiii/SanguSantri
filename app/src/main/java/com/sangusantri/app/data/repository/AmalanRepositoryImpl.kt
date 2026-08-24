package com.sangusantri.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.sangusantri.app.data.repository.AmalanRepositoryImpl.Companion.UDZUR_DATES
import com.sangusantri.app.data.repository.AmalanRepositoryImpl.Companion.UDZUR_SINCE
import com.sangusantri.app.domain.repository.AmalanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

/**
 * Udzur days and celebrated milestones, in the app's single canonical preferences DataStore.
 *
 * An open udzur period is stored as its start date rather than by writing a row every midnight:
 * [UDZUR_SINCE] plus today is expanded on read, so a phone left untouched for a week still reports
 * those days correctly, and turning the toggle off is what freezes the range into [UDZUR_DATES].
 */
class AmalanRepositoryImpl
@Inject
constructor(
    private val dataStore: DataStore<Preferences>,
) : AmalanRepository {
    private val preferences: Flow<Preferences> =
        dataStore.data.catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }

    override fun observeUdzurDates(): Flow<Set<LocalDate>> =
        preferences.map { stored ->
            val closed = stored[UDZUR_DATES].orEmpty().mapNotNull(::parseDate)
            closed.toSet() + openRange(stored[UDZUR_SINCE])
        }

    override fun observeUdzurActive(): Flow<Boolean> = preferences.map { it[UDZUR_SINCE] != null }

    /**
     * Turning it off ends udzur **today**, not yesterday: the days already spent in it stay
     * marked, but today becomes an ordinary 2-of-2 day again. Keeping today marked would leave
     * the card showing "sedang udzur" next to a switch that is off, and would also tell a woman
     * whose udzur has just ended that she still cannot read — both wrong.
     */
    override suspend fun setUdzurActive(active: Boolean) {
        dataStore.edit { stored ->
            if (active) {
                if (stored[UDZUR_SINCE] == null) stored[UDZUR_SINCE] = LocalDate.now().toString()
            } else {
                val today = LocalDate.now()
                val closing =
                    openRange(stored[UDZUR_SINCE])
                        .filter { it.isBefore(today) }
                        .map(LocalDate::toString)
                stored[UDZUR_DATES] = stored[UDZUR_DATES].orEmpty() + closing
                stored.remove(UDZUR_SINCE)
            }
        }
    }

    override fun observeCelebratedMilestones(): Flow<Set<Int>> =
        preferences.map { stored -> stored[MILESTONES].orEmpty().mapNotNull(String::toIntOrNull).toSet() }

    override suspend fun markMilestoneCelebrated(streakDays: Int) {
        dataStore.edit { stored ->
            stored[MILESTONES] = stored[MILESTONES].orEmpty() + streakDays.toString()
        }
    }

    /**
     * The days covered by an open udzur period: its start through today, inclusive. A start in
     * the future (a device clock moved backwards) yields just that day, never an empty walk.
     */
    private fun openRange(since: String?): Set<LocalDate> {
        val start = since?.let(::parseDate) ?: return emptySet()
        val today = maxOf(LocalDate.now(), start)
        return generateSequence(start) { day -> day.plusDays(1).takeIf { !it.isAfter(today) } }.toSet()
    }

    /** A malformed date is dropped rather than crashing the screen it feeds. */
    private fun parseDate(value: String): LocalDate? = runCatching { LocalDate.parse(value) }.getOrNull()

    private companion object {
        val UDZUR_SINCE = stringPreferencesKey("amalan_udzur_since")
        val UDZUR_DATES = stringSetPreferencesKey("amalan_udzur_dates")
        val MILESTONES = stringSetPreferencesKey("amalan_celebrated_milestones")
    }
}
