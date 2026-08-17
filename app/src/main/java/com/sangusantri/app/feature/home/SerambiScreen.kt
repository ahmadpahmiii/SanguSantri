@file:Suppress("TooManyFunctions")

package com.sangusantri.app.feature.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.component.SectionHeader
import com.sangusantri.app.core.designsystem.theme.LocalAppThemeMode
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.AppThemeMode
import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.ReaderMode
import com.sangusantri.app.feature.update.AppUpdateGate

@Composable
fun SerambiRoute(
    onContentSelected: (String) -> Unit,
    actions: SerambiActions,
    viewModel: SerambiViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // First launch only: ask for location so the prayer schedule can set itself up. Denying is a
    // normal outcome — the prayer section then invites picking a city by hand, and nothing else in
    // the app is affected. The prompt is marked as shown either way, so it never nags.
    val shouldAskForLocation by viewModel.shouldAskForLocation.collectAsStateWithLifecycle()
    val detectingCity by viewModel.detectingCity.collectAsStateWithLifecycle()
    val locationLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            viewModel.onLocationPermissionResult(granted)
        }
    LaunchedEffect(shouldAskForLocation) {
        if (shouldAskForLocation) locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    val screenActions =
        actions.copy(
            onDismissResume = viewModel::dismissResume,
            onThemeModeSelected = viewModel::setThemeMode,
        )
    SerambiScreen(
        uiState = uiState,
        onContentSelected = onContentSelected,
        actions = screenActions,
        snackbarHostState = snackbarHostState,
        detectingCity = detectingCity,
    )
    AppUpdateGate(snackbarHostState = snackbarHostState)
}

/**
 * Beranda, rebuilt to the revamp handoff (§1). Top to bottom: greeting row with the app-wide theme
 * toggle and search, the next-prayer block, four menu tiles, the continue row, and the curated
 * amaliyah scroller.
 *
 * The screen has no top app bar any more — the design replaced it with the greeting row, and the
 * two placeholder destinations it used to link to (Setelan, Tentang) went with it.
 *
 * Every section still decides its own visibility from genuine local data; nothing renders a section
 * with nothing in it. That rule is what keeps the prayer block off release builds while no
 * prayer-time source is wired.
 */
@Suppress("LongParameterList")
@Composable
fun SerambiScreen(
    uiState: SerambiUiState,
    onContentSelected: (String) -> Unit,
    actions: SerambiActions,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    detectingCity: Boolean = false,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (uiState) {
                SerambiUiState.Loading ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                is SerambiUiState.Loaded ->
                    SerambiDashboard(
                        uiState = uiState,
                        onContentSelected = onContentSelected,
                        actions = actions,
                        detectingCity = detectingCity,
                    )
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun SerambiDashboard(
    uiState: SerambiUiState.Loaded,
    onContentSelected: (String) -> Unit,
    actions: SerambiActions,
    detectingCity: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .widthIn(max = SanguSantriDimensions.dashboardContentMaxWidth),
    ) {
        BerandaGreetingRow(actions = actions)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = SanguSantriSpacing.large),
        ) {
            item(key = "prayer") {
                val schedule = uiState.prayerSchedule
                if (schedule == null) {
                    BerandaPrayerSetupRow(
                        onOpenSchedule = actions.onPrayerScheduleClick,
                        detecting = detectingCity,
                        modifier = Modifier.padding(horizontal = BerandaHorizontalPadding),
                    )
                } else {
                    BerandaPrayerBlock(
                        schedule = schedule,
                        now = uiState.now,
                        onOpenSchedule = actions.onPrayerScheduleClick,
                        onOpenKiblat = actions.onKiblatClick,
                        modifier = Modifier.padding(horizontal = BerandaHorizontalPadding),
                    )
                }
            }

            item(key = "menu") {
                BerandaMenuTiles(
                    actions = actions,
                    showSholawat = uiState.hasSholawatContent,
                    showNahwu = uiState.hasNahwuQuizContent,
                    modifier =
                        Modifier.padding(
                            start = BerandaHorizontalPadding,
                            end = BerandaHorizontalPadding,
                            top = SectionGap,
                        ),
                )
            }

            uiState.resumeItem?.let { resume ->
                item(key = "resume") {
                    BerandaContinueRow(
                        title = resume.resumeTitle(),
                        supporting = resume.resumeSupporting(),
                        fraction = resume.progress?.fraction,
                        onContinue = { actions.continueResume(resume) },
                        modifier =
                            Modifier.padding(
                                start = BerandaHorizontalPadding,
                                end = BerandaHorizontalPadding,
                                top = SectionGap,
                            ),
                    )
                }
            }

            if (uiState.featuredItems.isNotEmpty()) {
                item(key = "amaliyah_header") {
                    SectionHeader(
                        title = stringResource(R.string.serambi_featured_amaliyah_title),
                        actionLabel = stringResource(R.string.beranda_explore_action),
                        onActionClick = actions.onExploreClick,
                        modifier =
                            Modifier.padding(
                                start = BerandaHorizontalPadding,
                                end = BerandaHorizontalPadding,
                                top = SectionGap,
                                bottom = SanguSantriSpacing.medium,
                            ),
                    )
                }
                item(key = "amaliyah") {
                    BerandaAmaliyahScroller(
                        items = uiState.featuredItems,
                        onContentSelected = onContentSelected,
                        contentPadding = PaddingValues(horizontal = BerandaHorizontalPadding),
                    )
                }
            }
        }
    }
}

