package com.sangusantri.app.feature.sholawat

import com.sangusantri.app.core.network.ApiResult
import com.sangusantri.app.core.result.Resource
import com.sangusantri.app.domain.model.AppThemeMode
import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.ContentDetail
import com.sangusantri.app.domain.model.ContentStep
import com.sangusantri.app.domain.model.GuidedProgressionMode
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.domain.model.QuranDisplayMode
import com.sangusantri.app.domain.model.QuranMurottalSpeed
import com.sangusantri.app.domain.model.QuranReaderSettings
import com.sangusantri.app.domain.model.ReaderMode
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.domain.repository.ContentRepository
import com.sangusantri.app.domain.repository.QuranReaderSettingsRepository
import com.sangusantri.app.domain.repository.ReaderSettingsRepository
import com.sangusantri.app.feature.home.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SholawatReaderViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiStateStartsAsLoadingBeforeRepositoryResolves() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeReaderContentRepository(detail))

        assertEquals(SholawatReaderUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun uiStateBecomesContentAvailableWithSteps() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeReaderContentRepository(detail))

        val collected = mutableListOf<SholawatReaderUiState>()
        val job = launch { viewModel.uiState.toList(collected) }
        advanceUntilIdle()
        job.cancel()

        assertEquals(
            SholawatReaderUiState.ContentAvailable(
                title = detail.content.title,
                steps = detail.steps,
                layout = detail.content.layout,
                settings = ReaderSettings(),
            ),
            collected.last(),
        )
    }

    @Test
    fun uiStateBecomesUnavailableWhenContentIsMissing() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeReaderContentRepository(null))

        val collected = mutableListOf<SholawatReaderUiState>()
        val job = launch { viewModel.uiState.toList(collected) }
        advanceUntilIdle()
        job.cancel()

        assertEquals(SholawatReaderUiState.Unavailable, collected.last())
    }

    private fun createViewModel(contentRepository: ContentRepository) = SholawatReaderViewModel(
        contentId = "sholawat-nariyah",
        contentRepository = contentRepository,
        quranReaderSettingsRepository = FakeQuranReaderSettingsRepository(),
        readerSettingsRepository = FakeReaderSettingsRepository(),
    )

    private companion object {
        val steps =
            listOf(
                ContentStep(
                    id = "step-1",
                    contentId = "sholawat-nariyah",
                    position = 0,
                    arabicText = "[FIXTURE]",
                    translation = "[FIXTURE]",
                    repeatTarget = 1,
                ),
            )
        val detail =
            ContentDetail(
                content =
                    Content(
                        id = "sholawat-nariyah",
                        title = "[FIXTURE] Sholawat Nariyah",
                        description = "[FIXTURE]",
                        imageUrl = null,
                        category = Content.SHOLAWAT_CATEGORY,
                        version = 1,
                        order = 1,
                        isActive = true,
                        sourceName = "[FIXTURE]",
                        sourceUrl = "https://example.invalid/fixture",
                    ),
                steps = steps,
            )
    }
}

private class FakeReaderContentRepository(private val detail: ContentDetail?) : ContentRepository {
    override fun observeActiveContent(): Flow<Resource<List<Content>>> = flowOf(Resource.Success(emptyList()))

    override fun observeContentIdsMatchingStepText(query: String): Flow<List<String>> = flowOf(emptyList())

    override suspend fun getContentById(contentId: String): Content? = detail?.content

    override suspend fun getCachedContentDetail(contentId: String): ContentDetail? = detail

    override fun observeContentDetail(contentId: String): Flow<Resource<ContentDetail>> =
        detail?.let { flowOf(Resource.Success(it)) } ?: flowOf(Resource.Error(ApiResult.NetworkError("offline")))

    override suspend fun refreshCatalogue(): ApiResult<Unit> = ApiResult.Success(Unit)
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

    override suspend fun setSholawatTwoColumn(enabled: Boolean) {
        state.value = state.value.copy(sholawatTwoColumn = enabled)
    }

    override suspend fun setSholawatBaitGap(enabled: Boolean) {
        state.value = state.value.copy(sholawatBaitGap = enabled)
    }
}

private class FakeQuranReaderSettingsRepository : QuranReaderSettingsRepository {
    override fun observe(): Flow<QuranReaderSettings> = flowOf(QuranReaderSettings())

    override suspend fun setDisplayMode(mode: QuranDisplayMode) = Unit

    override suspend fun setArabicFont(font: QuranArabicFont) = Unit

    override suspend fun setArabicSize(sp: Int) = Unit

    override suspend fun setArabicLineSpacing(multiplier: Float) = Unit

    override suspend fun setTranslationSize(sp: Int) = Unit

    override suspend fun setBrightnessOverride(value: Float) = Unit

    override suspend fun setThemeMode(mode: AppThemeMode) = Unit

    override suspend fun setMurottalSpeed(speed: QuranMurottalSpeed) = Unit

    override suspend fun setMurottalContinueAcrossSurah(enabled: Boolean) = Unit

    override suspend fun setMurottalKeepScreenOn(enabled: Boolean) = Unit
}
