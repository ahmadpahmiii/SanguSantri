package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.AyatHariIni
import java.time.LocalDate

/**
 * The editorially scheduled ayat of the day (`docs/product/AYAT_HARI_INI.md`).
 *
 * Room is the source of truth as everywhere else: [forDate] never touches the network, so Beranda
 * and the home-screen widget both read the same cached schedule and both work offline. [sync] is
 * the only thing that talks to the CMS.
 */
interface AyatHariIniRepository {
    /**
     * The ayat scheduled for [date], with its Kemenag text attached, or `null` when the CMS has
     * published nothing for that day or the Quran dataset has not been downloaded yet. Callers
     * render nothing in that case — Beranda's standing rule that a section with no data is not
     * rendered.
     */
    suspend fun forDate(date: LocalDate): AyatHariIni?

    /**
     * Refreshes the cached schedule from the CMS. Safe to call on every launch: it no-ops when the
     * cache already covers today, so a normal day costs no request.
     */
    suspend fun sync(): Result<Unit>
}
