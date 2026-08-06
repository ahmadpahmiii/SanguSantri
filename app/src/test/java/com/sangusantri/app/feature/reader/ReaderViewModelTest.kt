package com.sangusantri.app.feature.reader

import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.ContentDetail
import com.sangusantri.app.domain.model.ContentStep
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
import com.sangusantri.app.feature.home.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * [ReaderViewModel.uiState] and [ReaderViewModel.settings] are `stateIn(WhileSubscribed(...))` —
 * reading `.value` alone never starts the upstream combine/DataStore flow. Every test that reads
 * either `.value` must keep an active collector for the assertion to see anything but the initial
 * value, exactly as a real `collectAsStateWithLifecycle()` subscriber would in the UI.
 */
private fun TestScope.subscribeToReaderState(viewModel: ReaderViewModel): List<Job> =
    listOf(
        launch { viewModel.uiState.collect {} },
        launch { viewModel.settings.collect {} },
    )

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiStateStartsAsLoadingBeforeContentResolves() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()

            assertEquals(ReaderUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun uiStateBecomesContentAvailableWhenDetailExists() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val jobs = subscribeToReaderState(viewModel)

            advanceUntilIdle()

            val state = viewModel.uiState.value
            check(state is ReaderUiState.ContentAvailable)
            assertEquals(content.id, state.contentId)
            assertEquals(steps.size, state.steps.size)
            assertEquals(0, state.initialItemIndex)
            assertEquals(0, state.initialItemOffset)
            jobs.forEach { it.cancel() }
        }

    @Test
    fun uiStateBecomesUnavailableWhenNoDetailForId() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel =
                createViewModel(contentRepository = FakeContentRepository(content = null, detail = null))
            val jobs = subscribeToReaderState(viewModel)

            advanceUntilIdle()

            assertEquals(ReaderUiState.ContentUnavailable, viewModel.uiState.value)
            jobs.forEach { it.cancel() }
        }

    @Test
    fun uiStateBecomesUnavailableWhenDetailHasNoSteps() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel =
                createViewModel(
                    contentRepository =
                        FakeContentRepository(content = content, detail = ContentDetail(content, emptyList())),
                )
            val jobs = subscribeToReaderState(viewModel)

            advanceUntilIdle()

            assertEquals(ReaderUiState.ContentUnavailable, viewModel.uiState.value)
            jobs.forEach { it.cancel() }
        }

    @Test
    fun uiStateBecomesRecoverableErrorWhenRepositoryThrows() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(contentRepository = ThrowingContentRepository())
            val jobs = subscribeToReaderState(viewModel)

            advanceUntilIdle()

            assertEquals(ReaderUiState.RecoverableError, viewModel.uiState.value)
            jobs.forEach { it.cancel() }
        }

    @Test
    fun retryReloadsContentAfterAnError() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(contentRepository = FlakyContentRepository(detail))
            val jobs = subscribeToReaderState(viewModel)

            advanceUntilIdle()
            assertEquals(ReaderUiState.RecoverableError, viewModel.uiState.value)

            viewModel.onAction(ReaderUiAction.Retry)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value is ReaderUiState.ContentAvailable)
            jobs.forEach { it.cancel() }
        }

    @Test
    fun restoredPositionWithinBoundsIsUsed() =
        runTest(mainDispatcherRule.testDispatcher) {
            val positionRepository = FakeReadingPositionRepository(initial = ReadingPosition(content.id, 1, 120, 0))
            val viewModel = createViewModel(readingPositionRepository = positionRepository)
            val jobs = subscribeToReaderState(viewModel)

            advanceUntilIdle()

            val state = viewModel.uiState.value
            check(state is ReaderUiState.ContentAvailable)
            assertEquals(1, state.initialItemIndex)
            assertEquals(120, state.initialItemOffset)
            jobs.forEach { it.cancel() }
        }

    @Test
    fun restoredPositionOutOfBoundsFallsBackToStart() =
        runTest(mainDispatcherRule.testDispatcher) {
            val positionRepository = FakeReadingPositionRepository(initial = ReadingPosition(content.id, 99, 500, 0))
            val viewModel = createViewModel(readingPositionRepository = positionRepository)
            val jobs = subscribeToReaderState(viewModel)

            advanceUntilIdle()

            val state = viewModel.uiState.value
            check(state is ReaderUiState.ContentAvailable)
            assertEquals(0, state.initialItemIndex)
            assertEquals(0, state.initialItemOffset)
            jobs.forEach { it.cancel() }
        }

    @Test
    fun scrollPositionChangesAreDebouncedBeforeSaving() =
        runTest(mainDispatcherRule.testDispatcher) {
            val positionRepository = FakeReadingPositionRepository(initial = null)
            val viewModel = createViewModel(readingPositionRepository = positionRepository)
            advanceUntilIdle()

            viewModel.onAction(ReaderUiAction.ScrollPositionChanged(0, 10))
            viewModel.onAction(ReaderUiAction.ScrollPositionChanged(0, 40))
            viewModel.onAction(ReaderUiAction.ScrollPositionChanged(1, 0))

            advanceTimeBy(200)
            assertEquals(0, positionRepository.savedPositions.size)

            advanceTimeBy(500)
            assertEquals(1, positionRepository.savedPositions.size)
            assertEquals(1, positionRepository.savedPositions.last().itemIndex)
        }

    @Test
    fun persistPositionNowBypassesTheDebounce() =
        runTest(mainDispatcherRule.testDispatcher) {
            val positionRepository = FakeReadingPositionRepository(initial = null)
            val viewModel = createViewModel(readingPositionRepository = positionRepository)
            advanceUntilIdle()

            viewModel.onAction(ReaderUiAction.PersistPositionNow(1, 200))
            advanceUntilIdle()

            assertEquals(1, positionRepository.savedPositions.size)
            assertEquals(1, positionRepository.savedPositions.last().itemIndex)
            assertEquals(200, positionRepository.savedPositions.last().itemOffset)
        }

    @Test
    fun settingsChangesFlowThroughToContentAvailableState() =
        runTest(mainDispatcherRule.testDispatcher) {
            val settingsRepository = FakeReaderSettingsRepository()
            val viewModel = createViewModel(readerSettingsRepository = settingsRepository)
            val jobs = subscribeToReaderState(viewModel)
            advanceUntilIdle()

            viewModel.onAction(ReaderUiAction.SetShowTranslation(false))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            check(state is ReaderUiState.ContentAvailable)
            assertEquals(false, state.settings.showTranslation)
            jobs.forEach { it.cancel() }
        }

    @Test
    fun settingsWriteIsClampedToBounds() =
        runTest(mainDispatcherRule.testDispatcher) {
            val settingsRepository = FakeReaderSettingsRepository()
            val viewModel = createViewModel(readerSettingsRepository = settingsRepository)
            val jobs = subscribeToReaderState(viewModel)
            advanceUntilIdle()

            viewModel.onAction(ReaderUiAction.SetArabicFontSize(ReaderSettings.MAX_ARABIC_FONT_SIZE_SP + 100))
            advanceUntilIdle()

            assertEquals(ReaderSettings.MAX_ARABIC_FONT_SIZE_SP, viewModel.settings.value.arabicFontSizeSp)
            jobs.forEach { it.cancel() }
        }

    private fun createViewModel(
        contentRepository: ContentRepository = FakeContentRepository(content, detail),
        readingPositionRepository: ReadingPositionRepository = FakeReadingPositionRepository(),
        readerSettingsRepository: ReaderSettingsRepository = FakeReaderSettingsRepository(),
        guidedReadingRepository: GuidedReadingRepository = FakeGuidedReadingRepository(),
    ) = ReaderViewModel(
        contentId = "tahlil",
        contentRepository = contentRepository,
        readingPositionRepository = readingPositionRepository,
        readerSettingsRepository = readerSettingsRepository,
        guidedReadingRepository = guidedReadingRepository,
    )

    private companion object {
        val content =
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

        val steps =
            listOf(
                ContentStep(
                    id = "step-1",
                    contentId = content.id,
                    position = 1,
                    arabicText = "[FIXTURE-AR]",
                    translation = "[FIXTURE]",
                    repeatTarget = 1,
                ),
                ContentStep(
                    id = "step-2",
                    contentId = content.id,
                    position = 2,
                    arabicText = "[FIXTURE-AR]",
                    translation = "[FIXTURE]",
                    repeatTarget = 1,
                ),
            )

        val detail = ContentDetail(content = content, steps = steps)
    }
}

