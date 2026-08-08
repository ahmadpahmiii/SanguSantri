package com.sangusantri.app.data.remote.quran

/**
 * The `username`/`token` header pair Kemenag requires (ADR 0016 §9). Never logged, never rendered,
 * never stored — held only in memory for the process lifetime by
 * [com.sangusantri.app.data.remote.quran.QuranCredentialProvider].
 */
data class QuranCredential(
    val username: String,
    val token: String,
)
