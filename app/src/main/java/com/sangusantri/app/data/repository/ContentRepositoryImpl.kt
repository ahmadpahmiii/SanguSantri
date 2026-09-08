package com.sangusantri.app.data.repository

import android.util.Log
import com.sangusantri.app.core.network.ApiResult
import com.sangusantri.app.core.result.Resource
import com.sangusantri.app.core.result.networkBoundResource
import com.sangusantri.app.data.content.ContentCategory
import com.sangusantri.app.data.content.ContentLocalDataSource
import com.sangusantri.app.data.content.ContentRemoteDataSource
import com.sangusantri.app.data.content.ContentWriteOutcome
import com.sangusantri.app.data.content.dto.ContentListItemDto
import com.sangusantri.app.data.local.dao.ContentDao
import com.sangusantri.app.data.local.dao.ContentStepDao
import com.sangusantri.app.data.mapper.toDomain
import com.sangusantri.app.di.ApplicationScope
import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.ContentDetail
import com.sangusantri.app.domain.repository.ContentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

/**
 * The content catalogue: Room as the source of truth, the CMS as the thing that keeps it current.
 *
 * This is where `ContentSyncManager` and `ContentDetailSyncManager` went. They were never separate
 * concerns — they were this class's two write paths, living outside it because the repository
 * interface had forbidden itself from touching the network. Folding them back in is what lets
 * [networkBoundResource] do the cache-then-refresh dance once, instead of three ViewModels doing it
 * three slightly different ways.
 */
