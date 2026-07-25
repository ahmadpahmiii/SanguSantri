package com.sangusantri.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sangusantri.app.R
import com.sangusantri.app.feature.home.SerambiRoute
import kotlinx.serialization.Serializable

@Serializable
private data object Serambi : NavKey

/** Stable identifier only (the amaliyah slug), never a full content object, per FR-002/FR-003. */
@Serializable
private data class AmaliyahDetail(
    val slug: String,
) : NavKey

@Serializable
private data object Setelan : NavKey

@Serializable
private data object About : NavKey

/**
 * Navigation 3 host. [Serambi] is the real Milestone 2 home destination; [AmaliyahDetail] and
 * [Setelan] replace [AmaliyahDetailPlaceholderScreen] / [SetelanPlaceholderScreen] with the real
 * Full Reader and Reader Settings screens when Milestone 3 lands (ADR 0004 placeholder pattern).
 */
@Composable
fun SanguSantriNavHost(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Serambi)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            entryProvider {
                entry<Serambi> {
                    SerambiRoute(
                        onAmaliyahSelected = { slug -> backStack.add(AmaliyahDetail(slug)) },
                        onSetelanClick = { backStack.add(Setelan) },
                        onAboutClick = { backStack.add(About) },
                    )
                }
                entry<AmaliyahDetail> { key ->
                    AmaliyahDetailPlaceholderScreen(
                        slug = key.slug,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<Setelan> {
                    PlaceholderScreen(
                        message = stringResource(R.string.setelan_placeholder_message),
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<About> {
                    PlaceholderScreen(
                        message = stringResource(R.string.about_placeholder_message),
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
            },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmaliyahDetailPlaceholderScreen(
    slug: String,
    onBack: () -> Unit,
) {
    PlaceholderScreen(
        message = stringResource(R.string.amaliyah_detail_placeholder_message, slug),
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(
    message: String,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back_content_description),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