private class FakeContentRepository(
    private val content: Content?,
    private val detail: ContentDetail?,
) : ContentRepository {
    override fun observeActiveContent(): Flow<List<Content>> = flowOf(emptyList())

    override suspend fun getContentById(contentId: String): Content? = content

    override suspend fun getContentDetail(contentId: String): ContentDetail? = detail
}

private class ThrowingContentRepository : ContentRepository {
    override fun observeActiveContent(): Flow<List<Content>> = flowOf(emptyList())

    override suspend fun getContentById(contentId: String): Content? = error("boom")

    override suspend fun getContentDetail(contentId: String): ContentDetail? = error("boom")
}

/** Fails the first load, then succeeds on retry — used to test [ReaderUiAction.Retry]. */
private class FlakyContentRepository(
    private val detail: ContentDetail,
) : ContentRepository {
    private var attempt = 0

    override fun observeActiveContent(): Flow<List<Content>> = flowOf(emptyList())

    override suspend fun getContentById(contentId: String): Content? = detail.content

    override suspend fun getContentDetail(contentId: String): ContentDetail? {
        attempt++
        if (attempt == 1) error("boom")
        return detail
    }
}

private class FakeReadingPositionRepository(
    initial: ReadingPosition? = null,
) : ReadingPositionRepository {
    private var stored: ReadingPosition? = initial
    val savedPositions = mutableListOf<ReadingPosition>()

    override suspend fun getPosition(contentId: String): ReadingPosition? = stored

    override suspend fun savePosition(position: ReadingPosition) {
        stored = position
        savedPositions.add(position)
    }
}

