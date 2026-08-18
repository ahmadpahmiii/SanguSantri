@file:Suppress("TooManyFunctions")

package com.sangusantri.app.feature.quran.reader

import android.Manifest
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.component.ThemeToggleButton
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranBackground
import com.sangusantri.app.core.designsystem.theme.QuranError
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOnPrimary
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranSurface
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.AppThemeMode
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.domain.model.QuranDisplayMode
import com.sangusantri.app.domain.model.QuranMurottalState
import com.sangusantri.app.domain.model.QuranMurottalStatus
import com.sangusantri.app.feature.quran.QuranBrightnessEffect
import com.sangusantri.app.feature.quran.murottal.QuranMiniPlayerActions
import com.sangusantri.app.feature.quran.murottal.QuranMiniPlayerBar
import com.sangusantri.app.feature.quran.murottal.QuranMurottalPanel
import com.sangusantri.app.feature.quran.murottal.QuranMurottalPanelActions
import com.sangusantri.app.feature.quran.murottal.QuranMurottalPanelUiState
import com.sangusantri.app.feature.quran.toFontFamily
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * The Quran reader (QUR-FR-008/009/010/011/012/014/017), opened for one whole surah at a time. Both
 * display modes render inside one [LazyColumn] this screen owns (rather than solely delegating to
 * [QuranFlowingPageText]/[QuranTranslationAyatList] as complete screens) so scroll position can be
 * observed for last-read/reading-session tracking — see [QuranReaderViewModel.onVisiblePositionChanged].
 */
