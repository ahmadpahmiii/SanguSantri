package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.TasbihHistoryEntry
import com.sangusantri.app.domain.model.TasbihSession
import com.sangusantri.app.domain.model.TasbihTargetPreset
import kotlinx.coroutines.flow.Flow

/** Reads and writes the Standalone Tasbih (0.0.2) active session and archived history via Room. */
interface TasbihRepository {
    fun observeSession(): Flow<TasbihSession?>

    /**
     * Increments the active session, creating an unlimited session first if none exists yet.
     * Tapping again after the target is reached starts a new repetition cycle (count resets to
     * 1) rather than counting past the target indefinitely.
     */
    suspend fun incrementCount()

    /** Starts a fresh count (0) against the given target, preserving any existing session name. */
    suspend fun startSession(
        targetPreset: TasbihTargetPreset,
        targetValue: Int?,
    )

    suspend fun renameSession(sessionName: String?)

    /** Archives the current session into history when it has a non-zero count, then clears it. */
    suspend fun resetSession()

    fun observeHistory(): Flow<List<TasbihHistoryEntry>>
}
