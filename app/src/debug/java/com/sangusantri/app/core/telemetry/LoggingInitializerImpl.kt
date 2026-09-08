package com.sangusantri.app.core.telemetry

import timber.log.Timber
import javax.inject.Inject

class LoggingInitializerImpl @Inject constructor() : LoggingInitializer {
    override fun init() {
        Timber.plant(Timber.DebugTree())
    }
}
