package com.sangusantri.app

import android.app.Application
import android.util.Log
import com.sangusantri.app.data.local.seed.SeedContentImporter
import com.sangusantri.app.data.local.seed.SeedImportOutcome
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SanguSantriApplication : Application() {
    @Inject
    lateinit var seedContentImporter: SeedContentImporter

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Idempotent and non-blocking (PRD 8.1): Serambi observes Room reactively and
        // renders as soon as rows exist, so this must not gate the first frame.
        applicationScope.launch {
            seedContentImporter.importSeedContent().forEach { outcome ->
                when (outcome) {
                    is SeedImportOutcome.Failed ->
                        Log.w(
                            TAG,
                            "Seed import failed for ${outcome.versionId}: ${outcome.reason}",
                        )

                    is SeedImportOutcome.Imported -> Log.d(TAG, "Seed import: imported ${outcome.versionId}")
                    is SeedImportOutcome.AlreadyImported ->
                        Log.d(
                            TAG,
                            "Seed import: already imported ${outcome.versionId}",
                        )
                }
            }
        }
    }

    private companion object {
        const val TAG = "SanguSantriApplication"
    }
}
