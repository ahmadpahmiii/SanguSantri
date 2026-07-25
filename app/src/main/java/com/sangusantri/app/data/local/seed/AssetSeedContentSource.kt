package com.sangusantri.app.data.local.seed

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val CONTENT_ASSET_DIR = "content"
private const val MANIFEST_FILE_NAME = "manifest.json"

/** Reads bundled seed content from `app/src/main/assets/content/` (PRD 12.2). */
class AssetSeedContentSource
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : SeedContentSource {
        override fun readManifest(): ByteArray = readAsset(MANIFEST_FILE_NAME)

        override fun readPackage(fileName: String): ByteArray = readAsset(fileName)

        private fun readAsset(fileName: String): ByteArray =
            context.assets.open("$CONTENT_ASSET_DIR/$fileName").use { it.readBytes() }
    }
