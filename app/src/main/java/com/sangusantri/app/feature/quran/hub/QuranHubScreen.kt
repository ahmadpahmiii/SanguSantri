package com.sangusantri.app.feature.quran.hub

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranBackground
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOnPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranSurface
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.feature.quran.QuranThemeBoundary

/** The Quran hub (QUR-FR-005): Surah/Juz/Bookmark/Terakhir Dibaca tabs plus a continue-reading
 * action, all backed by Room via [QuranHubViewModel]. Per-tab list/row composables live in
 * `QuranHubTabContent.kt` to keep this file focused on the app bar/tab/search chrome. */
@Suppress("LongParameterList")
@Composable
fun QuranHubRoute(
    onBack: () -> Unit,
    onSurahSelected: (surahNumber: Int) -> Unit,
    onAyatSelected: (surahNumber: Int, ayatNumber: Int) -> Unit,
    onOpenSource: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuranHubViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    QuranThemeBoundary {
        QuranHubScreen(
            uiState = uiState,
            onBack = onBack,
            onOpenSource = onOpenSource,
            actions =
                QuranHubActions(
                    onTabSelected = viewModel::selectTab,
                    onSearchQueryChanged = viewModel::updateSearchQuery,
                    onSurahSelected = onSurahSelected,
                    onAyatSelected = onAyatSelected,
                ),
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranHubScreen(
    uiState: QuranHubUiState,
    onBack: () -> Unit,
    onOpenSource: () -> Unit,
    actions: QuranHubActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = QuranBackground,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.quran_hub_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back_content_description),
                        )
                    }
                },
                actions = { QuranHubOverflowMenu(onOpenSource = onOpenSource) },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = QuranSurface,
                        titleContentColor = QuranArabicText,
                        navigationIconContentColor = QuranArabicText,
                    ),
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            uiState.continueReading?.let { continueReading ->
                QuranContinueReadingPanel(
                    continueReading = continueReading,
                    onClick = { actions.onAyatSelected(continueReading.surahNumber, continueReading.ayatNumber) },
                )
            }
            QuranHubTabRow(selectedTab = uiState.selectedTab, onTabSelected = actions.onTabSelected)
            if (uiState.selectedTab == QuranHubTab.SURAH) {
                QuranSurahSearchField(query = uiState.searchQuery, onQueryChanged = actions.onSearchQueryChanged)
            }
            QuranHubTabContent(uiState = uiState, actions = actions)
        }
    }
}

@Composable
private fun QuranHubOverflowMenu(onOpenSource: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.quran_hub_overflow_content_description),
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.quran_source_title)) },
            onClick = {
                expanded = false
                onOpenSource()
            },
        )
    }
}

@Composable
private fun QuranContinueReadingPanel(
    continueReading: QuranContinueReading,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = QuranPrimaryContainer,
        contentColor = QuranOnPrimaryContainer,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = SanguSantriSpacing.default, vertical = SanguSantriSpacing.small),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
            Text(
                text = stringResource(R.string.quran_hub_continue_reading_title),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text =
                    stringResource(
                        R.string.quran_hub_continue_reading_position,
                        continueReading.surahName,
                        continueReading.ayatNumber,
                    ),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun QuranHubTabRow(
    selectedTab: QuranHubTab,
    onTabSelected: (QuranHubTab) -> Unit,
) {
    val tabs = QuranHubTab.entries
    val selectedIndex = tabs.indexOf(selectedTab)
    SecondaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = QuranSurface,
        contentColor = QuranArabicText,
        edgePadding = SanguSantriSpacing.default,
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedIndex, matchContentSize = false),
                color = QuranPrimary,
            )
        },
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                text = { Text(text = stringResource(tab.labelRes())) },
                selectedContentColor = QuranPrimary,
                unselectedContentColor = QuranMutedText,
            )
        }
    }
}

private fun QuranHubTab.labelRes(): Int =
    when (this) {
        QuranHubTab.SURAH -> R.string.quran_hub_tab_surah
        QuranHubTab.JUZ -> R.string.quran_hub_tab_juz
        QuranHubTab.BOOKMARK -> R.string.quran_hub_tab_bookmark
        QuranHubTab.TERAKHIR_DIBACA -> R.string.quran_hub_tab_recent
    }

@Composable
private fun QuranSurahSearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text(text = stringResource(R.string.quran_hub_search_placeholder)) },
        leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
        singleLine = true,
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedTextColor = QuranArabicText,
                unfocusedTextColor = QuranArabicText,
                focusedBorderColor = QuranPrimary,
                unfocusedBorderColor = QuranOutline,
                cursorColor = QuranPrimary,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = SanguSantriSpacing.default, vertical = SanguSantriSpacing.small),
    )
}
