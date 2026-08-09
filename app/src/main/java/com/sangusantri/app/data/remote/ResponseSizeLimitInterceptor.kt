package com.sangusantri.app.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.IOException
import okio.Source
import okio.buffer

/**
 * Rejects any response body larger than [maxBytes], for every [ContentApiService] call (catalog
 * and content files alike). ADR 0015's `ContentApiService.getContent` returns `Response<ContentFileDto>`
 * directly (Retrofit's converter parses the body), which has no natural per-call interception
 * point for a manual size cap the way streaming raw [okhttp3.ResponseBody] bytes to a temp file
 * used to — this interceptor preserves the same response-size security control
 * (`docs/security/SECURITY_BASELINE.md`) at the OkHttp layer instead, transparently for every
 * request. Checks `Content-Length` first when present, and also enforces the limit while the body
 * is actually read, since a missing or understated `Content-Length` must not bypass the cap.
 */
class ResponseSizeLimitInterceptor(
    private val maxBytes: Long = DEFAULT_MAX_BYTES,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.body ?: return response
        if (body.contentLength() > maxBytes) {
            response.close()
            throw IOException("response body exceeds $maxBytes bytes (Content-Length: ${body.contentLength()})")
        }
        return response.newBuilder().body(SizeLimitedResponseBody(body, maxBytes)).build()
    }

    private class SizeLimitedResponseBody(
        private val delegate: ResponseBody,
        private val maxBytes: Long,
    ) : ResponseBody() {
        private val limitedSource by lazy { SizeLimitedSource(delegate.source(), maxBytes).buffer() }

        override fun contentType(): MediaType? = delegate.contentType()

        override fun contentLength(): Long = delegate.contentLength()

        override fun source(): BufferedSource = limitedSource
    }

    private class SizeLimitedSource(
        delegate: Source,
        private val maxBytes: Long,
    ) : ForwardingSource(delegate) {
        private var totalRead = 0L

        override fun read(
            sink: Buffer,
            byteCount: Long,
        ): Long {
            val read = super.read(sink, byteCount)
            if (read > 0) {
                totalRead += read
                if (totalRead > maxBytes) {
                    throw IOException("response body exceeds $maxBytes bytes")
                }
            }
            return read
        }
    }

    companion object {
        const val DEFAULT_MAX_BYTES = 5L * 1024 * 1024
    }
}
