package com.sangusantri.app.feature.nahwuquiz

import com.sangusantri.app.domain.model.NahwuQuizActiveAttempt
import com.sangusantri.app.domain.model.NahwuQuizAttempt
import com.sangusantri.app.domain.model.NahwuQuizOption
import com.sangusantri.app.domain.model.NahwuQuizOptionKey
import com.sangusantri.app.domain.model.NahwuQuizPackage
import com.sangusantri.app.domain.model.NahwuQuizPackageSummary
import com.sangusantri.app.domain.model.NahwuQuizQuestion
import com.sangusantri.app.domain.repository.NahwuQuizRepository
import com.sangusantri.app.feature.home.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** [NahwuQuizSessionViewModel.uiState] is `stateIn(WhileSubscribed(...))` — an active collector is
 * required for the assertion to see anything past the initial value, mirroring
 * `ReaderViewModelTest`'s own `subscribeToReaderState` helper. */
private fun TestScope.subscribeToSessionState(viewModel: NahwuQuizSessionViewModel): Job =
    launch { viewModel.uiState.collect {} }

@OptIn(ExperimentalCoroutinesApi::class)
class NahwuQuizSessionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiStateStartsAsLoadingBeforeQuestionsResolve() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            assertEquals(NahwuQuizSessionUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun uiStateBecomesQuestionVisibleWithFirstQuestionAndWithholdsCorrectOption() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val job = subscribeToSessionState(viewModel)

            advanceUntilIdle()

            val state = viewModel.uiState.value
            check(state is NahwuQuizSessionUiState.QuestionVisible)
            assertEquals(0, state.questionIndex)
            assertEquals(questions.size, state.questionCount)
            assertEquals(questions[0].id, state.question.id)
            assertNull(state.selectedOption)
            assertTrue(!state.isSubmitted)
            assertNull(state.correctOption)
            job.cancel()
        }

    @Test
    fun uiStateBecomesContentUnavailableWhenPackageHasNoQuestions() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(repository = FakeNahwuQuizRepository(questions = emptyList()))
            val job = subscribeToSessionState(viewModel)

            advanceUntilIdle()

            assertEquals(NahwuQuizSessionUiState.ContentUnavailable, viewModel.uiState.value)
            job.cancel()
        }

    @Test
    fun submittingCorrectOptionRevealsCorrectness() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val job = subscribeToSessionState(viewModel)
            advanceUntilIdle()

            viewModel.onAction(NahwuQuizSessionUiAction.SelectOption(questions[0].correctOption))
            viewModel.onAction(NahwuQuizSessionUiAction.Submit)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            check(state is NahwuQuizSessionUiState.QuestionVisible)
            assertTrue(state.isSubmitted)
            assertEquals(true, state.isCorrect)
            assertEquals(questions[0].correctOption, state.correctOption)
            job.cancel()
        }

    @Test
    fun submittingIncorrectOptionStillRevealsTheCorrectOption() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val job = subscribeToSessionState(viewModel)
            advanceUntilIdle()

            val wrongOption = questions[0].options.first { it.key != questions[0].correctOption }.key
            viewModel.onAction(NahwuQuizSessionUiAction.SelectOption(wrongOption))
            viewModel.onAction(NahwuQuizSessionUiAction.Submit)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            check(state is NahwuQuizSessionUiState.QuestionVisible)
            assertEquals(false, state.isCorrect)
            assertEquals(questions[0].correctOption, state.correctOption)
            job.cancel()
        }

    @Test
    fun continueAfterFeedbackAdvancesToNextQuestionAndResetsSelection() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val job = subscribeToSessionState(viewModel)
            advanceUntilIdle()

            viewModel.onAction(NahwuQuizSessionUiAction.SelectOption(questions[0].correctOption))
            viewModel.onAction(NahwuQuizSessionUiAction.Submit)
            advanceUntilIdle()
            viewModel.onAction(NahwuQuizSessionUiAction.Continue)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            check(state is NahwuQuizSessionUiState.QuestionVisible)
            assertEquals(1, state.questionIndex)
            assertEquals(questions[1].id, state.question.id)
            assertNull(state.selectedOption)
            assertTrue(!state.isSubmitted)
            job.cancel()
        }

    @Test
    fun continueOnLastQuestionCompletesTheAttempt() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeNahwuQuizRepository(questions = listOf(questions[0]))
            val viewModel = createViewModel(repository = repository)
            val job = subscribeToSessionState(viewModel)
            advanceUntilIdle()

            viewModel.onAction(NahwuQuizSessionUiAction.SelectOption(questions[0].correctOption))
            viewModel.onAction(NahwuQuizSessionUiAction.Submit)
            advanceUntilIdle()
            viewModel.onAction(NahwuQuizSessionUiAction.Continue)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            check(state is NahwuQuizSessionUiState.Completed)
            val completedAttempt = repository.attemptOrNull(state.attemptId)
            assertTrue(completedAttempt?.isCompleted == true)
            assertEquals(1, completedAttempt?.correctCount)
            job.cancel()
        }

    @Test
    fun resumingAnInProgressAttemptStartsAtItsSavedIndex() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeNahwuQuizRepository(questions = questions)
            repository.seedInProgressAttempt(packageId = PACKAGE_ID, questionIndex = 1, correctCount = 1)
            val viewModel = createViewModel(repository = repository)
            val job = subscribeToSessionState(viewModel)

            advanceUntilIdle()

            val state = viewModel.uiState.value
            check(state is NahwuQuizSessionUiState.QuestionVisible)
            assertEquals(1, state.questionIndex)
            assertEquals(questions[1].id, state.question.id)
            job.cancel()
        }

    private fun createViewModel(repository: NahwuQuizRepository = FakeNahwuQuizRepository(questions = questions)) =
        NahwuQuizSessionViewModel(packageId = PACKAGE_ID, nahwuQuizRepository = repository)

    private companion object {
        const val PACKAGE_ID = "nahwu-dasar-fixture"

        val questions =
            listOf(
                NahwuQuizQuestion(
                    id = "q1",
                    packageId = PACKAGE_ID,
                    order = 1,
                    stem = "[FIXTURE] Pertanyaan 1",
                    options =
                        listOf(
                            NahwuQuizOption(NahwuQuizOptionKey.A, "[FIXTURE] A"),
                            NahwuQuizOption(NahwuQuizOptionKey.B, "[FIXTURE] B"),
                            NahwuQuizOption(NahwuQuizOptionKey.C, "[FIXTURE] C"),
                            NahwuQuizOption(NahwuQuizOptionKey.D, "[FIXTURE] D"),
                        ),
                    correctOption = NahwuQuizOptionKey.B,
                    explanation = null,
                ),
                NahwuQuizQuestion(
                    id = "q2",
                    packageId = PACKAGE_ID,
                    order = 2,
                    stem = "[FIXTURE] Pertanyaan 2",
                    options =
                        listOf(
                            NahwuQuizOption(NahwuQuizOptionKey.A, "[FIXTURE] A"),
                            NahwuQuizOption(NahwuQuizOptionKey.B, "[FIXTURE] B"),
                            NahwuQuizOption(NahwuQuizOptionKey.C, "[FIXTURE] C"),
                            NahwuQuizOption(NahwuQuizOptionKey.D, "[FIXTURE] D"),
                        ),
                    correctOption = NahwuQuizOptionKey.A,
                    explanation = null,
                ),
            )
    }
}

