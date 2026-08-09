package com.sangusantri.app.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriElevation
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.ReaderMode

@Composable
fun SerambiSearchEntry(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SanguSantriDimensions.minimumTouchTarget),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SanguSantriSpacing.default, vertical = SanguSantriSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.serambi_search_placeholder),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun SerambiResumeCard(
    item: SerambiResumeItem,
    actions: SerambiActions,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = item,
        modifier = modifier.animateContentSize(),
        label = "serambi_resume_item",
    ) { target ->
        val progressTarget = target.progress?.fraction ?: 0f
        val progress by animateFloatAsState(
            targetValue = progressTarget,
            animationSpec = tween(PROGRESS_ANIMATION_MILLIS),
            label = "serambi_resume_progress",
        )
        ResumeCardSurface(
            item = target,
            progress = progress,
            onClick = target.resumeAction(actions),
            onDismiss = { onDismiss(target.dismissFingerprint) },
        )
    }
}

@Composable
private fun ResumeCardSurface(
    item: SerambiResumeItem,
    progress: Float,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = SanguSantriElevation.flat),
    ) {
        Column(
            modifier = Modifier.padding(SanguSantriSpacing.default),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.serambi_resume_eyebrow),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.serambi_resume_dismiss_description),
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.resumeTitle(), style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(SanguSantriSpacing.extraSmall))
                    Text(
                        text = item.resumeProgressText(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ResumeActionPill()
            }
            if (item.progress != null) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PROGRESS_HEIGHT),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ResumeActionPill() {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = SanguSantriSpacing.medium,
                    vertical = SanguSantriSpacing.small,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(SanguSantriSpacing.extraSmall))
            Text(
                text = stringResource(R.string.serambi_resume_action),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun SerambiResumeItem.resumeTitle(): String =
    when (this) {
        is SerambiResumeItem.Amaliyah -> title
        is SerambiResumeItem.Quran -> stringResource(R.string.serambi_resume_quran_title)
        is SerambiResumeItem.Tasbih ->
            sessionName?.takeIf(String::isNotBlank)
                ?: stringResource(R.string.serambi_resume_tasbih_title)
    }

@Composable
private fun SerambiResumeItem.resumeProgressText(): String =
    when (this) {
        is SerambiResumeItem.Amaliyah ->
            stringResource(
                if (mode == ReaderMode.GUIDED) {
                    R.string.serambi_resume_amaliyah_guided_progress
                } else {
                    R.string.serambi_resume_amaliyah_full_progress
                },
                current,
                total,
            )

        is SerambiResumeItem.Quran ->
            stringResource(R.string.serambi_resume_quran_progress, surahName, ayatNumber, totalAyat)

        is SerambiResumeItem.Tasbih ->
            targetCount?.let {
                stringResource(R.string.serambi_resume_tasbih_target_progress, currentCount, it)
            } ?: stringResource(R.string.serambi_resume_tasbih_unlimited_progress, currentCount)
    }

private fun SerambiResumeItem.resumeAction(actions: SerambiActions): () -> Unit =
    when (this) {
        is SerambiResumeItem.Amaliyah -> ({ actions.onContinueAmaliyah(contentId, mode) })
        is SerambiResumeItem.Quran -> ({ actions.onContinueQuran(surahNumber, ayatNumber) })
        is SerambiResumeItem.Tasbih -> actions.onContinueTasbih
    }

private val PROGRESS_HEIGHT = 6.dp
private const val PROGRESS_ANIMATION_MILLIS = 450
