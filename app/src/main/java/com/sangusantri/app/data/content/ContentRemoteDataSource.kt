package com.sangusantri.app.data.content

import com.sangusantri.app.core.network.ApiResult
import com.sangusantri.app.core.network.safeApiCall
import com.sangusantri.app.core.network.validate
import com.sangusantri.app.data.content.dto.ContentDetailDto
import com.sangusantri.app.data.content.dto.ContentListResponseDto
import com.sangusantri.app.data.remote.api.ContentApiService
import javax.inject.Inject

/**
 * The CMS, as this app's data layer sees it: two reads, each already fetched, error-classified and
 * structurally validated.
 *
 * This exists so [ContentRepositoryImpl][com.sangusantri.app.data.repository.ContentRepositoryImpl]
 * never touches Retrofit types. `Response`, HTTP codes and `SerializationException` stop here; what
 * leaves is [ApiResult], the same vocabulary the local side and every other feature speaks.
 *
 * Validation happens here, not in the repository, and that placement is deliberate: a payload that
 * fails structural validation is a *broken contract*, which is a property of the wire and not of
 * the domain. Handling it alongside a 500 means the repository has one failure path instead of two.
 */
class ContentRemoteDataSource @Inject constructor(private val api: ContentApiService) {
    /**
     * One category's published items — metadata only, no steps.
     *
     * Rejecting the whole list on one bad entry is deliberate: a half-imported catalogue leaves
     * Beranda showing an arbitrary subset with nothing to say which items are missing.
     */
    suspend fun getList(category: ContentCategory): ApiResult<ContentListResponseDto> {
        val source = "${category.path} list"
        return safeApiCall(source) { api.getList(category.path) }
            .validate(source, ContentValidator::validateList)
    }

    /**
     * One item and its steps.
     *
     * A bad detail fails only that item — unlike the list, one unreadable item does not have to
     * cost the reader the other sixty-two.
     */
    suspend fun getDetail(
        category: ContentCategory,
        contentId: String,
    ): ApiResult<ContentDetailDto> {
        val source = "${category.path}/$contentId"
        return safeApiCall(source) { api.getDetail(category.path, contentId) }
            .validate(source, ContentValidator::validateDetail)
    }
}
