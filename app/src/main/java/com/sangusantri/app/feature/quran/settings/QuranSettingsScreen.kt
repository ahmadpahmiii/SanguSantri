package com.sangusantri.app.feature.quran.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangusantri.app.BuildConfig
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranSurface
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.domain.model.QuranDisplayMode
import com.sangusantri.app.feature.quran.QuranBrightnessEffect
import com.sangusantri.app.feature.quran.QuranThemeBoundary
import kotlin.math.roundToInt

/** Tampilan Al-Qur'an (QUR-FR-015) — a full-screen nested settings destination because font
 * previews and live controls exceed a compact sheet (`docs/design/QURAN_DESIGN_SYSTEM.md` §5.7). */
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
                    onArabicSizeChanged = viewModel::setArabicSize,
                    onArabicLineSpacingChanged = viewModel::setArabicLineSpacing,
                    onTranslationSizeChanged = viewModel::setTranslationSize,
                    onBrightnessChanged = viewModel::setBrightness,
                    onOpenSource = onOpenSource,
                ),
            modifier = modifier,
        )
    }
}

/** [QuranSettingsScreen]'s action callbacks, bundled to keep the composable's own parameter list
 * short (mirrors `feature/quran/reader/QuranReaderBodyActions`). */
data class QuranSettingsActions(
    val onDisplayModeChanged: (QuranDisplayMode) -> Unit,
    val onArabicSizeChanged: (Int) -> Unit,
    val onArabicLineSpacingChanged: (Float) -> Unit,
    val onTranslationSizeChanged: (Int) -> Unit,
    val onBrightnessChanged: (Float) -> Unit,
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
        topBar = { QuranSettingsTopBar(onBack = onBack) },
    ) { innerPadding ->
        QuranSettingsBody(
            uiState = uiState,
            actions = actions,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuranSettingsTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.quran_settings_title)) },
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
                .verticalScroll(rememberScrollState())
                .padding(SanguSantriSpacing.default),
        verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.large),
    ) {
        QuranLivePreview(uiState = uiState)
        if (BuildConfig.DEBUG) QuranFontPreviewCards()
        val arabicSizeRange =
            QuranReaderSettingsRange.ARABIC_SIZE_MIN.toFloat()..QuranReaderSettingsRange.ARABIC_SIZE_MAX.toFloat()
        QuranSliderSetting(
            label = stringResource(R.string.quran_settings_arabic_size_label),
            value = uiState.arabicSizeSp.toFloat(),
            valueRange = arabicSizeRange,
            onValueChange = { actions.onArabicSizeChanged(it.roundToInt()) },
        )
        QuranSliderSetting(
            label = stringResource(R.string.quran_settings_arabic_spacing_label),
            value = uiState.arabicLineSpacingMultiplier,
            valueRange = QuranReaderSettingsRange.ARABIC_SPACING_MIN..QuranReaderSettingsRange.ARABIC_SPACING_MAX,
            onValueChange = actions.onArabicLineSpacingChanged,
        )
        val translationSizeRange =
            QuranReaderSettingsRange.TRANSLATION_SIZE_MIN
                .toFloat()..QuranReaderSettingsRange.TRANSLATION_SIZE_MAX.toFloat()
        QuranSliderSetting(
            label = stringResource(R.string.quran_settings_translation_size_label),
            value = uiState.translationSizeSp.toFloat(),
            valueRange = translationSizeRange,
            onValueChange = { actions.onTranslationSizeChanged(it.roundToInt()) },
        )
        QuranDisplayModeControl(selected = uiState.displayMode, onSelected = actions.onDisplayModeChanged)
        QuranSliderSetting(
            label = stringResource(R.string.quran_settings_brightness_label),
            value = uiState.brightnessOverride ?: DEFAULT_BRIGHTNESS_SLIDER_VALUE,
            valueRange = 0f..1f,
            onValueChange = actions.onBrightnessChanged,
        )
        QuranSourceLink(onClick = actions.onOpenSource)
    }
}

