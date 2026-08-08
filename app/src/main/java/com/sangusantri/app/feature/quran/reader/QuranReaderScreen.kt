package com.sangusantri.app.feature.quran.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranBackground
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranSurface
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.domain.model.QuranDisplayMode
import com.sangusantri.app.feature.quran.QuranBrightnessEffect
import com.sangusantri.app.feature.quran.QuranThemeBoundary
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

    QuranThemeBoundary {
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
                ),
            modifier = modifier,
        )
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

    Scaffold(
        modifier = modifier,
        containerColor = QuranBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            QuranReaderTopBar(
                title = (uiState as? QuranReaderUiState.Content)?.surahName.orEmpty(),
                onBack = onBack,
                onOpenSettings = onOpenSettings,
            )
        },
    ) { innerPadding ->
        QuranReaderBody(
            uiState = uiState,
            tafsirUiState = tafsirUiState,
            targetAyat = targetAyat,
            snackbarHostState = snackbarHostState,
            actions = actions,
            modifier = Modifier
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
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val positionInfoMessage = stringResource(R.string.quran_action_position_info_format)

    Box(modifier = modifier) {
        when (uiState) {
            QuranReaderUiState.Loading ->
                CircularProgressIndicator(color = QuranPrimary, modifier = Modifier.align(Alignment.Center))

            QuranReaderUiState.Unavailable ->
                Text(
                    text = stringResource(R.string.quran_reader_unavailable),
                    color = QuranMutedText,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .padding(SanguSantriDimensions.readerHorizontalPadding),
                )

            is QuranReaderUiState.Content ->
                QuranReaderContent(
                    state = uiState,
                    targetAyat = targetAyat,
                    onAyatLongPress = actions.onAyatLongPress,
                    onVisiblePositionChanged = actions.onVisiblePositionChanged,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuranReaderTopBar(
    title: String,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back_content_description),
                )
            }
        },
        actions = {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.quran_settings_action_content_description),
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

@Composable
private fun QuranReaderContent(
    state: QuranReaderUiState.Content,
    targetAyat: Int?,
    onAyatLongPress: (QuranReaderAyatUiModel) -> Unit,
    onVisiblePositionChanged: (Int) -> Unit,
) {
    val listState = rememberLazyListState()

    QuranReaderScrollToTarget(listState, state, targetAyat)
    QuranReaderTrackVisiblePosition(listState, state, onVisiblePositionChanged)

    val header: @Composable () -> Unit = {
        QuranSurahStartHeader(
            surahNumber = state.surahNumber,
            category = state.category,
            surahDisplayName = state.surahName,
            ayatCount = state.ayatCount,
        )
    }

    when (state.displayMode) {
        QuranDisplayMode.ARAB_TRANSLATION ->
            QuranTranslationAyatList(
                ayats = state.ayats,
                selectedAyatId = state.selectedAyat?.remoteId,
                onAyatLongPress = onAyatLongPress,
                lazyListState = listState,
                arabicSizeSp = state.arabicSizeSp,
                arabicLineHeightSp = state.arabicLineHeightSp,
                translationSizeSp = state.translationSizeSp,
                headerContent = header,
            )

        QuranDisplayMode.ARAB_ONLY ->
            QuranArabOnlyPages(
                pages = state.pages,
                selectedAyatId = state.selectedAyat?.remoteId,
                onAyatLongPress = onAyatLongPress,
                arabicSizeSp = state.arabicSizeSp,
                arabicLineHeightSp = state.arabicLineHeightSp,
                listState = listState,
                header = header,
            )
    }
}

/** Scrolls once to [targetAyat]'s item (or containing page, in Arab-only mode — flowing text has
 * no per-ayat scroll anchor) when the reader first opens. */
@Composable
private fun QuranReaderScrollToTarget(
    listState: LazyListState,
    state: QuranReaderUiState.Content,
    targetAyat: Int?,
) {
    LaunchedEffect(state.surahNumber, targetAyat) {
        val targetIndex =
            when (state.displayMode) {
                QuranDisplayMode.ARAB_TRANSLATION ->
                    state.ayats
                        .indexOfFirst { it.ayatNumber == targetAyat }
                        .takeIf { it >= 0 }
                        ?.plus(1)

                QuranDisplayMode.ARAB_ONLY ->
                    state.pages
                        .indexOfFirst { page -> page.any { it.ayatNumber == targetAyat } }
                        .takeIf { it >= 0 }
                        ?.plus(1)
            }
        if (targetIndex != null) listState.scrollToItem(targetIndex)
    }
}

/** Derives the currently visible ayat from scroll position for last-read/reading-session tracking
 * (QUR-FR-011/017) — ayat-precise in Arab+translation mode, page-precise in Arab-only mode (see
 * [QuranReaderScrollToTarget]'s same limitation). */
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
    listState: LazyListState,
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
                Column(modifier = Modifier.padding(SanguSantriDimensions.readerHorizontalPadding)) {
                    QuranFlowingPageText(
                        ayats = page,
                        selectedAyatId = selectedAyatId,
                        onAyatLongPress = onAyatLongPress,
                        textStyle =
                            TextStyle(
                                fontFamily = FontFamily.Serif,
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
