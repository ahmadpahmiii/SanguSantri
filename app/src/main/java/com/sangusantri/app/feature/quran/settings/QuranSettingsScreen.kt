@file:Suppress("TooManyFunctions")

package com.sangusantri.app.feature.quran.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranBackground
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOnPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranPrimaryContainer
import com.sangusantri.app.core.designsystem.theme.QuranSurface
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.domain.model.QuranDisplayMode
import com.sangusantri.app.domain.model.QuranReaderSettings
import com.sangusantri.app.domain.model.QuranThemeMode
import com.sangusantri.app.feature.quran.QuranBrightnessEffect
import com.sangusantri.app.feature.quran.QuranThemeBoundary
import com.sangusantri.app.feature.quran.toFontFamily
import com.sangusantri.app.feature.quran.withQuranFontFallback
import java.util.Locale
import kotlin.math.roundToInt

/** Tampilan Al-Qur'an: full-screen, live-persisted controls aligned to local frame 14. */
@Composable
fun QuranSettingsRoute(
    onBack: () -> Unit,
    onOpenSource: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuranSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    QuranBrightnessEffect(uiState.brightnessOverride)
    QuranThemeBoundary {
        QuranSettingsScreen(
            uiState = uiState,
            onBack = onBack,
            actions =
                QuranSettingsActions(
                    onDisplayModeChanged = viewModel::setDisplayMode,
                    onArabicFontChanged = viewModel::setArabicFont,
                    onArabicSizeChanged = viewModel::setArabicSize,
                    onArabicLineSpacingChanged = viewModel::setArabicLineSpacing,
                    onTranslationSizeChanged = viewModel::setTranslationSize,
                    onBrightnessChanged = viewModel::setBrightness,
                    onThemeModeChanged = viewModel::setThemeMode,
                    onOpenSource = onOpenSource,
                ),
            modifier = modifier,
        )
    }
}

data class QuranSettingsActions(
    val onDisplayModeChanged: (QuranDisplayMode) -> Unit,
    val onArabicFontChanged: (QuranArabicFont) -> Unit,
    val onArabicSizeChanged: (Int) -> Unit,
    val onArabicLineSpacingChanged: (Float) -> Unit,
    val onTranslationSizeChanged: (Int) -> Unit,
    val onBrightnessChanged: (Float) -> Unit,
    val onThemeModeChanged: (QuranThemeMode) -> Unit,
    val onOpenSource: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranSettingsScreen(
    uiState: QuranSettingsUiState,
    onBack: () -> Unit,
    actions: QuranSettingsActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = QuranBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.quran_settings_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(R.string.quran_settings_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = QuranMutedText,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back_content_description),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = QuranSurface,
                        titleContentColor = QuranArabicText,
                        navigationIconContentColor = QuranArabicText,
                    ),
            )
        },
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
        ) {
            QuranSettingsBody(
                uiState = uiState,
                actions = actions,
                modifier = Modifier.widthIn(max = SanguSantriDimensions.readerContentMaxWidth),
            )
        }
    }
}

@Composable
private fun QuranSettingsBody(
    uiState: QuranSettingsUiState,
    actions: QuranSettingsActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(SanguSantriSpacing.default),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.large),
    ) {
        QuranLivePreview(uiState)
        QuranThemeModeControl(uiState.themeMode, actions.onThemeModeChanged)
        QuranFontSelector(
            selectedFont = uiState.arabicFont,
            sampleText = uiState.previewAyat?.arabicText,
            onFontSelected = actions.onArabicFontChanged,
        )
        QuranTextSettings(uiState = uiState, actions = actions)
        QuranDisplayModeControl(uiState.displayMode, actions.onDisplayModeChanged)
        QuranBrightnessSetting(uiState.brightnessOverride, actions.onBrightnessChanged)
        QuranSourceLink(actions.onOpenSource)
        Spacer(modifier = Modifier.height(SanguSantriSpacing.default))
    }
}