@Suppress("LongParameterList")
@Composable
fun QuranReaderRoute(
    surahNumber: Int,
    targetAyat: Int?,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onFollowAudioToSurah: (surahNumber: Int, ayatNumber: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: QuranReaderViewModel =
        hiltViewModel<QuranReaderViewModel, QuranReaderViewModel.Factory>(
            creationCallback = { factory -> factory.create(surahNumber) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tafsirUiState by viewModel.tafsirUiState.collectAsStateWithLifecycle()
    val murottalState by viewModel.murottalState.collectAsStateWithLifecycle()
    val murottalPanelUiState by viewModel.murottalPanelUiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose { viewModel.recordSessionIfAdvanced() }
    }

    QuranReaderSideEffects(
        surahNumber = surahNumber,
        brightnessOverride = (uiState as? QuranReaderUiState.Content)?.brightnessOverride,
        keepScreenOn = murottalPanelUiState?.keepScreenOn == true && murottalState.isActive,
        murottalState = murottalState,
        onFollowAudioToSurah = onFollowAudioToSurah,
    )

    QuranReaderScreen(
        uiState = uiState,
        tafsirUiState = tafsirUiState,
        targetAyat = targetAyat,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        actions = viewModel.bodyActions(),
        murottalState = murottalState,
        murottalPanelUiState = murottalPanelUiState,
        murottalActions = viewModel.miniPlayerActions(),
        panelActions = viewModel.panelActions(),
        modifier = modifier,
    )
}

private fun QuranReaderViewModel.bodyActions() =
    QuranReaderBodyActions(
        onAyatLongPress = ::onAyatLongPress,
        onDismissActionSheet = ::onDismissActionSheet,
        onToggleBookmark = ::onToggleBookmark,
        onMarkLastRead = ::onMarkLastRead,
        onOpenTafsir = ::onOpenTafsir,
        onDismissTafsirSheet = ::onDismissTafsirSheet,
        onRetryTafsir = ::onRetryTafsir,
        onVisiblePositionChanged = ::onVisiblePositionChanged,
        onToggleTheme = ::setThemeMode,
        onPlayAyat = ::onPlayAyat,
        onPlayFromHere = ::onPlayFromHere,
        onPlaySingleAyat = ::onPlaySingleAyat,
        onRepeatAyat = ::onRepeatAyat,
    )

private fun QuranReaderViewModel.miniPlayerActions() =
    QuranMiniPlayerActions(
        onTogglePlayPause = ::onTogglePlayPause,
        onSkipPrevious = ::onSkipToPreviousAyat,
        onSkipNext = ::onSkipToNextAyat,
        onClose = ::onStopPlayback,
        onOpenPanel = ::onOpenMurottalPanel,
        onRetry = ::onRetryPlayback,
    )

private fun QuranReaderViewModel.panelActions() =
    QuranMurottalPanelActions(
        onSelectSpeed = ::onSelectMurottalSpeed,
        onToggleContinueAcrossSurah = ::onToggleContinueAcrossSurah,
        onToggleKeepScreenOn = ::onToggleKeepScreenOn,
        onDownloadAudio = ::onDownloadSurahAudio,
        onCancelDownload = ::onCancelSurahAudioDownload,
        onDismiss = ::onDismissMurottalPanel,
    )

/** The reader's window-level effects, grouped so the route composable stays readable as they
 * accumulate: screen brightness, the murottal wake lock, the notification prompt, and cross-surah
 * follow. */
@Composable
private fun QuranReaderSideEffects(
    surahNumber: Int,
    brightnessOverride: Float?,
    keepScreenOn: Boolean,
    murottalState: QuranMurottalState,
    onFollowAudioToSurah: (surahNumber: Int, ayatNumber: Int) -> Unit,
) {
    QuranBrightnessEffect(brightnessOverride)
    QuranKeepScreenOnEffect(enabled = keepScreenOn)
    QuranMurottalNotificationPermissionEffect(playbackActive = murottalState.isActive)
    QuranFollowAudioAcrossSurahEffect(
        readerSurahNumber = surahNumber,
        murottalState = murottalState,
        onFollowAudioToSurah = onFollowAudioToSurah,
    )
}

/**
 * Carries the open reader into the next surah when auto-continue crosses a surah boundary, so the
 * page keeps following the recitation instead of being stranded on a surah that stopped playing.
 *
 * The reader loads one surah at a time, so following means re-opening it on the new one — which makes
 * it important that this never fires against the reader's will. It only does so when *this* surah was
 * the one being recited and playback then moved elsewhere: opening Al-Kahfi by hand while Al-Baqarah
 * plays leaves [wasFollowing] false, so nothing yanks the reader to Al-Baqarah.
 *
 * No setting gates this. Crossing surahs only happens while "Lanjut otomatis antarsurah" is on, which
 * is already the reader asking for continuous recitation.
 */
@Composable
private fun QuranFollowAudioAcrossSurahEffect(
    readerSurahNumber: Int,
    murottalState: QuranMurottalState,
    onFollowAudioToSurah: (surahNumber: Int, ayatNumber: Int) -> Unit,
) {
    var wasFollowing by remember(readerSurahNumber) { mutableStateOf(false) }
    val playingSurah = murottalState.surahNumber
    val playingAyat = murottalState.ayahNumber
    LaunchedEffect(playingSurah, playingAyat) {
        when {
            playingSurah == readerSurahNumber -> wasFollowing = true
            playingSurah != null && playingAyat != null && wasFollowing -> {
                // Cleared first so a re-composition before navigation settles cannot fire twice.
                wasFollowing = false
                onFollowAudioToSurah(playingSurah, playingAyat)
            }
        }
    }
}

/**
 * Asks for the notification permission once, the first time a recitation starts.
 *
 * The murottal service posts a foreground media notification so playback survives leaving the reader;
 * on API 33+ that notification is suppressed without this permission, leaving the user with audio and
 * no way to pause it from outside the app. Asked at first playback rather than on open, so the prompt
 * arrives with obvious context. Denial is not fatal — playback still works in-app, so nothing blocks
 * on the answer (same graceful-degradation shape as `feature/reminder`'s flow).
 */
@Composable
private fun QuranMurottalNotificationPermissionEffect(playbackActive: Boolean) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    var alreadyAsked by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(playbackActive) {
        if (!playbackActive || alreadyAsked) return@LaunchedEffect
        alreadyAsked = true
        val granted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

/** "Layar tetap menyala" from the murottal panel — held only while a recitation is actually playing,
 * so the switch can never leave the screen awake indefinitely after playback ends. */
@Composable
private fun QuranKeepScreenOnEffect(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, enabled) {
        view.keepScreenOn = enabled
        onDispose { view.keepScreenOn = false }
    }
}

/** [QuranReaderBody]'s action callbacks, bundled to keep the composable's own parameter list short
 * (mirrors `feature/home/SerambiActions`/`feature/quran/hub/QuranHubActions`). */
data class QuranReaderBodyActions(
    val onAyatLongPress: (QuranReaderAyatUiModel) -> Unit,
    val onDismissActionSheet: () -> Unit,
    val onToggleBookmark: (QuranReaderAyatUiModel) -> Unit,
    val onMarkLastRead: (QuranReaderAyatUiModel) -> Unit,
    val onOpenTafsir: (QuranReaderAyatUiModel) -> Unit,
    val onDismissTafsirSheet: () -> Unit,
    val onRetryTafsir: (remoteAyatId: Long) -> Unit,
    val onVisiblePositionChanged: (Int) -> Unit,
    val onToggleTheme: (AppThemeMode) -> Unit,
    /** Tapping an ayah number plays it and continues from there (turn-4 addendum item 1). */
    val onPlayAyat: (QuranReaderAyatUiModel) -> Unit,
    val onPlayFromHere: (QuranReaderAyatUiModel) -> Unit,
    val onPlaySingleAyat: (QuranReaderAyatUiModel) -> Unit,
    val onRepeatAyat: (QuranReaderAyatUiModel) -> Unit,
)

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    uiState: QuranReaderUiState,
    tafsirUiState: QuranTafsirUiState,
    targetAyat: Int?,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    actions: QuranReaderBodyActions,
    murottalState: QuranMurottalState,
    murottalPanelUiState: QuranMurottalPanelUiState?,
    murottalActions: QuranMiniPlayerActions,
    panelActions: QuranMurottalPanelActions,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val readerContent = uiState as? QuranReaderUiState.Content
    val currentAyat =
        readerContent?.ayats?.firstOrNull { it.ayatNumber == targetAyat }
            ?: readerContent?.ayats?.firstOrNull()
    // design-export/quran/18-reader-invalid-target.html keeps the back/settings chrome but replaces
    // the surah title/position with a generic "Al-Qur'an" / "Posisi tidak tersedia" pair — never a
    // stale surah name for a target that doesn't resolve.
    val targetUnresolved =
        uiState == QuranReaderUiState.Unavailable ||
            (readerContent != null && targetAyat != null && readerContent.ayats.none { it.ayatNumber == targetAyat })

    // Mushaf immersion (handoff §5): a single tap on the page clears the chrome for a clean page,
    // another restores it. The design drops the title bar entirely in this mode; it is kept here and
    // folded into the same toggle instead, because the reader's only routes to settings, the theme
    // toggle, and back live in that bar — dropping it with no replacement control bar would strand
    // the reader. Chrome always returns when leaving Arab-only mode.
    var chromeVisible by remember { mutableStateOf(true) }
    val isMushafMode = readerContent?.displayMode == QuranDisplayMode.ARAB_ONLY
    LaunchedEffect(isMushafMode) { if (!isMushafMode) chromeVisible = true }

    Scaffold(
        modifier = modifier,
        containerColor = QuranBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!chromeVisible) return@Scaffold
            QuranReaderTopBar(
                title = quranReaderTitle(targetUnresolved, readerContent),
                position = quranReaderPosition(targetUnresolved, currentAyat),
                onBack = onBack,
                onOpenSettings = onOpenSettings,
                onToggleTheme = actions.onToggleTheme,
            )
        },
        bottomBar = { QuranMiniPlayerBar(state = murottalState, actions = murottalActions) },
    ) { innerPadding ->
        QuranReaderBody(
            uiState = uiState,
            tafsirUiState = tafsirUiState,
            targetAyat = targetAyat,
            snackbarHostState = snackbarHostState,
            actions = actions,
            onBack = onBack,
            chromeVisible = chromeVisible,
            onToggleChrome = { chromeVisible = !chromeVisible },
            murottalState = murottalState,
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
        )
    }
    if (murottalPanelUiState != null) {
        QuranMurottalPanel(uiState = murottalPanelUiState, actions = panelActions)
    }
}

