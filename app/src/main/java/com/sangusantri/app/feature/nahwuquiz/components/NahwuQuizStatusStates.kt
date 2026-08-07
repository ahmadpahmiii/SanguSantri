package com.sangusantri.app.feature.nahwuquiz.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriElevation
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

@Composable
fun NahwuQuizLoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** "Bank Soal Kosong" — design spec state 13: a specific package exists but has zero bundled
 * questions. No "Mulai" action anywhere near this state (nothing to start). */
@Composable
fun NahwuQuizEmptyPackageState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
            modifier = Modifier.padding(SanguSantriSpacing.large),
        ) {
            Icon(
                imageVector = Icons.Filled.Quiz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.nahwu_quiz_empty_package_heading),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.nahwu_quiz_empty_package_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** "Konten Tidak Tersedia" — design spec state 15: the whole bundled question bank failed to
 * load (corrupt/missing local data), replacing `Daftar Paket`/`Detail Paket` entirely. */
@Composable
fun NahwuQuizContentUnavailableState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
            modifier = Modifier.padding(SanguSantriSpacing.large),
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.nahwu_quiz_content_unavailable_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.nahwu_quiz_retry_action))
            }
        }
    }
}

/** Persistent, non-blocking reassurance banner — design spec state 14. Never a dialog; sits
 * inline above `Daftar Paket`/`Detail Paket`'s own content. */
@Composable
fun NahwuQuizOfflineBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = SanguSantriElevation.flat,
    ) {
        Row(
            modifier = Modifier.padding(SanguSantriSpacing.default),
            horizontalArrangement = Arrangement.spacedBy(SanguSantriSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.CloudOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.nahwu_quiz_offline_banner),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun NahwuQuizEmptyPackageStatePreview() {
    SanguSantriTheme { NahwuQuizEmptyPackageState() }
}

@PreviewLightDark
@Composable
private fun NahwuQuizContentUnavailableStatePreview() {
    SanguSantriTheme { NahwuQuizContentUnavailableState(onRetry = {}) }
}

@PreviewLightDark
@Composable
private fun NahwuQuizOfflineBannerPreview() {
    SanguSantriTheme { NahwuQuizOfflineBanner(modifier = Modifier.padding(SanguSantriSpacing.default)) }
}