@Composable
private fun QuranTextSettings(
    uiState: QuranSettingsUiState,
    actions: QuranSettingsActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.large)) {
        QuranSliderSetting(
            spec =
                QuranSliderSpec(
                    label = stringResource(R.string.quran_settings_arabic_size_label),
                    valueLabel = stringResource(R.string.quran_settings_sp_value, uiState.arabicSizeSp),
                    value = uiState.arabicSizeSp.toFloat(),
                    valueRange =
                        QuranReaderSettings.MIN_ARABIC_SIZE_SP
                            .toFloat()..QuranReaderSettings.MAX_ARABIC_SIZE_SP.toFloat(),
                    steps =
                        ((QuranReaderSettings.MAX_ARABIC_SIZE_SP - QuranReaderSettings.MIN_ARABIC_SIZE_SP) / 2) - 1,
                ),
            onValueChange = { value -> actions.onArabicSizeChanged((value / 2).roundToInt() * 2) },
        )
        QuranSliderSetting(
            spec =
                QuranSliderSpec(
                    label = stringResource(R.string.quran_settings_arabic_spacing_label),
                    valueLabel =
                        stringResource(
                            R.string.quran_settings_multiplier_value,
                            String.format(Locale.forLanguageTag("id-ID"), "%.2f", uiState.arabicLineSpacingMultiplier),
                        ),
                    value = uiState.arabicLineSpacingMultiplier,
                    valueRange =
                        QuranReaderSettings.MIN_ARABIC_LINE_SPACING..QuranReaderSettings.MAX_ARABIC_LINE_SPACING,
                    steps = 69,
                ),
            onValueChange = actions.onArabicLineSpacingChanged,
        )
        QuranSliderSetting(
            spec =
                QuranSliderSpec(
                    label = stringResource(R.string.quran_settings_translation_size_label),
                    valueLabel = stringResource(R.string.quran_settings_sp_value, uiState.translationSizeSp),
                    value = uiState.translationSizeSp.toFloat(),
                    valueRange =
                        QuranReaderSettings.MIN_TRANSLATION_SIZE_SP
                            .toFloat()..QuranReaderSettings.MAX_TRANSLATION_SIZE_SP.toFloat(),
                    steps =
                        QuranReaderSettings.MAX_TRANSLATION_SIZE_SP -
                            QuranReaderSettings.MIN_TRANSLATION_SIZE_SP -
                            1,
                ),
            onValueChange = { actions.onTranslationSizeChanged(it.roundToInt()) },
        )
    }
}

@Composable
private fun QuranBrightnessSetting(
    brightnessOverride: Float?,
    onValueChange: (Float) -> Unit,
) {
    val brightness = brightnessOverride ?: DEFAULT_BRIGHTNESS_SLIDER_VALUE
    QuranSliderSetting(
        spec =
            QuranSliderSpec(
                label = stringResource(R.string.quran_settings_brightness_label),
                valueLabel = stringResource(R.string.quran_settings_percent_value, (brightness * 100).roundToInt()),
                value = brightness,
                valueRange = 0f..1f,
                steps = 19,
            ),
        onValueChange = onValueChange,
    )
}

@Composable
private fun QuranLivePreview(uiState: QuranSettingsUiState) {
    Surface(
        color = QuranSurface,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, QuranOutline),
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 128.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(SanguSantriSpacing.default)) {
            val preview = uiState.previewAyat
            if (preview == null) {
                Text(text = stringResource(R.string.quran_settings_preview_unavailable), color = QuranMutedText)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.medium)) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(
                            text = preview.arabicText.withQuranFontFallback(uiState.arabicFont),
                            style =
                                TextStyle(
                                    fontFamily = uiState.arabicFont.toFontFamily(),
                                    fontSize = uiState.arabicSizeSp.sp,
                                    lineHeight = (uiState.arabicSizeSp * uiState.arabicLineSpacingMultiplier).sp,
                                    textAlign = TextAlign.Center,
                                ),
                            color = QuranArabicText,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (uiState.displayMode == QuranDisplayMode.ARAB_TRANSLATION) {
                        Text(
                            text = preview.translation,
                            color = QuranMutedText,
                            fontSize = uiState.translationSizeSp.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuranSliderSetting(
    spec: QuranSliderSpec,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = spec.label, color = QuranArabicText, style = MaterialTheme.typography.bodyLarge)
            Text(text = spec.valueLabel, color = QuranMutedText, style = MaterialTheme.typography.labelLarge)
        }
        Slider(
            value = spec.value,
            onValueChange = onValueChange,
            valueRange = spec.valueRange,
            steps = spec.steps,
            colors =
                SliderDefaults.colors(
                    thumbColor = QuranPrimary,
                    activeTrackColor = QuranPrimary,
                    inactiveTrackColor = QuranOutline,
                    activeTickColor = QuranPrimary,
                    inactiveTickColor = QuranOutline,
                ),
        )
    }
}

