package com.sangusantri.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.Content

/** Handoff §1.3 — exactly four tiles. Puasa lives inside Kalender and kiblat inside Jadwal Sholat,
 * which is what keeps this row from growing into the card wall the revamp set out to remove. */
@Composable
fun BerandaMenuTiles(
    actions: SerambiActions,
    showSholawat: Boolean,
    showNahwu: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MenuTileGap),
        modifier = modifier.fillMaxWidth(),
    ) {
        MenuTile(
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            label = stringResource(R.string.beranda_menu_quran),
            onClick = actions.onQuranClick,
            modifier = Modifier.weight(1f),
        )
        MenuTile(
            icon = Icons.Outlined.Favorite,
            label = stringResource(R.string.beranda_menu_sholawat),
            onClick = actions.onSholawatClick,
            enabled = showSholawat,
            modifier = Modifier.weight(1f),
        )
        MenuTile(
            icon = Icons.Outlined.CalendarMonth,
            label = stringResource(R.string.beranda_menu_kalender),
            onClick = actions.onHijriCalendarClick,
            modifier = Modifier.weight(1f),
        )
        MenuTile(
            icon = Icons.Outlined.School,
            label = stringResource(R.string.beranda_menu_nahwu),
            onClick = actions.onBelajarClick,
            enabled = showNahwu,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MenuTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(MenuTileSize)
                    .clip(RoundedCornerShape(MenuTileCornerRadius))
                    .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(MenuTileIconSize),
            )
        }
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = SanguSantriSpacing.small),
        )
    }
}

@Suppress("LongMethod")
/** Handoff §1.4 — one row that resumes the last session, with the session's own progress drawn as a
 * full-width 2dp track beneath it. */
@Composable
fun BerandaContinueRow(
    title: String,
    supporting: String,
    fraction: Float?,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ContinueRowGap),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onContinue)
                    .padding(bottom = SanguSantriSpacing.medium),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .size(ContinueIconTileSize)
                        .clip(RoundedCornerShape(ContinueIconTileCornerRadius))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(ContinueIconTileCornerRadius),
                        ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(21.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .height(ContinuePillHeight)
                        .clip(RoundedCornerShape(ContinuePillHeight / 2))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = SanguSantriSpacing.default),
            ) {
                Text(
                    text = stringResource(R.string.beranda_continue_action),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        if (fraction != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(ProgressTrackHeight)
                        .clip(RoundedCornerShape(ProgressTrackHeight))
                        .background(MaterialTheme.colorScheme.outline),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(fraction)
                            .height(ProgressTrackHeight)
                            .clip(RoundedCornerShape(ProgressTrackHeight))
                            .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

/** Handoff §1.5 — a horizontal scroller of curated amaliyah. No favourite icon: dropped in review. */
@Composable
fun BerandaAmaliyahScroller(
    items: List<Content>,
    onContentSelected: (String) -> Unit,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        contentPadding = contentPadding,
        modifier = modifier.fillMaxWidth(),
    ) {
        items(items = items, key = { it.id }) { content ->
            BerandaAmaliyahCard(content = content, onClick = { onContentSelected(content.id) })
        }
    }
}

@Composable
private fun BerandaAmaliyahCard(
    content: Content,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .width(AmaliyahCardWidth)
                .clip(RoundedCornerShape(AmaliyahCardCornerRadius))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(AmaliyahCardCornerRadius))
                .clickable(onClick = onClick)
                .padding(AmaliyahCardPadding),
    ) {
        Text(
            text = content.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = content.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = SanguSantriSpacing.extraSmall),
        )
        // The design's footer reads "37 langkah · offline"; the catalog row carries no step count,
        // so the category stands in and the row is simply dropped when there is none. No invented
        // step counts.
        content.category?.takeIf(String::isNotBlank)?.let { category ->
            Text(
                text = category,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = SanguSantriSpacing.medium),
            )
        }
    }
}

private val MenuTileGap = 6.dp
private val MenuTileSize = 56.dp
private val MenuTileCornerRadius = 20.dp
private val MenuTileIconSize = 25.dp
private val ContinueRowGap = 13.dp
private val ContinueIconTileSize = 42.dp
private val ContinueIconTileCornerRadius = 15.dp
private val ContinuePillHeight = 34.dp
private val ProgressTrackHeight = 2.dp
private val AmaliyahCardWidth = 205.dp
private val AmaliyahCardCornerRadius = 20.dp
private val AmaliyahCardPadding = 15.dp
