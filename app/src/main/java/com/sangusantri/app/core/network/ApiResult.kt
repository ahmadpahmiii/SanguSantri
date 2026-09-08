package com.sangusantri.app.core.network

/**
 * The outcome of one HTTP call, for every endpoint this app talks to.
 *
 * Before this existed there were eight vocabularies for the same three questions — `SyncResult`,
 * `QuranSyncResult`, `QuranTafsirFetchOutcome`, `CityDetection`, `kotlin.Result`, and a bare
 * `Boolean` among them — so a caller could not reason about a failure without first learning which
 * dialect its data source happened to speak.
 *
 * The three questions a caller ever actually asks:
 *
 * 1. Did I get the payload? — [Success].
 * 2. Is it worth trying again? — [Failure.isRetryable]. A dropped connection or a 503 is worth a
 *    retry; a 404 or a body this app cannot parse is not, and retrying it just burns battery.
 * 3. What do I say about it? — [Failure.reason], already carrying the source label.
 *
 * Deliberately *not* modelling "empty" as a failure: an endpoint returning zero items is a valid
 * answer, and whether that is acceptable is a question for the layer that knows the domain
 * (`ContentLocalDataSource.deactivateAbsent` refuses an empty published set; a search endpoint would not).
 */
sealed interface ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>

    /** Everything that is not a usable payload. Grouped so callers can handle failure once. */
    sealed interface Failure : ApiResult<Nothing> {
        val reason: String

        /**
         * Whether the *same* request could plausibly succeed later. This is the only thing
         * WorkManager retry policy and "Coba lagi" buttons need to know, and computing it here is
         * what stops each caller re-deriving it from a status code.
         */
        val isRetryable: Boolean
    }

    /** No usable response at all: no connectivity, DNS failure, timeout, TLS failure. */
    data class NetworkError(override val reason: String) : Failure {
        override val isRetryable: Boolean get() = true
    }

    /** A response arrived, with a status this app cannot use. */
    data class HttpError(val code: Int, override val reason: String) : Failure {
        override val isRetryable: Boolean get() = isRetryableHttpStatus(code)
    }

    /**
     * A `2xx` whose body this app cannot trust: unparseable JSON, an absent body, an envelope
     * whose own success flag says no, or a payload that failed structural validation.
     *
     * Never retryable. The server is answering successfully and consistently — it is the *contract*
     * that is broken, and hammering it changes nothing. This is the case that needs a released app
     * update or a CMS fix, so it must be visible in logs rather than hidden behind a retry loop.
     */
    data class MalformedResponse(override val reason: String) : Failure {
        override val isRetryable: Boolean get() = false
    }
}

/** Applies [transform] to a successful payload, passing any failure through untouched. */
inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.Failure -> this
}

/** The payload, or `null` for any failure — for callers that genuinely have nothing to say about
 * why, such as a best-effort refresh behind an already-populated cache. */
fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.data

/**
 * HTTP statuses worth retrying: request timeout, rate limiting, and any server error.
 *
 * A top-level function so it is directly JVM-unit-testable, and so [ApiResult.HttpError] can use it
 * without a dependency on anything that constructs one.
 */
fun isRetryableHttpStatus(code: Int): Boolean = code in TRANSIENT_HTTP_CODES || code >= HTTP_SERVER_ERROR

private val TRANSIENT_HTTP_CODES = setOf(408, 429)
private const val HTTP_SERVER_ERROR = 500
