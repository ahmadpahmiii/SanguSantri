package com.sangusantri.app.feature.quran.hub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranBackground
import com.sangusantri.app.core.designsystem.theme.QuranContinueCardGradientStart
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOnPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranSurface
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.feature.quran.QuranThemeBoundary

/** Quran hub aligned to the approved 360×800 local reference: calm two-line app bar, restrained
 * last-read card, three fixed tabs, and inset flat content lists backed by Room. */
@Suppress("LongParameterList")
@Composable
fun QuranHubRoute(
    onBack: () -> Unit,
    onSurahSelected: (surahNumber: Int) -> Unit,
    onAyatSelected: (surahNumber: Int, ayatNumber: Int) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSource: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuranHubViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    QuranThemeBoundary {
        QuranHubScreen(
            uiState = uiState,
            actions =
                QuranHubActions(
                    onBack = onBack,
                    onOpenSettings = onOpenSettings,
                    onOpenSource = onOpenSource,
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
    actions: QuranHubActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = QuranBackground,
        topBar = { QuranHubTopBar(actions) },
    ) { innerPadding ->
        QuranHubBody(uiState = uiState, actions = actions, modifier = Modifier.padding(innerPadding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuranHubTopBar(actions: QuranHubActions) {
    // Approved local reference (design-export/quran/01…04b/06c/06d) renders the hub's app bar as
    // `.top.plain` — title/subtitle and actions only, with no leading back icon, unlike the
    // reader's `.top`. The hub still pops via system/predictive back; [QuranHubActions.onBack]
    // is retained for that path even though this bar no longer renders it.
    TopAppBar(
        title = {
            Column {
                Text(text = stringResource(R.string.quran_hub_title), style = MaterialTheme.typography.titleLarge)
                Text(
                    text = stringResource(R.string.quran_hub_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = QuranMutedText,
                )
            }
        },
        actions = {
            IconButton(onClick = actions.onOpenSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.quran_settings_action_content_description),
                )
            }
            IconButton(onClick = actions.onOpenSource) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.quran_source_action_content_description),
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = QuranBackground,
                titleContentColor = QuranArabicText,
                navigationIconContentColor = QuranArabicText,
                actionIconContentColor = QuranArabicText,
            ),
    )
}

@Composable
private fun QuranHubBody(
    uiState: QuranHubUiState,
    actions: QuranHubActions,
    modifier: Modifier = Modifier,
) {
    Box(contentAlignment = Alignment.TopCenter, modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = SanguSantriDimensions.quranHubContentMaxWidth)
                    .fillMaxSize()
                    .padding(horizontal = SanguSantriSpacing.default),
        ) {
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
private fun QuranContinueReadingPanel(
    continueReading: QuranContinueReading,
    onClick: () -> Unit,
) {
    // design-export/quran/01-quran-hub-surah.html `.continue{background:linear-gradient(135deg,
    // #07351f,#101713)}` — a tinted highlight fading into the hub's own surface tone.
    val cardGradient = Brush.linearGradient(listOf(QuranContinueCardGradientStart, QuranSurface))
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        contentColor = QuranOnPrimaryContainer,
        border = BorderStroke(1.dp, QuranOutline),
        shape = MaterialTheme.shapes.large,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = SanguSantriSpacing.small)
                .background(brush = cardGradient, shape = MaterialTheme.shapes.large),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(SanguSantriSpacing.default),
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                tint = QuranPrimary,
                modifier = Modifier.padding(end = SanguSantriSpacing.default),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.quran_hub_continue_reading_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = QuranPrimary,
                )
                Text(
                    text = continueReading.surahName,
                    style = MaterialTheme.typography.titleMedium,
                    color = QuranArabicText,
                    modifier = Modifier.padding(top = SanguSantriSpacing.extraSmall),
                )
                Text(
                    text =
                        stringResource(
                            R.string.quran_hub_continue_reading_position,
                            continueReading.page,
                            continueReading.ayatNumber,
                        ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = QuranMutedText,
                )
            }
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
    SecondaryTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = QuranBackground,
        contentColor = QuranArabicText,
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedIndex, matchContentSize = false),
                color = QuranPrimary,
            )
        },
        divider = {},
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
        leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null) },
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedTextColor = QuranArabicText,
                unfocusedTextColor = QuranArabicText,
                focusedContainerColor = QuranSurface,
                unfocusedContainerColor = QuranSurface,
                focusedBorderColor = QuranPrimary,
                unfocusedBorderColor = QuranOutline,
                focusedLeadingIconColor = QuranPrimary,
                unfocusedLeadingIconColor = QuranMutedText,
                focusedPlaceholderColor = QuranMutedText,
                unfocusedPlaceholderColor = QuranMutedText,
                cursorColor = QuranPrimary,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = SanguSantriDimensions.minimumTouchTarget)
                .padding(vertical = SanguSantriSpacing.small),
    )
}
