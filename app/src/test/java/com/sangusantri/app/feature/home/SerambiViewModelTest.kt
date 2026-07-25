package com.sangusantri.app.feature.home

import com.sangusantri.app.domain.model.Amaliyah
import com.sangusantri.app.domain.model.AmaliyahVersionDetail
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
    fun uiStateBecomesContentWithRepositoryAmaliyah() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = SerambiViewModel(FakeContentRepository(flowOf(listOf(tahlil, istighosah))))

            val collected = mutableListOf<SerambiUiState>()
            val job = launch { viewModel.uiState.toList(collected) }
            advanceUntilIdle()
            job.cancel()

            assertEquals(SerambiUiState.Content(listOf(tahlil, istighosah)), collected.last())
        }

    @Test
    fun emptyCatalogueIsContentWithEmptyListNotLoading() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = SerambiViewModel(FakeContentRepository(flowOf(emptyList())))

            val collected = mutableListOf<SerambiUiState>()
            val job = launch { viewModel.uiState.toList(collected) }
            advanceUntilIdle()
            job.cancel()

            assertEquals(SerambiUiState.Content(emptyList()), collected.last())
        }

    private companion object {
        val tahlil =
            Amaliyah(
                id = "tahlil",
                slug = "tahlil",
                titleId = "Tahlil",
                titleAr = "[FIXTURE-AR] Tahlil",
                descriptionId = "FIXTURE PENGEMBANGAN",
                descriptionAr = null,
                category = "AMALIYAH",
            )
        val istighosah = tahlil.copy(id = "istighosah", slug = "istighosah", titleId = "Istighosah")
    }
}

private class FakeContentRepository(
    private val amaliyah: Flow<List<Amaliyah>>,
) : ContentRepository {
    override fun observeAmaliyah(): Flow<List<Amaliyah>> = amaliyah

    override suspend fun getAmaliyahBySlug(amaliyahSlug: String): Amaliyah? = null

    override suspend fun getDefaultVersionDetail(amaliyahSlug: String): AmaliyahVersionDetail? = null
}