/** design-export/quran/18-reader-invalid-target.html shows a generic "Al-Qur'an" rather than a stale
 * surah name when the navigation target does not resolve. */
@Composable
private fun quranReaderTitle(
    targetUnresolved: Boolean,
    readerContent: QuranReaderUiState.Content?,
): String =
    if (targetUnresolved) {
        stringResource(R.string.quran_hub_title)
    } else {
        readerContent?.surahName.orEmpty()
    }

@Composable
private fun quranReaderPosition(
    targetUnresolved: Boolean,
    currentAyat: QuranReaderAyatUiModel?,
): String =
    if (targetUnresolved) {
        stringResource(R.string.quran_reader_unavailable_subtitle)
    } else {
        currentAyat
            ?.let { stringResource(R.string.quran_reader_position, it.page, it.juz) }
            .orEmpty()
    }

@Suppress("LongParameterList")
@Composable
private fun QuranReaderBody(
    uiState: QuranReaderUiState,
    tafsirUiState: QuranTafsirUiState,
    targetAyat: Int?,
    snackbarHostState: SnackbarHostState,
    actions: QuranReaderBodyActions,
    onBack: () -> Unit,
    chromeVisible: Boolean,
    onToggleChrome: () -> Unit,
    murottalState: QuranMurottalState,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val positionInfoMessage = stringResource(R.string.quran_action_position_info_format)

    Box(modifier = modifier) {
        when (uiState) {
            QuranReaderUiState.Loading -> QuranReaderLoadingState()

            QuranReaderUiState.Unavailable -> QuranReaderUnavailableState(onBack = onBack)

            is QuranReaderUiState.Content -> {
                if (targetAyat != null && uiState.ayats.none { it.ayatNumber == targetAyat }) {
                    QuranReaderUnavailableState(onBack = onBack)
                } else {
                    QuranReaderContent(
                        state = uiState,
                        targetAyat = targetAyat,
                        onAyatLongPress = actions.onAyatLongPress,
                        onVisiblePositionChanged = actions.onVisiblePositionChanged,
                        chromeVisible = chromeVisible,
                        onToggleChrome = onToggleChrome,
                        onPlayAyat = actions.onPlayAyat,
                        murottalState = murottalState,
                    )
                }
            }
        }
        val selected = (uiState as? QuranReaderUiState.Content)?.selectedAyat
        val tafsirOpen = (uiState as? QuranReaderUiState.Content)?.tafsirSheetOpen == true
        if (selected != null && tafsirOpen) {
            QuranTafsirSheet(
                surahName = selected.surahName,
                ayatNumber = selected.ayatNumber,
                uiState = tafsirUiState,
                onRetry = { actions.onRetryTafsir(selected.remoteId) },
                onDismiss = actions.onDismissTafsirSheet,
            )
        } else if (selected != null) {
            QuranAyatActionSheet(
                ayat = selected,
                isBookmarked = (uiState as QuranReaderUiState.Content).isSelectedBookmarked,
                actions =
                    QuranAyatActionSheetActions(
                        onPlayFromHere = { actions.onPlayFromHere(selected) },
                        onPlaySingle = { actions.onPlaySingleAyat(selected) },
                        onRepeatAyat = { actions.onRepeatAyat(selected) },
                        onToggleBookmark = { actions.onToggleBookmark(selected) },
                        onOpenTafsir = { actions.onOpenTafsir(selected) },
                        onMarkLastRead = { actions.onMarkLastRead(selected) },
                        onShowPosition = {
                            val message = positionInfoMessage.format(selected.juz, selected.page)
                            coroutineScope.launch { snackbarHostState.showSnackbar(message) }
                        },
                    ),
                onDismiss = actions.onDismissActionSheet,
            )
        }
    }
}

