package com.sangusantri.app.feature.quran.murottal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
 * The bottom player bar shared by the translation reader (`4a`), its loading state (`4e`), and the
 * mushaf page (`4f`).
 *
 * The design removed the progress bar that used to sit above this bar in review — position inside
 * the ayah is shown in the ayah itself, not here.
 */
@Composable
fun QuranMiniPlayerBar(
    state: QuranMurottalState,
    actions: QuranMiniPlayerActions,
    modifier: Modifier = Modifier,
) {
    if (!state.isActive) return
    Surface(color = QuranSurface, contentColor = QuranArabicText, modifier = modifier.fillMaxWidth()) {
        Column {
            HorizontalDivider(color = QuranOutline)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(PlayerBarHeight)
                        .padding(horizontal = SanguSantriSpacing.default),
            ) {
                QuranMiniPlayerLeading(state = state, actions = actions)
                QuranMiniPlayerLabels(
                    state = state,
                    onOpenPanel = actions.onOpenPanel,
                    modifier = Modifier.weight(1f),
                )
                QuranMiniPlayerTrailing(state = state, actions = actions)
            }
        }
    }
}

@Composable
private fun QuranMiniPlayerLeading(
    state: QuranMurottalState,
    actions: QuranMiniPlayerActions,
) {
    when (state.status) {
        QuranMurottalStatus.PREPARING ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(PlayerButtonSize),
            ) {
                CircularProgressIndicator(color = QuranPrimary, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            }

        QuranMurottalStatus.ERROR ->
            QuranPlayerCircleButton(
                onClick = actions.onRetry,
                contentDescription = stringResource(R.string.quran_murottal_error_action),
                icon = Icons.Filled.Refresh,
            )

        else ->
            QuranPlayerCircleButton(
                onClick = actions.onTogglePlayPause,
                contentDescription = stringResource(R.string.quran_murottal_play_pause),
                icon =
                    if (state.status == QuranMurottalStatus.PLAYING) {
                        Icons.Filled.Pause
                    } else {
                        Icons.Filled.PlayArrow
                    },
            )
    }
}

@Composable
private fun QuranPlayerCircleButton(
    onClick: () -> Unit,
    contentDescription: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(PlayerButtonSize)
                .background(QuranPrimary, CircleShape)
                .clickable(onClick = onClick),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = QuranOnPrimary)
    }
}

/** Title and secondary line. Tapping this area opens the murottal panel (`4c`). */
@Composable
private fun QuranMiniPlayerLabels(
    state: QuranMurottalState,
    onOpenPanel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ayah = state.ayahNumber ?: return
    val title =
        when (state.status) {
            QuranMurottalStatus.PREPARING ->
                stringResource(R.string.quran_murottal_preparing_title, state.surahName, ayah)

            QuranMurottalStatus.ERROR -> stringResource(R.string.quran_murottal_error)
            else -> stringResource(R.string.quran_murottal_ayat_label, state.surahName, ayah)
        }
    val detail =
        when {
            state.status == QuranMurottalStatus.PREPARING && state.isDownloading ->
                stringResource(R.string.quran_murottal_preparing_downloading)

            state.status == QuranMurottalStatus.PREPARING -> null
            state.status == QuranMurottalStatus.ERROR -> null
            state.nextAyahNumber != null ->
                stringResource(R.string.quran_murottal_continue_to_ayat, state.nextAyahNumber)

            // Last ayah of the surah, but cross-surah continuation will carry on into the next one.
            state.queuedSurahNames.isNotEmpty() ->
                stringResource(R.string.quran_murottal_continue_to_surah, state.queuedSurahNames.first())

            else -> stringResource(R.string.quran_murottal_end_of_surah)
        }

    Column(modifier = modifier.clickable(onClick = onOpenPanel)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = QuranArabicText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (detail != null) {
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = QuranMutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun QuranMiniPlayerTrailing(
    state: QuranMurottalState,
    actions: QuranMiniPlayerActions,
) {
    if (state.status == QuranMurottalStatus.PREPARING) {
        Text(
            text = stringResource(R.string.quran_murottal_cancel),
            style = MaterialTheme.typography.labelLarge,
            color = QuranPrimary,
            modifier =
                Modifier
                    .clickable(onClick = actions.onClose)
                    .padding(SanguSantriSpacing.small),
        )
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = actions.onSkipPrevious) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = stringResource(R.string.quran_murottal_skip_previous),
                tint = QuranArabicText,
            )
        }
        IconButton(onClick = actions.onSkipNext) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = stringResource(R.string.quran_murottal_skip_next),
                tint = QuranArabicText,
            )
        }
        IconButton(onClick = actions.onClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.quran_murottal_close),
                tint = QuranMutedText,
            )
        }
    }
}

private val PlayerBarHeight = 56.dp
private val PlayerButtonSize = 42.dp
