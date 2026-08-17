@file:Suppress("TooManyFunctions")

package com.sangusantri.app.feature.quran.reader

import android.animation.ValueAnimator
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.sangusantri.app.feature.quran.QuranBrightnessEffect
import com.sangusantri.app.feature.quran.toFontFamily
import kotlinx.coroutines.launch

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
            creationCallback = { factory -> factory.create(surahNumber) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tafsirUiState by viewModel.tafsirUiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose { viewModel.recordSessionIfAdvanced() }
    }

    QuranBrightnessEffect((uiState as? QuranReaderUiState.Content)?.brightnessOverride)

    QuranReaderScreen(
        uiState = uiState,
        tafsirUiState = tafsirUiState,
        targetAyat = targetAyat,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        actions =
            QuranReaderBodyActions(
                onAyatLongPress = viewModel::onAyatLongPress,
                onDismissActionSheet = viewModel::onDismissActionSheet,
                onToggleBookmark = viewModel::onToggleBookmark,
                onMarkLastRead = viewModel::onMarkLastRead,
                onOpenTafsir = viewModel::onOpenTafsir,
                onDismissTafsirSheet = viewModel::onDismissTafsirSheet,
                onRetryTafsir = viewModel::onRetryTafsir,
                onVisiblePositionChanged = viewModel::onVisiblePositionChanged,
                onToggleTheme = viewModel::setThemeMode,
            ),
        modifier = modifier,
    )
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
                title =
                    if (targetUnresolved) {
                        stringResource(R.string.quran_hub_title)
                    } else {
                        readerContent?.surahName.orEmpty()
                    },
                position =
                    if (targetUnresolved) {
                        stringResource(R.string.quran_reader_unavailable_subtitle)
                    } else {
                        currentAyat
                            ?.let { stringResource(R.string.quran_reader_position, it.page, it.juz) }
                            .orEmpty()
                    },
                onBack = onBack,
                onOpenSettings = onOpenSettings,
                onToggleTheme = actions.onToggleTheme,
            )
        },
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
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
        )
    }
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

    val header: @Composable (mushaf: Boolean) -> Unit = { mushaf ->
        QuranSurahStartHeader(
            surahNumber = state.surahNumber,
            category = state.category,
            surahDisplayName = state.surahName,
            surahArabicName = state.surahArabicName,
            ayatCount = state.ayatCount,
            basmalahArabic = state.basmalahArabic,
            variant = state.surahHeaderVariant,
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
                    header = { header(true) },
                )
        }
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
                        if (chromeVisible) QuranMushafJuzStrip(juz = firstAyat.juz, page = firstAyat.page)
                    }
                    QuranFlowingPageText(
                        ayats = page,
                        selectedAyatId = selectedAyatId,
                        onAyatLongPress = onAyatLongPress,
                        onTap = onToggleChrome,
                        arabicFont = arabicFont,
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
 * per ayat but no hizb, and inventing a hizb number is not an option. */
@Composable
private fun QuranMushafJuzStrip(
    juz: Int,
    page: Int,
) {
    Text(
        text = stringResource(R.string.quran_reader_mushaf_strip, juz, page).uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
        color = QuranMutedText,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = SanguSantriSpacing.small),
    )
}

private const val MUSHAF_SURAH_NAME_SIZE_SP = 31
private const val MUSHAF_BASMALAH_SIZE_SP = 26
private const val TRANSLATION_SURAH_NAME_SIZE_SP = 33
private const val TRANSLATION_BASMALAH_SIZE_SP = 27
private const val MODE_CROSSFADE_MILLIS = 180
