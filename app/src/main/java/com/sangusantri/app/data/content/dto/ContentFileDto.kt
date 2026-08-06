package com.sangusantri.app.data.content.dto

import kotlinx.serialization.Serializable

/**
 * One content file (`packages/{id}-v{n}.json`, ADR 0015) — a single content item's ordered
 * reading steps plus source attribution. Shared verbatim between bundled assets and Firebase
 * Hosting. `id`/`version` are repeated here (not just in the catalog) so [ContentImporter] can
 * verify a fetched file actually matches the catalog entry that named it.
 */
@Serializable
data class ContentFileDto(
    val schemaVersion: Int,
    val id: String,
    val version: Int,
    val sourceName: String,
    val sourceUrl: String,
    val steps: List<ContentStepDto>,
)

/**
 * One ordered reading step. Array order is the step's position — there is no explicit `position`
 * field in the wire format (ADR 0015): `steps.mapIndexed { index, step -> step.toEntity(position
 * = index + 1) }`.
 */
@Serializable
data class ContentStepDto(
    val id: String,
    val arabicText: String,
    val translation: String,
    val repeatTarget: Int,
)
