package com.sangusantri.app.feature.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.ReaderMode
import com.sangusantri.app.domain.repository.ContentRepository
import com.sangusantri.app.domain.repository.ReaderSettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Owns the reading-mode gate (PRD 8.2): checks the amaliyah has a published version, then either
 * resolves immediately to a remembered [ReaderMode] or asks the user to choose once. [selectMode]
 * both remembers the choice and resolves the gate for the current visit.
 */
@HiltViewModel(assistedFactory = ReaderEntryViewModel.Factory::class)
class ReaderEntryViewModel
@AssistedInject
constructor(
    @Assisted private val amaliyahSlug: String,
    private val contentRepository: ContentRepository,
    private val readerSettingsRepository: ReaderSettingsRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(amaliyahSlug: String): ReaderEntryViewModel
    }

    private val _uiState = MutableStateFlow<ReaderEntryUiState>(ReaderEntryUiState.Loading)
    val uiState: StateFlow<ReaderEntryUiState> = _uiState

    init {
        resolve()
    }

    fun selectMode(mode: ReaderMode) {
        viewModelScope.launch { readerSettingsRepository.setLastReaderMode(mode) }
        _uiState.value = ReaderEntryUiState.Resolved(mode)
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun resolve() {
        viewModelScope.launch {
            val available =
                try {
                    val amaliyah = contentRepository.getAmaliyahBySlug(amaliyahSlug)
                    val detail = contentRepository.getDefaultVersionDetail(amaliyahSlug)
                    amaliyah != null && detail != null && detail.steps.isNotEmpty()
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (unexpected: Exception) {
                    false
                }

            if (!available) {
                _uiState.value = ReaderEntryUiState.ContentUnavailable
                return@launch
            }

            val amaliyahTitleId = contentRepository.getAmaliyahBySlug(amaliyahSlug)?.titleId.orEmpty()
            val rememberedMode = readerSettingsRepository.observe().first().lastReaderMode
            _uiState.value =
                if (rememberedMode != null) {
                    ReaderEntryUiState.Resolved(rememberedMode)
                } else {
                    ReaderEntryUiState.ModeChooser(amaliyahTitleId)
                }
        }
    }
}
