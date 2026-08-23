package com.sangusantri.app.data.content.dto

import kotlinx.serialization.Serializable

/**
 * One category listing from the CMS API — `GET /api/v1/sholawat` or
 * `GET /api/v1/amaliyah` (`schemaVersion` 3). Display metadata only: enough to draw a card and
 * open it, with no steps and no source attribution.
 *
 * This is the payload re-fetched every time Beranda resumes, which is why it is kept this thin.
 * Size is the lesser reason; the real one is the `ETag`. When steps were inlined here, correcting
 * one word in one item changed the whole category's bytes and therefore its validator, so every
 * device re-downloaded every step of every item in that category. Now a step edit moves only that
 * item's own detail validator, and the list stays untouched.
 *
 * Deliberately *not* the same type as [ContentCatalogDto], which is the bundled-asset format
 * (`app/src/main/assets/content/catalog.json`, `schemaVersion` 1) — that one still points at local
 * package files through `contentUrl` and still carries a `version` integer. The bundled layout is
 * a local file convention with no reason to track the wire.
 */
@Serializable
data class ContentListResponseDto(
    val schemaVersion: Int,
    val items: List<ContentListItemDto>,
)

@Serializable
data class ContentListItemDto(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val category: String? = null,
    val order: Int,
)

/**
 * One item and everything needed to read it — `GET /api/v1/sholawat/{id}` or
 * `GET /api/v1/amaliyah/{id}` (`schemaVersion` 3).
 *
 * It repeats the list fields rather than making the client join the two, so a detail fetched on tap
 * can render the whole screen on its own — including for an item whose list entry this device has
 * not seen yet.
 *
 * There is no `version`. The server stopped versioning items; whether this detail differs from the
 * stored copy is decided on-device, in
 * [com.sangusantri.app.data.content.ContentImporter.importRemoteDetail], and whether it is worth
 * re-downloading at all is decided by HTTP (`ETag`/`If-None-Match`, handled by the OkHttp cache in
 * [com.sangusantri.app.di.NetworkModule]).
 */
@Serializable
data class ContentDetailDto(
    val schemaVersion: Int,
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val category: String? = null,
    val order: Int,
    val sourceName: String,
    val sourceUrl: String,
    val steps: List<ContentStepDto>,
)
