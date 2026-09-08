package com.sangusantri.app.core.network

import android.util.Log
import com.sangusantri.app.core.validation.Validation
import kotlinx.serialization.SerializationException
import retrofit2.Response
import java.io.IOException

private const val TAG = "ApiCall"

/**
 * The one way this app makes an HTTP call.
 *
 * Every endpoint needs the same five steps — catch [IOException], catch [SerializationException],
 * check `isSuccessful`, null-check `body()`, log the failure — and before this they were written
 * out by hand at every call site, each one free to forget a step or invent its own outcome type.
 *
 * [source] labels the call in both the log line and [ApiResult.Failure.reason], so a failure is
 * traceable without a stack trace ("amaliyah list HTTP 503", "surah 18 ayat network error").
 *
 * Deliberately no `withContext(Dispatchers.IO)` here: Retrofit's `suspend` functions are already
 * main-safe. Callers that follow a call with genuinely expensive CPU work — validating a
 * 6,236-ayat dataset, say — still own that decision, because only they know the cost.
 *
 * ### Security note
 * The reason string carries the source label and the status code, never a response body or header.
 * `IOException.message` at this layer is host/protocol detail (timeout, DNS, TLS) or this app's own
 * fixed interceptor messages — never a credential (`docs/security/SECURITY_BASELINE.md`).
 */
// Five guard clauses, one per way a call can fail. Folding them into a single expression would
// hide exactly the distinctions this function exists to draw.
@Suppress("ReturnCount")
suspend fun <T : Any> safeApiCall(
    source: String,
    call: suspend () -> Response<T>,
): ApiResult<T> {
    val response =
        try {
            call()
        } catch (io: IOException) {
            return failed(ApiResult.NetworkError("$source network error: ${io.reasonText()}"))
        } catch (malformed: SerializationException) {
            return failed(ApiResult.MalformedResponse("$source: unparseable body (${malformed.reasonText()})"))
        }

    if (!response.isSuccessful) {
        return failed(ApiResult.HttpError(response.code(), "$source HTTP ${response.code()}"))
    }
    val body = response.body() ?: return failed(ApiResult.MalformedResponse("$source: empty body"))
    return ApiResult.Success(body)
}

/**
 * Unwraps a service envelope, turning its own success flag into an [ApiResult.MalformedResponse].
 *
 * A failing envelope is never retryable: HTTP already said 200, so the transport is healthy and the
 * service is deliberately reporting something this app cannot use.
 */
fun <T : Any> ApiResult<ApiEnvelope<T>>.unwrap(source: String): ApiResult<T> = when (this) {
    is ApiResult.Failure -> this
    is ApiResult.Success -> {
        val failure = data.envelopeFailure
        val payload = data.payload
        when {
            failure != null -> failed(ApiResult.MalformedResponse("$source: $failure"))
            payload == null -> failed(ApiResult.MalformedResponse("$source: envelope carried no data"))
            else -> ApiResult.Success(payload)
        }
    }
}

/**
 * Rejects a payload that parsed but is structurally wrong, so validation failures reach the caller
 * in the same vocabulary as transport failures instead of a second, parallel one.
 *
 * Structural validity is a contract question, so a rejection is [ApiResult.MalformedResponse] and
 * never retryable — the same bytes would fail identically next time.
 */
fun <T : Any> ApiResult<T>.validate(
    source: String,
    validator: (T) -> Validation,
): ApiResult<T> = when (this) {
    is ApiResult.Failure -> this
    is ApiResult.Success ->
        when (val validation = validator(data)) {
            is Validation.Valid -> this
            is Validation.Invalid -> failed(ApiResult.MalformedResponse("$source: ${validation.reason}"))
        }
}

/** One log line per failure, at the one place every failure passes through. */
private fun failed(failure: ApiResult.Failure): ApiResult.Failure {
    Log.w(TAG, failure.reason)
    return failure
}

/** A message is often null (`SocketTimeoutException` in particular); the class name is the only
 * thing left that says what happened. */
private fun Throwable.reasonText(): String = message ?: this::class.java.simpleName
