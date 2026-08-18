package com.sangusantri.app.feature.quran.hub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOnPrimary
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranSurface
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.QuranMurottalState
import com.sangusantri.app.domain.model.QuranMurottalStatus

/**
 * The hub's murottal surfaces (design frame `4d`) — split out of `QuranHubScreen.kt`/
 * `QuranHubTabContent.kt` for the same reason those two were split from each other: to keep every
 * hub file under the project's per-file function-count threshold.
 */
@Composable
internal fun QuranNowPlayingPanel(
    state: QuranMurottalState,
    nextSurahName: String?,
    actions: QuranHubActions,
    onOpenReader: () -> Unit,
) {
    val ayah = state.ayahNumber ?: return
    Surface(
        color = QuranSurface,
        contentColor = QuranArabicText,
        border = BorderStroke(1.dp, QuranOutline),
        shape = MaterialTheme.shapes.large,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = SanguSantriSpacing.small),
    ) {
        Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
            Text(
                text = stringResource(R.string.quran_hub_now_playing_label).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = QuranPrimary,
                modifier = Modifier.clickable(onClick = onOpenReader),
            )
            QuranNowPlayingTransportRow(
                label = stringResource(R.string.quran_murottal_ayat_label, state.surahName, ayah),
                isPlaying = state.status == QuranMurottalStatus.PLAYING,
                actions = actions,
                onOpenReader = onOpenReader,
            )
            LinearProgressIndicator(
                progress = { state.positionFraction },
                color = QuranPrimary,
                trackColor = QuranOutline,
                gapSize = 0.dp,
                drawStopIndicator = {},
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(AudioProgressHeight),
            )
            Text(
                text = quranNowPlayingMeta(nextSurahName),
                style = MaterialTheme.typography.bodySmall,
                color = QuranMutedText,
                modifier =
                    Modifier
                        .padding(top = SanguSantriSpacing.small)
                        .clickable(onClick = onOpenReader),
            )
        }
    }
}

/**
 * The design's meta line read "Alafasy · antrean 3 surah". A count cannot be honest here: with
 * "Lanjut otomatis antarsurah" on, playback runs to the end of the mushaf, and the "3" only ever
 * reflected how many surahs the player happened to name ahead. This states the reciter and the surah
 * playback will actually continue into, which is the fact a reader can act on.
 */
@Composable
private fun quranNowPlayingMeta(nextSurahName: String?): String {
    val reciter = stringResource(R.string.quran_murottal_reciter)
    return if (nextSurahName == null) {
        stringResource(R.string.quran_hub_now_playing_meta_single, reciter)
    } else {
        stringResource(R.string.quran_hub_now_playing_meta_next, reciter, nextSurahName)
    }
}

@Composable
private fun QuranNowPlayingTransportRow(
    label: String,
    isPlaying: Boolean,
    actions: QuranHubActions,
    onOpenReader: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = SanguSantriSpacing.small),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier =
                Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenReader),
        )
        IconButton(onClick = actions.onSkipPrevious) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = stringResource(R.string.quran_murottal_skip_previous),
                tint = QuranArabicText,
            )
        }
        IconButton(onClick = actions.onTogglePlayPause) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = stringResource(R.string.quran_murottal_play_pause),
                tint = QuranPrimary,
            )
        }
        IconButton(onClick = actions.onSkipNext) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = stringResource(R.string.quran_murottal_skip_next),
                tint = QuranArabicText,
            )
        }
    }
}

/**
 * A surah row's audio affordance (`4d`).
 *
 * Every state names audio explicitly — `headphones` with "Unduh", a `downloading` glyph over a 2dp
 * track, or `check_circle` with "tersimpan" — because the design review found that a bare download
 * icon here reads as "download the surah text", which is already stored locally.
 */
