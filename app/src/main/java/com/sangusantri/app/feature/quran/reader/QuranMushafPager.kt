package com.sangusantri.app.feature.quran.reader

import android.animation.ValueAnimator
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.feature.quran.toFontFamily

/**
 * The mushaf as it is actually printed: one halaman per swipe, all 604 of them, whatever surahs each
 * one carries.
 *
 * Pages, not surahs, are the unit here. Page 603 shows Al-Kafirun, An-Nasr and Al-Lahab one after the
 * other on a single screen, each opening with its own header, exactly as the printed page does — the
 * reader was previously scoped to one surah, so that page was drawn three separate times, each
 * showing a third of it against a screen of empty space. Reading straight through therefore no longer
 * crosses a "surah boundary" at all: there is nothing to navigate to, the next page is simply the next
 * page, which is why none of the boundary and continuity machinery this replaced is needed any more.
 *
 * Laid out right-to-left so a left-to-right swipe turns forward, the way a printed mushaf is turned.
 * Only the page *order* is mushaf-handed; each page's content is composed back in the app's own
 * direction, because the Arabic establishes its own RTL context inside [QuranFlowingPageText].
 */
@Suppress("LongParameterList")
@Composable
internal fun QuranMushafPager(
    pages: Map<Int, QuranMushafPageUiModel>,
    pagerState: PagerState,
    basmalahArabic: String,
    selectedAyatId: Long?,
    arabicSizeSp: Int,
    arabicLineHeightSp: Int,
    arabicFont: QuranArabicFont,
    playingAyatNumber: Int?,
    onAyatLongPress: (QuranReaderAyatUiModel) -> Unit,
    onToggleChrome: () -> Unit,
    onVisibleAyatChanged: (QuranReaderAyatUiModel) -> Unit,
) {
    val appLayoutDirection = LocalLayoutDirection.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HorizontalPager(
            state = pagerState,
            // The pager's default settle is `spring(StiffnessMediumLow)` — soft enough that a page
            // keeps drifting after the thumb lifts, which reads as the page taking time to open. A
            // turning page is a discrete, mechanical thing, so it gets a short fixed tween.
            flingBehavior =
                PagerDefaults.flingBehavior(
                    state = pagerState,
                    snapAnimationSpec =
                        tween(
                            durationMillis = if (ValueAnimator.areAnimatorsEnabled()) PAGE_SNAP_MILLIS else 0,
                            easing = FastOutSlowInEasing,
                        ),
                ),
            key = { index -> index },
            modifier = Modifier.fillMaxSize(),
        ) { index ->
            CompositionLocalProvider(LocalLayoutDirection provides appLayoutDirection) {
                // Absent while the loaded window catches up. Reaching such a page takes a swipe, and
                // the read finishes first, so nothing is drawn rather than a placeholder that would
                // flash away.
                val page = pages[index + 1]
                if (page != null) {
                    QuranMushafPage(
                        page = page,
                        // Only the page actually being read reports where it is. The pager keeps its
                        // neighbours composed, and they report on composing too, so without this the
                        // page one swipe away would overwrite the title bar with its own surah and
                        // halaman — the reader appeared to skip pages it had never been shown.
                        isActive = index == pagerState.currentPage,
                        basmalahArabic = basmalahArabic,
                        selectedAyatId = selectedAyatId,
                        arabicSizeSp = arabicSizeSp,
                        arabicLineHeightSp = arabicLineHeightSp,
                        arabicFont = arabicFont,
                        playingAyatNumber = playingAyatNumber,
                        onAyatLongPress = onAyatLongPress,
                        onToggleChrome = onToggleChrome,
                        onVisibleAyatChanged = onVisibleAyatChanged,
                    )
                }
            }
        }
    }
}

/**
 * One halaman, scrollable on its own when the Arabic size makes it taller than the screen.
 *
 * The page owns its scroll state rather than the screen hoisting one per page: the only thing that
 * ever scrolls a page besides the reader's thumb is the recitation running through it, and the page
 * holding the recited ayah is by definition the composed one.
 */