@Composable
private fun QuranSourceLink(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = QuranSurface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.heightIn(min = SanguSantriDimensions.minimumTouchTarget),
    ) {
        Text(
            text = stringResource(R.string.quran_source_title),
            color = QuranPrimary,
            style = MaterialTheme.typography.labelLarge,
            modifier =
                Modifier
                    .padding(SanguSantriSpacing.default)
                    .wrapContentHeight(),
        )
    }
}

@Composable
private fun QuranLivePreview(uiState: QuranSettingsUiState) {
    Surface(color = QuranSurface, shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
            val preview = uiState.previewAyat
            if (preview == null) {
                Text(text = stringResource(R.string.quran_settings_preview_unavailable), color = QuranMutedText)
            } else {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        text = preview.arabicText,
                        style =
                            TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontSize = uiState.arabicSizeSp.sp,
                                lineHeight = (uiState.arabicSizeSp * uiState.arabicLineSpacingMultiplier).sp,
                                textAlign = TextAlign.End,
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
                    )
                }
            }
        }
    }
}

/** Development-only review aid (`docs/design/QURAN_DESIGN_SYSTEM.md` §4) — no candidate has passed
 * the licence/glyph gate yet (QUR-FR-016), so nothing here is selectable in any build, and this
 * whole section is absent from release entirely. */
@Composable
private fun QuranFontPreviewCards() {
    Column(verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small)) {
        Text(text = stringResource(R.string.quran_settings_font_section_title), color = QuranMutedText)
        listOf(
            R.string.quran_font_candidate_lpmq,
            R.string.quran_font_candidate_amiri,
            R.string.quran_font_candidate_king_fahd,
        ).forEach { nameRes ->
            Surface(
                color = QuranSurface,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, QuranOutline),
            ) {
                Column(modifier = Modifier.padding(SanguSantriSpacing.default)) {
                    Text(text = stringResource(nameRes), color = QuranArabicText)
                    Text(
                        text = stringResource(R.string.quran_font_not_yet_cleared),
                        color = QuranMutedText,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuranSliderSetting(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Text(text = label, color = QuranArabicText)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors =
                SliderDefaults.colors(
                    thumbColor = QuranPrimary,
                    activeTrackColor = QuranPrimary,
                    inactiveTrackColor = QuranOutline,
                ),
        )
    }
}

@Composable
private fun QuranDisplayModeControl(
    selected: QuranDisplayMode,
    onSelected: (QuranDisplayMode) -> Unit,
) {
    Column {
        Text(text = stringResource(R.string.quran_settings_display_mode_label), color = QuranArabicText)
        Column(verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall)) {
            QuranDisplayModeChip(
                label = stringResource(R.string.quran_settings_display_mode_arab_only),
                selected = selected == QuranDisplayMode.ARAB_ONLY,
                onClick = { onSelected(QuranDisplayMode.ARAB_ONLY) },
            )
            QuranDisplayModeChip(
                label = stringResource(R.string.quran_settings_display_mode_arab_translation),
                selected = selected == QuranDisplayMode.ARAB_TRANSLATION,
                onClick = { onSelected(QuranDisplayMode.ARAB_TRANSLATION) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuranDisplayModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label) },
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = QuranPrimary,
                selectedLabelColor = QuranArabicText,
                labelColor = QuranArabicText,
            ),
    )
}

private object QuranReaderSettingsRange {
    const val ARABIC_SIZE_MIN = 24
    const val ARABIC_SIZE_MAX = 52
    const val ARABIC_SPACING_MIN = 1.45f
    const val ARABIC_SPACING_MAX = 2.20f
    const val TRANSLATION_SIZE_MIN = 14
    const val TRANSLATION_SIZE_MAX = 24
}

private const val DEFAULT_BRIGHTNESS_SLIDER_VALUE = 0.5f
