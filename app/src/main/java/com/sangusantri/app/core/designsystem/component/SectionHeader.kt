package com.sangusantri.app.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

/**
 * A section title with an optional "Lihat semua" action — the shared `Section Header`
 * (`01-navigation-and-shared-components.md`), first built for Aktivitas (`0.0.3`). Never wrapped
 * in a `Card` (`docs/design/DESIGN_SYSTEM.md`'s anti-pattern rule) — sits directly on the screen
 * background.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SanguSantriSpacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SectionHeaderWithActionPreview() {
    SanguSantriTheme {
        SectionHeader(title = "Riwayat penyelesaian amaliyah", actionLabel = "Lihat semua", onActionClick = {})
    }
}

@PreviewLightDark
@Composable
private fun SectionHeaderPlainPreview() {
    SanguSantriTheme {
        SectionHeader(title = "Ringkasan streak")
    }
}
