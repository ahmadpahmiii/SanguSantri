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
import com.sangusantri.app.domain.model.ReaderMode
import com.sangusantri.app.feature.guidedreader.GuidedReaderRoute
import com.sangusantri.app.feature.home.SerambiRoute
import com.sangusantri.app.feature.reader.ReaderEntryRoute
import com.sangusantri.app.feature.reader.ReaderRoute
import kotlinx.serialization.Serializable

@Serializable
private data object Serambi : NavKey

/** The reading-mode gate (PRD 8.2) — stable identifier only (the amaliyah slug), per FR-002/FR-003. */
@Serializable
private data class AmaliyahDetail(
    val slug: String,
) : NavKey

/** The Milestone 3 Full Reader (Bacaan Lengkap). */
@Serializable
private data class FullReader(
    val slug: String,
) : NavKey

/** The Milestone 4 Guided Reader (Panduan). */
@Serializable
private data class GuidedReader(
    val slug: String,
) : NavKey

@Serializable
private data object Setelan : NavKey

@Serializable
private data object About : NavKey

/**
 * Navigation 3 host. [Serambi] is the Milestone 2 home destination; [AmaliyahDetail] is the
 * Milestone 4 reading-mode gate, which resolves into either [FullReader] (Milestone 3) or
 * [GuidedReader] (Milestone 4) — the gate entry is replaced (not pushed under) by the resolved
 * reader, so the back button from either reader returns to Serambi, not the gate. Reader settings
 * are a contextual bottom sheet inside each reader, not a destination, so [Setelan] remains a
 * placeholder (ADR 0004 placeholder pattern) — it is Serambi's own settings entry point, unrelated
 * to a specific amaliyah being read.
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
        entryProvider = sanguSantriEntryProvider(backStack),
    )
}

/** Builds every [NavKey]'s composable — split out of [SanguSantriNavHost] to keep that function short. */
private fun sanguSantriEntryProvider(backStack: MutableList<NavKey>) =
    entryProvider {
        entry<Serambi> {
            SerambiRoute(
                onAmaliyahSelected = { slug -> backStack.add(AmaliyahDetail(slug)) },
                onSetelanClick = { backStack.add(Setelan) },
                onAboutClick = { backStack.add(About) },
            )
        }
        entry<AmaliyahDetail> { key ->
            ReaderEntryRoute(
                amaliyahSlug = key.slug,
                onBack = { backStack.removeLastOrNull() },
                onModeResolved = { mode -> replaceTopEntryWithReader(backStack, key.slug, mode) },
            )
        }
        entry<FullReader> { key ->
            ReaderRoute(
                amaliyahSlug = key.slug,
                onBack = { backStack.removeLastOrNull() },
                onSwitchToGuided = { replaceTopEntryWithReader(backStack, key.slug, ReaderMode.GUIDED) },
            )
        }
        entry<GuidedReader> { key ->
            GuidedReaderRoute(
                amaliyahSlug = key.slug,
                onBack = { backStack.removeLastOrNull() },
                onSwitchToFull = { replaceTopEntryWithReader(backStack, key.slug, ReaderMode.FULL) },
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
    }

/**
 * Pops the current top entry and pushes the given reader in its place — used both for the
 * Milestone 4 mode gate (resolving [AmaliyahDetail] into a reader) and the Milestone 5 in-reader
 * mode switch (replacing [FullReader] with [GuidedReader] or vice versa, FR-016). Popping first
 * means repeated switching never accumulates duplicate backstack entries, and back navigation from
 * either reader always lands on [Serambi], never on a stale gate or the previous reader mode.
 */
private fun replaceTopEntryWithReader(
    backStack: MutableList<NavKey>,
    slug: String,
    mode: ReaderMode,
) {
    backStack.removeLastOrNull()
    backStack.add(
        when (mode) {
            ReaderMode.FULL -> FullReader(slug)
            ReaderMode.GUIDED -> GuidedReader(slug)
        },
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
