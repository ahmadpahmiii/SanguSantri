package com.sangusantri.app.core.telemetry

/**
 * Strategy for initializing the application's logging framework.
 *
 * This abstraction allows [com.sangusantri.app.SanguSantriApplication] to initialize logging
 * without a direct dependency on Timber, which is only available in debug builds.
 */
interface LoggingInitializer {
    fun init()
}
