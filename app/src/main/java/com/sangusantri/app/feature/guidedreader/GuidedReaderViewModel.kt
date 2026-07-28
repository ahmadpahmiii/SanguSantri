package com.sangusantri.app.feature.guidedreader

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.AmaliyahStep
import com.sangusantri.app.domain.model.AmaliyahVersionDetail
import com.sangusantri.app.domain.model.GuidedProgressionMode
import com.sangusantri.app.domain.model.GuidedReadingSession
import com.sangusantri.app.domain.model.ReaderMode
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.domain.model.ReadingPosition
import com.sangusantri.app.domain.model.StepProgress
import com.sangusantri.app.domain.repository.ContentRepository
import com.sangusantri.app.domain.repository.GuidedReadingRepository
import com.sangusantri.app.domain.repository.ReaderSettingsRepository
import com.sangusantri.app.domain.repository.ReadingPositionRepository
import com.sangusantri.app.feature.reader.ReaderUiAction
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Owns Guided Reader screen state (Milestone 4, FR-005/FR-006/FR-007): one ordered step at a time,
 * an interactive tasbih counter per step, automatic/manual progression, and completion. Restores
 * the last visited step and every counter from Room (`GuidedReadingRepository`), keyed by the
 * immutable content version id — the same per-version scoping [com.sangusantri.app.feature.reader.ReaderViewModel]
 * uses for `reading_positions`, but a separate table since guided progress (current step,
 * completion) has no Full Reader equivalent.
 *
 * The function count follows directly from the milestone's own action set (navigation, counter,
 * progression, completion, plus the settings-sheet bridge) — splitting further would mean an
 * artificial helper class introduced only to satisfy a lint metric, which `CODING_STANDARD.md`
 * warns against.
 */
