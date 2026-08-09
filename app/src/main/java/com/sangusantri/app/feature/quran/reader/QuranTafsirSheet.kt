package com.sangusantri.app.feature.quran.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranError
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOnPrimary
import com.sangusantri.app.core.designsystem.theme.QuranOnPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranScrim
import com.sangusantri.app.core.designsystem.theme.QuranSurfaceHigh
import com.sangusantri.app.core.designsystem.theme.QuranTranslationText
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing

/**
 * The tafsir bottom sheet (QUR-FR-013, `docs/design/QURAN_DESIGN_SYSTEM.md` §5.6): cached content
 * shows immediately with a subtle refreshing indicator when stale, an inline retry never closes or
 * replaces the reader behind it, and long content scrolls independently of the reader.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranTafsirSheet(
    surahName: String,
    ayatNumber: Int,
    uiState: QuranTafsirUiState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = QuranSurfaceHigh,
        contentColor = QuranArabicText,
        dragHandle = { BottomSheetDefaults.DragHandle(color = QuranMutedText) },
        scrimColor = QuranScrim,
        shape =
            RoundedCornerShape(
                topStart = SanguSantriDimensions.quranSheetCornerRadius,
                topEnd = SanguSantriDimensions.quranSheetCornerRadius,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .heightIn(max = SanguSantriDimensions.quranSheetMaxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SanguSantriSpacing.large)
                    .padding(bottom = SanguSantriSpacing.large),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
        ) {
            QuranTafsirHeader(surahName = surahName, ayatNumber = ayatNumber, onDismiss = onDismiss)
            when (uiState) {
                QuranTafsirUiState.Loading ->
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(SanguSantriSpacing.large),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            CircularProgressIndicator(color = QuranPrimary)
                            Text(
                                text = stringResource(R.string.quran_tafsir_loading),
                                color = QuranMutedText,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = SanguSantriSpacing.small),
                            )
                        }
                    }

                is QuranTafsirUiState.Loaded ->
                    QuranTafsirLoadedContent(uiState)

                is QuranTafsirUiState.Unavailable ->
                    QuranTafsirUnavailable(retryable = uiState.retryable, onRetry = onRetry)
            }
        }
    }
}

@Composable
private fun QuranTafsirHeader(
    surahName: String,
    ayatNumber: Int,
    onDismiss: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(R.string.quran_tafsir_sheet_title, surahName, ayatNumber),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.quran_tafsir_source_line),
                style = MaterialTheme.typography.bodySmall,
                color = QuranMutedText,
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.quran_tafsir_close),
            )
        }
    }
}

@Composable
private fun QuranTafsirLoadedContent(state: QuranTafsirUiState.Loaded) {
    // design-export/quran/13a-tafsir-cached-refreshing.html `.cache-chip` — cached content stays
    // visible the whole time; this is a quiet status pill, not a blocking loading bar.
    if (state.isRefreshing) {
        Surface(
            color = QuranPrimaryContainer,
            contentColor = QuranOnPrimaryContainer,
            shape = RoundedCornerShape(percent = 50),
        ) {
            Text(
                text = stringResource(R.string.quran_tafsir_cache_chip),
                style = MaterialTheme.typography.labelSmall,
                modifier =
                    Modifier.padding(
                        horizontal = SanguSantriSpacing.small,
                        vertical = SanguSantriSpacing.extraSmall,
                    ),
            )
        }
    }
    QuranTafsirSection(label = stringResource(R.string.quran_tafsir_label_ringkas), body = state.tafsir.ringkas)
    QuranTafsirSection(label = stringResource(R.string.quran_tafsir_label_tahlili), body = state.tafsir.tahlili)
}

@Composable
private fun QuranTafsirSection(
    label: String,
    body: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = QuranPrimary)
        Text(text = body, style = MaterialTheme.typography.bodyLarge, color = QuranTranslationText)
    }
}

@Composable
private fun QuranTafsirUnavailable(
    retryable: Boolean,
    onRetry: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(SanguSantriSpacing.large),
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = QuranError,
        )
        Text(
            text =
                stringResource(
                    if (retryable) R.string.quran_tafsir_error_title else R.string.quran_tafsir_unavailable_title,
                ),
            style = MaterialTheme.typography.titleMedium,
            color = QuranArabicText,
        )
        Text(
            text =
                stringResource(
                    if (retryable) R.string.quran_tafsir_error_body else R.string.quran_tafsir_unavailable,
                ),
            style = MaterialTheme.typography.bodyMedium,
            color = QuranMutedText,
        )
        // design-export/quran/13b-tafsir-offline-no-cache.html has no button at all — an offline/
        // no-cache state with nothing to retry against yet. Only the retryable (13c) branch offers
        // "Coba lagi".
        if (retryable) {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = QuranPrimary, contentColor = QuranOnPrimary),
                modifier = Modifier.heightIn(min = SanguSantriDimensions.minimumTouchTarget),
            ) {
                Text(text = stringResource(R.string.quran_entry_retry_action))
            }
        }
    }
}
