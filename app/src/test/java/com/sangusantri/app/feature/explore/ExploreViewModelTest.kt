package com.sangusantri.app.feature.explore

import androidx.lifecycle.SavedStateHandle
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
class ExploreViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun sholawatCategoryItemsAreExcludedFromAmaliyahCatalogue() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel =
                ExploreViewModel(
                    contentRepository = FakeContentRepository(flowOf(listOf(tahlil, sholawatNariyah))),
                    savedStateHandle = SavedStateHandle(),
                )

            val collected = mutableListOf<ExploreUiState>()
            val job = launch { viewModel.uiState.toList(collected) }
            advanceUntilIdle()
            job.cancel()

            val state = collected.last() as ExploreUiState.ContentReady
            assertEquals(listOf(tahlil), state.items)
            assertEquals(listOf(tahlil), state.filteredItems)
            assertEquals(listOf("Tahlil dan Doa"), state.categories)
        }

    private companion object {
        val tahlil =
            Content(
                id = "tahlil",
                title = "Tahlil",
                description = "[FIXTURE]",
                imageUrl = null,
                category = "Tahlil dan Doa",
                version = 1,
                order = 1,
                isActive = true,
                sourceName = "[FIXTURE]",
                sourceUrl = "https://example.invalid/fixture",
            )
        val sholawatNariyah =
            tahlil.copy(
                id = "sholawat-nariyah",
                title = "[FIXTURE] Sholawat Nariyah",
                category = Content.SHOLAWAT_CATEGORY,
            )
    }
}

private class FakeContentRepository(
    private val content: Flow<List<Content>>,
) : ContentRepository {
    override fun observeActiveContent(): Flow<List<Content>> = content

    override suspend fun getContentById(contentId: String): Content? = null

    override suspend fun getContentDetail(contentId: String): ContentDetail? = null
}
