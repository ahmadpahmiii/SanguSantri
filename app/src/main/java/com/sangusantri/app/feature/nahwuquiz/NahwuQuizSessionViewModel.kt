package com.sangusantri.app.feature.nahwuquiz

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.NahwuQuizAttempt
import com.sangusantri.app.domain.model.NahwuQuizOptionKey
import com.sangusantri.app.domain.model.NahwuQuizQuestion
import com.sangusantri.app.domain.repository.NahwuQuizRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Owns `Pertanyaan`/`Jawaban Dipilih`/`Feedback Benar`/`Feedback Salah`/`Hasil Kuis` transition
 * state (design spec states 5-9's non-Result part). Runs entirely off one local
 * [MutableStateFlow] rather than continuously observing Room — [InternalState.Session.attempt] is
 * only refreshed by the two suspend calls that actually change it
 * ([NahwuQuizRepository.submitAnswer]/`completeAttempt`), mirroring
 * [com.sangusantri.app.feature.guidedreader.GuidedReaderViewModel]'s own local-state approach.
 *
 * [InternalState.Session.displayedIndex] intentionally stays behind
 * [InternalState.Session.attempt]'s already-advanced `currentQuestionIndex` while feedback is
 * showing (see [NahwuQuizAttempt]'s own doc comment) — [onAction]'s
 * [NahwuQuizSessionUiAction.Continue] branch is what catches it up.
 */
@HiltViewModel(assistedFactory = NahwuQuizSessionViewModel.Factory::class)
class NahwuQuizSessionViewModel
@AssistedInject
constructor(
    @Assisted private val packageId: String,
    private val nahwuQuizRepository: NahwuQuizRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(packageId: String): NahwuQuizSessionViewModel
    }

    private val internalState = MutableStateFlow<InternalState>(InternalState.Loading)

    val uiState: StateFlow<NahwuQuizSessionUiState> =
        internalState
            .map { it.toUiState() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = NahwuQuizSessionUiState.Loading,
            )

    init {
        load()
    }

    fun onAction(action: NahwuQuizSessionUiAction) {
        when (action) {
            is NahwuQuizSessionUiAction.SelectOption -> selectOption(action.option)
            NahwuQuizSessionUiAction.Submit -> submit()
            NahwuQuizSessionUiAction.Continue -> continueToNext()
            NahwuQuizSessionUiAction.Retry -> load()
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun load() {
        internalState.value = InternalState.Loading
        viewModelScope.launch {
            internalState.value =
                try {
                    loadSession()
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (unexpected: Exception) {
                    Log.e(TAG, "Nahwu Quiz session load failed for packageId=$packageId", unexpected)
                    InternalState.Error
                }
        }
    }

    @Suppress("ReturnCount")
    private suspend fun loadSession(): InternalState {
        val questions = nahwuQuizRepository.getQuestions(packageId)
        if (questions.isEmpty()) return InternalState.Unavailable

        val quizPackage = nahwuQuizRepository.getPackage(packageId)
        val attempt = nahwuQuizRepository.getOrCreateActiveAttempt(packageId)

        // Defensive resume: a process death between Submit (which already advanced
        // currentQuestionIndex) and the user tapping "Lanjut" on the final question would
        // otherwise strand the attempt one step short of Completed forever.
        if (attempt.currentQuestionIndex >= questions.size) {
            val completed = if (attempt.isCompleted) attempt else nahwuQuizRepository.completeAttempt(attempt.id)
            return InternalState.Done(completed.id)
        }

        return InternalState.Session(
            packageTitle = quizPackage?.title.orEmpty(),
            questions = questions,
            attempt = attempt,
            displayedIndex = attempt.currentQuestionIndex,
            selectedOption = null,
            isSubmitted = false,
            isCorrect = null,
            correctOption = null,
        )
    }

    private fun selectOption(option: NahwuQuizOptionKey) {
        val session = internalState.value as? InternalState.Session ?: return
        if (session.isSubmitted) return
        internalState.value = session.copy(selectedOption = option)
    }

    @Suppress("ReturnCount")
    private fun submit() {
        val session = internalState.value as? InternalState.Session ?: return
        val selected = session.selectedOption ?: return
        if (session.isSubmitted) return

        val question = session.questions[session.displayedIndex]
        val isCorrect = selected == question.correctOption
        viewModelScope.launch {
            val updatedAttempt = nahwuQuizRepository.submitAnswer(session.attempt.id, isCorrect)
            internalState.value =
                session.copy(
                    attempt = updatedAttempt,
                    isSubmitted = true,
                    isCorrect = isCorrect,
                    correctOption = question.correctOption,
                )
        }
    }

    private fun continueToNext() {
        val session = internalState.value as? InternalState.Session ?: return
        if (!session.isSubmitted) return

        if (session.displayedIndex >= session.questions.lastIndex) {
            viewModelScope.launch {
                val completed = nahwuQuizRepository.completeAttempt(session.attempt.id)
                internalState.value = InternalState.Done(completed.id)
            }
        } else {
            internalState.value =
                session.copy(
                    displayedIndex = session.attempt.currentQuestionIndex,
                    selectedOption = null,
                    isSubmitted = false,
                    isCorrect = null,
                    correctOption = null,
                )
        }
    }

    private sealed interface InternalState {
        data object Loading : InternalState

        data object Unavailable : InternalState

        data object Error : InternalState

        data class Done(
            val attemptId: String,
        ) : InternalState

        data class Session(
            val packageTitle: String,
            val questions: List<NahwuQuizQuestion>,
            val attempt: NahwuQuizAttempt,
            val displayedIndex: Int,
            val selectedOption: NahwuQuizOptionKey?,
            val isSubmitted: Boolean,
            val isCorrect: Boolean?,
            val correctOption: NahwuQuizOptionKey?,
        ) : InternalState

        fun toUiState(): NahwuQuizSessionUiState =
            when (this) {
                Loading -> NahwuQuizSessionUiState.Loading
                Unavailable -> NahwuQuizSessionUiState.ContentUnavailable
                Error -> NahwuQuizSessionUiState.RecoverableError
                is Done -> NahwuQuizSessionUiState.Completed(attemptId)
                is Session ->
                    NahwuQuizSessionUiState.QuestionVisible(
                        packageTitle = packageTitle,
                        questionIndex = displayedIndex,
                        questionCount = questions.size,
                        question = questions[displayedIndex],
                        selectedOption = selectedOption,
                        isSubmitted = isSubmitted,
                        isCorrect = isCorrect,
                        correctOption = correctOption,
                    )
            }
    }

    private companion object {
        const val TAG = "NahwuQuizSessionVM"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
