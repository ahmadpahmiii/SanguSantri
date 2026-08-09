package com.sangusantri.app.domain.model

/** Domain-safe outcome of the explicit first-use Quran preparation. */
sealed interface QuranPreparationResult {
    data object Ready : QuranPreparationResult

    data class Failed(
        val retryable: Boolean,
        val reason: String,
    ) : QuranPreparationResult
}
