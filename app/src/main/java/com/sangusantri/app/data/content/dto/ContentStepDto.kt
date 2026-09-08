package com.sangusantri.app.data.content.dto

import kotlinx.serialization.Serializable

/**
 * One ordered reading step of a [ContentDetailDto]. Array order is the step's position — there is
 * no explicit `position` field in the wire format: `steps.mapIndexed { index, step ->
 * step.toEntity(position = index + 1) }`.
 */
@Serializable
data class ContentStepDto(
    val id: String,
    val arabicText: String,
    val translation: String,
    /**
     * `null` (or absent) when the step has no repetition count: no tasbih counter for the step, and
     * when no step in the item has one, no Panduan mode for the item at all. Sholawat is the
     * motivating case — verses are read straight through, not counted.
     */
    val repeatTarget: Int? = null,
)
