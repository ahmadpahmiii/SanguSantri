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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.sangusantri.app.domain.model.QuranDisplayMode
import com.sangusantri.app.domain.model.QuranMurottalState
import com.sangusantri.app.domain.model.QuranMurottalStatus
import com.sangusantri.app.feature.quran.QuranBrightnessEffect
import com.sangusantri.app.feature.quran.murottal.QuranMiniPlayerActions
import com.sangusantri.app.feature.quran.murottal.QuranMiniPlayerBar
import com.sangusantri.app.feature.quran.murottal.QuranMurottalPanel
import com.sangusantri.app.feature.quran.murottal.QuranMurottalPanelActions
import com.sangusantri.app.feature.quran.murottal.QuranMurottalPanelUiState
import kotlinx.coroutines.delay
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
    modifier: Modifier = Modifier,
    viewModel: QuranReaderViewModel =
        hiltViewModel<QuranReaderViewModel, QuranReaderViewModel.Factory>(
            creationCallback = { factory -> factory.create(surahNumber, targetAyat) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tafsirUiState by viewModel.tafsirUiState.collectAsStateWithLifecycle()
    val murottalState by viewModel.murottalState.collectAsStateWithLifecycle()
    val murottalPanelUiState by viewModel.murottalPanelUiState.collectAsStateWithLifecycle()
    val chromeVisible by viewModel.chromeVisible.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose { viewModel.recordSessionIfAdvanced() }
    }

    QuranReaderSideEffects(
        surahNumber = surahNumber,
        brightnessOverride = (uiState as? QuranReaderUiState.Content)?.brightnessOverride,
        keepScreenOn = murottalPanelUiState?.keepScreenOn == true && murottalState.isActive,
        murottalState = murottalState,
        onFollowAudioSurah = viewModel::onFollowAudioSurah,
    )

    QuranReaderScreen(
        uiState = uiState,
        tafsirUiState = tafsirUiState,
        targetAyat = targetAyat,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        chromeVisible = chromeVisible,
        onToggleChrome = viewModel::onToggleChrome,
        onShowChrome = viewModel::onShowChrome,
        actions = viewModel.bodyActions(),
        onMushafPageChanged = viewModel::onMushafPageChanged,
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
        onVisibleAyatChanged = ::onVisibleAyatChanged,
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
    onFollowAudioSurah: (surahNumber: Int) -> Unit,
) {
    QuranBrightnessEffect(brightnessOverride)
    QuranKeepScreenOnEffect(enabled = keepScreenOn)
    QuranMurottalNotificationPermissionEffect(playbackActive = murottalState.isActive)
    QuranFollowAudioAcrossSurahEffect(
        readerSurahNumber = surahNumber,
        murottalState = murottalState,
        onFollowAudioSurah = onFollowAudioSurah,
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
    onFollowAudioSurah: (surahNumber: Int) -> Unit,
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
                onFollowAudioSurah(playingSurah)
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
    val onVisibleAyatChanged: (QuranReaderAyatUiModel) -> Unit,
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
    chromeVisible: Boolean,
    onToggleChrome: () -> Unit,
    onShowChrome: () -> Unit,
    actions: QuranReaderBodyActions,
    onMushafPageChanged: (Int) -> Unit,
    murottalState: QuranMurottalState,
    murottalPanelUiState: QuranMurottalPanelUiState?,
    murottalActions: QuranMiniPlayerActions,
    panelActions: QuranMurottalPanelActions,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val readerContent = uiState as? QuranReaderUiState.Content
    // The title bar's "Halaman N · Juz N" follows the reading position rather than the ayat the
    // reader was opened on: turning mushaf pages moves through several halaman without the target
    // ayat ever changing, which otherwise left the bar reporting the opening page indefinitely.
    var visibleAyat by remember { mutableStateOf<QuranReaderAyatUiModel?>(null) }
    val currentAyat =
        visibleAyat
            ?: readerContent?.ayats?.firstOrNull { it.ayatNumber == targetAyat }
            ?: readerContent?.ayats?.firstOrNull()
    // design-export/quran/18-reader-invalid-target.html keeps the back/settings chrome but replaces
    // the surah title/position with a generic "Al-Qur'an" / "Posisi tidak tersedia" pair — never a
    // stale surah name for a target that doesn't resolve.
    // Only the state's own verdict. The opening ayat cannot be re-checked against the loaded surah
    // any more: mushaf mode pages through the whole mushaf, so reading on past the surah it was in
    // legitimately leaves it behind, and testing for it would call a working reader unavailable.
    val targetUnresolved = uiState == QuranReaderUiState.Unavailable

    // Mushaf immersion (handoff §5): a single tap on the page clears the chrome for a clean page,
    // another restores it. The design drops the title bar entirely in this mode; it is kept here and
    // folded into the same toggle instead, because the reader's only routes to settings, the theme
    // toggle, and back live in that bar — dropping it with no replacement control bar would strand
    // the reader. Chrome always returns when leaving Arab-only mode.
    // Held in QuranReaderChromeState, not here: a surah crossing builds a fresh screen, and local
    // state made a deliberately hidden title bar pop back into view on the swipe that crossed.
    val isMushafMode = readerContent?.displayMode == QuranDisplayMode.ARAB_ONLY
    LaunchedEffect(isMushafMode) { if (!isMushafMode) onShowChrome() }

    Scaffold(
        modifier = modifier,
        containerColor = QuranBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!chromeVisible) return@Scaffold
            QuranReaderTopBar(
                title = quranReaderTitle(targetUnresolved, readerContent),
                position = quranReaderPosition(targetUnresolved, currentAyat),
                followingAudio =
                    murottalState.surahNumber == readerContent?.surahNumber && murottalState.isActive,
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
            snackbarHostState = snackbarHostState,
            actions =
                actions.copy(
                    onVisibleAyatChanged = { ayat ->
                        visibleAyat = ayat
                        actions.onVisibleAyatChanged(ayat)
                    },
                ),
            anchorAyat = visibleAyat?.ayatNumber ?: targetAyat,
            onMushafPageChanged = onMushafPageChanged,
            onBack = onBack,
            onToggleChrome = onToggleChrome,
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
            ?.let { stringResource(R.string.quran_reader_position, it.juz, it.page) }
            .orEmpty()
    }

@Suppress("LongParameterList")
@Composable
private fun QuranReaderBody(
    uiState: QuranReaderUiState,
    tafsirUiState: QuranTafsirUiState,
    snackbarHostState: SnackbarHostState,
    actions: QuranReaderBodyActions,
    anchorAyat: Int?,
    onMushafPageChanged: (Int) -> Unit,
    onBack: () -> Unit,
    onToggleChrome: () -> Unit,
    murottalState: QuranMurottalState,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val positionInfoMessage = stringResource(R.string.quran_action_position_info_format)

    Box(modifier = modifier) {
        when (uiState) {
            QuranReaderUiState.Loading -> QuranReaderDelayedLoadingState()

            QuranReaderUiState.Unavailable -> QuranReaderUnavailableState(onBack = onBack)

            is QuranReaderUiState.Content ->
                QuranReaderContent(
                    state = uiState,
                    anchorAyat = anchorAyat,
                    onAyatLongPress = actions.onAyatLongPress,
                    onVisibleAyatChanged = actions.onVisibleAyatChanged,
                    onMushafPageChanged = onMushafPageChanged,
                    onToggleChrome = onToggleChrome,
                    onPlayAyat = actions.onPlayAyat,
                    murottalState = murottalState,
                )
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

/**
 * Holds the skeleton back briefly.
 *
 * A new surah means a new ViewModel, whose state starts at [QuranReaderUiState.Loading] before Room
 * answers — which it does in a few milliseconds. Painting the skeleton immediately meant every surah
 * change flashed a screen of placeholder blocks: near-white in light mode, a lighter block in dark,
 * a blink either way. Nothing is drawn until the read is actually slow enough to be worth reporting.
 */
@Composable
private fun QuranReaderDelayedLoadingState() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(LOADING_SKELETON_DELAY_MILLIS)
        visible = true
    }
    if (visible) QuranReaderLoadingState()
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

/** One line, sharing the row with the back icon: juz/halaman used to be printed a second time inside
 * the page under the basmalah, which is a detail the reader glances at rather than reads, and it
 * interrupted the run-up into the Arabic. */
@Composable
private fun QuranReaderTopBarTitle(
    title: String,
    position: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            // Does not fill: the position keeps its natural width and a long surah name ellipsises
            // instead, because the page number is the part that changes.
            modifier = Modifier.weight(1f, fill = false),
        )
        if (position.isNotBlank()) {
            // The one part of the bar that changes on every page turn. Swapped outright it ticked
            // over abruptly against a page that slides; a short crossfade lets the two land together.
            Crossfade(
                targetState = position,
                animationSpec = tween(if (ValueAnimator.areAnimatorsEnabled()) HEADER_FADE_MILLIS else 0),
                label = "Quran reader position",
            ) { positionText ->
                Text(
                    text = positionText,
                    style = MaterialTheme.typography.bodySmall,
                    color = QuranMutedText,
                    maxLines = 1,
                    modifier = Modifier.padding(start = SanguSantriSpacing.small),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
@Composable
private fun QuranReaderTopBar(
    title: String,
    position: String,
    followingAudio: Boolean,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleTheme: (AppThemeMode) -> Unit,
) {
    val settingsContentDescription = stringResource(R.string.quran_settings_action_content_description)
    TopAppBar(
        title = { QuranReaderTopBarTitle(title = title, position = position) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back_content_description),
                )
            }
        },
        actions = {
            if (followingAudio) {
                Icon(
                    imageVector = Icons.Outlined.GraphicEq,
                    contentDescription = stringResource(R.string.quran_murottal_following_audio),
                    tint = QuranPrimary,
                    modifier = Modifier.size(FollowingAudioIconSize),
                )
            }
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

/** Longer than a Room read of one surah, shorter than the eye reads as a pause. */
private const val LOADING_SKELETON_DELAY_MILLIS = 300L

@Composable
@Suppress("LongParameterList", "LongMethod")
private fun QuranReaderContent(
    state: QuranReaderUiState.Content,
    anchorAyat: Int?,
    onAyatLongPress: (QuranReaderAyatUiModel) -> Unit,
    onVisibleAyatChanged: (QuranReaderAyatUiModel) -> Unit,
    onMushafPageChanged: (Int) -> Unit,
    onToggleChrome: () -> Unit,
    onPlayAyat: (QuranReaderAyatUiModel) -> Unit,
    murottalState: QuranMurottalState,
) {
    val translationListState = rememberLazyListState()
    // One pager page per mushaf halaman, all 604 of them. Pages are the unit, so reading straight
    // through never leaves this pager and there is no surah to navigate to.
    val mushafPagerState =
        rememberPagerState(
            initialPage = (state.currentMushafPage - 1).coerceAtLeast(0),
            pageCount = { QURAN_MUSHAF_PAGE_COUNT },
        )

    QuranReaderSynchronizePosition(
        state = state,
        anchorAyat = anchorAyat,
        translationListState = translationListState,
    )

    // The loaded window follows the page being read, so the neighbours of wherever the reader stops
    // are already in hand for the next swipe.
    LaunchedEffect(mushafPagerState) {
        snapshotFlow { mushafPagerState.currentPage }.collect { onMushafPageChanged(it + 1) }
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

    // Mushaf mode splits following the recitation: the pager turns to the halaman holding the ayah,
    // and that page scrolls itself to it (see QuranMushafPager), because only the page has measured
    // where the ayah sits. Crossing a surah is now just another page turn.
    LaunchedEffect(playback.playingAyatNumber, state.displayMode, state.mushafPages) {
        if (state.displayMode != QuranDisplayMode.ARAB_ONLY) return@LaunchedEffect
        val ayat = playback.playingAyatNumber ?: return@LaunchedEffect
        val page =
            state.mushafPages.entries
                .firstOrNull { entry ->
                    entry.value.segments.any { segment ->
                        segment.surahNumber == state.surahNumber && segment.ayats.any { it.ayatNumber == ayat }
                    }
                }?.key ?: return@LaunchedEffect
        if (page - 1 != mushafPagerState.currentPage) mushafPagerState.animateScrollToPage(page - 1)
    }
    // Translation mode is one continuous list, so the recited ayah is followed by scrolling it here.
    QuranFollowScrollEffect(
        playingAyatNumber =
            playback.playingAyatNumber.takeIf {
                state.displayMode == QuranDisplayMode.ARAB_TRANSLATION
            },
        itemIndex =
            state.ayats
                .indexOfFirst { it.ayatNumber == playback.playingAyatNumber }
                .takeIf { it >= 0 }
                ?.plus(1),
        offsetInItem = 0f,
        listState = translationListState,
    )

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
                    headerContent = {
                        QuranSurahStartHeader(
                            surahNumber = state.surahNumber,
                            category = state.category,
                            surahDisplayName = state.surahName,
                            surahArabicName = state.surahArabicName,
                            ayatCount = state.ayatCount,
                            basmalahArabic = state.basmalahArabic,
                            arabicFont = state.arabicFont,
                            surahNameSizeSp = TRANSLATION_SURAH_NAME_SIZE_SP,
                            basmalahSizeSp = TRANSLATION_BASMALAH_SIZE_SP,
                        )
                    },
                )

            QuranDisplayMode.ARAB_ONLY ->
                QuranMushafPager(
                    pages = state.mushafPages,
                    pagerState = mushafPagerState,
                    basmalahArabic = state.basmalahArabic,
                    selectedAyatId = state.selectedAyat?.remoteId,
                    arabicSizeSp = state.arabicSizeSp,
                    arabicLineHeightSp = state.arabicLineHeightSp,
                    arabicFont = state.arabicFont,
                    playingAyatNumber = playback.playingAyatNumber,
                    onAyatLongPress = onAyatLongPress,
                    onToggleChrome = onToggleChrome,
                    onVisibleAyatChanged = onVisibleAyatChanged,
                )
        }
    }
}

/**
 * Keeps the ayah being recited in view (`4f`), positioned against the actual viewport rather than by
 * whole list items.
 *
 * Item-level scrolling was not enough in either mode. In mushaf mode the flowing page of one Kemenag
 * `halaman` can be several screens tall at a large Arabic size, so every ayah after the first scrolled
 * nowhere and the recitation ran off the bottom. In translation mode `animateScrollToItem(index)`
 * pinned each ayah flush against the top edge, with no context above it.
 *
 * So the target is computed in pixels: the item's own offset, plus [offsetInItem] for where the ayah
 * actually sits inside a measured page of text, minus a lead of [FOLLOW_LEAD_FRACTION] of the viewport
 * so the ayah lands a little below the top with its previous line still visible. Both inputs come from
 * real measurement, so the result adapts to font size, line spacing and screen height without knowing
 * any of them.
 *
 * It also holds still when it can: if the ayah is already inside a comfortable band of the viewport,
 * nothing scrolls. Short ayat would otherwise re-centre the page every few seconds.
 *
 * Keyed on the ayah number, which preserves the design's "manual scroll suspends the follow until the
 * next ayah boundary": scrolling away mid-ayah is never corrected, because nothing re-runs until the
 * next ayah begins.
 */
@Composable
internal fun QuranFollowScrollEffect(
    playingAyatNumber: Int?,
    itemIndex: Int?,
    offsetInItem: Float,
    listState: LazyListState,
) {
    // Keyed on the offset's *value*: a page composed only once the recitation reaches it arrives here
    // with no measurement, scrolls to the item's top, and then re-runs to refine once the page reports
    // where the ayah actually sits. Keying on mere presence made the first, coarse pass final.
    LaunchedEffect(playingAyatNumber, itemIndex, offsetInItem, listState) {
        if (playingAyatNumber == null || itemIndex == null) return@LaunchedEffect
        val viewportHeight = listState.layoutInfo.viewportSize.height
        if (viewportHeight <= 0) return@LaunchedEffect

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

internal const val MUSHAF_SURAH_NAME_SIZE_SP = 31
internal const val MUSHAF_BASMALAH_SIZE_SP = 26
private const val TRANSLATION_SURAH_NAME_SIZE_SP = 33
private const val TRANSLATION_BASMALAH_SIZE_SP = 27
private const val MODE_CROSSFADE_MILLIS = 180

private const val HEADER_FADE_MILLIS = 180

/** The "mengikuti audio" mark in the title bar, sized to sit with the bar's other icons. */
private val FollowingAudioIconSize = 16.dp

/**
 * A measured ayah position inside a mushaf page, kept with the ayah it belongs to.
 *
 * The pairing is the point. Holding a bare offset let the previous ayah's position survive a page
 * change and be applied to the next page, which scrolled clean past that page's opening ayah.
 */
internal data class QuranMeasuredAyatOffset(
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
