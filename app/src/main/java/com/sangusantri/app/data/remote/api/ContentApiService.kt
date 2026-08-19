package com.sangusantri.app.data.remote.api

import com.sangusantri.app.data.content.dto.ContentCatalogDto
import com.sangusantri.app.data.content.dto.ContentFileDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Content client for the CMS API (`cms/api`, deployed on Vercel). The catalog is checked at most
 * once every 24 hours (the sync scheduler's own gate), so no conditional-request header is needed.
 * [getContent]'s `url` is the catalog item's own `contentUrl` rather than a templated path — the
 * API builds that URL from its own host, so the app never has to know the route shape.
 */
interface ContentApiService {
    @GET("api/v1/catalog")
    suspend fun getCatalog(): Response<ContentCatalogDto>

    @GET
    suspend fun getContent(
        @Url url: String,
    ): Response<ContentFileDto>
}