@Suppress("LongParameterList")
@Composable
private fun QuranMushafPage(
    page: QuranMushafPageUiModel,
    isActive: Boolean,
    basmalahArabic: String,
    selectedAyatId: Long?,
    arabicSizeSp: Int,
    arabicLineHeightSp: Int,
    arabicFont: QuranArabicFont,
    playingAyatNumber: Int?,
    onAyatLongPress: (QuranReaderAyatUiModel) -> Unit,
    onToggleChrome: () -> Unit,
    onVisibleAyatChanged: (QuranReaderAyatUiModel) -> Unit,
) {
    val listState = rememberLazyListState()
    var measuredAyatOffset by remember(page.page) { mutableStateOf<QuranMeasuredAyatOffset?>(null) }

    // The surah named in the title bar follows what is actually on screen, so scrolling down page 603
    // moves it Al-Kafirun -> An-Nasr -> Al-Lahab rather than naming the page's first surah throughout.
    LaunchedEffect(listState, page, isActive) {
        if (!isActive) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }.collect { itemIndex ->
            val ayat = page.ayatAtItem(itemIndex) ?: return@collect
            onVisibleAyatChanged(ayat)
        }
    }

    val recitedSegment =
        page.segments.indexOfFirst { segment ->
            segment.ayats.any { it.ayatNumber == playingAyatNumber }
        }
    val recitedHere = playingAyatNumber?.takeIf { recitedSegment >= 0 }
    QuranFollowScrollEffect(
        playingAyatNumber = recitedHere,
        itemIndex = if (recitedSegment >= 0) page.textItemIndex(recitedSegment) else null,
        offsetInItem = measuredAyatOffset?.takeIf { it.ayatNumber == recitedHere }?.offsetPx ?: 0f,
        listState = listState,
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = SanguSantriSpacing.medium),
            modifier =
                Modifier
                    .widthIn(max = SanguSantriDimensions.readerContentMaxWidth)
                    .fillMaxSize(),
        ) {
            mushafSegments(
                page = page,
                basmalahArabic = basmalahArabic,
                selectedAyatId = selectedAyatId,
                arabicSizeSp = arabicSizeSp,
                arabicLineHeightSp = arabicLineHeightSp,
                arabicFont = arabicFont,
                recitedSegment = recitedSegment,
                recitedAyatNumber = recitedHere,
                onAyatLongPress = onAyatLongPress,
                onToggleChrome = onToggleChrome,
                onPlayingAyatOffset = { ayatNumber, offsetPx ->
                    measuredAyatOffset = QuranMeasuredAyatOffset(ayatNumber, offsetPx)
                },
            )
        }
    }
}

/** The halaman's surahs, each opening with its own header when the surah begins here — which is why
 * three headers appear part-way down page 603, and none on a page that only continues a surah. */
@Suppress("LongParameterList")
private fun LazyListScope.mushafSegments(
    page: QuranMushafPageUiModel,
    basmalahArabic: String,
    selectedAyatId: Long?,
    arabicSizeSp: Int,
    arabicLineHeightSp: Int,
    arabicFont: QuranArabicFont,
    recitedSegment: Int,
    recitedAyatNumber: Int?,
    onAyatLongPress: (QuranReaderAyatUiModel) -> Unit,
    onToggleChrome: () -> Unit,
    onPlayingAyatOffset: (ayatNumber: Int, offsetPx: Float) -> Unit,
) {
    page.segments.forEachIndexed { segmentIndex, segment ->
        if (segment.startsSurah) {
            item(key = "header-${segment.surahNumber}") {
                QuranSurahStartHeader(
                    surahNumber = segment.surahNumber,
                    category = segment.category,
                    surahDisplayName = segment.surahName,
                    surahArabicName = segment.surahArabicName,
                    ayatCount = segment.ayatCount,
                    basmalahArabic = basmalahArabic,
                    arabicFont = arabicFont,
                    surahNameSizeSp = MUSHAF_SURAH_NAME_SIZE_SP,
                    basmalahSizeSp = MUSHAF_BASMALAH_SIZE_SP,
                )
            }
        }
        item(key = "text-${segment.surahNumber}") {
            QuranFlowingPageText(
                ayats = segment.ayats,
                selectedAyatId = selectedAyatId,
                onAyatLongPress = onAyatLongPress,
                onTap = onToggleChrome,
                arabicFont = arabicFont,
                playingAyatNumber = if (segmentIndex == recitedSegment) recitedAyatNumber else null,
                onPlayingAyatOffset = onPlayingAyatOffset,
                textStyle =
                    TextStyle(
                        fontFamily = arabicFont.toFontFamily(),
                        fontSize = arabicSizeSp.sp,
                        lineHeight = arabicLineHeightSp.sp,
                        textAlign = TextAlign.Justify,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SanguSantriDimensions.readerHorizontalPadding),
            )
        }
    }
}

/** How long a halaman takes to settle after the thumb lifts. Short on purpose: a page turn should
 * land, not glide. */
private const val PAGE_SNAP_MILLIS = 220

/**
 * Lazy item index of a segment's flowing text, counting the surah headers printed above it.
 *
 * Segments do not map one-to-one onto items: a segment that opens a surah contributes a header and a
 * text block, one that merely continues a surah contributes only text. Working the index out by hand
 * at each call site is how a follow-scroll ends up on the wrong block of a three-surah page.
 */
private fun QuranMushafPageUiModel.textItemIndex(segmentIndex: Int): Int =
    segments.take(segmentIndex).sumOf { if (it.startsSurah) 2 else 1 } +
        if (segments[segmentIndex].startsSurah) 1 else 0

/** The first ayat of whichever segment owns [itemIndex] — the page's own item numbering resolved back
 * to something that names a surah and a position. */
private fun QuranMushafPageUiModel.ayatAtItem(itemIndex: Int): QuranReaderAyatUiModel? {
    var cursor = 0
    segments.forEach { segment ->
        val span = if (segment.startsSurah) 2 else 1
        if (itemIndex < cursor + span) return segment.ayats.firstOrNull()
        cursor += span
    }
    return segments.lastOrNull()?.ayats?.firstOrNull()
}
