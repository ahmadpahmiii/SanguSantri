package com.sangusantri.app.feature.quran.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.RepeatOne
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOnPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranScrim
import com.sangusantri.app.core.designsystem.theme.QuranSurfaceHigh
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranAyatActionSheet(
    ayat: QuranReaderAyatUiModel,
    isBookmarked: Boolean,
    actions: QuranAyatActionSheetActions,
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
        QuranAyatActionSheetContent(ayat, isBookmarked, actions, onDismiss)
    }
}

@Composable
private fun QuranAyatActionSheetContent(
    ayat: QuranReaderAyatUiModel,
    isBookmarked: Boolean,
    actions: QuranAyatActionSheetActions,
    onDismiss: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(max = SanguSantriDimensions.quranSheetMaxHeight)
                .padding(horizontal = SanguSantriSpacing.extraSmall)
                .padding(bottom = SanguSantriSpacing.large),
    ) {
        QuranAyatActionSheetHeader(ayat, onDismiss)
        HorizontalDivider(
            color = QuranOutline,
            modifier = Modifier.padding(top = SanguSantriSpacing.medium),
        )
        QuranAyatPlaybackActions(actions)
        HorizontalDivider(color = QuranOutline)
        QuranSheetAction(
            icon = if (isBookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
            label =
                stringResource(
                    if (isBookmarked) R.string.quran_action_remove_bookmark else R.string.quran_action_add_bookmark,
                ),
            onClick = actions.onToggleBookmark,
        )
        QuranSheetAction(
            icon = Icons.Outlined.Description,
            label = stringResource(R.string.quran_action_open_tafsir),
            onClick = actions.onOpenTafsir,
        )
        QuranSheetAction(
            icon = Icons.Outlined.History,
            label = stringResource(R.string.quran_action_mark_last_read),
            onClick = actions.onMarkLastRead,
        )
        QuranSheetAction(
            icon = Icons.Outlined.Info,
            label = stringResource(R.string.quran_action_show_position),
            onClick = actions.onShowPosition,
        )
    }
}

/** Playback rows come first (`4b`): the primary action is a tint block, so the sheet reads as
 * "play from here" before the quieter bookmark/tafsir rows. */
@Composable
private fun QuranAyatPlaybackActions(actions: QuranAyatActionSheetActions) {
    QuranSheetAction(
        icon = Icons.Outlined.PlaylistPlay,
        label = stringResource(R.string.quran_action_play_from_here),
        onClick = actions.onPlayFromHere,
        style =
            QuranSheetActionStyle(
                detail = stringResource(R.string.quran_action_play_from_here_detail),
                highlighted = true,
            ),
    )
    QuranSheetAction(
        icon = Icons.Outlined.PlayArrow,
        label = stringResource(R.string.quran_action_play_single),
        onClick = actions.onPlaySingle,
        style = QuranSheetActionStyle(detail = stringResource(R.string.quran_action_play_single_detail)),
    )
    QuranSheetAction(
        icon = Icons.Outlined.RepeatOne,
        label = stringResource(R.string.quran_action_repeat_ayat),
        onClick = actions.onRepeatAyat,
        style = QuranSheetActionStyle(trailingChip = stringResource(R.string.quran_action_repeat_ayat_chip)),
    )
}

@Composable
private fun QuranAyatActionSheetHeader(
    ayat: QuranReaderAyatUiModel,
    onDismiss: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = SanguSantriSpacing.default),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(R.string.quran_action_sheet_title, ayat.surahName, ayat.ayatNumber),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.quran_action_sheet_context, ayat.juz, ayat.page),
                style = MaterialTheme.typography.bodyMedium,
                color = QuranMutedText,
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.quran_action_close),
            )
        }
    }
}

/**
 * The optional presentation the turn-4 play rows need, bundled so [QuranSheetAction] keeps the short
 * parameter list the four pre-existing rows already had.
 *
 * [detail] is the design's secondary line, [highlighted] renders the row as the tint block the
 * primary play action uses, and [trailingChip] carries the "3×" repeat count.
 */
private data class QuranSheetActionStyle(
    val detail: String? = null,
    val highlighted: Boolean = false,
    val trailingChip: String? = null,
)

@Composable
private fun QuranSheetAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    style: QuranSheetActionStyle = QuranSheetActionStyle(),
) {
    val (detail, highlighted, trailingChip) = style
    Surface(
        onClick = onClick,
        color = if (highlighted) QuranPrimaryContainer else QuranSurfaceHigh,
        contentColor = if (highlighted) QuranOnPrimaryContainer else QuranArabicText,
        shape = if (highlighted) RoundedCornerShape(HighlightedActionRadius) else RectangleShape,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = SanguSantriDimensions.minimumTouchTarget),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
            modifier =
                Modifier.padding(
                    horizontal = SanguSantriSpacing.large,
                    vertical = SanguSantriSpacing.medium,
                ),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = QuranPrimary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
                if (detail != null) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = QuranMutedText,
                    )
                }
            }
            if (trailingChip != null) {
                Surface(
                    color = QuranPrimaryContainer,
                    contentColor = QuranOnPrimaryContainer,
                    shape = RoundedCornerShape(ChipRadius),
                ) {
                    Text(
                        text = trailingChip,
                        style = MaterialTheme.typography.labelMedium,
                        modifier =
                            Modifier.padding(
                                horizontal = SanguSantriSpacing.medium,
                                vertical = SanguSantriSpacing.extraSmall,
                            ),
                    )
                }
            }
        }
    }
}

private val HighlightedActionRadius = 16.dp
private val ChipRadius = 13.dp
