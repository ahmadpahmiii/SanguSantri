package com.sangusantri.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.Content

@Composable
fun SerambiRoute(
    onContentSelected: (String) -> Unit,
    onSetelanClick: () -> Unit,
    onAboutClick: () -> Unit,
    viewModel: SerambiViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SerambiScreen(
        uiState = uiState,
        onContentSelected = onContentSelected,
        onSetelanClick = onSetelanClick,
        onAboutClick = onAboutClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SerambiScreen(
    uiState: SerambiUiState,
    onContentSelected: (String) -> Unit,
    onSetelanClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onSetelanClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.serambi_setelan_content_description),
                        )
                    }
                    IconButton(onClick = onAboutClick) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.serambi_about_content_description),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            is SerambiUiState.Loading -> SerambiLoading(modifier = Modifier.padding(innerPadding))
            is SerambiUiState.Loaded ->
                SerambiContent(
                    items = uiState.items,
                    onContentSelected = onContentSelected,
                    modifier = Modifier.padding(innerPadding),
                )
        }
    }
}

@Composable
private fun SerambiLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SerambiContent(
    items: List<Content>,
    onContentSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.serambi_empty_state),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(SanguSantriSpacing.default),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
    ) {
        items(items = items, key = { it.id }) { item ->
            ContentCard(content = item, onClick = onContentSelected)
        }
    }
}

// Development-only preview fixtures — bracketed placeholders, never real amaliyah text.
private val previewItems =
    listOf(
        Content(
            id = "tahlil",
            title = "Tahlil",
            description = "Rangkaian bacaan Tahlil. FIXTURE PENGEMBANGAN.",
            imageUrl = null,
            category = "Tahlil dan Doa",
            version = 1,
            order = 1,
            isActive = true,
            sourceName = "[FIXTURE]",
            sourceUrl = "[FIXTURE]",
        ),
        Content(
            id = "istighosah",
            title = "Istighosah",
            description = "Rangkaian bacaan Istighosah. FIXTURE PENGEMBANGAN.",
            imageUrl = null,
            category = "Tahlil dan Doa",
            version = 1,
            order = 2,
            isActive = true,
            sourceName = "[FIXTURE]",
            sourceUrl = "[FIXTURE]",
        ),
    )

@PreviewLightDark
@Composable
private fun SerambiScreenContentPreview() {
    SanguSantriTheme {
        SerambiScreen(
            uiState = SerambiUiState.Loaded(previewItems),
            onContentSelected = {},
            onSetelanClick = {},
            onAboutClick = {},
        )
    }
}

@Preview(name = "Empty catalogue")
@Composable
private fun SerambiScreenEmptyPreview() {
    SanguSantriTheme {
        SerambiScreen(
            uiState = SerambiUiState.Loaded(emptyList()),
            onContentSelected = {},
            onSetelanClick = {},
            onAboutClick = {},
        )
    }
}

@Preview(name = "Loading")
@Composable
private fun SerambiScreenLoadingPreview() {
    SanguSantriTheme {
        SerambiScreen(
            uiState = SerambiUiState.Loading,
            onContentSelected = {},
            onSetelanClick = {},
            onAboutClick = {},
        )
    }
}
