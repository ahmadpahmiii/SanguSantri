package com.sangusantri.app.feature.sholawat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.matchesSearch
import com.sangusantri.app.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Lists only [Content.SHOLAWAT_CATEGORY][com.sangusantri.app.domain.model.Content.SHOLAWAT_CATEGORY]
 * items — deliberately not Explore's full catalogue — filtered by a title-or-ayah search. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SholawatListViewModel
@Inject
constructor(
    contentRepository: ContentRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val query = savedStateHandle.getStateFlow(QUERY_KEY, "")

    val uiState: StateFlow<SholawatListUiState> =
        combine(
            contentRepository.observeActiveContent(),
            query,
            query.flatMapLatest(contentRepository::observeContentIdsMatchingStepText),
        ) { items, searchQuery, stepMatchedIds ->
            val matchedIds = stepMatchedIds.toSet()
                SholawatListUiState.ContentReady(
                    items = items.filter { it.isSholawat && it.matchesSearch(searchQuery, matchedIds) },
                    query = searchQuery,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = SholawatListUiState.Loading,
            )

    fun setQuery(value: String) {
        savedStateHandle[QUERY_KEY] = value
    }

    private companion object {
        const val QUERY_KEY = "sholawat_query"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
