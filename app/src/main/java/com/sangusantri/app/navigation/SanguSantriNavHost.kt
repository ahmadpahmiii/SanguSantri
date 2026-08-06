package com.sangusantri.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.icon.TasbihIcon
import com.sangusantri.app.domain.model.ReaderMode
import com.sangusantri.app.feature.activity.ActivityRoute
import com.sangusantri.app.feature.activity.detail.ActivityAmaliyahHistoryRoute
import com.sangusantri.app.feature.activity.detail.ActivityTasbihHistoryRoute
import com.sangusantri.app.feature.guidedreader.GuidedReaderRoute
import com.sangusantri.app.feature.home.SerambiRoute
import com.sangusantri.app.feature.reader.ReaderEntryRoute
import com.sangusantri.app.feature.reader.ReaderRoute
import com.sangusantri.app.feature.tasbih.TasbihRoute
import com.sangusantri.app.feature.tasbih.history.TasbihHistoryRoute
import kotlinx.serialization.Serializable

/** Root destination — Beranda (0.0.2+, initial destination). */
@Serializable
private data object Serambi : NavKey

/** Root destination — Aktivitas (0.0.3+). */
@Serializable
private data object Aktivitas : NavKey

@Serializable
private data object ActivityAmaliyahHistory : NavKey

@Serializable
private data object ActivityTasbihHistory : NavKey

/** Root destination — Tasbih (0.0.2+). */
@Serializable
private data object Tasbih : NavKey

@Serializable
private data object TasbihHistory : NavKey

/** The reading-mode gate (PRD 8.2) — stable identifier only (the catalog content id, ADR 0015), per FR-002/FR-003. */
@Serializable
private data class ReaderGate(
    val contentId: String,
) : NavKey

/** The Milestone 3 Full Reader (Bacaan Lengkap). */
@Serializable
private data class FullReader(
    val contentId: String,
) : NavKey

/** The Milestone 4 Guided Reader (Panduan). */
@Serializable
private data class GuidedReader(
    val contentId: String,
) : NavKey

@Serializable
private data object Setelan : NavKey

@Serializable
private data object About : NavKey

/**
 * Navigation 3 host and bottom-navigation shell in one composable — the one navigation system this
 * project uses, wrapped by a [Scaffold]/[BottomNavigationBar] that never becomes a Navigation Rail
 * on any window-size class (explicit product/tech-lead decision, bottom-navigation-only through
 * `0.0.5`, superseding `docs/design/DESIGN_SYSTEM.md`'s previously documented adaptive-rail plan).
 * [TopLevelBackStack] gives Beranda, Aktivitas, and Tasbih each their own back stack so switching
 * tabs never duplicates a [NavKey] and neither tab's state is lost.
 * [Serambi] is the initial destination; [ReaderGate] is the Milestone 4 reading-mode gate,
 * which resolves into either [FullReader] (Milestone 3) or [GuidedReader] (Milestone 4) — the gate
 * entry is replaced (not pushed under) by the resolved reader, so back from either reader returns
 * to Beranda, not the gate.
 */
@Composable
fun SanguSantriNavHost(modifier: Modifier = Modifier) {
    val topLevelBackStack = remember { TopLevelBackStack(Serambi) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (topLevelBackStack.isAtTopLevelRoot) {
                BottomNavigationBar(
                    destinations = rootDestinations(),
                    selectedKey = topLevelBackStack.topLevelKey,
                    onSelect = topLevelBackStack::addTopLevel,
                )
            }
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = topLevelBackStack.backStack,
            modifier = Modifier.padding(innerPadding),
            onBack = { topLevelBackStack.removeLast() },
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            entryProvider = sanguSantriEntryProvider(topLevelBackStack),
        )
    }
}

@Composable
private fun rootDestinations(): List<RootDestination> =
    listOf(
        RootDestination(
            key = Serambi,
            label = stringResource(R.string.nav_beranda_label),
            icon = { selected ->
                Icon(
                    imageVector = if (selected) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = null,
                )
            },
        ),
        RootDestination(
            key = Aktivitas,
            label = stringResource(R.string.nav_aktivitas_label),
            icon = { selected ->
                Icon(
                    imageVector = if (selected) Icons.Filled.History else Icons.Outlined.History,
                    contentDescription = null,
                )
            },
        ),
        RootDestination(
            key = Tasbih,
            label = stringResource(R.string.nav_tasbih_label),
            icon = { selected -> TasbihIcon(filled = selected) },
        ),
    )

