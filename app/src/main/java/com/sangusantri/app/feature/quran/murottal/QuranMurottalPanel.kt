package com.sangusantri.app.feature.quran.murottal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.sangusantri.app.core.designsystem.theme.QuranOnPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranScrim
import com.sangusantri.app.core.designsystem.theme.QuranSurfaceHigh
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.QuranAudioDownloadProgress
import com.sangusantri.app.domain.model.QuranMurottalSpeed

data class QuranMurottalPanelUiState(
    val surahNumber: Int,
    val surahName: String,
    val ayatCount: Int,
    /** `null` when this is An-Nas — there is no surah after it to continue into. */
    val nextSurahName: String?,
    val speed: QuranMurottalSpeed,
    val continueAcrossSurah: Boolean,
    val keepScreenOn: Boolean,
    val storedAyahCount: Int,
    val storedBytes: Long,
    /** Non-null only while this surah's audio is downloading. */
    val download: QuranAudioDownloadProgress?,
    /**
     * The whole queue as it should read, already starting with the surah actually being recited.
     *
     * Built from the player rather than from [surahName]: the reader can be open on one surah while
     * playback has moved on to another, and joining this panel's surah onto the player's queue
     * produced a line that skipped whatever was playing.
     */
    val queueDisplayNames: List<String>,
) {
    val isFullyStored: Boolean get() = ayatCount > 0 && storedAyahCount >= ayatCount
}

data class QuranMurottalPanelActions(
    val onSelectSpeed: (QuranMurottalSpeed) -> Unit,
    val onToggleContinueAcrossSurah: (Boolean) -> Unit,
    val onToggleKeepScreenOn: (Boolean) -> Unit,
    val onDownloadAudio: () -> Unit,
    val onCancelDownload: () -> Unit,
    val onDismiss: () -> Unit,
)

/** The murottal bottom sheet (`4c`): qari, speed, the two switches, the audio-download block, and
 * the queue line. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranMurottalPanel(
    uiState: QuranMurottalPanelUiState,
    actions: QuranMurottalPanelActions,
) {
    ModalBottomSheet(
        onDismissRequest = actions.onDismiss,
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
            QuranMurottalPanelHeader(onDismiss = actions.onDismiss)
            HorizontalDivider(color = QuranOutline, modifier = Modifier.padding(vertical = SanguSantriSpacing.medium))
            QuranQariRow()
            QuranSpeedSelector(selected = uiState.speed, onSelect = actions.onSelectSpeed)
            QuranMurottalSwitchRow(
                title = stringResource(R.string.quran_murottal_panel_continue_title),
                subtitle =
                    uiState.nextSurahName?.let {
                        stringResource(R.string.quran_murottal_panel_continue_subtitle, it)
                    } ?: stringResource(R.string.quran_murottal_panel_continue_subtitle_last),
                checked = uiState.continueAcrossSurah,
                onCheckedChange = actions.onToggleContinueAcrossSurah,
            )
            QuranMurottalSwitchRow(
                title = stringResource(R.string.quran_murottal_panel_keep_screen_on),
                subtitle = null,
                checked = uiState.keepScreenOn,
                onCheckedChange = actions.onToggleKeepScreenOn,
            )
            QuranAudioDownloadBlock(uiState = uiState, actions = actions)
            if (uiState.queueDisplayNames.isNotEmpty()) {
                Text(
                    text =
                        stringResource(
                            R.string.quran_murottal_panel_queue,
                            uiState.queueDisplayNames.joinToString(" → "),
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = QuranMutedText,
                    modifier = Modifier.padding(top = SanguSantriSpacing.medium),
                )
            }
        }
    }
}

@Composable
private fun QuranMurottalPanelHeader(onDismiss: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.quran_murottal_panel_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.quran_murottal_panel_close),
            )
        }
    }
}

/**
 * The qari row. The design draws a chevron to a picker; myquran publishes exactly one recitation and
 * documents no reciter, so this renders the single value without a control that would open a list of
 * one. The name is the product owner's attribution ([com.sangusantri.app.data.audio.QuranAudioSource]),
 * not something the API states.
 */
@Composable
private fun QuranQariRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = SanguSantriDimensions.minimumTouchTarget),
    ) {
        Icon(imageVector = Icons.Outlined.RecordVoiceOver, contentDescription = null, tint = QuranPrimary)
        Text(
            text = stringResource(R.string.quran_murottal_panel_qari_label),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.quran_murottal_reciter),
            style = MaterialTheme.typography.bodyMedium,
            color = QuranMutedText,
        )
    }
}

@Composable
private fun QuranSpeedSelector(
    selected: QuranMurottalSpeed,
    onSelect: (QuranMurottalSpeed) -> Unit,
) {
    Column(modifier = Modifier.padding(top = SanguSantriSpacing.medium)) {
        Text(
            text = stringResource(R.string.quran_murottal_panel_speed_label),
            style = MaterialTheme.typography.bodyMedium,
            color = QuranMutedText,
        )
        Surface(
            color = QuranSurfaceHigh,
            border = BorderStroke(1.dp, QuranOutline),
            shape = RoundedCornerShape(SegmentedContainerRadius),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = SanguSantriSpacing.small),
        ) {
            Row(modifier = Modifier.padding(SegmentedContainerPadding)) {
                QuranMurottalSpeed.entries.forEach { speed ->
                    val isSelected = speed == selected
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier
                                .weight(1f)
                                .heightIn(min = SegmentHeight)
                                .background(
                                    color = if (isSelected) QuranPrimary else QuranSurfaceHigh,
                                    shape = RoundedCornerShape(SegmentRadius),
                                )
                                .clickable { onSelect(speed) },
                    ) {
                        Text(
                            text = stringResource(speed.labelRes()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) QuranOnPrimary else QuranMutedText,
                        )
                    }
                }
            }
        }
    }
}

