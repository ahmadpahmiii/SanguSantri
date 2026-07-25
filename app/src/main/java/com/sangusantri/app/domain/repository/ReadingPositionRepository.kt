package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.ReadingPosition

/** Reads and writes the reader's last visible position per immutable content version. */
interface ReadingPositionRepository {
    suspend fun getPosition(versionId: String): ReadingPosition?

    suspend fun savePosition(position: ReadingPosition)
}
