package com.sangusantri.app.feature.nahwuquiz.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sangusantri.app.core.designsystem.theme.SanguSantriElevation
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.NahwuQuizOption
import com.sangusantri.app.domain.model.NahwuQuizOptionKey

@Composable
fun NahwuQuizAnswerOption(
    option: NahwuQuizOption,
    state: NahwuQuizAnswerOptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val borderColor =
        when (state) {
            NahwuQuizAnswerOptionState.DEFAULT -> MaterialTheme.colorScheme.outlineVariant
            NahwuQuizAnswerOptionState.SELECTED -> MaterialTheme.colorScheme.primary
            NahwuQuizAnswerOptionState.CORRECT -> MaterialTheme.colorScheme.primary
            NahwuQuizAnswerOptionState.INCORRECT -> MaterialTheme.colorScheme.error
        }
    val containerColor =
        when (state) {
            NahwuQuizAnswerOptionState.DEFAULT -> MaterialTheme.colorScheme.surface
            NahwuQuizAnswerOptionState.SELECTED -> MaterialTheme.colorScheme.primaryContainer
            NahwuQuizAnswerOptionState.CORRECT -> MaterialTheme.colorScheme.primaryContainer
            NahwuQuizAnswerOptionState.INCORRECT -> MaterialTheme.colorScheme.errorContainer
        }

    OutlinedCard(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
        border = BorderStroke(OPTION_BORDER_WIDTH, borderColor),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = SanguSantriElevation.flat),
    ) {
        Row(
            modifier = Modifier.padding(SanguSantriSpacing.default),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OptionKeyBadge(key = option.key, tint = borderColor)
            Spacer(modifier = Modifier.width(SanguSantriSpacing.default))
            Text(text = option.text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            when (state) {
                NahwuQuizAnswerOptionState.CORRECT ->
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )

                NahwuQuizAnswerOptionState.INCORRECT ->
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )

                NahwuQuizAnswerOptionState.DEFAULT, NahwuQuizAnswerOptionState.SELECTED -> Unit
            }
        }
    }
}

@Composable
private fun OptionKeyBadge(
    key: NahwuQuizOptionKey,
    tint: Color,
) {
    Surface(
        shape = CircleShape,
        color = tint.copy(alpha = BADGE_BACKGROUND_ALPHA),
        contentColor = tint,
        modifier = Modifier.size(BADGE_SIZE),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = key.name, style = MaterialTheme.typography.labelLarge)
        }
    }
}

private val OPTION_BORDER_WIDTH = 1.5.dp
private val BADGE_SIZE = 32.dp
private const val BADGE_BACKGROUND_ALPHA = 0.15f

@PreviewLightDark
@Composable
private fun NahwuQuizAnswerOptionStatesPreview() {
    SanguSantriTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
            modifier = Modifier.padding(SanguSantriSpacing.default),
        ) {
            NahwuQuizAnswerOption(
                option = NahwuQuizOption(NahwuQuizOptionKey.A, "[FIXTURE] Pilihan A"),
                state = NahwuQuizAnswerOptionState.DEFAULT,
                onClick = {},
            )
            NahwuQuizAnswerOption(
                option = NahwuQuizOption(NahwuQuizOptionKey.B, "[FIXTURE] Pilihan B"),
                state = NahwuQuizAnswerOptionState.SELECTED,
                onClick = {},
            )
            NahwuQuizAnswerOption(
                option = NahwuQuizOption(NahwuQuizOptionKey.C, "[FIXTURE] Pilihan C"),
                state = NahwuQuizAnswerOptionState.CORRECT,
                onClick = {},
                enabled = false,
            )
            NahwuQuizAnswerOption(
                option = NahwuQuizOption(NahwuQuizOptionKey.D, "[FIXTURE] Pilihan D"),
                state = NahwuQuizAnswerOptionState.INCORRECT,
                onClick = {},
                enabled = false,
            )
        }
    }
}

@Preview(name = "Default only")
@Composable
private fun NahwuQuizAnswerOptionDefaultPreview() {
    SanguSantriTheme {
        NahwuQuizAnswerOption(
            option = NahwuQuizOption(NahwuQuizOptionKey.A, "[FIXTURE] Pilihan A"),
            state = NahwuQuizAnswerOptionState.DEFAULT,
            onClick = {},
            modifier = Modifier.padding(SanguSantriSpacing.default),
        )
    }
}