@Suppress("LongParameterList")
@Composable
internal fun QuranSurahAudioControl(
    surahName: String,
    audioState: QuranSurahAudioState,
    downloadFraction: Float?,
    onDownloadAudio: () -> Unit,
    onCancelDownload: () -> Unit,
    onPlayAudio: () -> Unit,
) {
    when (audioState) {
        QuranSurahAudioState.DOWNLOADING ->
            QuranSurahAudioDownloading(fraction = downloadFraction ?: 0f, onCancel = onCancelDownload)

        QuranSurahAudioState.STORED ->
            QuranSurahAudioPlayPill(surahName = surahName, onPlayAudio = onPlayAudio)

        QuranSurahAudioState.NONE, QuranSurahAudioState.PARTIAL ->
            QuranSurahAudioDownloadPill(surahName = surahName, onDownloadAudio = onDownloadAudio)
    }
}

/**
 * A fully stored surah's control: `play_arrow` "Putar", which starts the recitation from ayat 1
 * without opening the reader.
 *
 * It replaces the design's bare `check_circle`, which said "stored" but could not be acted on — and
 * "Putar" only ever appears once every ayah is present, so it still carries that meaning. Filled
 * primary, against the download state's outlined pill, so a glance down the list separates
 * "ready to play" from "needs downloading". The stored fact stays explicit for screen readers via
 * the content description.
 */
@Composable
private fun QuranSurahAudioPlayPill(
    surahName: String,
    onPlayAudio: () -> Unit,
) {
    val pillLabel = stringResource(R.string.quran_audio_play_surah_content_description, surahName)
    Surface(
        onClick = onPlayAudio,
        color = QuranPrimary,
        contentColor = QuranOnPrimary,
        shape = RoundedCornerShape(AudioPillRadius),
        modifier = Modifier.semantics { contentDescription = pillLabel },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall),
            modifier =
                Modifier.padding(
                    horizontal = SanguSantriSpacing.small,
                    vertical = SanguSantriSpacing.extraSmall,
                ),
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(AudioPillIconSize),
            )
            Text(
                text = stringResource(R.string.quran_audio_play_surah),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun QuranSurahAudioDownloading(
    fraction: Float,
    onCancel: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .width(AudioControlWidth)
                .clickable(onClick = onCancel),
    ) {
        Icon(
            imageVector = Icons.Outlined.Downloading,
            contentDescription = stringResource(R.string.quran_audio_download_cancel),
            tint = QuranPrimary,
            modifier = Modifier.size(AudioControlIconSize),
        )
        LinearProgressIndicator(
            progress = { fraction },
            color = QuranPrimary,
            trackColor = QuranOutline,
            gapSize = 0.dp,
            drawStopIndicator = {},
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(AudioProgressHeight)
                    .padding(top = SanguSantriSpacing.extraSmall),
        )
    }
}

@Composable
private fun QuranSurahAudioDownloadPill(
    surahName: String,
    onDownloadAudio: () -> Unit,
) {
    // "Unduh" alone is ambiguous read aloud out of context, so the pill carries an explicit
    // "download the audio of surah X" label.
    val pillLabel = stringResource(R.string.quran_audio_download_surah_content_description, surahName)
    Surface(
        onClick = onDownloadAudio,
        color = Color.Transparent,
        contentColor = QuranPrimary,
        border = BorderStroke(1.dp, QuranOutline),
        shape = RoundedCornerShape(AudioPillRadius),
        modifier = Modifier.semantics { contentDescription = pillLabel },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall),
            modifier =
                Modifier.padding(
                    horizontal = SanguSantriSpacing.small,
                    vertical = SanguSantriSpacing.extraSmall,
                ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Headphones,
                contentDescription = null,
                modifier = Modifier.size(AudioPillIconSize),
            )
            Text(
                text = stringResource(R.string.quran_audio_download_action),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

private val AudioControlWidth = 44.dp
private val AudioControlIconSize = 22.dp
private val AudioProgressHeight = 2.dp
private val AudioPillRadius = 17.dp
private val AudioPillIconSize = 16.dp
