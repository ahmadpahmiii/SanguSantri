package com.sangusantri.app.feature.home

import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.ContentDetail
import com.sangusantri.app.domain.model.GuidedReadingSession
import com.sangusantri.app.domain.model.NahwuQuizActiveAttempt
import com.sangusantri.app.domain.model.NahwuQuizAttempt
import com.sangusantri.app.domain.model.NahwuQuizPackage
import com.sangusantri.app.domain.model.NahwuQuizPackageSummary
import com.sangusantri.app.domain.model.NahwuQuizQuestion
import com.sangusantri.app.domain.model.QuranBookmark
import com.sangusantri.app.domain.model.QuranPreparationResult
import com.sangusantri.app.domain.model.QuranReadingSession
import com.sangusantri.app.domain.model.QuranReadingState
import com.sangusantri.app.domain.model.QuranSurah
import com.sangusantri.app.domain.model.QuranTafsir
import com.sangusantri.app.domain.model.QuranTafsirResult
import com.sangusantri.app.domain.model.QuranVerse
import com.sangusantri.app.domain.model.ReadingPosition
import com.sangusantri.app.domain.model.Reminder
import com.sangusantri.app.domain.model.StepProgress
import com.sangusantri.app.domain.model.TasbihHistoryEntry
import com.sangusantri.app.domain.model.TasbihSession
import com.sangusantri.app.domain.model.TasbihTargetPreset
import com.sangusantri.app.domain.repository.ContentRepository
import com.sangusantri.app.domain.repository.GuidedReadingRepository
import com.sangusantri.app.domain.repository.HomePreferencesRepository
import com.sangusantri.app.domain.repository.NahwuQuizRepository
import com.sangusantri.app.domain.repository.QuranRepository
import com.sangusantri.app.domain.repository.ReadingPositionRepository
import com.sangusantri.app.domain.repository.ReminderRepository
import com.sangusantri.app.domain.repository.TasbihRepository
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
                createViewModel(FakeContentRepository(flowOf(listOf(tahlil))))

            assertEquals(SerambiUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun uiStateBecomesLoadedWithRepositoryContent() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel =
                createViewModel(FakeContentRepository(flowOf(listOf(tahlil, istighosah))))

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
                createViewModel(FakeContentRepository(flowOf(emptyList())))

            val collected = mutableListOf<SerambiUiState>()
            val job = launch { viewModel.uiState.toList(collected) }
            advanceUntilIdle()
            job.cancel()

            assertEquals(SerambiUiState.Loaded(emptyList()), collected.last())
        }

    private fun createViewModel(contentRepository: ContentRepository): SerambiViewModel =
        SerambiViewModel(
            contentRepository = contentRepository,
            reminderRepository = FakeReminderRepository(),
            nahwuQuizRepository = FakeNahwuQuizRepository(),
            resumeCoordinator =
                SerambiResumeCoordinator(
                    contentRepository = contentRepository,
                    readingPositionRepository = FakeReadingPositionRepository(),
                    guidedReadingRepository = FakeGuidedReadingRepository(),
                    quranRepository = FakeQuranRepository(),
                    tasbihRepository = FakeTasbihRepository(),
                    homePreferencesRepository = FakeHomePreferencesRepository(),
                ),
        )

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

private class FakeReadingPositionRepository : ReadingPositionRepository {
    override suspend fun getPosition(contentId: String): ReadingPosition? = null

    override suspend fun getMostRecentPosition(): ReadingPosition? = null

    override suspend fun savePosition(position: ReadingPosition) = Unit
}

private class FakeGuidedReadingRepository : GuidedReadingRepository {
    override suspend fun getSession(contentId: String): GuidedReadingSession? = null

    override suspend fun getMostRecentIncompleteSession(): GuidedReadingSession? = null

    override suspend fun saveSession(session: GuidedReadingSession) = Unit

    override suspend fun getStepProgress(contentId: String): List<StepProgress> = emptyList()

    override suspend fun saveStepProgress(progress: StepProgress) = Unit
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

private class FakeQuranRepository : QuranRepository {
    override fun observeSurahs(): Flow<List<QuranSurah>> = flowOf(emptyList())

    override fun observeVersesBySurah(surahNumber: Int): Flow<List<QuranVerse>> = flowOf(emptyList())

    override fun observeJuzStarts(): Flow<List<QuranVerse>> = flowOf(emptyList())

    override fun observeBookmarks(): Flow<List<QuranBookmark>> = flowOf(emptyList())

    override fun observeIsBookmarked(
        surahNumber: Int,
        ayatNumber: Int,
    ): Flow<Boolean> = flowOf(false)

    override fun observeReadingState(): Flow<QuranReadingState?> = flowOf(null)

    override fun observeReadingSessions(): Flow<List<QuranReadingSession>> = flowOf(emptyList())

    override suspend fun hasLocalDataset(): Boolean = false

    override suspend fun ensureInitialPreparation(
        onProgress: (completed: Int, total: Int) -> Unit,
    ): QuranPreparationResult = throw UnsupportedOperationException("not needed by SerambiViewModelTest")

    override suspend fun toggleBookmark(
        surahNumber: Int,
        ayatNumber: Int,
    ) = Unit

    override suspend fun setLastRead(
        surahNumber: Int,
        ayatNumber: Int,
        page: Int,
    ) = Unit

    override suspend fun recordReadingSession(
        surahNumber: Int,
        startAyat: Int,
        endAyat: Int,
    ) = Unit

    override suspend fun getCachedTafsir(remoteAyatId: Long): QuranTafsir? = null

    override suspend fun fetchTafsir(remoteAyatId: Long): QuranTafsirResult =
        throw UnsupportedOperationException("not needed by SerambiViewModelTest")
}

private class FakeTasbihRepository : TasbihRepository {
    override fun observeSession(): Flow<TasbihSession?> = flowOf(null)

    override suspend fun incrementCount() = Unit

    override suspend fun startSession(
        targetPreset: TasbihTargetPreset,
        targetValue: Int?,
    ) = Unit

    override suspend fun renameSession(sessionName: String?) = Unit

    override suspend fun resetSession() = Unit

    override fun observeHistory(): Flow<List<TasbihHistoryEntry>> = flowOf(emptyList())
}

private class FakeHomePreferencesRepository : HomePreferencesRepository {
    override fun observeDismissedResumeFingerprint(): Flow<String?> = flowOf(null)

    override suspend fun dismissResume(fingerprint: String) = Unit
}