@Suppress("TooManyFunctions")
@HiltViewModel(assistedFactory = GuidedReaderViewModel.Factory::class)
class GuidedReaderViewModel
@AssistedInject
constructor(
    @Assisted private val amaliyahSlug: String,
    private val contentRepository: ContentRepository,
    private val guidedReadingRepository: GuidedReadingRepository,
    private val readerSettingsRepository: ReaderSettingsRepository,
    private val readingPositionRepository: ReadingPositionRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(amaliyahSlug: String): GuidedReaderViewModel
    }

    private val contentState = MutableStateFlow<ContentState>(ContentState.Loading)
    private val stepCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val currentStepIndex = MutableStateFlow(0)
    private val completedAtEpochMillis = MutableStateFlow<Long?>(null)
    private var autoAdvanceJob: Job? = null

    private val _switchToFullReady = MutableStateFlow(false)
    val switchToFullReady: StateFlow<Boolean> = _switchToFullReady

    val uiState: StateFlow<GuidedReaderUiState> =
        combine(
            contentState,
            stepCounts,
            currentStepIndex,
            completedAtEpochMillis,
            readerSettingsRepository.observe(),
        ) { content, counts, index, completedAt, settings ->
            content.toUiState(counts, index, completedAt, settings)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = GuidedReaderUiState.Loading,
        )

    /** Observed independently so the shared settings sheet reflects real values even while loading. */
    val settings: StateFlow<ReaderSettings> =
        readerSettingsRepository.observe().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ReaderSettings(),
        )

    init {
        loadContent()
    }

    fun onAction(action: GuidedReaderUiAction) {
        when (action) {
            GuidedReaderUiAction.Previous -> moveTo(currentStepIndex.value - 1)
            GuidedReaderUiAction.Continue -> onContinue()
            GuidedReaderUiAction.IncrementCounter -> onIncrement()
            GuidedReaderUiAction.ResetCounter -> onResetCounter()
            GuidedReaderUiAction.ConfirmCompletion -> onConfirmCompletion()
            GuidedReaderUiAction.Retry -> loadContent()
            GuidedReaderUiAction.SwitchToFull -> onSwitchToFull()
            is GuidedReaderUiAction.JumpToStep -> onJumpToStep(action.stepId)
        }
    }

    /** Table of Contents jump (FR-017) — moves to the section's first step, no counter side effects. */
    private fun onJumpToStep(stepId: String) {
        val detail = availableDetail() ?: return
        val index = detail.steps.indexOfFirst { it.id == stepId }
        if (index >= 0) moveTo(index)
    }

    /**
     * Maps the current step to the Full Reader's starting position (FR-016): writes it directly
     * into the existing per-version [ReadingPosition] row with a safe zero offset, so the Full
     * Reader simply restores its usual position on load and finds this step already current — no
     * second progress model, no nav-key state.
     */
    private fun onSwitchToFull() {
        val detail = availableDetail() ?: return
        viewModelScope.launch {
            val clampedIndex = currentStepIndex.value.coerceIn(0, detail.steps.lastIndex)
            readingPositionRepository.savePosition(
                ReadingPosition(
                    versionId = detail.version.id,
                    itemIndex = clampedIndex,
                    itemOffset = 0,
                    lastOpenedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
            readerSettingsRepository.setLastReaderMode(ReaderMode.FULL)
            _switchToFullReady.value = true
        }
    }

    /**
     * Handles the subset of [com.sangusantri.app.feature.reader.ReaderUiAction] the shared
     * `ReaderSettingsSheet` can send from within the Guided Reader (appearance + progression mode).
     * Scroll-position and retry actions belong to the Full Reader only and are no-ops here — the
     * settings sheet never dispatches them.
     */
    fun onSettingsAction(action: ReaderUiAction) {
        when (action) {
            is ReaderUiAction.SetArabicFontSize ->
                viewModelScope.launch { readerSettingsRepository.setArabicFontSize(action.sp) }

            is ReaderUiAction.SetTranslationFontSize ->
                viewModelScope.launch { readerSettingsRepository.setTranslationFontSize(action.sp) }

            is ReaderUiAction.SetArabicLineSpacing ->
                viewModelScope.launch { readerSettingsRepository.setArabicLineSpacing(action.multiplier) }

            is ReaderUiAction.SetTranslationLineSpacing ->
                viewModelScope.launch { readerSettingsRepository.setTranslationLineSpacing(action.multiplier) }

            is ReaderUiAction.SetShowTranslation ->
                viewModelScope.launch { readerSettingsRepository.setShowTranslation(action.show) }

            is ReaderUiAction.ScrollPositionChanged,
            is ReaderUiAction.PersistPositionNow,
            ReaderUiAction.Retry,
            ReaderUiAction.SwitchToGuided,
            is ReaderUiAction.SwitchToGuidedAtStep,
                -> Unit
        }
    }

    fun setProgressionMode(mode: GuidedProgressionMode) {
        viewModelScope.launch { readerSettingsRepository.setGuidedProgressionMode(mode) }
    }

    // Room/DataStore failures surface as unpredictable exception types; catching Exception here is
    // the deliberate boundary that turns any of them into RecoverableError instead of a crash,
    // mirroring ReaderViewModel's own loadContent boundary.
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun loadContent() {
        autoAdvanceJob?.cancel()
        contentState.value = ContentState.Loading
        viewModelScope.launch {
            contentState.value =
                try {
                    val amaliyah = contentRepository.getAmaliyahBySlug(amaliyahSlug)
                    val detail = contentRepository.getDefaultVersionDetail(amaliyahSlug)
                    if (amaliyah == null || detail == null || detail.steps.isEmpty()) {
                        Log.w(
                            TAG,
                            "Content unavailable for slug=$amaliyahSlug: " +
                                    "amaliyahFound=${amaliyah != null}, activeVersionFound=${detail != null}, " +
                                    "stepCount=${detail?.steps?.size ?: 0}",
                        )
                        ContentState.Unavailable
                    } else {
                        restoreProgress(detail)
                        ContentState.Available(amaliyah.titleId, detail)
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (unexpected: Exception) {
                    Log.e(TAG, "Guided content load failed for slug=$amaliyahSlug", unexpected)
                    ContentState.Error
                }
        }
    }

    private suspend fun restoreProgress(detail: AmaliyahVersionDetail) {
        val session = guidedReadingRepository.getSession(detail.version.id)
        val progress = guidedReadingRepository.getStepProgress(detail.version.id)
        stepCounts.value = progress.associate { it.stepId to it.currentCount }
        completedAtEpochMillis.value = session?.completedAtEpochMillis
        val restoredIndex = session?.currentStepId?.let { id -> detail.steps.indexOfFirst { it.id == id } }
        currentStepIndex.value = restoredIndex?.takeIf { it >= 0 } ?: 0
    }

    // Each function is a short chain of independent guard clauses (no step / not a counter step /
    // already at target / wrong step to act on) — flat early returns read more clearly here than
    // nesting every guard inside `?.let`, mirroring ContentRepositoryImpl's own ReturnCount suppression.
    @Suppress("ReturnCount")
    private fun onContinue() {
        val detail = availableDetail() ?: return
        val step = detail.steps.getOrNull(currentStepIndex.value) ?: return
        if (!isStepContinueEnabled(step, stepCounts.value)) return
        if (currentStepIndex.value < detail.steps.lastIndex) moveTo(currentStepIndex.value + 1)
    }

    @Suppress("ReturnCount")
    private fun onIncrement() {
        val detail = availableDetail() ?: return
        val step = detail.steps.getOrNull(currentStepIndex.value) ?: return
        val target = step.repeatTarget ?: return
        if (target <= 0) return
        val current = stepCounts.value[step.id] ?: 0
        if (current >= target) return

        val updated = current + 1
        stepCounts.value = stepCounts.value + (step.id to updated)
        persistCounter(detail.version.id, step.id, updated)

        if (updated >= target && settings.value.guidedProgressionMode == GuidedProgressionMode.AUTOMATIC) {
            scheduleAutoAdvance()
        }
    }

    @Suppress("ReturnCount")
    private fun onResetCounter() {
        val detail = availableDetail() ?: return
        val step = detail.steps.getOrNull(currentStepIndex.value) ?: return
        if (step.repeatTarget == null) return
        autoAdvanceJob?.cancel()
        stepCounts.value = stepCounts.value + (step.id to 0)
        persistCounter(detail.version.id, step.id, 0)
    }

    @Suppress("ReturnCount")
    private fun onConfirmCompletion() {
        val detail = availableDetail() ?: return
        if (currentStepIndex.value != detail.steps.lastIndex) return
        if (!allRequiredCountersComplete(detail.steps, stepCounts.value)) return

        val now = System.currentTimeMillis()
        completedAtEpochMillis.value = now
        val stepId = detail.steps[currentStepIndex.value].id
        viewModelScope.launch {
            guidedReadingRepository.saveSession(
                GuidedReadingSession(
                    versionId = detail.version.id,
                    currentStepId = stepId,
                    lastOpenedAtEpochMillis = now,
                    completedAtEpochMillis = now,
                ),
            )
        }
    }

    private fun scheduleAutoAdvance() {
        autoAdvanceJob?.cancel()
        autoAdvanceJob =
            viewModelScope.launch {
                delay(AUTO_ADVANCE_DELAY_MILLIS)
                val detail = availableDetail() ?: return@launch
                if (currentStepIndex.value < detail.steps.lastIndex) moveTo(currentStepIndex.value + 1)
            }
    }

    private fun moveTo(index: Int) {
        val detail = availableDetail() ?: return
        autoAdvanceJob?.cancel()
        val clamped = index.coerceIn(0, detail.steps.lastIndex)
        currentStepIndex.value = clamped
        persistSession(detail.version.id, detail.steps[clamped].id)
    }

    private fun persistSession(
        versionId: String,
        stepId: String,
    ) {
        viewModelScope.launch {
            guidedReadingRepository.saveSession(
                GuidedReadingSession(
                    versionId = versionId,
                    currentStepId = stepId,
                    lastOpenedAtEpochMillis = System.currentTimeMillis(),
                    completedAtEpochMillis = completedAtEpochMillis.value,
                ),
            )
        }
    }

    private fun persistCounter(
        versionId: String,
        stepId: String,
        count: Int,
    ) {
        viewModelScope.launch {
            guidedReadingRepository.saveStepProgress(
                StepProgress(
                    versionId = versionId,
                    stepId = stepId,
                    currentCount = count,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
        }
    }

    private fun availableDetail(): AmaliyahVersionDetail? = (contentState.value as? ContentState.Available)?.detail

    private sealed interface ContentState {
        data object Loading : ContentState

        data class Available(
            val amaliyahTitleId: String,
            val detail: AmaliyahVersionDetail,
        ) : ContentState

        data object Unavailable : ContentState

        data object Error : ContentState

        fun toUiState(
            counts: Map<String, Int>,
            index: Int,
            completedAt: Long?,
            settings: ReaderSettings,
        ): GuidedReaderUiState =
            when (this) {
                Loading -> GuidedReaderUiState.Loading
                Unavailable -> GuidedReaderUiState.ContentUnavailable
                Error -> GuidedReaderUiState.RecoverableError
                is Available -> {
                    val steps = detail.steps
                    val clampedIndex = index.coerceIn(0, steps.lastIndex)
                    val step = steps[clampedIndex]
                    GuidedReaderUiState.StepVisible(
                        amaliyahTitleId = amaliyahTitleId,
                        versionId = detail.version.id,
                        allSteps = steps,
                        step = step,
                        stepIndex = clampedIndex,
                        stepCount = steps.size,
                        currentCount = counts[step.id] ?: 0,
                        settings = settings,
                        isFirstStep = clampedIndex == 0,
                        isLastStep = clampedIndex == steps.lastIndex,
                        continueEnabled = isStepContinueEnabled(step, counts),
                        allRequiredCountersComplete = allRequiredCountersComplete(steps, counts),
                        isCompleted = completedAt != null,
                        sourceName = detail.version.sourceName,
                        approval = detail.approval,
                    )
                }
            }
    }

    private companion object {
        const val TAG = "GuidedReaderViewModel"
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val AUTO_ADVANCE_DELAY_MILLIS = 500L
    }
}

/** A step with no positive repetition target needs no counter progress to continue past it. */
private fun isStepContinueEnabled(
    step: AmaliyahStep,
    counts: Map<String, Int>,
): Boolean {
    val target = step.repeatTarget
    return target == null || target <= 0 || (counts[step.id] ?: 0) >= target
}

/** FR-007: completion requires every step's own counter (if it has one) to have reached its target. */
private fun allRequiredCountersComplete(
    steps: List<AmaliyahStep>,
    counts: Map<String, Int>,
): Boolean = steps.all { step -> isStepContinueEnabled(step, counts) }
