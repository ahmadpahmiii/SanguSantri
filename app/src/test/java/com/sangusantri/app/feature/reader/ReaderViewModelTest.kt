package com.sangusantri.app.feature.reader

import com.sangusantri.app.domain.model.Amaliyah
import com.sangusantri.app.domain.model.AmaliyahStep
import com.sangusantri.app.domain.model.AmaliyahVersion
import com.sangusantri.app.domain.model.AmaliyahVersionDetail
import com.sangusantri.app.domain.model.AmaliyahVersionStatus
import com.sangusantri.app.domain.model.Approval
import com.sangusantri.app.domain.model.ApprovalStatus
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.domain.model.ReadingPosition
import com.sangusantri.app.domain.model.StepType
import com.sangusantri.app.domain.repository.ContentRepository
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
    fun uiStateBecomesContentAvailableWhenPublishedVersionExists() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            val jobs = subscribeToReaderState(viewModel)

            advanceUntilIdle()

            val state = viewModel.uiState.value
            check(state is ReaderUiState.ContentAvailable)
            assertEquals(version.id, state.versionId)
            assertEquals(steps.size, state.steps.size)
            assertEquals(0, state.initialItemIndex)
            assertEquals(0, state.initialItemOffset)
            jobs.forEach { it.cancel() }
        }

    @Test
    fun uiStateBecomesUnavailableWhenNoAmaliyahForSlug() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel =
                createViewModel(contentRepository = FakeContentRepository(amaliyah = null, detail = detail))
            val jobs = subscribeToReaderState(viewModel)

            advanceUntilIdle()

            assertEquals(ReaderUiState.ContentUnavailable, viewModel.uiState.value)
            jobs.forEach { it.cancel() }
        }

    @Test
    fun uiStateBecomesUnavailableWhenNoPublishedVersion() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel =
                createViewModel(contentRepository = FakeContentRepository(amaliyah = tahlil, detail = null))
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
            val viewModel = createViewModel(contentRepository = FlakyContentRepository(tahlil, detail))
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
            val positionRepository = FakeReadingPositionRepository(initial = ReadingPosition(version.id, 1, 120, 0))
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
            val positionRepository = FakeReadingPositionRepository(initial = ReadingPosition(version.id, 99, 500, 0))
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
        contentRepository: ContentRepository = FakeContentRepository(tahlil, detail),
        readingPositionRepository: ReadingPositionRepository = FakeReadingPositionRepository(),
        readerSettingsRepository: ReaderSettingsRepository = FakeReaderSettingsRepository(),
    ) = ReaderViewModel(
        amaliyahSlug = "tahlil",
        contentRepository = contentRepository,
        readingPositionRepository = readingPositionRepository,
        readerSettingsRepository = readerSettingsRepository,
    )

    private companion object {
        val tahlil =
            Amaliyah(
                id = "tahlil",
                slug = "tahlil",
                titleId = "Tahlil",
                titleAr = "[FIXTURE-AR] Tahlil",
                descriptionId = null,
                descriptionAr = null,
                category = "AMALIYAH",
            )

        val approval =
            Approval(
                id = "approval-1",
                approverName = "[FIXTURE]",
                approverRole = "[FIXTURE]",
                institutionName = null,
                approvalDate = "2026-01-01",
                approvalScope = "[FIXTURE]",
                publicDocumentStorageKey = null,
                documentReferenceNumber = null,
                status = ApprovalStatus.PENDING,
            )

        val version =
            AmaliyahVersion(
                id = "tahlil-umum-v1",
                variantId = "tahlil-umum",
                versionNumber = 1,
                schemaVersion = 1,
                status = AmaliyahVersionStatus.PUBLISHED,
                sourceName = "[FIXTURE]",
                sourceReference = "[FIXTURE]",
                approvalId = approval.id,
                checksumSha256 = "abc",
                minimumAppVersionCode = 1,
                publishedAt = "2026-01-01T00:00:00Z",
                revokedAt = null,
            )

        val steps =
            listOf(
                AmaliyahStep(
                    id = "step-1",
                    versionId = version.id,
                    position = 1,
                    stepType = StepType.HEADING,
                    titleId = "Pembukaan",
                    titleAr = null,
                    arabicText = null,
                    translationId = null,
                    instructionId = null,
                    instructionAr = null,
                    repeatTarget = null,
                    quranSurahNumber = null,
                    quranAyahStart = null,
                    quranAyahEnd = null,
                    audioGroupId = null,
                ),
                AmaliyahStep(
                    id = "step-2",
                    versionId = version.id,
                    position = 2,
                    stepType = StepType.ARABIC_TEXT,
                    titleId = null,
                    titleAr = null,
                    arabicText = "[FIXTURE-AR]",
                    translationId = "[FIXTURE]",
                    instructionId = null,
                    instructionAr = null,
                    repeatTarget = null,
                    quranSurahNumber = null,
                    quranAyahStart = null,
                    quranAyahEnd = null,
                    audioGroupId = null,
                ),
            )

        val detail = AmaliyahVersionDetail(version = version, approval = approval, steps = steps)
    }
}

private class FakeContentRepository(
    private val amaliyah: Amaliyah?,
    private val detail: AmaliyahVersionDetail?,
) : ContentRepository {
    override fun observeAmaliyah(): Flow<List<Amaliyah>> = flowOf(emptyList())

    override suspend fun getAmaliyahBySlug(amaliyahSlug: String): Amaliyah? = amaliyah

    override suspend fun getDefaultVersionDetail(amaliyahSlug: String): AmaliyahVersionDetail? = detail
}

private class ThrowingContentRepository : ContentRepository {
    override fun observeAmaliyah(): Flow<List<Amaliyah>> = flowOf(emptyList())

    override suspend fun getAmaliyahBySlug(amaliyahSlug: String): Amaliyah? = error("boom")

    override suspend fun getDefaultVersionDetail(amaliyahSlug: String): AmaliyahVersionDetail? = error("boom")
}

/** Fails the first load, then succeeds on retry — used to test [ReaderUiAction.Retry]. */
private class FlakyContentRepository(
    private val amaliyah: Amaliyah,
    private val detail: AmaliyahVersionDetail,
) : ContentRepository {
    private var attempt = 0

    override fun observeAmaliyah(): Flow<List<Amaliyah>> = flowOf(emptyList())

    override suspend fun getAmaliyahBySlug(amaliyahSlug: String): Amaliyah? {
        attempt++
        if (attempt == 1) error("boom")
        return amaliyah
    }

    override suspend fun getDefaultVersionDetail(amaliyahSlug: String): AmaliyahVersionDetail? = detail
}

private class FakeReadingPositionRepository(
    initial: ReadingPosition? = null,
) : ReadingPositionRepository {
    private var stored: ReadingPosition? = initial
    val savedPositions = mutableListOf<ReadingPosition>()

    override suspend fun getPosition(versionId: String): ReadingPosition? = stored

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
}
