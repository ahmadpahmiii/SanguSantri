package com.sangusantri.app.feature.home

import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.ContentDetail
import com.sangusantri.app.domain.model.NahwuQuizActiveAttempt
import com.sangusantri.app.domain.model.NahwuQuizAttempt
import com.sangusantri.app.domain.model.NahwuQuizPackage
import com.sangusantri.app.domain.model.NahwuQuizPackageSummary
import com.sangusantri.app.domain.model.NahwuQuizQuestion
import com.sangusantri.app.domain.model.Reminder
import com.sangusantri.app.domain.repository.ContentRepository
import com.sangusantri.app.domain.repository.NahwuQuizRepository
import com.sangusantri.app.domain.repository.ReminderRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SerambiViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiStateStartsAsLoadingBeforeRepositoryEmits() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel =
                SerambiViewModel(
                    FakeContentRepository(flowOf(listOf(tahlil))),
                    FakeReminderRepository(),
                    FakeNahwuQuizRepository(),
                )

            assertEquals(SerambiUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun uiStateBecomesLoadedWithRepositoryContent() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel =
                SerambiViewModel(
                    FakeContentRepository(flowOf(listOf(tahlil, istighosah))),
                    FakeReminderRepository(),
                    FakeNahwuQuizRepository(),
                )

            val collected = mutableListOf<SerambiUiState>()
            val job = launch { viewModel.uiState.toList(collected) }
            advanceUntilIdle()
            job.cancel()

            assertEquals(SerambiUiState.Loaded(listOf(tahlil, istighosah)), collected.last())
        }

    @Test
    fun emptyCatalogueIsLoadedWithEmptyListNotLoading() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel =
                SerambiViewModel(
                    FakeContentRepository(flowOf(emptyList())),
                    FakeReminderRepository(),
                    FakeNahwuQuizRepository(),
                )

            val collected = mutableListOf<SerambiUiState>()
            val job = launch { viewModel.uiState.toList(collected) }
            advanceUntilIdle()
            job.cancel()

            assertEquals(SerambiUiState.Loaded(emptyList()), collected.last())
        }

    private companion object {
        val tahlil =
            Content(
                id = "tahlil",
                title = "Tahlil",
                description = "[FIXTURE] Tahlil",
                imageUrl = null,
                category = "Tahlil dan Doa",
                version = 1,
                order = 1,
                isActive = true,
                sourceName = "[FIXTURE]",
                sourceUrl = "https://example.invalid/fixture",
            )
        val istighosah = tahlil.copy(id = "istighosah", title = "Istighosah")
    }
}

private class FakeContentRepository(
    private val content: Flow<List<Content>>,
) : ContentRepository {
    override fun observeActiveContent(): Flow<List<Content>> = content

    override suspend fun getContentById(contentId: String): Content? = null

    override suspend fun getContentDetail(contentId: String): ContentDetail? = null
}

private class FakeReminderRepository : ReminderRepository {
    override suspend fun save(reminder: Reminder) = Unit

    override suspend fun delete(reminderId: String) = Unit

    override suspend fun getById(reminderId: String): Reminder? = null

    override suspend fun getAllEnabled(): List<Reminder> = emptyList()

    override fun observeAll(): Flow<List<Reminder>> = flowOf(emptyList())

    override fun observeNearestEnabled(): Flow<Reminder?> = flowOf(null)
}

private class FakeNahwuQuizRepository : NahwuQuizRepository {
    override fun observePackageSummaries(): Flow<List<NahwuQuizPackageSummary>> = flowOf(emptyList())

    override fun observeActiveAttempt(): Flow<NahwuQuizActiveAttempt?> = flowOf(null)

    override suspend fun getPackage(packageId: String): NahwuQuizPackage? = null

    override suspend fun getQuestions(packageId: String): List<NahwuQuizQuestion> = emptyList()

    override suspend fun getOrCreateActiveAttempt(packageId: String): NahwuQuizAttempt =
        throw UnsupportedOperationException("not needed by SerambiViewModelTest")

    override fun observeAttempt(attemptId: String): Flow<NahwuQuizAttempt?> = flowOf(null)

    override suspend fun submitAnswer(
        attemptId: String,
        isCorrect: Boolean,
    ): NahwuQuizAttempt = throw UnsupportedOperationException("not needed by SerambiViewModelTest")

    override suspend fun completeAttempt(attemptId: String): NahwuQuizAttempt =
        throw UnsupportedOperationException("not needed by SerambiViewModelTest")

    override fun observeCompletedAttempts(packageId: String): Flow<List<NahwuQuizAttempt>> = flowOf(emptyList())

    override suspend fun getPreviousScorePercent(
        packageId: String,
        excludingAttemptId: String,
    ): Int? = null
}
