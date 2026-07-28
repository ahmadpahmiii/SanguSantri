package com.sangusantri.app.data.remote

import com.sangusantri.app.data.remote.dto.RemoteContentManifestDto

/** Typed remote-fetch failure — never a raw exception, HTTP code, or Retrofit type outside
 * [ContentRemoteDataSource]. */
sealed interface RemoteContentFailure {
    /** [java.io.IOException]/timeout/DNS failure — transient, worth retrying. */
    data object NoConnectivityOrTimeout : RemoteContentFailure

    data class HttpStatus(
        val code: Int,
    ) : RemoteContentFailure

    data class PayloadTooLarge(
        val limitBytes: Long,
    ) : RemoteContentFailure

    /** Empty body, undecodable JSON, or another response-shape problem — not retriable as-is. */
    data class MalformedBody(
        val reason: String,
    ) : RemoteContentFailure
}

sealed interface ManifestFetchOutcome {
    data class Fetched(
        val manifest: RemoteContentManifestDto,
        val etag: String?,
    ) : ManifestFetchOutcome

    data object NotModified : ManifestFetchOutcome

    data class Failed(
        val failure: RemoteContentFailure,
    ) : ManifestFetchOutcome
}

sealed interface PackageFetchOutcome {
    data class Fetched(
        val bytes: ByteArray,
    ) : PackageFetchOutcome

    data class Failed(
        val failure: RemoteContentFailure,
    ) : PackageFetchOutcome
}