class ContentRepositoryImpl @Inject constructor(
    private val contentDao: ContentDao,
    private val contentStepDao: ContentStepDao,
    private val local: ContentLocalDataSource,
    private val remote: ContentRemoteDataSource,
    @param:ApplicationScope private val appScope: CoroutineScope,
) : ContentRepository {
    private val refreshLock = Mutex()
    private var inFlightRefresh: Deferred<ApiResult<Unit>>? = null
    override fun observeActiveContent(): Flow<Resource<List<Content>>> = networkBoundResource(
        query = { contentDao.observeActive().map { rows -> rows.map { it.toDomain() } } },
        fetch = ::refreshCatalogue,
        isEmpty = List<Content>::isEmpty,
    )

    override fun observeContentDetail(contentId: String): Flow<Resource<ContentDetail>> = flow {
        // The category decides the route, and it is only knowable from the cached row. Without one
        // there is nothing to fetch *from*: reporting that is the point, because returning success
        // with no data would leave the caller waiting on a load that is never coming.
        val category = contentDao.getById(contentId)?.toDomain()?.let(ContentCategory::of)
        emitAll(
            networkBoundResource(
                query = { observeCachedDetail(contentId) },
                fetch = {
                    category?.let { refreshDetail(it, contentId) }
                        ?: ApiResult.MalformedResponse("no catalogue row for $contentId")
                },
                // Steps are what makes an item readable: a row from the list sync with none yet is
                // as empty as no row at all, and must show loading rather than an empty reader.
                isEmpty = { detail -> detail == null || detail.steps.isEmpty() },
            ).map { resource -> resource.narrowToNonNull() },
        )
    }

    override fun observeContentIdsMatchingStepText(query: String): Flow<List<String>> {
        val needle = query.trim()
        return if (needle.isEmpty()) {
            flowOf(emptyList())
        } else {
            contentStepDao.observeContentIdsMatchingText(needle.escapeLikeWildcards())
        }
    }

    override suspend fun getContentById(contentId: String): Content? = contentDao.getById(contentId)?.toDomain()

    override suspend fun getCachedContentDetail(contentId: String): ContentDetail? {
        val content = contentDao.getById(contentId) ?: return null
        return ContentDetail(
            content = content.toDomain(),
            steps = contentStepDao.getByContentId(contentId).map { it.toDomain() },
        )
    }

    /**
     * Refreshes the catalogue, or joins the refresh already running.
     *
     * De-duplication is not an optimisation here, it is a correctness fix. [observeActiveContent]
     * refreshes when it is *collected*, and Beranda alone collects it twice — once for the featured
     * list and once for the resume widget — so every appearance of the screen fired two complete
     * catalogue syncs at the same instant: two `GET /amaliyah` and two `GET /sholawat`. Two screens
     * alive during a navigation transition made it four.
     *
     * A [Mutex] alone would only serialise them, still sending every request twice. Sharing one
     * [Deferred] is what makes the second caller *await the first* instead. It runs on
     * [ApplicationScope] rather than a caller's scope so that the first collector going away — which
     * is routine, `WhileSubscribed` drops subscriptions constantly — cannot cancel a refresh the
     * second collector is still waiting on.
     */
    override suspend fun refreshCatalogue(): ApiResult<Unit> {
        val refresh =
            refreshLock.withLock {
                inFlightRefresh?.takeIf { it.isActive }
                    ?: appScope.async { performCatalogueRefresh() }.also { inFlightRefresh = it }
            }
        return refresh.await()
    }

    /**
     * Both category listings, then one reconciliation pass.
     *
     * Both are fetched before anything is written, and a failure in either aborts the whole
     * refresh: a half-fetched catalogue must not be allowed to deactivate the half it never saw,
     * and aborting early leaves Room exactly as it was.
     */
    private suspend fun performCatalogueRefresh(): ApiResult<Unit> {
        val listings = mutableListOf<ContentListItemDto>()
        for (category in ContentCategory.entries) {
            when (val result = remote.getList(category)) {
                is ApiResult.Failure -> return result
                is ApiResult.Success -> listings += result.data.items
            }
        }
        saveListings(listings)
        return ApiResult.Success(Unit)
    }

    private suspend fun saveListings(items: List<ContentListItemDto>) {
        val rejected = items.count { local.saveListItem(it) is ContentWriteOutcome.Rejected }
        if (rejected > 0) {
            Log.w(TAG, "$rejected catalogue item(s) rejected on write")
        }

        // Runs only after every listing parsed and validated: an item missing because a request
        // failed must never be mistaken for one the CMS unpublished.
        val hidden = local.deactivateAbsent(items.map { it.id })
        if (hidden > 0) {
            Log.i(TAG, "hid $hidden item(s) no longer published by the CMS")
        }
    }

    private suspend fun refreshDetail(
        category: ContentCategory,
        contentId: String,
    ): ApiResult<Unit> = when (val result = remote.getDetail(category, contentId)) {
        is ApiResult.Failure -> result
        is ApiResult.Success -> {
            val detail = result.data
            if (detail.id != contentId) {
                ApiResult.MalformedResponse("detail id ${detail.id} does not match requested $contentId")
            } else {
                when (val write = local.saveDetail(detail)) {
                    is ContentWriteOutcome.Rejected -> ApiResult.MalformedResponse(write.reason)
                    else -> ApiResult.Success(Unit)
                }
            }
        }
    }

    /** Metadata and steps change independently — a title or layout correction touches no step — so
     * both are observed and combined, and either one landing mid-read reaches an open reader. */
    private fun observeCachedDetail(contentId: String): Flow<ContentDetail?> = combine(
        contentDao.observeById(contentId),
        contentStepDao.observeByContentId(contentId),
    ) { row, steps ->
        row?.let { ContentDetail(content = it.toDomain(), steps = steps.map { step -> step.toDomain() }) }
    }

    private companion object {
        const val TAG = "ContentRepository"
    }
}

/**
 * `isEmpty` above already treats a null detail as nothing to show, so a null can only reach
 * [Resource.Success] if Room contradicted itself between two reads. Mapping it to
 * [Resource.Loading] keeps the public type non-null without inventing an error for a state
 * that resolves on the next emission.
 */
private fun Resource<ContentDetail?>.narrowToNonNull(): Resource<ContentDetail> = when (this) {
    is Resource.Loading -> Resource.Loading(data)
    is Resource.Error -> Resource.Error(failure, data)
    is Resource.Success -> data?.let { Resource.Success(it) } ?: Resource.Loading(null)
}

/** `%`, `_` and the escape character itself are literal text when typed into a search box. */
private fun String.escapeLikeWildcards(): String = replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
