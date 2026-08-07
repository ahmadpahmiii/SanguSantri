package com.sangusantri.app.feature.nahwuquiz.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

/** "Lanjutkan Kuis Belum Selesai" — design spec state 12, surfaced on both `Landing` (global,
 * across every package) and `Detail Paket` (that package's own progress). */
@Composable
fun NahwuQuizResumeCard(
    packageTitle: String,
    answeredCount: Int,
    totalCount: Int,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
            Text(text = packageTitle, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.nahwu_quiz_resume_card_progress, answeredCount, totalCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = SanguSantriSpacing.extraSmall, bottom = SanguSantriSpacing.small),
            )
            Button(onClick = onResumeClick) {
                Text(text = stringResource(R.string.nahwu_quiz_resume_card_action))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun NahwuQuizResumeCardPreview() {
    SanguSantriTheme {
        NahwuQuizResumeCard(
            packageTitle = "[FIXTURE] Nahwu Dasar",
            answeredCount = 3,
            totalCount = 6,
            onResumeClick = {},
            modifier = Modifier.padding(SanguSantriSpacing.default),
        )
    }
}
