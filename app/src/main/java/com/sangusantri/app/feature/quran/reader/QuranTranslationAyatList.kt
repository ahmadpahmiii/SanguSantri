package com.sangusantri.app.feature.quran.reader

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranTranslationText
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing

/** Arab + translation rendering: one stable lazy item per ordered ayat. */
@Composable
fun QuranTranslationAyatList(
    ayats: List<QuranReaderAyatUiModel>,
    selectedAyatId: Long?,
    onAyatLongPress: (QuranReaderAyatUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier =
                Modifier
                    .widthIn(max = SanguSantriDimensions.readerContentMaxWidth)
                    .fillMaxSize(),
        ) {
            items(items = ayats, key = { it.remoteId }) { ayat ->
                QuranTranslationAyatItem(
                    ayat = ayat,
                    selected = ayat.remoteId == selectedAyatId,
                    onLongPress = { onAyatLongPress(ayat) },
                )
            }
        }
    }
}

@Composable
private fun QuranTranslationAyatItem(
    ayat: QuranReaderAyatUiModel,
    selected: Boolean,
    onLongPress: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val semanticsLabel = stringResource(R.string.quran_open_ayat_action_number, ayat.ayatNumber)
    val selectionColor = QuranPrimaryContainer
    Column(
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
        modifier =
            Modifier
                .fillMaxWidth()
                .drawBehind { if (selected) drawRect(selectionColor) }
                .semantics {
                    onLongClick(label = semanticsLabel) {
                        onLongPress()
                        true
                    }
                }
                .pointerInput(ayat.remoteId) {
                    detectTapGestures(
                        onLongPress = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongPress()
                        },
                    )
                }
                .padding(
                    horizontal = SanguSantriDimensions.readerHorizontalPadding,
                    vertical = SanguSantriSpacing.large,
                ),
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(
                text = ayat.arabicText,
                style =
                    TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = 34.sp,
                        lineHeight = 60.sp,
                        textAlign = TextAlign.End,
                    ),
                color = QuranArabicText,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Text(
            text = ayat.translation,
            color = QuranTranslationText,
            modifier = Modifier.fillMaxWidth(),
        )
        HorizontalDivider(color = QuranOutline)
    }
}
