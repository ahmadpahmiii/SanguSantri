package com.sangusantri.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.Amaliyah
import com.sangusantri.app.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SerambiViewModel
@Inject
constructor(
    contentRepository: ContentRepository,
) : ViewModel() {
    val uiState: StateFlow<SerambiUiState> =
        contentRepository
            .observeAmaliyah()
            .map<List<Amaliyah>, SerambiUiState> { amaliyah ->
                SerambiUiState.Content(amaliyah)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = SerambiUiState.Loading,
            )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
