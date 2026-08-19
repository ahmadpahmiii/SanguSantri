package com.sangusantri.app.feature.reader

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.ReaderMode
import com.sangusantri.app.domain.model.hasGuidedMode
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
 * Owns the reading-mode gate (PRD 8.2): checks the content item has steps, then either resolves
 * immediately to a remembered [ReaderMode] or asks the user to choose once. [selectMode] both
 * remembers the choice and resolves the gate for the current visit.
 */
@HiltViewModel(assistedFactory = ReaderEntryViewModel.Factory::class)
class ReaderEntryViewModel
    @AssistedInject
    constructor(
        @Assisted private val contentId: String,
        private val contentRepository: ContentRepository,
        private val readerSettingsRepository: ReaderSettingsRepository,
    ) : ViewModel() {
        @AssistedFactory
        interface Factory {
            fun create(contentId: String): ReaderEntryViewModel
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
                val detail =
                    try {
                        contentRepository.getContentDetail(contentId)?.takeIf { it.steps.isNotEmpty() }
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (unexpected: Exception) {
                        Log.e(TAG, "Reader entry availability check failed for id=$contentId", unexpected)
                        null
                    }

                if (detail == null) {
                    Log.w(TAG, "Content unavailable or has no steps for id=$contentId")
                    _uiState.value = ReaderEntryUiState.ContentUnavailable
                    return@launch
                }

                // Content where nothing is counted has no Panduan mode, so there is no choice to
                // offer and no remembered choice to honour — it always opens in Bacaan Lengkap.
                if (!detail.steps.hasGuidedMode()) {
                    _uiState.value = ReaderEntryUiState.Resolved(ReaderMode.FULL)
                    return@launch
                }

                val rememberedMode = readerSettingsRepository.observe().first().lastReaderMode
                _uiState.value =
                    if (rememberedMode != null) {
                        ReaderEntryUiState.Resolved(rememberedMode)
                    } else {
                        ReaderEntryUiState.ModeChooser(detail.content.title)
                    }
            }
        }

        private companion object {
            const val TAG = "ReaderEntryViewModel"
        }
    }
