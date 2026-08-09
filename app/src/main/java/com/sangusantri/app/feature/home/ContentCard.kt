package com.sangusantri.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.sangusantri.app.core.designsystem.theme.SanguSantriElevation
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.Content

private val CardImageHeight = 120.dp

/**
 * Tappable catalog entry (FR-002, ADR 0015) — flat surface with a hairline border, no shadow.
 * Renders [Content.imageUrl] when present (via Coil,
 * [SanguSantriApplication][com.sangusantri.app.SanguSantriApplication]
 * supplies the network-capable `ImageLoader`); the card layout is otherwise unchanged if no image
 * exists yet for an item, since neither bundled amaliyah currently has one.
 */
@Composable
fun ContentCard(
    content: Content,
    onClick: (contentId: String) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    OutlinedCard(
        onClick = { onClick(content.id) },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(SanguSantriElevation.outlineWidth, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = SanguSantriElevation.flat),
    ) {
        Column {
            content.imageUrl?.takeIf { it.isNotBlank() && !compact }?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(CardImageHeight),
                )
            }
            ContentCardBody(content = content, compact = compact)
        }
    }
}

@Composable
private fun ContentCardBody(
    content: Content,
    compact: Boolean,
) {
    Row(
        modifier = Modifier.padding(SanguSantriSpacing.default),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            content.category?.takeIf { it.isNotBlank() }?.let { category ->
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(SanguSantriSpacing.extraSmall))
            }
            Text(text = content.title, style = MaterialTheme.typography.titleMedium)
            if (content.description.isNotBlank() && !compact) {
                Spacer(modifier = Modifier.height(SanguSantriSpacing.extraSmall))
                Text(
                    text = content.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// Development-only preview fixture — mirrors the bracketed placeholder convention used for
// non-production bundled content fixtures, never real amaliyah text.
private val previewContent =
    Content(
        id = "tahlil",
        title = "Tahlil",
        description = "Rangkaian bacaan Tahlil. FIXTURE PENGEMBANGAN — bukan konten produksi.",
        imageUrl = null,
        category = "Tahlil dan Doa",
        version = 1,
        order = 1,
        isActive = true,
        sourceName = "[FIXTURE]",
        sourceUrl = "[FIXTURE]",
    )

@PreviewLightDark
@Composable
private fun ContentCardPreview() {
    SanguSantriTheme {
        ContentCard(content = previewContent, onClick = {}, modifier = Modifier.padding(SanguSantriSpacing.default))
    }
}

@Preview(name = "No category")
@Composable
private fun ContentCardNoCategoryPreview() {
    SanguSantriTheme {
        ContentCard(
            content = previewContent.copy(category = null),
            onClick = {},
            modifier = Modifier.padding(SanguSantriSpacing.default),
        )
    }
}
