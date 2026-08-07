package com.sangusantri.app.feature.nahwuquiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.repository.NahwuQuizRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** `Instruksi` — a one-shot load (package title/question count never change mid-session), unlike
 * every other Nahwu Quiz screen's reactive [kotlinx.coroutines.flow.Flow]. */
sealed interface NahwuQuizInstructionUiState {
    data object Loading : NahwuQuizInstructionUiState

    data class Content(
        val packageTitle: String,
        val questionCount: Int,
    ) : NahwuQuizInstructionUiState

    data object NotFound : NahwuQuizInstructionUiState
}

@HiltViewModel(assistedFactory = NahwuQuizInstructionViewModel.Factory::class)
class NahwuQuizInstructionViewModel
@AssistedInject
constructor(
    @Assisted private val packageId: String,
    private val nahwuQuizRepository: NahwuQuizRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(packageId: String): NahwuQuizInstructionViewModel
    }

    private val _uiState = MutableStateFlow<NahwuQuizInstructionUiState>(NahwuQuizInstructionUiState.Loading)
    val uiState: StateFlow<NahwuQuizInstructionUiState> = _uiState

    init {
        viewModelScope.launch {
            val quizPackage = nahwuQuizRepository.getPackage(packageId)
            _uiState.value =
                if (quizPackage == null) {
                    NahwuQuizInstructionUiState.NotFound
                } else {
                    NahwuQuizInstructionUiState.Content(
                        packageTitle = quizPackage.title,
                        questionCount = quizPackage.questionCount,
                    )
                }
        }
    }
}
