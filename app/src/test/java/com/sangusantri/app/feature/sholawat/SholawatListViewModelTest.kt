package com.sangusantri.app.feature.sholawat

import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.ContentDetail
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
class SholawatListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiStateStartsAsLoadingBeforeRepositoryEmits() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = SholawatListViewModel(FakeListContentRepository(flowOf(listOf(sholawatNariyah))))

            assertEquals(SholawatListUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun onlyShalawatCategoryItemsAreListed() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel =
                SholawatListViewModel(FakeListContentRepository(flowOf(listOf(sholawatNariyah, tahlil))))

            val collected = mutableListOf<SholawatListUiState>()
            val job = launch { viewModel.uiState.toList(collected) }
            advanceUntilIdle()
            job.cancel()

            assertEquals(SholawatListUiState.ContentReady(listOf(sholawatNariyah)), collected.last())
        }

    private companion object {
        val sholawatNariyah =
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
            )
        val tahlil = sholawatNariyah.copy(id = "tahlil", title = "Tahlil", category = "Tahlil dan Doa")
    }
}

private class FakeListContentRepository(
    private val content: Flow<List<Content>>,
) : ContentRepository {
    override fun observeActiveContent(): Flow<List<Content>> = content

    override suspend fun getContentById(contentId: String): Content? = null

    override suspend fun getContentDetail(contentId: String): ContentDetail? = null
}
