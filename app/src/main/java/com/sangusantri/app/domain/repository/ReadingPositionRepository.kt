package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.ReadingPosition

/** Reads and writes the reader's last visible position per content id. */
interface ReadingPositionRepository {
    suspend fun getPosition(contentId: String): ReadingPosition?

    suspend fun getMostRecentPosition(): ReadingPosition?

    suspend fun savePosition(position: ReadingPosition)
}
