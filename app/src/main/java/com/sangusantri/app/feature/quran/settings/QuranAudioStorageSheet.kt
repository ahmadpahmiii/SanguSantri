package com.sangusantri.app.feature.quran.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranError
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranScrim
import com.sangusantri.app.core.designsystem.theme.QuranSurfaceHigh
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.feature.quran.murottal.asAudioSize

/**
 * The storage sheet behind "Penyimpanan".
 *
 * Deleting audio is irreversible and can discard hundreds of megabytes, so the sheet does three
 * things before offering it: states how much is stored, says plainly that Quran text/translation/
 * tafsir are untouched and stay readable offline, and puts the amount inside the button label so the
 * action can never be mistaken for something smaller than it is.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuranAudioStorageSheet(
    surahCount: Int,
    totalBytes: Long,
    onDeleteAll: () -> Unit,
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
                    .fillMaxWidth()
                    .padding(horizontal = SanguSantriSpacing.default)
                    .padding(bottom = SanguSantriSpacing.large),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.quran_audio_storage_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.quran_audio_storage_close),
                    )
                }
            }
            HorizontalDivider(color = QuranOutline, modifier = Modifier.padding(vertical = SanguSantriSpacing.medium))

            if (surahCount == 0) {
                Text(
                    text = stringResource(R.string.quran_audio_storage_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = QuranMutedText,
                )
                return@Column
            }
            QuranAudioStorageAmount(surahCount = surahCount, totalBytes = totalBytes)
            Text(
                text = stringResource(R.string.quran_audio_storage_explainer),
                style = MaterialTheme.typography.bodySmall,
                color = QuranMutedText,
                modifier = Modifier.padding(top = SanguSantriSpacing.default),
            )
            QuranDeleteAllAudioButton(totalBytes = totalBytes, onDeleteAll = onDeleteAll)
        }
    }
}

@Composable
private fun QuranAudioStorageAmount(
    surahCount: Int,
    totalBytes: Long,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Outlined.Headphones,
            contentDescription = null,
            tint = QuranPrimary,
            modifier = Modifier.size(StorageIconSize),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.quran_audio_storage_amount, surahCount, totalBytes.asAudioSize()),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text =
                    stringResource(
                        R.string.quran_audio_storage_amount_detail,
                        stringResource(R.string.quran_murottal_reciter),
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = QuranMutedText,
            )
        }
    }
}

/** Outlined in the error role rather than filled: destructive, but not the thing the sheet is
 * steering the reader towards. The amount sits in the label so the tap is unambiguous. */
@Composable
private fun QuranDeleteAllAudioButton(
    totalBytes: Long,
    onDeleteAll: () -> Unit,
) {
    Surface(
        onClick = onDeleteAll,
        color = Color.Transparent,
        contentColor = QuranError,
        border = BorderStroke(1.dp, QuranError),
        shape = RoundedCornerShape(DeleteButtonRadius),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = SanguSantriSpacing.large)
                .heightIn(min = SanguSantriDimensions.minimumTouchTarget),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = null,
                modifier = Modifier.size(StorageIconSize),
            )
            Text(
                text = stringResource(R.string.quran_audio_storage_delete_all, totalBytes.asAudioSize()),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = SanguSantriSpacing.small),
            )
        }
    }
}

private val DeleteButtonRadius = 17.dp

private val StorageIconSize = 22.dp

/**
 * Tampilan's "AUDIO TERUNDUH · N SURAH · X MB" line, between Kecerahan and Sumber Al-Qur'an.
 *
 * The design labelled the trailing link "Kelola". That was replaced with "Penyimpanan": the only
 * thing behind it removes audio, so a "manage" label set up anyone who tapped it to expect per-surah
 * controls — and, before this change, deleted the whole library on that single tap. It now opens
 * [QuranAudioStorageSheet], which states what is stored and what deleting does before offering it.
 */
@Composable
internal fun QuranStoredAudioSummary(
    surahCount: Int,
    totalBytes: Long,
    onOpenStorage: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = SanguSantriSpacing.small),
    ) {
        Text(
            text =
                stringResource(
                    R.string.quran_hub_audio_stored_summary,
                    surahCount,
                    totalBytes.asAudioSize(),
                ).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = QuranMutedText,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.quran_hub_audio_manage),
            style = MaterialTheme.typography.labelLarge,
            color = QuranPrimary,
            modifier =
                Modifier
                    .clickable(onClick = onOpenStorage)
                    .padding(SanguSantriSpacing.extraSmall),
        )
    }
}
