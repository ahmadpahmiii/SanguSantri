package com.sangusantri.app.data.remote.api

import com.sangusantri.app.data.content.dto.ContentCatalogDto
import com.sangusantri.app.data.content.dto.ContentFileDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Static Firebase Hosting content client (ADR 0015). The catalog is checked at most once every 24
 * hours (the sync scheduler's own gate), so no conditional-request header is needed. Both
 * endpoints serve static files, not a dynamic API — [getContent]'s `url` is the catalog item's own
 * `contentUrl`, not a templated path, since a static host has no path-parameter routing.
 */
interface ContentApiService {
    @GET("content/catalog.json")
    suspend fun getCatalog(): Response<ContentCatalogDto>

    @GET
    suspend fun getContent(
        @Url url: String,
    ): Response<ContentFileDto>
}