/** Builds every [NavKey]'s composable — split out of [SanguSantriNavHost] to keep that function short. */
private fun sanguSantriEntryProvider(topLevelBackStack: TopLevelBackStack) =
    entryProvider {
        entry<Serambi> {
            SerambiRoute(
                onContentSelected = { contentId -> topLevelBackStack.add(ReaderGate(contentId)) },
                onSetelanClick = { topLevelBackStack.add(Setelan) },
                onAboutClick = { topLevelBackStack.add(About) },
            )
        }
        activityEntries(topLevelBackStack)
        entry<Tasbih> {
            TasbihRoute(onHistoryClick = { topLevelBackStack.add(TasbihHistory) })
        }
        entry<TasbihHistory> {
            TasbihHistoryRoute(onBack = { topLevelBackStack.removeLast() })
        }
        entry<ReaderGate> { key ->
            ReaderEntryRoute(
                contentId = key.contentId,
                onBack = { topLevelBackStack.removeLast() },
                onModeResolved = { mode -> replaceTopEntryWithReader(topLevelBackStack, key.contentId, mode) },
            )
        }
        entry<FullReader> { key ->
            ReaderRoute(
                contentId = key.contentId,
                onBack = { topLevelBackStack.removeLast() },
                onSwitchToGuided = {
                    replaceTopEntryWithReader(topLevelBackStack, key.contentId, ReaderMode.GUIDED)
                },
            )
        }
        entry<GuidedReader> { key ->
            GuidedReaderRoute(
                contentId = key.contentId,
                onBack = { topLevelBackStack.removeLast() },
                onSwitchToFull = {
                    replaceTopEntryWithReader(topLevelBackStack, key.contentId, ReaderMode.FULL)
                },
            )
        }
        entry<Setelan> {
            PlaceholderScreen(
                message = stringResource(R.string.setelan_placeholder_message),
                onBack = { topLevelBackStack.removeLast() },
            )
        }
        entry<About> {
            PlaceholderScreen(
                message = stringResource(R.string.about_placeholder_message),
                onBack = { topLevelBackStack.removeLast() },
            )
        }
    }

/** Aktivitas' own root + "Lihat semua" entries — split out to keep [sanguSantriEntryProvider] short. */
private fun EntryProviderScope<NavKey>.activityEntries(topLevelBackStack: TopLevelBackStack) {
    entry<Aktivitas> {
        ActivityRoute(
            onAmaliyahHistoryClick = { topLevelBackStack.add(ActivityAmaliyahHistory) },
            onTasbihHistoryClick = { topLevelBackStack.add(ActivityTasbihHistory) },
        )
    }
    entry<ActivityAmaliyahHistory> {
        ActivityAmaliyahHistoryRoute(onBack = { topLevelBackStack.removeLast() })
    }
    entry<ActivityTasbihHistory> {
        ActivityTasbihHistoryRoute(onBack = { topLevelBackStack.removeLast() })
    }
}

/**
 * Pops the current tab's top entry and pushes the given reader in its place — used both for the
 * Milestone 4 mode gate (resolving [ReaderGate] into a reader) and the Milestone 5 in-reader
 * mode switch (replacing [FullReader] with [GuidedReader] or vice versa, FR-016). Popping first
 * means repeated switching never accumulates duplicate backstack entries, and back navigation from
 * either reader always lands on [Serambi], never on a stale gate or the previous reader mode.
 */
private fun replaceTopEntryWithReader(
    topLevelBackStack: TopLevelBackStack,
    contentId: String,
    mode: ReaderMode,
) {
    topLevelBackStack.replaceLast(
        when (mode) {
            ReaderMode.FULL -> FullReader(contentId)
            ReaderMode.GUIDED -> GuidedReader(contentId)
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
