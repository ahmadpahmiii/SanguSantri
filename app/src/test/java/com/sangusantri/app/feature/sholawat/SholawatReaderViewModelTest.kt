package com.sangusantri.app.feature.sholawat

import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.ContentDetail
import com.sangusantri.app.domain.model.ContentStep
import com.sangusantri.app.domain.repository.ContentRepository
import com.sangusantri.app.feature.home.MainDispatcherRule
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
class SholawatReaderViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiStateStartsAsLoadingBeforeRepositoryResolves() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(FakeReaderContentRepository(detail))

            assertEquals(SholawatReaderUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun uiStateBecomesContentAvailableWithSteps() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(FakeReaderContentRepository(detail))

            val collected = mutableListOf<SholawatReaderUiState>()
            val job = launch { viewModel.uiState.toList(collected) }
            advanceUntilIdle()
            job.cancel()

            assertEquals(
                SholawatReaderUiState.ContentAvailable(title = detail.content.title, steps = detail.steps),
                collected.last(),
            )
        }

    @Test
    fun uiStateBecomesUnavailableWhenContentIsMissing() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(FakeReaderContentRepository(null))

            val collected = mutableListOf<SholawatReaderUiState>()
            val job = launch { viewModel.uiState.toList(collected) }
            advanceUntilIdle()
            job.cancel()

            assertEquals(SholawatReaderUiState.Unavailable, collected.last())
        }

    private fun createViewModel(contentRepository: ContentRepository) =
        SholawatReaderViewModel(contentId = "sholawat-nariyah", contentRepository = contentRepository)

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

private class FakeReaderContentRepository(
    private val detail: ContentDetail?,
) : ContentRepository {
    override fun observeActiveContent(): Flow<List<Content>> = flowOf(emptyList())

    override suspend fun getContentById(contentId: String): Content? = detail?.content

    override suspend fun getContentDetail(contentId: String): ContentDetail? = detail
}
