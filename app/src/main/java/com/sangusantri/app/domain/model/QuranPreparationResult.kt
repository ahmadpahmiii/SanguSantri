package com.sangusantri.app.domain.model

/** Outcome of [com.sangusantri.app.domain.repository.QuranRepository.ensureInitialPreparation]
 * or `.refreshIfStale()` — a domain-safe projection of the data-layer sync result. */
sealed interface QuranPreparationResult {
    data object Ready : QuranPreparationResult

    data class Failed(
        val retryable: Boolean,
    ) : QuranPreparationResult
}
