package com.sangusantri.app.feature.explore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel
@Inject
constructor(
    contentRepository: ContentRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val query = savedStateHandle.getStateFlow(QUERY_KEY, "")
    private val selectedCategory = savedStateHandle.getStateFlow<String?>(CATEGORY_KEY, null)

    val uiState: StateFlow<ExploreUiState> =
        combine(contentRepository.observeActiveContent(), query, selectedCategory) { items, searchQuery, category ->
            val categories = items.mapNotNull { it.category?.takeIf(String::isNotBlank) }.distinct()
            val effectiveCategory = category?.takeIf(categories::contains)
            val filteredItems =
                items.filter { item ->
                    val matchesCategory = effectiveCategory == null || item.category == effectiveCategory
                    val matchesQuery =
                        searchQuery.isBlank() ||
                                item.title.contains(searchQuery, ignoreCase = true) ||
                                item.description.contains(searchQuery, ignoreCase = true) ||
                                item.category?.contains(searchQuery, ignoreCase = true) == true
                    matchesCategory && matchesQuery
                }
            ExploreUiState.ContentReady(
                items = items,
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
