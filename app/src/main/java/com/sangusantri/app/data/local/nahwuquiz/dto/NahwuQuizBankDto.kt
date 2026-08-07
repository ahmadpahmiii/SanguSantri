package com.sangusantri.app.data.local.nahwuquiz.dto

import kotlinx.serialization.Serializable

/** Wire format for `assets/nahwu_quiz/nahwu_quiz_bank.json` — the whole bundled question bank in
 * one file, no separate catalog/content-file split (unlike `data/content/`): the roadmap scope is
 * "bundled static JSON question bank", not remote sync, so there is no second source to reconcile
 * against and no per-item version field to gate a fetch on. */
@Serializable
data class NahwuQuizBankDto(
    val schemaVersion: Int,
    val packages: List<NahwuQuizPackageDto>,
)

@Serializable
data class NahwuQuizPackageDto(
    val id: String,
    val title: String,
    val description: String,
    val order: Int,
    val isActive: Boolean = true,
    val questions: List<NahwuQuizQuestionDto> = emptyList(),
)

@Serializable
data class NahwuQuizQuestionDto(
    val id: String,
    val stem: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    /** One of `"A"`/`"B"`/`"C"`/`"D"` — validated and parsed to [com.sangusantri.app.domain.model.NahwuQuizOptionKey]
     * by `NahwuQuizValidator`/`NahwuQuizDtoMapper`. */
    val correctOption: String,
    val explanation: String? = null,
)
