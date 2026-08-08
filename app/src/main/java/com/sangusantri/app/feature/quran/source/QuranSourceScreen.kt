package com.sangusantri.app.feature.quran.source

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.QuranArabicText
import com.sangusantri.app.core.designsystem.theme.QuranBackground
import com.sangusantri.app.core.designsystem.theme.QuranMutedText
import com.sangusantri.app.core.designsystem.theme.QuranSurface
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.feature.quran.QuranThemeBoundary

/**
 * Sumber Al-Qur'an (QUR-FR-019, `docs/design/QURAN_DESIGN_SYSTEM.md` §5.8) — a static, full-screen
 * attribution destination reachable from the hub overflow and the bottom of Tampilan Al-Qur'an. No
 * ViewModel: every fact here is fixed copy already established in ADR 0016/`docs/security/
 * PRIVACY.md`, not derived from live state. Deliberately does not claim SanguSantri is an official
 * Kemenag application, and has no copy/share control.
 */
@Composable
fun QuranSourceRoute(onBack: () -> Unit) {
    QuranThemeBoundary {
        QuranSourceScreen(onBack = onBack)
    }
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
                        containerColor = QuranSurface,
                        titleContentColor = QuranArabicText,
                        navigationIconContentColor = QuranArabicText,
                    ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(SanguSantriSpacing.default),
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.large),
        ) {
            QuranSourceSection(
                title = stringResource(R.string.quran_source_provenance_title),
                body = stringResource(R.string.quran_source_provenance_body),
            )
            QuranSourceSection(
                title = stringResource(R.string.quran_source_disclaimer_title),
                body = stringResource(R.string.quran_source_disclaimer_body),
            )
            QuranSourceSection(
                title = stringResource(R.string.quran_source_fields_title),
                body = stringResource(R.string.quran_source_fields_body),
            )
            QuranSourceSection(
                title = stringResource(R.string.quran_source_offline_title),
                body = stringResource(R.string.quran_source_offline_body),
            )
            QuranSourceSection(
                title = stringResource(R.string.quran_source_permission_title),
                body = stringResource(R.string.quran_source_permission_body),
            )
        }
    }
}

@Composable
private fun QuranSourceSection(
    title: String,
    body: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.extraSmall)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = QuranArabicText)
        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = QuranMutedText)
    }
}