private fun QuranMurottalSpeed.labelRes(): Int =
    when (this) {
        QuranMurottalSpeed.SLOW -> R.string.quran_murottal_speed_slow
        QuranMurottalSpeed.NORMAL -> R.string.quran_murottal_speed_normal
        QuranMurottalSpeed.FAST -> R.string.quran_murottal_speed_fast
    }

@Composable
private fun QuranMurottalSwitchRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = SanguSantriDimensions.minimumTouchTarget)
                .padding(top = SanguSantriSpacing.small),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = QuranMutedText)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = QuranOnPrimary,
                    checkedTrackColor = QuranPrimary,
                    uncheckedTrackColor = QuranSurfaceHigh,
                    uncheckedBorderColor = QuranOutline,
                ),
        )
    }
}

/**
 * The audio-download block. Every label names *audio* explicitly: the design review flagged that a
 * bare download control here reads as "download the surah text", which is already stored locally.
 */
@Composable
private fun QuranAudioDownloadBlock(
    uiState: QuranMurottalPanelUiState,
    actions: QuranMurottalPanelActions,
) {
    val download = uiState.download
    Surface(
        color = QuranPrimaryContainer,
        contentColor = QuranOnPrimaryContainer,
        shape = RoundedCornerShape(DownloadBlockRadius),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = SanguSantriSpacing.default),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
            modifier = Modifier.padding(SanguSantriSpacing.default),
        ) {
            Icon(
                imageVector = Icons.Outlined.Headphones,
                contentDescription = null,
                tint = QuranPrimary,
                modifier = Modifier.size(DownloadBlockIconSize),
            )
            Column(modifier = Modifier.weight(1f)) {
                if (download != null) {
                    QuranAudioDownloadingLabels(surahName = uiState.surahName, download = download)
                } else {
                    QuranAudioIdleLabels(uiState = uiState)
                }
            }
            QuranAudioDownloadAction(uiState = uiState, actions = actions)
        }
    }
}

@Composable
private fun QuranAudioDownloadingLabels(
    surahName: String,
    download: QuranAudioDownloadProgress,
) {
    val percent = (download.fraction * PERCENT).toInt()
    Text(
        text = stringResource(R.string.quran_audio_downloading_title, surahName),
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        // Before the first ayah lands there is no basis for a MB estimate, so the ayah count leads
        // instead of an invented size.
        text =
            if (download.estimatedTotalBytes > 0) {
                stringResource(
                    R.string.quran_audio_downloading_detail,
                    download.downloadedBytes.asAudioSize(),
                    download.estimatedTotalBytes.asAudioSize(),
                    percent,
                )
            } else {
                stringResource(
                    R.string.quran_audio_downloading_detail_counting,
                    download.completedAyat,
                    download.totalAyat,
                    percent,
                )
            },
        style = MaterialTheme.typography.bodySmall,
        color = QuranMutedText,
    )
    LinearProgressIndicator(
        progress = { download.fraction },
        color = QuranPrimary,
        trackColor = QuranOutline,
        gapSize = 0.dp,
        drawStopIndicator = {},
        modifier =
            Modifier
                .fillMaxWidth()
                .height(ProgressTrackHeight)
                .padding(top = SanguSantriSpacing.small),
    )
}

@Composable
private fun QuranAudioIdleLabels(uiState: QuranMurottalPanelUiState) {
    Text(
        text = stringResource(R.string.quran_audio_download_title),
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        text =
            when {
                uiState.isFullyStored ->
                    stringResource(R.string.quran_audio_stored_pill, stringResource(R.string.quran_murottal_reciter))

                uiState.storedAyahCount > 0 ->
                    stringResource(
                        R.string.quran_audio_stored_partial,
                        uiState.storedAyahCount,
                        uiState.ayatCount,
                    )

                else ->
                    stringResource(
                        R.string.quran_audio_download_subtitle,
                        stringResource(R.string.quran_murottal_reciter),
                    )
            },
        style = MaterialTheme.typography.bodySmall,
        color = QuranMutedText,
    )
}

@Composable
private fun QuranAudioDownloadAction(
    uiState: QuranMurottalPanelUiState,
    actions: QuranMurottalPanelActions,
) {
    when {
        uiState.download != null ->
            Text(
                text = stringResource(R.string.quran_audio_download_cancel),
                style = MaterialTheme.typography.labelLarge,
                color = QuranPrimary,
                modifier =
                    Modifier
                        .clickable(onClick = actions.onCancelDownload)
                        .padding(SanguSantriSpacing.small),
            )

        uiState.isFullyStored -> Unit

        else ->
            Surface(
                onClick = actions.onDownloadAudio,
                color = QuranPrimary,
                contentColor = QuranOnPrimary,
                shape = RoundedCornerShape(PillRadius),
            ) {
                Text(
                    text = stringResource(R.string.quran_audio_download_action),
                    style = MaterialTheme.typography.labelLarge,
                    modifier =
                        Modifier.padding(
                            horizontal = SanguSantriSpacing.default,
                            vertical = SanguSantriSpacing.small,
                        ),
                )
            }
    }
}

private val SegmentedContainerRadius = 16.dp
private val SegmentedContainerPadding = 3.dp
private val SegmentRadius = 13.dp
private val SegmentHeight = 34.dp
private val DownloadBlockRadius = 20.dp
private val DownloadBlockIconSize = 22.dp
private val ProgressTrackHeight = 2.dp
private val PillRadius = 17.dp
private const val PERCENT = 100
