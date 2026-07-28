package com.sangusantri.app.data.remote.api

import com.sangusantri.app.data.remote.dto.RemoteContentManifestDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

/** Backend content API client (section 7). The manifest is checked at most once every 24 hours
 * (the sync scheduler's own gate), so no conditional-request header is needed. */
interface ContentApiService {
    @GET("v1/content/manifest")
    suspend fun getManifest(): Response<RemoteContentManifestDto>

    // Streamed rather than buffered whole: ContentSyncManager copies the body to a size-limited
    // temporary file instead of assuming every package stays tiny forever.
    @Streaming
    @GET("v1/content/packages/{versionId}")
    suspend fun getPackage(
        @Path("versionId") versionId: String,
    ): Response<ResponseBody>
}
