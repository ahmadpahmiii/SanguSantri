package com.sangusantri.app.feature.quran.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOnPrimary
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranSurfaceHigh
import com.sangusantri.app.core.designsystem.theme.QuranTranslationText
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
    ) {
        Column(
            modifier =
                Modifier
                    .heightIn(max = TAFSIR_SHEET_MAX_HEIGHT)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SanguSantriSpacing.default, vertical = SanguSantriSpacing.small)
                    .padding(bottom = SanguSantriSpacing.large),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
        ) {
            Text(
                text = stringResource(R.string.quran_tafsir_sheet_title, surahName, ayatNumber),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.quran_tafsir_source_line),
                style = MaterialTheme.typography.bodySmall,
                color = QuranMutedText,
            )
            when (uiState) {
                QuranTafsirUiState.Loading ->
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(SanguSantriSpacing.large)) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            CircularProgressIndicator(color = QuranPrimary)
                            Text(
                                text = stringResource(R.string.quran_tafsir_loading),
                                color = QuranMutedText,
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
private fun QuranTafsirLoadedContent(state: QuranTafsirUiState.Loaded) {
    if (state.isRefreshing) {
        LinearProgressIndicator(color = QuranPrimary, modifier = Modifier.fillMaxWidth())
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
        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = QuranTranslationText)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(SanguSantriSpacing.large),
    ) {
        Text(
            text =
                stringResource(
                    if (retryable) R.string.quran_tafsir_offline_or_failed else R.string.quran_tafsir_unavailable,
                ),
            color = QuranMutedText,
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = QuranPrimary, contentColor = QuranOnPrimary),
        ) {
            Text(text = stringResource(R.string.quran_entry_retry_action))
        }
    }
}

private val TAFSIR_SHEET_MAX_HEIGHT = 480.dp
