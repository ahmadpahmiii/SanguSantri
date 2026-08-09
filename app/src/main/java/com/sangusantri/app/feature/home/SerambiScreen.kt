package com.sangusantri.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.component.SectionHeader
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.Reminder
import com.sangusantri.app.feature.reminder.ReminderScheduleFormatter
import com.sangusantri.app.feature.update.AppUpdateGate

@Composable
fun SerambiRoute(
    onContentSelected: (String) -> Unit,
    actions: SerambiActions,
    viewModel: SerambiViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    SerambiScreen(
        uiState = uiState,
        onContentSelected = onContentSelected,
        actions = actions,
        snackbarHostState = snackbarHostState,
    )
    // Checked once per cold start (ADR 0017) — mounted alongside, not inside, SerambiScreen so the
    // latter stays a pure, Hilt-free, preview-safe composable.
    AppUpdateGate(snackbarHostState = snackbarHostState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SerambiScreen(
    uiState: SerambiUiState,
    onContentSelected: (String) -> Unit,
    actions: SerambiActions,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = actions.onSetelanClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.serambi_setelan_content_description),
                        )
                    }
                    IconButton(onClick = actions.onAboutClick) {
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
                    uiState = uiState,
                    onContentSelected = onContentSelected,
                    actions = actions,
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
    uiState: SerambiUiState.Loaded,
    onContentSelected: (String) -> Unit,
    actions: SerambiActions,
    modifier: Modifier = Modifier,
) {
    if (uiState.items.isEmpty()) {
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
        item(key = "quran") { QuranEntrySection(onClick = actions.onQuranClick) }
        item(key = "nearest_reminder") {
            NearestReminderSection(
                reminder = uiState.nearestReminder,
                contentTitle = uiState.nearestReminderContentTitle(),
                onClick = actions.onPengingatClick,
            )
        }
        if (uiState.hasNahwuQuizContent) {
            item(key = "belajar") { BelajarSection(onClick = actions.onBelajarClick) }
        }
        items(items = uiState.items, key = { it.id }) { item ->
            ContentCard(content = item, onClick = onContentSelected)
        }
    }
}

/**
 * `0.0.6`, standalone Al-Qur'an Kemenag — a real, accessible entry point (QUR-FR-001), always
 * shown like [NearestReminderSection] rather than hidden behind any data condition: the feature
 * always exists once shipped, and its own entry gate (not this card) is what decides whether local
 * Quran data still needs preparing.
 */
@Composable
private fun QuranEntrySection(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(SanguSantriSpacing.default),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.serambi_quran_card_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.serambi_quran_card_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

/** `0.0.5`, Nahwu Quiz — a static entry-point tile to the Landing screen, not a specific package's
 * own summary card (design spec: "'Kuis Nahwu', short description, chevron_right"). Hidden until
 * [SerambiUiState.Loaded.hasNahwuQuizContent] is true, unlike [NearestReminderSection]'s always-
 * shown rule — Nahwu Quiz has its own separate creation entry point (`Daftar Paket`), so there is
 * no equivalent "nowhere else to start" problem to guard against here. */
@Composable
private fun BelajarSection(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(title = stringResource(R.string.serambi_belajar_title))
        Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
            Row(
                modifier = Modifier.padding(SanguSantriSpacing.default),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.serambi_belajar_card_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.serambi_belajar_card_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}

/**
 * `0.0.4`, Pengingat Amaliyah. Always shown (unlike other Beranda sections' hide-if-empty rule) —
 * with zero reminders this is the app's *only* entry point into the Pengingat screen, since
 * Aktivitas's own "Pengingat" section only appears once a reminder already exists. Hiding this one
 * too whenever [reminder] is null would make the feature permanently undiscoverable for a user who
 * has never created a reminder.
 */
@Composable
private fun NearestReminderSection(
    reminder: Reminder?,
    contentTitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hijriMonthNames = stringArrayResource(R.array.reminder_hijri_month_names).toList()
    Column(modifier = modifier) {
        SectionHeader(title = stringResource(R.string.serambi_nearest_reminder_title))
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick,
        ) {
            Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
                if (reminder == null) {
                    Text(
                        text = stringResource(R.string.serambi_nearest_reminder_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = reminder.label.ifBlank { contentTitle ?: reminder.contentId },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = ReminderScheduleFormatter.formatScheduleSummary(reminder.schedule, hijriMonthNames),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
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

private val previewActions =
    SerambiActions(
        onSetelanClick = {},
        onAboutClick = {},
        onPengingatClick = {},
        onBelajarClick = {},
        onQuranClick = {},
    )

@PreviewLightDark
@Composable
private fun SerambiScreenContentPreview() {
    SanguSantriTheme {
        SerambiScreen(
            uiState = SerambiUiState.Loaded(previewItems),
            onContentSelected = {},
            actions = previewActions,
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
            actions = previewActions,
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
            actions = previewActions,
        )
    }
}
