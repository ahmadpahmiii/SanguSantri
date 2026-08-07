package com.sangusantri.app.feature.nahwuquiz.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriElevation
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.NahwuQuizPackage
import com.sangusantri.app.domain.model.NahwuQuizPackageStatus
import com.sangusantri.app.domain.model.NahwuQuizPackageSummary

/** One `Daftar Paket` list card (design spec state 2) — flat surface with a hairline border,
 * matching `ContentCard`'s elevation policy. */
@Composable
fun NahwuQuizPackageCard(
    summary: NahwuQuizPackageSummary,
    onClick: (packageId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        onClick = { onClick(summary.quizPackage.id) },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(SanguSantriElevation.outlineWidth, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = SanguSantriElevation.flat),
    ) {
        Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
            NahwuQuizStatusChip(status = summary.status)
            Spacer(modifier = Modifier.height(SanguSantriSpacing.extraSmall))
            Text(text = summary.quizPackage.title, style = MaterialTheme.typography.titleMedium)
            if (summary.quizPackage.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(SanguSantriSpacing.extraSmall))
                Text(
                    text = summary.quizPackage.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val answeredCount = summary.answeredCount
            if (answeredCount != null) {
                Spacer(modifier = Modifier.height(SanguSantriSpacing.small))
                NahwuQuizProgressRow(answeredCount = answeredCount, totalCount = summary.quizPackage.questionCount)
            }
        }
    }
}

@Composable
private fun NahwuQuizStatusChip(status: NahwuQuizPackageStatus) {
    val (labelRes, containerColor, contentColor) =
        when (status) {
            NahwuQuizPackageStatus.NEW ->
                Triple(
                    R.string.nahwu_quiz_status_new,
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                )

            NahwuQuizPackageStatus.IN_PROGRESS ->
                Triple(
                    R.string.nahwu_quiz_status_in_progress,
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.onTertiaryContainer,
                )

            NahwuQuizPackageStatus.COMPLETED ->
                Triple(
                    R.string.nahwu_quiz_status_completed,
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                )

            NahwuQuizPackageStatus.UNAVAILABLE ->
                Triple(
                    R.string.nahwu_quiz_status_unavailable,
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.outline,
                )
        }
    Surface(
        shape = RoundedCornerShape(CHIP_CORNER_RADIUS),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = SanguSantriElevation.flat,
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            modifier =
                Modifier.padding(
                    horizontal = SanguSantriSpacing.small,
                    vertical = SanguSantriSpacing.extraSmall,
                ),
        )
    }
}

private val CHIP_CORNER_RADIUS = 8.dp

// Development-only preview fixtures — bracketed placeholders, never real quiz content.
private val previewSummaries =
    listOf(
        NahwuQuizPackageSummary(
            quizPackage =
                NahwuQuizPackage(
                    id = "new",
                    title = "[FIXTURE] Nahwu Dasar",
                    description = "[FIXTURE] Paket contoh pengembangan.",
                    order = 1,
                    isActive = true,
                    questionCount = 6,
                ),
            status = NahwuQuizPackageStatus.NEW,
            answeredCount = null,
        ),
        NahwuQuizPackageSummary(
            quizPackage =
                NahwuQuizPackage(
                    id = "in-progress",
                    title = "[FIXTURE] Nahwu I'rab",
                    description = "[FIXTURE] Paket contoh pengembangan.",
                    order = 2,
                    isActive = true,
                    questionCount = 10,
                ),
            status = NahwuQuizPackageStatus.IN_PROGRESS,
            answeredCount = 4,
        ),
        NahwuQuizPackageSummary(
            quizPackage =
                NahwuQuizPackage(
                    id = "completed",
                    title = "[FIXTURE] Nahwu Kalimat",
                    description = "[FIXTURE] Paket contoh pengembangan.",
                    order = 3,
                    isActive = true,
                    questionCount = 8,
                ),
            status = NahwuQuizPackageStatus.COMPLETED,
            answeredCount = 8,
        ),
        NahwuQuizPackageSummary(
            quizPackage =
                NahwuQuizPackage(
                    id = "unavailable",
                    title = "[FIXTURE] Nahwu Lanjutan",
                    description = "[FIXTURE] Paket contoh pengembangan — belum memiliki soal.",
                    order = 4,
                    isActive = true,
                    questionCount = 0,
                ),
            status = NahwuQuizPackageStatus.UNAVAILABLE,
            answeredCount = null,
        ),
    )

@PreviewLightDark
@Composable
private fun NahwuQuizPackageCardPreview() {
    SanguSantriTheme {
        Column {
            previewSummaries.forEach { summary ->
                NahwuQuizPackageCard(
                    summary = summary,
                    onClick = {},
                    modifier = Modifier.padding(SanguSantriSpacing.default),
                )
            }
        }
    }
}

@Preview(name = "New, no progress")
@Composable
private fun NahwuQuizPackageCardNewPreview() {
    SanguSantriTheme {
        NahwuQuizPackageCard(
            summary = previewSummaries[0],
            onClick = {},
            modifier = Modifier.padding(SanguSantriSpacing.default),
        )
    }
}
