package com.sangusantri.app.feature.explore

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.feature.home.ContentCard
import androidx.compose.foundation.lazy.grid.items as gridItems

@Composable
fun ExploreRoute(
    onBack: () -> Unit,
    onContentSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExploreScreen(
        uiState = uiState,
        actions =
            ExploreActions(
                onBack = onBack,
                onContentSelected = onContentSelected,
                onQueryChanged = viewModel::setQuery,
                onCategorySelected = viewModel::selectCategory,
            ),
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    uiState: ExploreUiState,
    actions: ExploreActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.explore_title)) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back_content_description),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            ExploreUiState.Loading ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

            is ExploreUiState.ContentReady ->
                ExploreContent(
                    state = uiState,
                    actions = actions,
                    modifier = Modifier.padding(innerPadding),
                )
        }
    }
}

@Composable
private fun ExploreContent(
    state: ExploreUiState.ContentReady,
    actions: ExploreActions,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(SanguSantriDimensions.catalogueGridMinCellWidth),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = SanguSantriDimensions.dashboardContentMaxWidth),
            contentPadding = PaddingValues(SanguSantriSpacing.default),
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        ) {
            item(key = "search", span = { GridItemSpan(maxLineSpan) }) {
                ExploreSearchField(
                    query = state.query,
                    onQueryChanged = actions.onQueryChanged,
                    focusManager = focusManager,
                )
            }
            if (state.categories.isNotEmpty()) {
                item(key = "categories", span = { GridItemSpan(maxLineSpan) }) {
                    ExploreCategoryFilters(
                        categories = state.categories,
                        selectedCategory = state.selectedCategory,
                        onCategorySelected = actions.onCategorySelected,
                    )
                }
            }
            item(key = "result_count", span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.explore_result_count, state.filteredItems.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (state.filteredItems.isEmpty()) {
                item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                    ExploreEmptyState(hasCatalogue = state.items.isNotEmpty())
                }
            } else {
                gridItems(items = state.filteredItems, key = { it.id }) { item ->
                    ContentCard(content = item, onClick = actions.onContentSelected)
                }
            }
        }
    }
}

@Composable
private fun ExploreSearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
    focusManager: FocusManager,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = stringResource(R.string.explore_search_placeholder)) },
        leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.explore_clear_search),
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
    )
}

@Composable
private fun ExploreCategoryFilters(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
        contentPadding = PaddingValues(horizontal = SanguSantriSpacing.extraSmall),
    ) {
        item(key = "all") {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text(text = stringResource(R.string.explore_category_all)) },
            )
        }
        items(items = categories, key = { it }) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(text = category) },
            )
        }
    }
}

@Composable
private fun ExploreEmptyState(
    hasCatalogue: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = hasCatalogue,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        label = "explore_empty_state",
    ) { catalogueAvailable ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SanguSantriSpacing.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text =
                    stringResource(
                        if (catalogueAvailable) R.string.explore_empty_search else R.string.serambi_empty_state,
                    ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val previewContent =
    listOf(
        Content(
            id = "tahlil",
            title = "Tahlil",
            description = "[FIXTURE] Konten pengembangan.",
            imageUrl = null,
            category = "Tahlil dan Doa",
            version = 1,
            order = 1,
            isActive = true,
            sourceName = "[FIXTURE]",
            sourceUrl = "[FIXTURE]",
        ),
    )

@PreviewLightDark
@Composable
private fun ExploreScreenPreview() {
    SanguSantriTheme {
        ExploreScreen(
            uiState =
                ExploreUiState.ContentReady(
                    items = previewContent,
                    filteredItems = previewContent,
                    categories = listOf("Tahlil dan Doa"),
                    query = "",
                    selectedCategory = null,
                ),
            actions =
                ExploreActions(
                    onBack = {},
                    onContentSelected = {},
                    onQueryChanged = {},
                    onCategorySelected = {},
                ),
        )
    }
}

data class ExploreActions(
    val onBack: () -> Unit,
    val onContentSelected: (String) -> Unit,
    val onQueryChanged: (String) -> Unit,
    val onCategorySelected: (String?) -> Unit,
)
