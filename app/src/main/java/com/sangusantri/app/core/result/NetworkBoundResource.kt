package com.sangusantri.app.core.result

import com.sangusantri.app.core.network.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * The one offline-first read in this app. Every repository that has both a cache and an upstream
 * goes through here, so "how does this screen behave offline" has exactly one answer.
 *
 * ```
 * cache present → render it immediately, refresh behind it, re-render when the refresh lands
 * cache empty   → Loading → Success, or Error if the refresh failed with nothing to fall back on
 * ```
 *
 * **Room stays the source of truth.** [fetch] does not return data — it writes to Room through a
 * local data source and reports only whether it worked. Everything rendered comes from [query], so
 * a screen cannot accidentally show a network DTO, and a partially-failed refresh cannot leave the
 * UI showing something Room does not agree with.
 *
 * **The cache is emitted before the network is touched**, so the refresh overlaps with the reader
 * actually reading. What it deliberately does *not* do is keep collecting [query] while the fetch
 * is in flight: Room's invalidation is asynchronous, so a collection started *before* the write can
 * still be holding the pre-write value at the instant the fetch reports success. Combining those
 * two produced a "succeeded, and here is the stale empty cache" emission — which is exactly how the
 * reading-mode gate came to report Ratib al-Haddad unavailable 157ms after a `200` carrying its 44
 * steps. Re-collecting [query] *after* the write is what makes that unrepresentable.
 *
 * @param query the cache, as a live [Flow] — a Room `@Query` returning `Flow`.
 * @param fetch performs the refresh *and persists it*, returning only the outcome.
 * @param isEmpty whether a cached value counts as "nothing to show". Drives the [Resource.Loading]
 *   vs render-the-cache decision, so it must mean *nothing for the reader*, not merely `null`.
 * @param shouldFetch consulted once against the cached value, for callers whose upstream is not
 *   worth asking every time (a schedule already covering today, a dataset already complete).
 */
fun <T> networkBoundResource(
    query: () -> Flow<T>,
    fetch: suspend () -> ApiResult<*>,
    isEmpty: (T) -> Boolean = { false },
    shouldFetch: (T) -> Boolean = { true },
): Flow<Resource<T>> = flow {
    val cached = query().first()

    if (!shouldFetch(cached)) {
        emitAll(query().map { Resource.Success(it) })
        return@flow
    }

    // Whoever has content reads it now rather than watching a spinner; whoever has none has
    // nothing else to look at, so they get Loading.
    emit(if (isEmpty(cached)) Resource.Loading(cached) else Resource.Success(cached))

    val outcome = fetch()

    // A *fresh* collection, started after the write — see the class note on the invalidation race.
    emitAll(
        query().map { data ->
            // Reported honestly even when `data` is populated. Screens that should stay quiet
            // offline check `data` themselves — see `Resource.isUnavailable`.
            if (outcome is ApiResult.Failure) Resource.Error(outcome, data) else Resource.Success(data)
        },
    )
}
