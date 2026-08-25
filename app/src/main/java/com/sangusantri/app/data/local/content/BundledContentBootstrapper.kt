package com.sangusantri.app.data.local.content

import android.content.Context
import com.sangusantri.app.data.content.ContentImportOutcome
import com.sangusantri.app.data.content.ContentImporter
import com.sangusantri.app.data.content.ContentValidation
import com.sangusantri.app.data.content.ContentValidator
import com.sangusantri.app.data.content.ContentVersionAction
import com.sangusantri.app.data.content.decideContentVersionAction
import com.sangusantri.app.data.content.dto.ContentCatalogDto
import com.sangusantri.app.data.content.dto.ContentCatalogItemDto
import com.sangusantri.app.data.content.dto.ContentFileDto
import com.sangusantri.app.data.local.content.BundledContentBootstrapper.Companion.CONTENT_ASSET_DIR
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Reads bundled content straight from [android.content.res.AssetManager] (no source-abstraction
 * interface — this is the only bundled-storage implementation there is) and imports it through
 * the shared [ContentImporter], using exactly the same catalog contract the CMS API serves
 * (ADR 0015) — `app/src/main/assets/content/catalog.json` plus its `packages/` content files.
 * Guarantees offline
 * content on a fresh install, recovers content if remote hosting has never been reached, and
 * reconciles the bundled baseline against whatever remote sync has already put in Room without
 * ever downgrading it (PRD 3.2, FR-001).
 *
 * Idempotent and safe to call on every launch: metadata is refreshed unconditionally and cheaply
 * for every catalog item, while each item's content file is only read from
 * [android.content.res.AssetManager] when its version is actually newer than what Room already
 * holds (an older or already-current bundled entry never touches the asset file at all).
 */
class BundledContentBootstrapper
@Inject
constructor(
    @param:ApplicationContext private val context: Context,
    private val contentImporter: ContentImporter,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun bootstrap(): List<ContentImportOutcome> =
        withContext(Dispatchers.IO) {
            val catalog = readCatalog()
            when {
                catalog == null ->
                    listOf(ContentImportOutcome.Rejected(null, "unable to read or parse bundled catalog.json"))

                else -> {
                    val validation = ContentValidator.validateCatalog(catalog)
                    if (validation is ContentValidation.Invalid) {
                        listOf(ContentImportOutcome.Rejected(null, "invalid bundled catalog: ${validation.reason}"))
                    } else {
                        catalog.items.map { item -> evaluate(item) }
                    }
                }
            }
        }

    /**
     * The metadata refresh is gated on the version comparison, not run ahead of it.
     *
     * It used to run unconditionally, which meant the bundled catalog overwrote title,
     * category, order and isActive on every cold start — including for items the CMS had
     * already replaced with newer content, whose own content import this method then correctly
     * skipped as older. The visible symptom was Tahlil and Istighosah reverting from their CMS
     * category ("Amaliyah") to the bundled one ("Tahlil dan Doa") every launch, and flipping
     * back only when the once-a-day sync next ran.
     *
     * SKIP_OLDER means the local copy came from the CMS and is ahead of the bundle, so the
     * bundle is not the authority on any of its fields. IMPORT rewrites the whole row anyway.
     */
    private suspend fun evaluate(item: ContentCatalogItemDto): ContentImportOutcome {
        val localVersion = contentImporter.localVersion(item.id)
        return when (decideContentVersionAction(item.version, localVersion)) {
            ContentVersionAction.SKIP_OLDER -> ContentImportOutcome.SkippedOlderVersion(item.id, localVersion ?: 0)
            ContentVersionAction.SKIP_UP_TO_DATE -> {
                contentImporter.refreshCatalogMetadata(item)
                ContentImportOutcome.SkippedUpToDate(item.id)
            }

            ContentVersionAction.IMPORT -> readAndImport(item)
        }
    }

    private suspend fun readAndImport(item: ContentCatalogItemDto): ContentImportOutcome =
        runCatching {
            val file =
                json.decodeFromString<ContentFileDto>(readAsset(assetPath(item.contentUrl)).decodeToString())
            contentImporter.importContentFile(item, file)
        }.getOrElse {
            ContentImportOutcome.Rejected(
                item.id,
                "unable to read bundled content file ${item.contentUrl}: ${it.message}",
            )
        }

    private fun readCatalog(): ContentCatalogDto? =
        runCatching {
            json.decodeFromString<ContentCatalogDto>(readAsset(CATALOG_FILE_NAME).decodeToString())
        }.getOrNull()

    private fun readAsset(relativePath: String): ByteArray =
        context.assets.open("$CONTENT_ASSET_DIR/$relativePath").use {
            it.readBytes()
        }

    /**
     * `contentUrl` is always rooted at "/content/..." (the bundled asset path
     * convention, ADR 0015); bundled assets are already rooted at [CONTENT_ASSET_DIR], so the
     * leading segment is stripped to resolve the equivalent asset-relative path.
     */
    private fun assetPath(contentUrl: String): String = contentUrl.removePrefix("/$CONTENT_ASSET_DIR/")

    private companion object {
        const val CONTENT_ASSET_DIR = "content"
        const val CATALOG_FILE_NAME = "catalog.json"
    }
}