private data class QuranSliderSpec(
    val label: String,
    val valueLabel: String,
    val value: Float,
    val valueRange: ClosedFloatingPointRange<Float>,
    val steps: Int,
)

/** Explicit Light/Dark control (2026-08-10 addition, ADR 0016 amendment) — the same choice as the
 * hub/reader top bar's quick [com.sangusantri.app.feature.quran.QuranThemeToggleButton], offered
 * here too since not everyone notices or wants to use a top-bar icon. */
@Composable
private fun QuranThemeModeControl(
    selected: QuranThemeMode,
    onSelected: (QuranThemeMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small)) {
        Text(
            text = stringResource(R.string.quran_settings_theme_label),
            color = QuranArabicText,
            style = MaterialTheme.typography.bodyLarge,
        )
        Surface(
            color = QuranSurface,
            border = BorderStroke(1.dp, QuranOutline),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(modifier = Modifier.padding(SanguSantriSpacing.extraSmall)) {
                QuranDisplayModeSegment(
                    label = stringResource(R.string.quran_settings_theme_light),
                    selected = selected == QuranThemeMode.LIGHT,
                    onClick = { onSelected(QuranThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f),
                )
                QuranDisplayModeSegment(
                    label = stringResource(R.string.quran_settings_theme_dark),
                    selected = selected == QuranThemeMode.DARK,
                    onClick = { onSelected(QuranThemeMode.DARK) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun QuranDisplayModeControl(
    selected: QuranDisplayMode,
    onSelected: (QuranDisplayMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small)) {
        Text(
            text = stringResource(R.string.quran_settings_display_mode_label),
            color = QuranArabicText,
            style = MaterialTheme.typography.bodyLarge,
        )
        Surface(
            color = QuranSurface,
            border = BorderStroke(1.dp, QuranOutline),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(modifier = Modifier.padding(SanguSantriSpacing.extraSmall)) {
                QuranDisplayModeSegment(
                    label = stringResource(R.string.quran_settings_display_mode_arab_only),
                    selected = selected == QuranDisplayMode.ARAB_ONLY,
                    onClick = { onSelected(QuranDisplayMode.ARAB_ONLY) },
                    modifier = Modifier.weight(1f),
                )
                QuranDisplayModeSegment(
                    label = stringResource(R.string.quran_settings_display_mode_arab_translation),
                    selected = selected == QuranDisplayMode.ARAB_TRANSLATION,
                    onClick = { onSelected(QuranDisplayMode.ARAB_TRANSLATION) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun QuranDisplayModeSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        color = if (selected) QuranPrimaryContainer else QuranSurface,
        contentColor = if (selected) QuranOnPrimaryContainer else QuranMutedText,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.heightIn(min = SanguSantriDimensions.minimumTouchTarget),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = SanguSantriSpacing.small)) {
            Text(text = label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun QuranSourceLink(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = QuranSurface,
        border = BorderStroke(1.dp, QuranOutline),
        shape = MaterialTheme.shapes.medium,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = SanguSantriDimensions.minimumTouchTarget),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = SanguSantriSpacing.default, vertical = SanguSantriSpacing.medium),
        ) {
            Icon(imageVector = Icons.Outlined.Info, contentDescription = null, tint = QuranPrimary)
            Text(
                text = stringResource(R.string.quran_source_title),
                color = QuranArabicText,
                style = MaterialTheme.typography.labelLarge,
                modifier =
                    Modifier
                        .padding(horizontal = SanguSantriSpacing.medium)
                        .weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = QuranMutedText,
            )
        }
    }
}

private const val DEFAULT_BRIGHTNESS_SLIDER_VALUE = 0.5f