@Composable
private fun QuranReaderLoadingState() {
    // design-export/quran/17-reader-loading.html `.loading-row` — three equal 120dp placeholders,
    // no distinct header-shaped block: the target surah/page is already known while Room loads,
    // so the design shows only content-row skeletons, not a header skeleton.
    Column(
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
        modifier =
            Modifier
                .widthIn(max = SanguSantriDimensions.readerContentMaxWidth)
                .fillMaxWidth()
                .padding(SanguSantriSpacing.default),
    ) {
        repeat(READER_LOADING_PLACEHOLDER_COUNT) {
            Surface(
                color = QuranSurface,
                shape = MaterialTheme.shapes.large,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp),
            ) {}
        }
    }
}

@Composable
private fun QuranReaderUnavailableState(onBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
        modifier =
            Modifier
                .fillMaxSize()
                .padding(SanguSantriSpacing.large),
    ) {
        Surface(
            color = QuranSurface,
            border = BorderStroke(1.dp, QuranOutline),
            shape = RoundedCornerShape(SanguSantriDimensions.quranStateMarkCornerRadius),
            modifier =
                Modifier
                    .padding(top = SanguSantriSpacing.extraLarge)
                    .size(SanguSantriDimensions.quranStateMarkSize),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = QuranError,
                )
            }
        }
        Text(
            text = stringResource(R.string.quran_reader_unavailable_title),
            style = MaterialTheme.typography.titleLarge,
            color = QuranArabicText,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.quran_reader_unavailable_description),
            style = MaterialTheme.typography.bodyMedium,
            color = QuranMutedText,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp),
        )
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = QuranPrimary, contentColor = QuranOnPrimary),
            modifier =
                Modifier
                    .heightIn(min = SanguSantriDimensions.minimumTouchTarget)
                    .widthIn(min = SanguSantriDimensions.quranStateActionButtonMinWidth),
        ) {
            Text(text = stringResource(R.string.quran_reader_unavailable_action))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuranReaderTopBar(
    title: String,
    position: String,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleTheme: (AppThemeMode) -> Unit,
) {
    val settingsContentDescription = stringResource(R.string.quran_settings_action_content_description)
    TopAppBar(
        title = {
            Column {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                if (position.isNotBlank()) {
                    Text(
                        text = position,
                        style = MaterialTheme.typography.bodySmall,
                        color = QuranMutedText,
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back_content_description),
                )
            }
        },
        actions = {
            ThemeToggleButton(onSelect = onToggleTheme)
            IconButton(onClick = onOpenSettings) {
                Text(
                    text = "aA",
                    style = MaterialTheme.typography.titleMedium,
                    color = QuranArabicText,
                    modifier =
                        Modifier.semantics {
                            contentDescription = settingsContentDescription
                        },
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = QuranSurface,
                titleContentColor = QuranArabicText,
                navigationIconContentColor = QuranArabicText,
                actionIconContentColor = QuranArabicText,
            ),
    )
}

private const val READER_LOADING_PLACEHOLDER_COUNT = 3

@Composable
@Suppress("LongParameterList", "LongMethod")
private fun QuranReaderContent(
    state: QuranReaderUiState.Content,
    targetAyat: Int?,
    onAyatLongPress: (QuranReaderAyatUiModel) -> Unit,
    onVisiblePositionChanged: (Int) -> Unit,
    chromeVisible: Boolean,
    onToggleChrome: () -> Unit,
    onPlayAyat: (QuranReaderAyatUiModel) -> Unit,
    murottalState: QuranMurottalState,
) {
    val arabOnlyListState = rememberLazyListState()
    val translationListState = rememberLazyListState()
    val activeListState =
        when (state.displayMode) {
            QuranDisplayMode.ARAB_ONLY -> arabOnlyListState
            QuranDisplayMode.ARAB_TRANSLATION -> translationListState
        }

    val positionSynchronized =
        QuranReaderSynchronizePosition(
            state = state,
            targetAyat = targetAyat,
            arabOnlyListState = arabOnlyListState,
            translationListState = translationListState,
        )
    if (positionSynchronized) {
        QuranReaderTrackVisiblePosition(activeListState, state, onVisiblePositionChanged)
    }

    val playback =
        if (murottalState.surahNumber == state.surahNumber) {
            QuranAyatPlaybackState(
                playingAyatNumber =
                    murottalState.ayahNumber?.takeIf {
                        murottalState.status == QuranMurottalStatus.PLAYING ||
                            murottalState.status == QuranMurottalStatus.PAUSED
                    },
                nextAyatNumber = murottalState.nextAyahNumber,
                preparingAyatNumber =
                    murottalState.ayahNumber?.takeIf { murottalState.status == QuranMurottalStatus.PREPARING },
                positionFraction = murottalState.positionFraction,
            )
        } else {
            QuranAyatPlaybackState()
        }
    // Reported by whichever mushaf page holds the recited ayah, once it has been measured. Carried
    // with its ayah number and resolved below, because a measurement belonging to the *previous* ayah
    // must never be applied to the next page — doing so scrolled far past the new page's first ayah.
    var measuredAyatOffset by remember { mutableStateOf<QuranMeasuredAyatOffset?>(null) }
    QuranReaderFollowAudio(
        playingAyatNumber = playback.playingAyatNumber,
        state = state,
        listState = activeListState,
        ayatOffsetInPage = measuredAyatOffset?.takeIf { it.ayatNumber == playback.playingAyatNumber }?.offsetPx,
    )

    val header: @Composable (mushaf: Boolean) -> Unit = { mushaf ->
        QuranSurahStartHeader(
            surahNumber = state.surahNumber,
            category = state.category,
            surahDisplayName = state.surahName,
            surahArabicName = state.surahArabicName,
            ayatCount = state.ayatCount,
            basmalahArabic = state.basmalahArabic,
            arabicFont = state.arabicFont,
            // Handoff §5 sets the mushaf page's own surah name/basmalah two points smaller than
            // the translation reader's, so the flowing page below stays the dominant element.
            surahNameSizeSp = if (mushaf) MUSHAF_SURAH_NAME_SIZE_SP else TRANSLATION_SURAH_NAME_SIZE_SP,
            basmalahSizeSp = if (mushaf) MUSHAF_BASMALAH_SIZE_SP else TRANSLATION_BASMALAH_SIZE_SP,
        )
    }

    Crossfade(
        targetState = state.displayMode,
        animationSpec = tween(durationMillis = if (ValueAnimator.areAnimatorsEnabled()) MODE_CROSSFADE_MILLIS else 0),
        label = "Quran display mode",
    ) { displayMode ->
        when (displayMode) {
            QuranDisplayMode.ARAB_TRANSLATION ->
                QuranTranslationAyatList(
                    ayats = state.ayats,
                    selectedAyatId = state.selectedAyat?.remoteId,
                    onAyatLongPress = onAyatLongPress,
                    lazyListState = translationListState,
                    arabicSizeSp = state.arabicSizeSp,
                    arabicLineHeightSp = state.arabicLineHeightSp,
                    translationSizeSp = state.translationSizeSp,
                    arabicFont = state.arabicFont,
                    onAyatSelected = onAyatLongPress,
                    playback = playback,
                    onPlayAyat = onPlayAyat,
                    headerContent = { header(false) },
                )

            QuranDisplayMode.ARAB_ONLY ->
                QuranArabOnlyPages(
                    pages = state.pages,
                    selectedAyatId = state.selectedAyat?.remoteId,
                    onAyatLongPress = onAyatLongPress,
                    arabicSizeSp = state.arabicSizeSp,
                    arabicLineHeightSp = state.arabicLineHeightSp,
                    arabicFont = state.arabicFont,
                    listState = arabOnlyListState,
                    chromeVisible = chromeVisible,
                    onToggleChrome = onToggleChrome,
                    playingAyatNumber = playback.playingAyatNumber,
                    onPlayingAyatOffset = { ayatNumber, offsetPx ->
                        measuredAyatOffset = QuranMeasuredAyatOffset(ayatNumber, offsetPx)
                    },
                    header = { header(true) },
                )
        }
    }
}

/**
 * Keeps the ayah being recited in view (`4f`), positioned against the actual viewport rather than by
 * whole list items.
 *
 * Item-level scrolling was not enough in either mode. In mushaf mode one list item is a whole *page*
 * of flowing text: with a large Arabic size that page is several screens tall, so every ayah after the
 * first scrolled nowhere and the recitation ran off the bottom — the taller the font, the worse it
 * got. In translation mode `animateScrollToItem(index)` pinned each ayah flush against the top edge,
 * with no context above it.
 *
 * So the target is computed in pixels: the item's own offset, plus [ayatOffsetInPage] for where the
 * ayah actually sits inside a measured page of text, minus a lead of [FOLLOW_LEAD_FRACTION] of the
 * viewport so the ayah lands a little below the top with its previous line still visible. Both inputs
 * come from real measurement, so the result adapts to font size, line spacing and screen height
 * without knowing any of them.
 *
 * It also holds still when it can: if the ayah is already inside a comfortable band of the viewport,
 * nothing scrolls. Short ayat would otherwise re-centre the page every few seconds.
 *
 * Keyed on the ayah number, which preserves the design's "manual scroll suspends the follow until the
 * next ayah boundary": scrolling away mid-ayah is never corrected, because nothing re-runs until the
 * next ayah begins.
 */
@Composable
private fun QuranReaderFollowAudio(
    playingAyatNumber: Int?,
    state: QuranReaderUiState.Content,
    listState: LazyListState,
    ayatOffsetInPage: Float?,
) {
    // Keyed on the offset's *value*: crossing onto a page that has not been composed yet arrives here
    // with no measurement, scrolls to that page's top, and then re-runs to refine once the page
    // reports where the ayah actually sits. Keying on mere presence made the first, coarse pass final.
    LaunchedEffect(playingAyatNumber, state.displayMode, ayatOffsetInPage) {
        val ayat = playingAyatNumber ?: return@LaunchedEffect
        val contentIndex =
            when (state.displayMode) {
                QuranDisplayMode.ARAB_TRANSLATION -> state.ayats.indexOfFirst { it.ayatNumber == ayat }
                QuranDisplayMode.ARAB_ONLY -> state.pages.indexOfFirst { page -> page.any { it.ayatNumber == ayat } }
            }
        if (contentIndex < 0) return@LaunchedEffect
        // +1 for the surah-start header, which occupies index 0 of both lists.
        val itemIndex = contentIndex + 1
        val viewportHeight = listState.layoutInfo.viewportSize.height
        if (viewportHeight <= 0) return@LaunchedEffect

        // In mushaf mode the ayah sits somewhere inside the page item; in translation mode the item
        // *is* the ayah, so its own top is the target.
        val offsetInItem = if (state.displayMode == QuranDisplayMode.ARAB_ONLY) ayatOffsetInPage ?: 0f else 0f
        val visibleItem = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == itemIndex }
        if (visibleItem != null) {
            val ayatTop = visibleItem.offset + offsetInItem
            val comfortableTop = viewportHeight * FOLLOW_COMFORT_TOP_FRACTION
            val comfortableBottom = viewportHeight * FOLLOW_COMFORT_BOTTOM_FRACTION
            if (ayatTop in comfortableTop..comfortableBottom) return@LaunchedEffect
        }
        val lead = viewportHeight * FOLLOW_LEAD_FRACTION
        listState.animateScrollToItem(itemIndex, (offsetInItem - lead).roundToInt())
    }
}

/** Derives the currently visible ayat from scroll position for last-read/reading-session tracking
 * (QUR-FR-011/017) — ayat-precise in Arab+translation mode, page-precise in Arab-only mode (see
 * [QuranReaderSynchronizePosition]'s same limitation). */
@Composable
private fun QuranReaderTrackVisiblePosition(
    listState: LazyListState,
    state: QuranReaderUiState.Content,
    onVisiblePositionChanged: (Int) -> Unit,
) {
    LaunchedEffect(listState, state.displayMode, state.pages, state.ayats) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
            val contentIndex = (index - 1).coerceAtLeast(0)
            val visiblePage = state.pages.getOrNull(contentIndex)
            val ayatNumber =
                when (state.displayMode) {
                    QuranDisplayMode.ARAB_TRANSLATION -> state.ayats.getOrNull(contentIndex)?.ayatNumber
                    QuranDisplayMode.ARAB_ONLY -> visiblePage?.lastOrNull()?.ayatNumber
                }
            if (ayatNumber != null) onVisiblePositionChanged(ayatNumber)
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun QuranArabOnlyPages(
    pages: List<List<QuranReaderAyatUiModel>>,
    selectedAyatId: Long?,
    onAyatLongPress: (QuranReaderAyatUiModel) -> Unit,
    arabicSizeSp: Int,
    arabicLineHeightSp: Int,
    arabicFont: QuranArabicFont,
    listState: LazyListState,
    chromeVisible: Boolean,
    onToggleChrome: () -> Unit,
    playingAyatNumber: Int?,
    onPlayingAyatOffset: (ayatNumber: Int, offsetPx: Float) -> Unit,
    header: @Composable () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = listState,
            modifier =
                Modifier
                    .widthIn(max = SanguSantriDimensions.readerContentMaxWidth)
                    .fillMaxSize(),
        ) {
            item(key = "surah-header") { header() }
            itemsIndexed(items = pages, key = { _, page -> "page-${page.firstOrNull()?.page}" }) { _, page ->
                Column(
                    modifier =
                        Modifier.padding(
                            horizontal = SanguSantriDimensions.readerHorizontalPadding,
                            vertical = SanguSantriSpacing.medium,
                        ),
                ) {
                    page.firstOrNull()?.let { firstAyat ->
                        if (chromeVisible) {
                            QuranMushafJuzStrip(
                                juz = firstAyat.juz,
                                page = firstAyat.page,
                                followingAudio = playingAyatNumber != null,
                            )
                        }
                    }
                    QuranFlowingPageText(
                        ayats = page,
                        selectedAyatId = selectedAyatId,
                        onAyatLongPress = onAyatLongPress,
                        onTap = onToggleChrome,
                        arabicFont = arabicFont,
                        playingAyatNumber = playingAyatNumber,
                        onPlayingAyatOffset = onPlayingAyatOffset,
                        textStyle =
                            TextStyle(
                                fontFamily = arabicFont.toFontFamily(),
                                fontSize = arabicSizeSp.sp,
                                lineHeight = arabicLineHeightSp.sp,
                                textAlign = TextAlign.Justify,
                            ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/** Handoff §5's caps strip above the flowing page. The design also prints the hizb and a "DITANDAI"
 * bookmark flag; neither is shown here — the Kemenag dataset this app stores carries juz and page
 * per ayat but no hizb, and inventing a hizb number is not an option. Turn 4's `4f` replaces the
 * strip's right side with a "MENGIKUTI AUDIO" flag while a recitation plays. */
@Composable
private fun QuranMushafJuzStrip(
    juz: Int,
    page: Int,
    followingAudio: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = SanguSantriSpacing.small),
    ) {
        Text(
            text = stringResource(R.string.quran_reader_mushaf_strip, juz, page).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
            color = QuranMutedText,
        )
        if (followingAudio) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall),
            ) {
                Icon(
                    imageVector = Icons.Outlined.GraphicEq,
                    contentDescription = null,
                    tint = QuranPrimary,
                    modifier = Modifier.size(MushafStripIconSize),
                )
                Text(
                    text = stringResource(R.string.quran_murottal_following_audio).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                    color = QuranPrimary,
                )
            }
        }
    }
}

private val MushafStripIconSize = 14.dp

private const val MUSHAF_SURAH_NAME_SIZE_SP = 31
private const val MUSHAF_BASMALAH_SIZE_SP = 26
private const val TRANSLATION_SURAH_NAME_SIZE_SP = 33
private const val TRANSLATION_BASMALAH_SIZE_SP = 27
private const val MODE_CROSSFADE_MILLIS = 180

/**
 * A measured ayah position inside a mushaf page, kept with the ayah it belongs to.
 *
 * The pairing is the point. Holding a bare offset let the previous ayah's position survive a page
 * change and be applied to the next page, which scrolled clean past that page's opening ayah.
 */
private data class QuranMeasuredAyatOffset(
    val ayatNumber: Int,
    val offsetPx: Float,
)

/** Where the recited ayah is placed when a follow scroll runs — a fifth of the way down, so the line
 * before it stays visible for context instead of the ayah sitting flush against the top edge. */
private const val FOLLOW_LEAD_FRACTION = 0.2f

/** An ayah already sitting between these fractions of the viewport is left alone, so short ayat do
 * not drag the page every few seconds. */
private const val FOLLOW_COMFORT_TOP_FRACTION = 0.05f
private const val FOLLOW_COMFORT_BOTTOM_FRACTION = 0.6f
