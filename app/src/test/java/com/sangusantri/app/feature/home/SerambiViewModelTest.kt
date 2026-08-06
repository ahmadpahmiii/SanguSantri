package com.sangusantri.app.feature.home

import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.ContentDetail
import com.sangusantri.app.domain.repository.ContentRepository
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
            val viewModel = SerambiViewModel(FakeContentRepository(flowOf(listOf(tahlil))))

            assertEquals(SerambiUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun uiStateBecomesLoadedWithRepositoryContent() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = SerambiViewModel(FakeContentRepository(flowOf(listOf(tahlil, istighosah))))

            val collected = mutableListOf<SerambiUiState>()
            val job = launch { viewModel.uiState.toList(collected) }
            advanceUntilIdle()
            job.cancel()

            assertEquals(SerambiUiState.Loaded(listOf(tahlil, istighosah)), collected.last())
        }

    @Test
    fun emptyCatalogueIsLoadedWithEmptyListNotLoading() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = SerambiViewModel(FakeContentRepository(flowOf(emptyList())))

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
