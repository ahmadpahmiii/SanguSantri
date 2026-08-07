package com.sangusantri.app.feature.nahwuquiz.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

/** "`n`/`total` selesai" plus a linear progress bar — shown on `Detail Paket` and the package
 * list card whenever a package has ever been opened (design spec state 3), hidden otherwise. */
@Composable
fun NahwuQuizProgressRow(
    answeredCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { if (totalCount == 0) 0f else answeredCount.toFloat() / totalCount },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.nahwu_quiz_progress_row, answeredCount, totalCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = SanguSantriSpacing.extraSmall),
        )
    }
}

@PreviewLightDark
@Composable
private fun NahwuQuizProgressRowPreview() {
    SanguSantriTheme {
        NahwuQuizProgressRow(answeredCount = 3, totalCount = 6, modifier = Modifier.padding(SanguSantriSpacing.default))
    }
}
