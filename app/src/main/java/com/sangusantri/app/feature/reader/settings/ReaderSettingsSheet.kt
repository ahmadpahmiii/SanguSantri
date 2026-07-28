package com.sangusantri.app.feature.reader.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriShapes
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.GuidedProgressionMode
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.feature.reader.ReaderUiAction
import java.util.Locale

private val SheetTopCornerRadius = 28.dp

/**
 * Restrained reader appearance settings (FR-008 subset) — a bottom sheet, contextual to the
 * reader, reached from the reader overflow menu (decision F, Figma product-alignment pass — no
 * longer a standalone top-bar icon). Shared by the Full Reader and the Guided Reader (Milestone 4)
 * rather than duplicated — [progressionModeControl] is non-null only when opened from the Guided
 * Reader, which adds one extra section for the automatic/manual progression preference (FR-005).
 *
 * Matches the revised Figma sheet (node `16:89`) exactly: a title + subtitle, three steppers
 * (Arabic font size, translation font size, Arabic line spacing — translation line spacing has no
 * dedicated control in the revised design, though the underlying preference and its DataStore
 * field remain unchanged for forward compatibility), the translation toggle, and a single primary
 * "Selesai" button rather than a header close action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsSheet(
    settings: ReaderSettings,
    onAction: (ReaderUiAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    progressionModeControl: ProgressionModeControl? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = SheetTopCornerRadius, topEnd = SheetTopCornerRadius),
    ) {
        ReaderSettingsContent(
            settings = settings,
            onAction = onAction,
            onClose = onDismiss,
            progressionModeControl = progressionModeControl,
        )
    }
}

@Composable
private fun ReaderSettingsContent(
    settings: ReaderSettings,
    onAction: (ReaderUiAction) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    progressionModeControl: ProgressionModeControl? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = SanguSantriSpacing.default)
                .padding(bottom = SanguSantriSpacing.default)
                .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
    ) {
        ReaderSettingsHeader()
        ReaderSettingsFontSizeControls(settings, onAction)
        ReaderSettingsArabicLineSpacingControl(settings, onAction)
        HorizontalDivider()
        ReaderSettingsTranslationToggleRow(settings, onAction)
        if (progressionModeControl != null) {
            HorizontalDivider()
            ReaderSettingsProgressionModeRow(progressionModeControl)
        }
        Button(
            onClick = onClose,
            shape = SanguSantriShapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.reader_settings_done_action))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderSettingsProgressionModeRow(control: ProgressionModeControl) {
    Column(verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small)) {
        Text(text = stringResource(R.string.guided_progression_mode_label), style = MaterialTheme.typography.bodyLarge)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = control.mode == GuidedProgressionMode.MANUAL,
                onClick = { control.onChange(GuidedProgressionMode.MANUAL) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) {
                Text(text = stringResource(R.string.guided_progression_mode_manual))
            }
            SegmentedButton(
                selected = control.mode == GuidedProgressionMode.AUTOMATIC,
                onClick = { control.onChange(GuidedProgressionMode.AUTOMATIC) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) {
                Text(text = stringResource(R.string.guided_progression_mode_automatic))
            }
        }
    }
}

@Composable
private fun ReaderSettingsHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall)) {
        Text(
            text = stringResource(R.string.reader_settings_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.reader_settings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ReaderSettingsFontSizeControls(
    settings: ReaderSettings,
    onAction: (ReaderUiAction) -> Unit,
) {
    val arabicLabel = stringResource(R.string.reader_settings_arabic_font_size)
    ReaderSettingStepper(
        label = arabicLabel,
        control =
            ReaderStepperControl(
                valueText = "${settings.arabicFontSizeSp}sp",
                onDecrease = {
                    onAction(
                        ReaderUiAction.SetArabicFontSize(
                            settings.arabicFontSizeSp - ReaderSettings.ARABIC_FONT_SIZE_STEP_SP,
                        ),
                    )
                },
                onIncrease = {
                    onAction(
                        ReaderUiAction.SetArabicFontSize(
                            settings.arabicFontSizeSp + ReaderSettings.ARABIC_FONT_SIZE_STEP_SP,
                        ),
                    )
                },
                decreaseEnabled = settings.arabicFontSizeSp > ReaderSettings.MIN_ARABIC_FONT_SIZE_SP,
                increaseEnabled = settings.arabicFontSizeSp < ReaderSettings.MAX_ARABIC_FONT_SIZE_SP,
            ),
    )

    val translationLabel = stringResource(R.string.reader_settings_translation_font_size)
    ReaderSettingStepper(
        label = translationLabel,
        control =
            ReaderStepperControl(
                valueText = "${settings.translationFontSizeSp}sp",
                onDecrease = {
                    onAction(
                        ReaderUiAction.SetTranslationFontSize(
                            settings.translationFontSizeSp - ReaderSettings.TRANSLATION_FONT_SIZE_STEP_SP,
                        ),
                    )
                },
                onIncrease = {
                    onAction(
                        ReaderUiAction.SetTranslationFontSize(
                            settings.translationFontSizeSp + ReaderSettings.TRANSLATION_FONT_SIZE_STEP_SP,
                        ),
                    )
                },
                decreaseEnabled = settings.translationFontSizeSp > ReaderSettings.MIN_TRANSLATION_FONT_SIZE_SP,
                increaseEnabled = settings.translationFontSizeSp < ReaderSettings.MAX_TRANSLATION_FONT_SIZE_SP,
            ),
    )
}

/**
 * Only Arabic line spacing has a dedicated control, matching the revised Figma sheet exactly — see
 * this file's class doc for why translation line spacing has no stepper this phase.
 */
@Composable
private fun ReaderSettingsArabicLineSpacingControl(
    settings: ReaderSettings,
    onAction: (ReaderUiAction) -> Unit,
) {
    val arabicLabel = stringResource(R.string.reader_settings_arabic_line_spacing)
    ReaderSettingStepper(
        label = arabicLabel,
        control =
            ReaderStepperControl(
                valueText = formatMultiplier(settings.arabicLineSpacingMultiplier),
                onDecrease = {
                    onAction(
                        ReaderUiAction.SetArabicLineSpacing(
                            settings.arabicLineSpacingMultiplier - ReaderSettings.LINE_SPACING_STEP,
                        ),
                    )
                },
                onIncrease = {
                    onAction(
                        ReaderUiAction.SetArabicLineSpacing(
                            settings.arabicLineSpacingMultiplier + ReaderSettings.LINE_SPACING_STEP,
                        ),
                    )
                },
                decreaseEnabled = settings.arabicLineSpacingMultiplier > ReaderSettings.MIN_LINE_SPACING,
                increaseEnabled = settings.arabicLineSpacingMultiplier < ReaderSettings.MAX_LINE_SPACING,
            ),
    )
}

@Composable
private fun ReaderSettingsTranslationToggleRow(
    settings: ReaderSettings,
    onAction: (ReaderUiAction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.reader_settings_show_translation),
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(
            checked = settings.showTranslation,
            onCheckedChange = { onAction(ReaderUiAction.SetShowTranslation(it)) },
        )
    }
}

private fun formatMultiplier(value: Float): String = String.format(Locale.US, "%.1fx", value)

@PreviewLightDark
@Composable
private fun ReaderSettingsContentPreview() {
    SanguSantriTheme {
        ReaderSettingsContent(
            settings = ReaderSettings(),
            onAction = {},
            onClose = {},
            modifier = Modifier.padding(top = SanguSantriSpacing.default),
        )
    }
}
