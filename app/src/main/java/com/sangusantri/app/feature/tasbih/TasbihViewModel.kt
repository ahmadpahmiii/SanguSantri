package com.sangusantri.app.feature.tasbih

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.TasbihSession
import com.sangusantri.app.domain.model.TasbihTargetPreset
import com.sangusantri.app.domain.repository.TasbihRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Owns Standalone Tasbih screen state (0.0.2) — a thin wrapper around [TasbihRepository]'s single
 * active-session Flow, deriving the transient "restored" indicator (see [TasbihUiState.Active]). */
@HiltViewModel
class TasbihViewModel
@Inject
constructor(
    private val repository: TasbihRepository,
) : ViewModel() {
    // Set once from this ViewModel instance's first non-null emission — deliberately not
    // `rememberSaveable`/Room state, since "restored" must reset to a fresh judgement whenever a
    // fresh ViewModel is created (new process, or the Tasbih tab's own back stack was fully
    // exited and re-entered), matching the design spec's "shown once per cold start" intent.
    private var restoredAnchorCaptured = false
    private var restoredAnchorTimestamp: Long? = null

    val uiState: StateFlow<TasbihUiState> =
        repository
            .observeSession()
            .map(::toUiState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = TasbihUiState.NoSession,
            )

    fun onAction(action: TasbihUiAction) {
        when (action) {
            TasbihUiAction.IncrementCounter ->
                viewModelScope.launch { repository.incrementCount() }

            is TasbihUiAction.SelectPreset -> onSelectPreset(action.preset)

            is TasbihUiAction.SetCustomTarget ->
                viewModelScope.launch { repository.startSession(TasbihTargetPreset.CUSTOM, action.value) }

            is TasbihUiAction.RenameSession ->
                viewModelScope.launch { repository.renameSession(action.name) }

            TasbihUiAction.ResetSession ->
                viewModelScope.launch { repository.resetSession() }
        }
    }

    private fun onSelectPreset(preset: TasbihTargetPreset) {
        // CUSTOM is never dispatched via SelectPreset by the UI (it opens a dialog instead), but
        // guard defensively rather than assume the caller always honours that contract.
        if (preset == TasbihTargetPreset.CUSTOM) return
        val targetValue =
            when (preset) {
                TasbihTargetPreset.THIRTY_THREE -> TasbihTargetPreset.THIRTY_THREE_TARGET
                TasbihTargetPreset.ONE_HUNDRED -> TasbihTargetPreset.ONE_HUNDRED_TARGET
                TasbihTargetPreset.UNLIMITED -> null
                TasbihTargetPreset.CUSTOM -> return
            }
        viewModelScope.launch { repository.startSession(preset, targetValue) }
    }

    private fun toUiState(session: TasbihSession?): TasbihUiState {
        if (session == null) return TasbihUiState.NoSession
        if (!restoredAnchorCaptured) {
            restoredAnchorCaptured = true
            restoredAnchorTimestamp = session.updatedAtEpochMillis.takeIf { session.currentCount > 0 }
        }
        return TasbihUiState.Active(
            currentCount = session.currentCount,
            targetValue = session.targetValue,
            targetPreset = session.targetPreset,
            sessionName = session.sessionName,
            isTargetReached = session.isTargetReached,
            isRestored = restoredAnchorTimestamp != null && session.updatedAtEpochMillis == restoredAnchorTimestamp,
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