/** Handoff §1.1. The bell was dropped in review; the two controls are the app-wide theme toggle and
 * search. */
@Composable
private fun BerandaGreetingRow(actions: SerambiActions) {
    val isDark = LocalAppThemeMode.current == AppThemeMode.DARK
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = BerandaHorizontalPadding,
                    end = BerandaHorizontalPadding,
                    top = SanguSantriSpacing.extraSmall,
                    bottom = GreetingBottomPadding,
                ),
    ) {
        Column {
            Text(
                text = stringResource(R.string.beranda_greeting_salutation),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 21.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.3).sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall + 2.dp)) {
            CircularAction(
                icon = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                contentDescription =
                    stringResource(
                        if (isDark) {
                            R.string.theme_toggle_to_light_content_description
                        } else {
                            R.string.theme_toggle_to_dark_content_description
                        },
                    ),
                tint = MaterialTheme.colorScheme.primary,
                onClick = {
                    actions.onThemeModeSelected(if (isDark) AppThemeMode.LIGHT else AppThemeMode.DARK)
                },
            )
            CircularAction(
                icon = Icons.Outlined.Search,
                contentDescription = stringResource(R.string.beranda_search_content_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = actions.onExploreClick,
            )
        }
    }
}

@Composable
private fun CircularAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(CircularActionSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(21.dp),
        )
    }
}

@Composable
private fun SerambiResumeItem.resumeTitle(): String =
    when (this) {
        is SerambiResumeItem.Amaliyah -> title
        is SerambiResumeItem.Quran -> surahName
        is SerambiResumeItem.Tasbih -> sessionName ?: stringResource(R.string.beranda_resume_tasbih_title)
    }

@Composable
private fun SerambiResumeItem.resumeSupporting(): String =
    when (this) {
        is SerambiResumeItem.Amaliyah ->
            stringResource(R.string.beranda_resume_amaliyah_supporting, current, total)

        is SerambiResumeItem.Quran ->
            stringResource(R.string.beranda_resume_quran_supporting, ayatNumber, totalAyat)

        is SerambiResumeItem.Tasbih ->
            targetCount
                ?.let { stringResource(R.string.beranda_resume_tasbih_supporting, currentCount, it) }
                ?: stringResource(R.string.beranda_resume_tasbih_supporting_unlimited, currentCount)
    }

private fun SerambiActions.continueResume(item: SerambiResumeItem) {
    when (item) {
        is SerambiResumeItem.Amaliyah -> onContinueAmaliyah(item.contentId, item.mode)
        is SerambiResumeItem.Quran -> onContinueQuran(item.surahNumber, item.ayatNumber)
        is SerambiResumeItem.Tasbih -> onContinueTasbih()
    }
}

private val BerandaHorizontalPadding = 20.dp
private val SectionGap = 24.dp
private val GreetingBottomPadding = 14.dp
private val CircularActionSize = 42.dp

// Development-only preview fixtures — no religious text is invented.
private val previewItems =
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
        Content(
            id = "istighosah",
            title = "Istighosah",
            description = "[FIXTURE] Konten pengembangan.",
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
        onExploreClick = {},
        onPengingatClick = {},
        onBelajarClick = {},
        onQuranClick = {},
        onContinueAmaliyah = { _, _ -> },
        onContinueQuran = { _, _ -> },
        onContinueTasbih = {},
    )

@PreviewLightDark
@Composable
private fun SerambiScreenContentPreview() {
    SanguSantriTheme {
        SerambiScreen(
            uiState =
                SerambiUiState.Loaded(
                    items = previewItems,
                    hasNahwuQuizContent = true,
                    hasActiveNahwuQuiz = true,
                    resumeItem =
                        SerambiResumeItem.Amaliyah(
                            contentId = "tahlil",
                            title = "Tahlil",
                            mode = ReaderMode.GUIDED,
                            current = 12,
                            total = 37,
                            lastActivityAtEpochMillis = 1L,
                        ),
                ),
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
