package com.sangusantri.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriElevation
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SerambiMainFeatures(
    showAmaliyah: Boolean,
    actions: SerambiActions,
    modifier: Modifier = Modifier,
) {
    val fontScale = LocalDensity.current.fontScale
    val features =
        buildList {
            add(
                MainFeatureSpec(
                    title = stringResource(R.string.serambi_quran_feature_title),
                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                    onClick = actions.onQuranClick,
                ),
            )
            if (showAmaliyah) {
                add(
                    MainFeatureSpec(
                        title = stringResource(R.string.serambi_amaliyah_feature_title),
                        icon = Icons.Outlined.AutoStories,
                        onClick = actions.onExploreClick,
                    ),
                )
            }
            add(
                MainFeatureSpec(
                    title = stringResource(R.string.serambi_hijri_calendar_title),
                    icon = Icons.Outlined.CalendarMonth,
                    onClick = actions.onHijriCalendarClick,
                ),
            )
        }
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        maxItemsInEachRow = MAIN_FEATURE_MAX_COLUMNS,
    ) {
        features.forEach { feature ->
            SerambiMainFeatureTile(
                title = feature.title,
                icon = feature.icon,
                badge = feature.badge,
                onClick = feature.onClick,
                modifier = featureCell(fontScale),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SerambiSupportingFeatures(
    reminderDescription: String,
    showNahwuQuiz: Boolean,
    nahwuDescription: String,
    actions: SerambiActions,
    modifier: Modifier = Modifier,
) {
    val fontScale = LocalDensity.current.fontScale
    val features =
        buildList {
            add(
                SupportingFeatureSpec(
                    title = stringResource(R.string.serambi_reminder_feature_title),
                    description = reminderDescription,
                    icon = Icons.Outlined.NotificationsActive,
                    onClick = actions.onPengingatClick,
                ),
            )
            if (showNahwuQuiz) {
                add(
                    SupportingFeatureSpec(
                        title = stringResource(R.string.serambi_belajar_card_title),
                        description = nahwuDescription,
                        icon = Icons.Outlined.Quiz,
                        onClick = actions.onBelajarClick,
                    ),
                )
            }
        }
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        maxItemsInEachRow = SUPPORTING_FEATURE_MAX_COLUMNS,
    ) {
        features.forEach { feature ->
            SerambiSupportingMenuItem(
                title = feature.title,
                description = feature.description,
                icon = feature.icon,
                onClick = feature.onClick,
                modifier = supportingCell(fontScale),
            )
        }
    }
}

@Composable
private fun SerambiMainFeatureTile(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = MAIN_FEATURE_TILE_MIN_HEIGHT),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(SanguSantriElevation.outlineWidth, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = SanguSantriElevation.flat),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(SanguSantriSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
        ) {
            Surface(
                modifier = Modifier.size(FEATURE_ICON_CONTAINER_SIZE),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(SanguSantriSpacing.small),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            badge?.let { FeatureBadge(text = it) }
        }
    }
}

@Composable
private fun SerambiSupportingMenuItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = SUPPORTING_MENU_MIN_HEIGHT),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = SanguSantriElevation.flat),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(SanguSantriSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(SUPPORTING_ICON_SIZE),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun FeatureBadge(text: String) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = SanguSantriSpacing.small, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
private fun FlowRowScope.featureCell(fontScale: Float): Modifier =
    Modifier
        .weight(1f)
        .widthIn(
            min = SanguSantriDimensions.dashboardMainFeatureMinCellWidth * fontScale.coerceIn(1f, 2f),
        )

@OptIn(ExperimentalLayoutApi::class)
private fun FlowRowScope.supportingCell(fontScale: Float): Modifier =
    Modifier
        .weight(1f)
        .widthIn(
            min = SanguSantriDimensions.dashboardSupportingMinCellWidth * fontScale.coerceIn(1f, 2f),
        )

private data class MainFeatureSpec(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val badge: String? = null,
)

private data class SupportingFeatureSpec(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

private val MAIN_FEATURE_TILE_MIN_HEIGHT = 112.dp
private val FEATURE_ICON_CONTAINER_SIZE = 44.dp
private val SUPPORTING_MENU_MIN_HEIGHT = 72.dp
private val SUPPORTING_ICON_SIZE = 32.dp
private const val MAIN_FEATURE_MAX_COLUMNS = 3
private const val SUPPORTING_FEATURE_MAX_COLUMNS = 2
