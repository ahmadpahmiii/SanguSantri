package com.sangusantri.app.core.telemetry

import javax.inject.Inject

class LoggingInitializerImpl @Inject constructor() : LoggingInitializer {
    override fun init() {
        // No-op in release
    }
}
