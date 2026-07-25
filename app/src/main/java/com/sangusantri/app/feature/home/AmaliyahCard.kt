package com.sangusantri.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sangusantri.app.core.designsystem.theme.SanguSantriElevation
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.Amaliyah

/** Tappable amaliyah catalogue entry (FR-002) — flat surface with a hairline border, no shadow. */
@Composable
fun AmaliyahCard(
    amaliyah: Amaliyah,
    onClick: (slug: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        onClick = { onClick(amaliyah.slug) },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(SanguSantriElevation.outlineWidth, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = SanguSantriElevation.flat),
    ) {
        Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
            Text(text = amaliyah.titleId, style = MaterialTheme.typography.titleMedium)
            val description = amaliyah.descriptionId
            if (!description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(SanguSantriSpacing.extraSmall))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// Development-only preview fixture — mirrors the bracketed placeholder convention used by the
// bundled non-production seed content (app/src/main/assets/content), never real amaliyah text.
private val previewAmaliyah =
    Amaliyah(
        id = "tahlil",
        slug = "tahlil",
        titleId = "Tahlil",
        titleAr = "[FIXTURE-AR] Tahlil",
        descriptionId = "Rangkaian bacaan Tahlil. FIXTURE PENGEMBANGAN — bukan konten produksi.",
        descriptionAr = null,
        category = "AMALIYAH",
    )

@PreviewLightDark
@Composable
private fun AmaliyahCardPreview() {
    SanguSantriTheme {
        AmaliyahCard(amaliyah = previewAmaliyah, onClick = {}, modifier = Modifier.padding(SanguSantriSpacing.default))
    }
}

@Preview(name = "No description")
@Composable
private fun AmaliyahCardNoDescriptionPreview() {
    SanguSantriTheme {
        AmaliyahCard(
            amaliyah = previewAmaliyah.copy(descriptionId = null),
            onClick = {},
            modifier = Modifier.padding(SanguSantriSpacing.default),
        )
    }
}
