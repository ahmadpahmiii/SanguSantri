package com.sangusantri.app.feature.sholawat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Lists only [Content.SHOLAWAT_CATEGORY] items — deliberately not Explore's full catalogue. */
@HiltViewModel
class SholawatListViewModel
@Inject
constructor(
    contentRepository: ContentRepository,
) : ViewModel() {
    val uiState: StateFlow<SholawatListUiState> =
        contentRepository
            .observeActiveContent()
            .map { items ->
                SholawatListUiState.ContentReady(
                    items = items.filter { it.category == Content.SHOLAWAT_CATEGORY },
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = SholawatListUiState.Loading,
            )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
