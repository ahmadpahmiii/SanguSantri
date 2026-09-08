package com.sangusantri.app.feature.explore

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    contentRepository: ContentRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val query = savedStateHandle.getStateFlow(QUERY_KEY, "")
    private val selectedCategory = savedStateHandle.getStateFlow<String?>(CATEGORY_KEY, null)

    val uiState: StateFlow<ExploreUiState> =
        combine(
            contentRepository.observeActiveContent().map { it.data.orEmpty() },
            query,
            selectedCategory,
            query.flatMapLatest(contentRepository::observeContentIdsMatchingStepText),
        ) { items, searchQuery, category, stepMatchedIds ->
            // Sholawat (0.0.8) has its own list + reader (feature/sholawat), so Jelajahi *browses*
            // amaliyah only — but a search from Beranda reaches the whole catalogue: typing a
            // sholawat title into the app's one search box and getting nothing was the confusing
            // part, not seeing it listed here.
            val amaliyah = items.filterNot { it.isSholawat }
            val searchable = if (searchQuery.isBlank()) amaliyah else items
            val categories = amaliyah.mapNotNull { it.category?.takeIf(String::isNotBlank) }.distinct()
            val effectiveCategory = category?.takeIf(categories::contains)
            val matchedIds = stepMatchedIds.toSet()
            val filteredItems =
                searchable.filter { item ->
                    val matchesCategory = effectiveCategory == null || item.category == effectiveCategory
                    matchesCategory && item.matchesSearch(searchQuery, matchedIds)
                }
            ExploreUiState.ContentReady(
                items = amaliyah,
                filteredItems = filteredItems,
                categories = categories,
                query = searchQuery,
                selectedCategory = effectiveCategory,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ExploreUiState.Loading,
        )

    fun setQuery(value: String) {
        savedStateHandle[QUERY_KEY] = value
    }

    fun selectCategory(category: String?) {
        savedStateHandle[CATEGORY_KEY] = category
    }

    private companion object {
        const val QUERY_KEY = "explore_query"
        const val CATEGORY_KEY = "explore_category"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
