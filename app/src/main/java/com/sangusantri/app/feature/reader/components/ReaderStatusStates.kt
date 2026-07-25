package com.sangusantri.app.feature.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriSpacing
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme

@Composable
fun ReaderLoadingState(modifier: Modifier = Modifier) {
    val loadingDescription = stringResource(R.string.reader_loading_content_description)
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.semantics { contentDescription = loadingDescription },
        )
    }
}

@Composable
fun ReaderContentUnavailableState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.reader_content_unavailable),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(SanguSantriSpacing.large),
        )
    }
}

@Composable
fun ReaderRecoverableErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SanguSantriSpacing.default),
            modifier = Modifier.padding(SanguSantriSpacing.large),
        ) {
            Text(
                text = stringResource(R.string.reader_error_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.reader_retry_action))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ReaderLoadingStatePreview() {
    SanguSantriTheme { ReaderLoadingState() }
}

@PreviewLightDark
@Composable
private fun ReaderContentUnavailableStatePreview() {
    SanguSantriTheme { ReaderContentUnavailableState() }
}

@PreviewLightDark
@Composable
private fun ReaderRecoverableErrorStatePreview() {
    SanguSantriTheme { ReaderRecoverableErrorState(onRetry = {}) }
}
