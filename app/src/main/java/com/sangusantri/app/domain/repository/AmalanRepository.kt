package com.sangusantri.app.domain.repository

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Amalan Harian's own small preference store: which days were marked udzur, and which streak
 * milestones have already been celebrated.
 *
 * Deliberately not Room. This data must outlive the standing
 * `fallbackToDestructiveMigration(dropAllTables = true)` policy — a wipe that silently un-marked a
 * user's udzur days would rewrite her history as missed days — and it is a handful of dates, which
 * is exactly what DataStore is for.
 */
interface AmalanRepository {
    /** Every date marked udzur, including today when the toggle is currently on. */
    fun observeUdzurDates(): Flow<Set<LocalDate>>

    /** Whether the toggle is on right now. */
    fun observeUdzurActive(): Flow<Boolean>

    /** Turning it off closes the open range, so the days already spent in udzur stay marked. */
    suspend fun setUdzurActive(active: Boolean)

    fun observeCelebratedMilestones(): Flow<Set<Int>>

    suspend fun markMilestoneCelebrated(streakDays: Int)
}