/** In-memory fake mirroring `NahwuQuizRepositoryImpl`'s real attempt-advancement semantics
 * (`submitAnswer` advances the index atomically, `completeAttempt` is a separate call) — the
 * behaviour under test is the state machine in [NahwuQuizSessionViewModel], not this fake. */
private class FakeNahwuQuizRepository(
    private val questions: List<NahwuQuizQuestion>,
) : NahwuQuizRepository {
    private val attempts = mutableMapOf<String, NahwuQuizAttempt>()
    private var nextAttemptId = 0

    fun attemptOrNull(attemptId: String): NahwuQuizAttempt? = attempts[attemptId]

    fun seedInProgressAttempt(
        packageId: String,
        questionIndex: Int,
        correctCount: Int,
    ) {
        val id = "seeded-attempt"
        attempts[id] =
            NahwuQuizAttempt(
                id = id,
                packageId = packageId,
                startedAtEpochMillis = 0,
                completedAtEpochMillis = null,
                currentQuestionIndex = questionIndex,
                correctCount = correctCount,
                totalCount = questions.size,
            )
    }

    override fun observePackageSummaries(): Flow<List<NahwuQuizPackageSummary>> = flowOf(emptyList())

    override fun observeActiveAttempt(): Flow<NahwuQuizActiveAttempt?> = flowOf(null)

    override suspend fun getPackage(packageId: String): NahwuQuizPackage =
        NahwuQuizPackage(
            id = packageId,
            title = "[FIXTURE] Nahwu Dasar",
            description = "[FIXTURE]",
            order = 1,
            isActive = true,
            questionCount = questions.size,
        )

    override suspend fun getQuestions(packageId: String): List<NahwuQuizQuestion> = questions

    override suspend fun getOrCreateActiveAttempt(packageId: String): NahwuQuizAttempt {
        attempts.values.firstOrNull { it.packageId == packageId && !it.isCompleted }?.let { return it }
        val attempt =
            NahwuQuizAttempt(
                id = "attempt-${nextAttemptId++}",
                packageId = packageId,
                startedAtEpochMillis = 0,
                completedAtEpochMillis = null,
                currentQuestionIndex = 0,
                correctCount = 0,
                totalCount = questions.size,
            )
        attempts[attempt.id] = attempt
        return attempt
    }

    override fun observeAttempt(attemptId: String): Flow<NahwuQuizAttempt?> = flowOf(attempts[attemptId])

    override suspend fun submitAnswer(
        attemptId: String,
        isCorrect: Boolean,
    ): NahwuQuizAttempt {
        val current = attempts.getValue(attemptId)
        val updated =
            current.copy(
                currentQuestionIndex = current.currentQuestionIndex + 1,
                correctCount = current.correctCount + if (isCorrect) 1 else 0,
            )
        attempts[attemptId] = updated
        return updated
    }

    override suspend fun completeAttempt(attemptId: String): NahwuQuizAttempt {
        val updated = attempts.getValue(attemptId).copy(completedAtEpochMillis = 1_000L)
        attempts[attemptId] = updated
        return updated
    }

    override fun observeCompletedAttempts(packageId: String): Flow<List<NahwuQuizAttempt>> = flowOf(emptyList())

    override suspend fun getPreviousScorePercent(
        packageId: String,
        excludingAttemptId: String,
    ): Int? = null
}
