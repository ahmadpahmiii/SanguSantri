package com.sangusantri.app.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sangusantri.app.core.designsystem.icon.TasbihIcon
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

/**
 * A plain, uncarded list row — the shared `Activity Row` (`01-navigation-and-shared-components.md`),
 * used by Aktivitas' (`0.0.3`) amaliyah-completion and tasbih-history sections, and their "Lihat
 * semua" detail screens. [content] is pre-formatted by the caller — this component stays
 * presentational, with no knowledge of the underlying domain models.
 */
@Composable
fun ActivityRow(
    kind: ActivityRowKind,
    content: ActivityRowContent,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = SanguSantriSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (kind) {
                ActivityRowKind.AMALIYAH ->
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                ActivityRowKind.TASBIH ->
                    TasbihIcon(filled = true, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = content.primaryText, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = content.secondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = content.trailingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        }
    }
}

@PreviewLightDark
@Composable
private fun ActivityRowAmaliyahPreview() {
    SanguSantriTheme {
        ActivityRow(
            kind = ActivityRowKind.AMALIYAH,
            content =
                ActivityRowContent(
                    primaryText = "Tahlil",
                    secondaryText = "Versi 2 · 8 menit",
                    trailingText = "14:32",
                ),
        )
    }
}

@PreviewLightDark
@Composable
private fun ActivityRowTasbihPreview() {
    SanguSantriTheme {
        ActivityRow(
            kind = ActivityRowKind.TASBIH,
            content =
                ActivityRowContent(
                    primaryText = "Tahlil malam Jumat",
                    secondaryText = "Target 33 · Hitungan akhir 33",
                    trailingText = "20:10 · 5 menit",
                ),
        )
    }
}