private class FakeReaderSettingsRepository : ReaderSettingsRepository {
    private val state = MutableStateFlow(ReaderSettings())

    override fun observe(): Flow<ReaderSettings> = state

    override suspend fun setArabicFontSize(sp: Int) {
        state.value = state.value.copy(arabicFontSizeSp = ReaderSettings.coerceArabicFontSize(sp))
    }

    override suspend fun setTranslationFontSize(sp: Int) {
        state.value = state.value.copy(translationFontSizeSp = ReaderSettings.coerceTranslationFontSize(sp))
    }

    override suspend fun setArabicLineSpacing(multiplier: Float) {
        state.value = state.value.copy(arabicLineSpacingMultiplier = ReaderSettings.coerceLineSpacing(multiplier))
    }

    override suspend fun setTranslationLineSpacing(multiplier: Float) {
        state.value =
            state.value.copy(translationLineSpacingMultiplier = ReaderSettings.coerceLineSpacing(multiplier))
    }

    override suspend fun setShowTranslation(show: Boolean) {
        state.value = state.value.copy(showTranslation = show)
    }

    override suspend fun setLastReaderMode(mode: ReaderMode) {
        state.value = state.value.copy(lastReaderMode = mode)
    }

    override suspend fun setGuidedProgressionMode(mode: GuidedProgressionMode) {
        state.value = state.value.copy(guidedProgressionMode = mode)
    }
}

private class FakeGuidedReadingRepository : GuidedReadingRepository {
    private val sessions = mutableMapOf<String, GuidedReadingSession>()
    private val progress = mutableMapOf<String, MutableList<StepProgress>>()

    override suspend fun getSession(contentId: String): GuidedReadingSession? = sessions[contentId]

    override suspend fun saveSession(session: GuidedReadingSession) {
        sessions[session.contentId] = session
    }

    override suspend fun getStepProgress(contentId: String): List<StepProgress> = progress[contentId].orEmpty()

    override suspend fun saveStepProgress(progress: StepProgress) {
        this.progress.getOrPut(progress.contentId) { mutableListOf() }.add(progress)
    }
}
