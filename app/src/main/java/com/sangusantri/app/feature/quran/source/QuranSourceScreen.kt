package com.sangusantri.app.feature.quran.source

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranBackground
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranOutline
import com.sangusantri.app.core.designsystem.theme.QuranPrimary
import com.sangusantri.app.core.designsystem.theme.QuranSurface
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing

/**
 * Sumber Al-Qur'an (QUR-FR-019, `docs/design/QURAN_DESIGN_SYSTEM.md` §5.8) — a static, full-screen
 * attribution destination reachable from the hub overflow and the bottom of Tampilan Al-Qur'an. No
 * ViewModel: every fact here is fixed copy already established in ADR 0016/`docs/security/
 * PRIVACY.md`, not derived from live state. Deliberately does not claim SanguSantri is an official
 * Kemenag application, and has no copy/share control.
 */
@Composable
fun QuranSourceRoute(onBack: () -> Unit) {
    QuranSourceScreen(onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuranSourceScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = QuranBackground,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.quran_source_title)) },
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
                        containerColor = QuranBackground,
                        titleContentColor = QuranArabicText,
                        navigationIconContentColor = QuranArabicText,
                    ),
            )
        },
    ) { innerPadding ->
        QuranSourceBody(modifier = Modifier.padding(innerPadding))
    }
}

@Composable
private fun QuranSourceBody(modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.TopCenter, modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = SanguSantriDimensions.readerContentMaxWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(SanguSantriSpacing.default),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
        ) {
            QuranSourceIcon()
            Text(
                text = stringResource(R.string.quran_source_heading),
                style = MaterialTheme.typography.titleLarge,
                color = QuranArabicText,
            )
            Text(
                text = stringResource(R.string.quran_source_provenance_body),
                style = MaterialTheme.typography.bodyMedium,
                color = QuranMutedText,
            )
            QuranSourceSection(
                title = stringResource(R.string.quran_source_fields_title),
                body = stringResource(R.string.quran_source_fields_body),
                emphasized = true,
            )
            HorizontalDivider(color = QuranOutline)
            Text(
                text = stringResource(R.string.quran_source_offline_body),
                style = MaterialTheme.typography.bodyMedium,
                color = QuranMutedText,
            )
            Text(
                text = stringResource(R.string.quran_source_read_only_body),
                style = MaterialTheme.typography.bodyMedium,
                color = QuranMutedText,
            )
            QuranSourceSection(
                title = stringResource(R.string.quran_source_permission_title),
                body = stringResource(R.string.quran_source_permission_body),
            )
            QuranSourceNotice(
                title = stringResource(R.string.quran_source_disclaimer_title),
                body = stringResource(R.string.quran_source_disclaimer_body),
            )
        }
    }
}

/** Matches the design's bordered `.notice` treatment — visually distinct from the plain prose
 * above it, since this is the institutional-non-endorsement disclaimer, not routine attribution. */
@Composable
private fun QuranSourceNotice(
    title: String,
    body: String,
) {
    Surface(
        color = QuranSurface,
        border = BorderStroke(1.dp, QuranOutline),
        shape = RoundedCornerShape(SanguSantriDimensions.quranNoticeCornerRadius),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall),
            modifier = Modifier.padding(SanguSantriSpacing.default),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = QuranArabicText)
            Text(text = body, style = MaterialTheme.typography.bodyMedium, color = QuranMutedText)
        }
    }
}

@Composable
private fun QuranSourceIcon() {
    Surface(
        color = QuranSurface,
        contentColor = QuranPrimary,
        border = BorderStroke(1.dp, QuranOutline),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.size(58.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
        }
    }
}

@Composable
private fun QuranSourceSection(
    title: String,
    body: String,
    emphasized: Boolean = false,
) {
    Surface(
        color = if (emphasized) QuranSurface else QuranBackground,
        border = if (emphasized) BorderStroke(1.dp, QuranOutline) else null,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
            modifier = Modifier.padding(if (emphasized) SanguSantriSpacing.default else 0.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = QuranArabicText)
            Text(text = body, style = MaterialTheme.typography.bodyMedium, color = QuranMutedText)
        }
    }
}
