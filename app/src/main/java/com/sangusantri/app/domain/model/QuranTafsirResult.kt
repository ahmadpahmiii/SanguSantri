package com.sangusantri.app.domain.model

/** Outcome of [com.sangusantri.app.domain.repository.QuranRepository.fetchTafsir]. */
sealed interface QuranTafsirResult {
    data class Success(
        val tafsir: QuranTafsir,
    ) : QuranTafsirResult

    data class Failure(
        val retryable: Boolean,
    ) : QuranTafsirResult
}
