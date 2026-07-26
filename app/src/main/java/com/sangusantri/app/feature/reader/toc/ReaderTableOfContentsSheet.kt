package com.sangusantri.app.feature.reader.toc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.AmaliyahStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val SheetTopCornerRadius = 28.dp
private val MinRowTouchTarget = 48.dp

/**
 * Reader Table of Contents (FR-017, `docs/design/FIGMA_HANDOFF.md` node `16:148`) — a modal bottom
 * sheet listing logical reading sections and their step ranges, reused by both Full Reader and
 * Guided Reader. Jumping to a section never marks skipped content complete; it only changes
 * position/current step.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTableOfContentsSheet(
    sections: List<TocSection>,
    currentSectionStepId: String?,
    onSectionSelected: (stepId: String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = SheetTopCornerRadius, topEnd = SheetTopCornerRadius),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SanguSantriSpacing.default)
                    .padding(bottom = SanguSantriSpacing.default)
                    .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
        ) {
            Text(
                text = stringResource(R.string.reader_toc_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.reader_toc_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            sections.forEach { section ->
                TocItemRow(
                    section = section,
                    isCurrent = section.stepId == currentSectionStepId,
                    onClick = { onSectionSelected(section.stepId) },
                )
            }
        }
    }
}

@Composable
private fun TocItemRow(
    section: TocSection,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    val rangeText =
        if (section.startPosition == section.endPosition) {
            section.startPosition.toString()
        } else {
            "${section.startPosition}–${section.endPosition}"
        }
    val stateText = stringResource(R.string.reader_toc_item_state, rangeText)

    Surface(
        onClick = onClick,
        shape = SanguSantriShapes.medium,
        color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = MinRowTouchTarget)
                .semantics { stateDescription = stateText },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SanguSantriSpacing.default, vertical = SanguSantriSpacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = section.titleId,
                style = MaterialTheme.typography.bodyLarge,
                color =
                    if (isCurrent) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
            )
            Text(
                text = rangeText,
                style = MaterialTheme.typography.labelLarge,
                color =
                    if (isCurrent) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
    }
}

/**
 * Full Reader's Table of Contents entry point: derives sections from [steps], scrolls
 * [listState] to the selected section (which naturally re-triggers the reader's existing
 * scroll-position persistence — no separate jump action needed for the Full Reader).
 */
@Composable
fun ReaderTableOfContentsOverlay(
    steps: List<AmaliyahStep>,
    currentItemIndex: Int,
    listState: LazyListState,
    coroutineScope: CoroutineScope,
    onDismiss: () -> Unit,
) {
    val sections = steps.toTocSections()
    val currentPosition = steps.getOrNull(currentItemIndex)?.position ?: 1
    ReaderTableOfContentsSheet(
        sections = sections,
        currentSectionStepId = sections.sectionContaining(currentPosition)?.stepId,
        onSectionSelected = { stepId ->
            onDismiss()
            val targetIndex = steps.indexOfFirst { it.id == stepId }
            if (targetIndex >= 0) {
                coroutineScope.launch { listState.animateScrollToItem(targetIndex) }
            }
        },
        onDismiss = onDismiss,
    )
}

// Development-only preview fixtures — bracketed placeholders, never real amaliyah text.
private val previewSections =
    listOf(
        TocSection(stepId = "s1", titleId = "[FIXTURE] Pembukaan", startPosition = 1, endPosition = 4),
        TocSection(stepId = "s2", titleId = "[FIXTURE] Surat Al-Ikhlas", startPosition = 5, endPosition = 5),
        TocSection(stepId = "s3", titleId = "[FIXTURE] Al-Falaq & An-Nas", startPosition = 6, endPosition = 7),
        TocSection(stepId = "s4", titleId = "[FIXTURE] Tahlil", startPosition = 8, endPosition = 42),
        TocSection(stepId = "s5", titleId = "[FIXTURE] Doa Tahlil", startPosition = 43, endPosition = 59),
    )

@PreviewLightDark
@Composable
private fun ReaderTableOfContentsSheetPreview() {
    SanguSantriTheme {
        Column(
            modifier =
                Modifier
                    .padding(SanguSantriSpacing.default)
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
        ) {
            Text(text = stringResource(R.string.reader_toc_title), style = MaterialTheme.typography.titleLarge)
            previewSections.forEach { section ->
                TocItemRow(section = section, isCurrent = section.stepId == "s2", onClick = {})
            }
        }
    }
}
