package com.sangusantri.app.domain.model

/** One of a question's four fixed answer slots. */
data class NahwuQuizOption(
    val key: NahwuQuizOptionKey,
    val text: String,
)

/**
 * One bundled multiple-choice question (`0.0.5`). [correctOption] is part of this model because
 * the whole bank is already fully present in Room on an offline device — there is no server round
 * trip to protect it from. UI state built from this model must still withhold [correctOption]
 * until the user submits an answer (see `NahwuQuizSessionUiState`), matching the design spec's
 * "no sensitive answer-key data in raw UI state" annotation even though the underlying storage
 * has no such constraint.
 */
data class NahwuQuizQuestion(
    val id: String,
    val packageId: String,
    val order: Int,
    val stem: String,
    val options: List<NahwuQuizOption>,
    val correctOption: NahwuQuizOptionKey,
    val explanation: String?,
)
