package com.sangusantri.app.feature.nahwuquiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.NahwuQuizActiveAttempt
import com.sangusantri.app.domain.repository.NahwuQuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Landing has no loading gate of its own — its text is static, and the optional "Lanjutkan kuis"
 * card (design spec state 12) simply appears once [NahwuQuizRepository.observeActiveAttempt]
 * resolves, never blocking the rest of the screen. */
data class NahwuQuizLandingUiState(
    val activeAttempt: NahwuQuizActiveAttempt?,
)

@HiltViewModel
class NahwuQuizLandingViewModel
@Inject
constructor(
    nahwuQuizRepository: NahwuQuizRepository,
) : ViewModel() {
    val uiState: StateFlow<NahwuQuizLandingUiState> =
        nahwuQuizRepository
            .observeActiveAttempt()
            .map { active -> NahwuQuizLandingUiState(activeAttempt = active) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = NahwuQuizLandingUiState(activeAttempt = null),
            )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
