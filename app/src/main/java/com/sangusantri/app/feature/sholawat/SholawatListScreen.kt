package com.sangusantri.app.feature.sholawat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.feature.home.ContentCard

@Composable
fun SholawatListRoute(
    onBack: () -> Unit,
    onSholawatSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SholawatListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SholawatListScreen(
        uiState = uiState,
        onBack = onBack,
        onSholawatSelected = onSholawatSelected,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SholawatListScreen(
    uiState: SholawatListUiState,
    onBack: () -> Unit,
    onSholawatSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.sholawat_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            SholawatListUiState.Loading ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

            is SholawatListUiState.ContentReady ->
                SholawatListContent(
                    items = uiState.items,
                    onSholawatSelected = onSholawatSelected,
                    modifier = Modifier.padding(innerPadding),
                )
        }
    }
}

@Composable
private fun SholawatListContent(
    items: List<Content>,
    onSholawatSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        if (items.isEmpty()) {
            SholawatListEmptyState()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(SanguSantriDimensions.catalogueGridMinCellWidth),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = SanguSantriDimensions.dashboardContentMaxWidth),
                contentPadding = PaddingValues(SanguSantriSpacing.default),
                horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
            ) {
                items(items = items, key = { it.id }) { item ->
                    ContentCard(content = item, onClick = onSholawatSelected)
                }
            }
        }
    }
}

@Composable
private fun SholawatListEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .fillMaxSize()
        .padding(SanguSantriSpacing.large), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.sholawat_empty_state),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// Development-only preview fixture — mirrors the bracketed placeholder convention used elsewhere,
// never real sholawat text (content is a blocking production input, not yet supplied).
private val previewItems =
    listOf(
        Content(
            id = "sholawat-fixture",
            title = "[FIXTURE] Sholawat",
            description = "[FIXTURE] Konten pengembangan.",
            imageUrl = null,
            category = Content.SHOLAWAT_CATEGORY,
            version = 1,
            order = 1,
            isActive = true,
            sourceName = "[FIXTURE]",
            sourceUrl = "[FIXTURE]",
        ),
    )

@PreviewLightDark
@Composable
private fun SholawatListScreenPreview() {
    SanguSantriTheme {
        SholawatListScreen(
            uiState = SholawatListUiState.ContentReady(items = previewItems),
            onBack = {},
            onSholawatSelected = {},
        )
    }
}
