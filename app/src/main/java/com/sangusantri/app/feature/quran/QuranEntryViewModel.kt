package com.sangusantri.app.feature.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.data.remote.quran.QuranConnectivityChecker
import com.sangusantri.app.domain.model.QuranPreparationResult
import com.sangusantri.app.domain.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Owns the Al-Qur'an Kemenag entry gate (QUR-FR-002, §6.1): checks Room for a complete local
 * dataset, offers one connected initial preparation with determinate progress if none exists yet,
 * and resolves to [QuranEntryUiState.Ready] the moment the hub can render from Room — either
 * because a dataset already existed or because preparation just completed successfully.
 */
@HiltViewModel
class QuranEntryViewModel
@Inject
constructor(
    private val quranRepository: QuranRepository,
    private val connectivityChecker: QuranConnectivityChecker,
) : ViewModel() {
    private val _uiState = MutableStateFlow<QuranEntryUiState>(QuranEntryUiState.Checking)
    val uiState: StateFlow<QuranEntryUiState> = _uiState

    init {
        checkAndPrepare()
    }

    fun retry() {
        checkAndPrepare()
    }

    private fun checkAndPrepare() {
        viewModelScope.launch {
            _uiState.value = QuranEntryUiState.Checking
            if (quranRepository.hasLocalDataset()) {
                _uiState.value = QuranEntryUiState.Ready
                return@launch
            }
            if (!connectivityChecker.isConnected()) {
                _uiState.value = QuranEntryUiState.OfflineNoLocalData
                return@launch
            }
            _uiState.value = QuranEntryUiState.Preparing(completed = 0, total = 0)
            val result =
                quranRepository.ensureInitialPreparation { completed, total ->
                    _uiState.value = QuranEntryUiState.Preparing(completed, total)
                }
            _uiState.value =
                when (result) {
                    is QuranPreparationResult.Ready -> QuranEntryUiState.Ready
                    is QuranPreparationResult.Failed -> QuranEntryUiState.PreparationFailed(result.reason)
                }
        }
    }
}
