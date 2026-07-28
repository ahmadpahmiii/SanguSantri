package com.sangusantri.app.data.remote.api

import com.sangusantri.app.data.remote.dto.RemoteContentManifestDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Streaming

/**
 * Backend content API client (section 7). A Retrofit header is never used as a local/remote
 * content-source switch — `If-None-Match` is the real conditional-request header, nothing more.
 */
interface ContentApiService {
    @GET("v1/content/manifest")
    suspend fun getManifest(
        @Header("If-None-Match") ifNoneMatch: String?,
    ): Response<RemoteContentManifestDto>

    // Streamed rather than buffered whole: ContentRemoteDataSource copies the body to a
    // size-limited temporary file instead of assuming every package stays tiny forever.
    @Streaming
    @GET("v1/content/packages/{versionId}")
    suspend fun getPackage(
        @Path("versionId") versionId: String,
    ): Response<ResponseBody>
}
