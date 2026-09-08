package com.sangusantri.app.core.network

/**
 * A response body that hides its payload behind the service's own success flag, rather than relying
 * on the HTTP status alone.
 *
 * Two of this app's three upstreams do this, and each spells it differently: LPMQ Kemenag sends
 * `{"code": 200, "res": "success", "data": …}` and myquran sends `{"status": true, "data": …}`.
 * Both can return HTTP 200 while reporting failure in the body, so unwrapping is a real step and
 * not ceremony — skipping it is how a caller ends up persisting `data: null` as if it were content.
 *
 * The CMS returns raw bodies and does not implement this; [safeApiCall] handles it directly.
 *
 * `out T` so `ApiResult<QuranEnvelopeDto<X>>` is usable as `ApiResult<ApiEnvelope<X>>` at the
 * [unwrap] call site without a cast.
 */
interface ApiEnvelope<out T> {
    /** The payload, or `null` when the service reports failure (or sends nothing). */
    val payload: T?

    /** `null` when the envelope reports success; otherwise why it did not, for the failure reason. */
    val envelopeFailure: String?
}
