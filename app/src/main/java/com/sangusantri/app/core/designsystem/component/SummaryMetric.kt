package com.sangusantri.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

/**
 * A single labelled number — the shared `Summary Metric` (`01-navigation-and-shared-components.md`),
 * used by Aktivitas' (`0.0.3`) streak and this-week sections. No card/border by default.
 */
@Composable
fun SummaryMetric(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    emphasis: SummaryMetricEmphasis = SummaryMetricEmphasis.PLAIN,
) {
    val containerModifier =
        if (emphasis == SummaryMetricEmphasis.HIGHLIGHTED) {
            modifier
                .background(MaterialTheme.colorScheme.primaryContainer, SanguSantriShapes.medium)
                .padding(SanguSantriSpacing.default)
        } else {
            modifier
        }
    Column(modifier = containerModifier, verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall)) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color =
                if (emphasis == SummaryMetricEmphasis.HIGHLIGHTED) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@PreviewLightDark
@Composable
private fun SummaryMetricPlainPreview() {
    SanguSantriTheme {
        SummaryMetric(value = "12", label = "Sesi tasbih")
    }
}

@PreviewLightDark
@Composable
private fun SummaryMetricHighlightedPreview() {
    SanguSantriTheme {
        SummaryMetric(value = "5 hari", label = "Streak saat ini", emphasis = SummaryMetricEmphasis.HIGHLIGHTED)
    }
}
