package com.sangusantri.app.data.remote.api

import com.sangusantri.app.data.content.dto.ContentDetailDto
import com.sangusantri.app.data.content.dto.ContentListResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Content client for the CMS API (`cms/api`, deployed on Vercel), `schemaVersion` 3.
 *
 * Two tiers, one pair of routes per home-screen category. The list is cheap and fetched often —
 * every time Beranda resumes, so a publish or unpublish in the CMS shows up without waiting for a
 * background sync window. The detail is fetched when the reader actually opens an item.
 *
 * No Retrofit `@Url` anywhere: every path is fixed, or built from an id this app already holds.
 * That is what retired the old origin-pin rule — there is no server-supplied URL left to follow.
 *
 * Conditional requests are not written by hand: the OkHttp `Cache` installed in
 * [com.sangusantri.app.di.NetworkModule] stores each resource's `ETag` and replays it as
 * `If-None-Match`, so an unchanged list or detail costs a `304` and no body.
 */
interface ContentApiService {
    @GET("api/v1/sholawat")
    suspend fun getSholawatList(): Response<ContentListResponseDto>

    @GET("api/v1/amaliyah")
    suspend fun getAmaliyahList(): Response<ContentListResponseDto>

    @GET("api/v1/sholawat/{id}")
    suspend fun getSholawatDetail(
        @Path("id") id: String,
    ): Response<ContentDetailDto>

    @GET("api/v1/amaliyah/{id}")
    suspend fun getAmaliyahDetail(
        @Path("id") id: String,
    ): Response<ContentDetailDto>
}
