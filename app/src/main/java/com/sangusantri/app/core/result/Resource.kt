package com.sangusantri.app.core.result

import com.sangusantri.app.core.network.ApiResult

/**
 * What a repository hands the UI: the cached data, plus what the refresh behind it is doing.
 *
 * [data] is present on **all three** states, and that is the whole point of the type. Offline-first
 * means a reader who already has content sees it immediately and keeps seeing it — while a refresh
 * runs, and after that refresh fails. A state machine that dropped the data on `Loading` or `Error`
 * would force every screen to cache it again locally, which is the mistake this replaces.
 *
 * The repository reports what is true; the *screen* decides what that looks like. An [Error] with
 * a populated [data] is a reader who is offline with a usable cache — most screens should render it
 * exactly like [Success] and say nothing. An [Error] with empty [data] is the one case worth
 * interrupting someone for: a first launch that never reached the CMS.
 */
sealed interface Resource<out T> {
    val data: T?

    /** A refresh is in flight and there is nothing cached to show meanwhile. */
    data class Loading<out T>(override val data: T? = null) : Resource<T>

    /** [data] is current, or cached with a refresh still in flight — either way, render it. */
    data class Success<out T>(override val data: T) : Resource<T>

    /**
     * The refresh failed. [data] is whatever Room still holds — usually worth rendering anyway,
     * and empty or null only when this device has never successfully synced.
     */
    data class Error<out T>(val failure: ApiResult.Failure, override val data: T? = null) : Resource<T>
}

/** True only when there is genuinely nothing to show *and* the refresh failed — the one state that
 * warrants an error screen rather than silently rendering the cache. */
fun Resource<Collection<*>?>.isUnavailable(): Boolean = this is Resource.Error && data.isNullOrEmpty()
