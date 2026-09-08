package com.sangusantri.app.data.remote.api

import com.sangusantri.app.data.content.dto.ContentDetailDto
import com.sangusantri.app.data.content.dto.ContentListResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Content client for the CMS API (`cms/api`, deployed on Vercel), `schemaVersion` 3.
 *
 * Two tiers. The list is cheap and fetched often — every time Beranda resumes, so a publish or
 * unpublish in the CMS shows up without waiting for a background sync window. The detail is fetched
 * when the reader actually opens an item.
 *
 * `category` is a path segment rather than four hand-written methods, because the two categories
 * differ only in that segment: a pair of methods per category meant every caller carried a boolean
 * to pick between them, which is how `refresh(contentId, isSholawat = true)` came about. The only
 * values ever passed are
 * [ContentCategory][com.sangusantri.app.data.content.ContentCategory]'s, so this is a closed set,
 * not free text off the wire.
 *
 * No Retrofit `@Url` anywhere: every path is fixed, or built from an id this app already holds —
 * there is no server-supplied URL left to follow.
 *
 * Conditional requests are not written by hand: the OkHttp `Cache` installed in
 * [com.sangusantri.app.di.NetworkModule] stores each resource's `ETag` and replays it as
 * `If-None-Match`, so an unchanged list or detail costs a `304` and no body.
 */
interface ContentApiService {
    @GET("api/v1/{category}")
    suspend fun getList(@Path("category") category: String): Response<ContentListResponseDto>

    @GET("api/v1/{category}/{id}")
    suspend fun getDetail(
        @Path("category") category: String,
        @Path("id") id: String,
    ): Response<ContentDetailDto>
}
