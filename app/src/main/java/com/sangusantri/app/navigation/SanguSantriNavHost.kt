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
import androidx.compose.runtime.LaunchedEffect
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
import com.sangusantri.app.core.designsystem.theme.QuranBackground
import com.sangusantri.app.domain.model.ReaderMode
import com.sangusantri.app.feature.activity.ActivityRoute
import com.sangusantri.app.feature.activity.detail.ActivityAmaliyahHistoryRoute
import com.sangusantri.app.feature.activity.detail.ActivityQuranHistoryRoute
import com.sangusantri.app.feature.activity.detail.ActivityTasbihHistoryRoute
import com.sangusantri.app.feature.guidedreader.GuidedReaderRoute
import com.sangusantri.app.feature.home.SerambiActions
import com.sangusantri.app.feature.home.SerambiRoute
import com.sangusantri.app.feature.nahwuquiz.NahwuQuizHistoryRoute
import com.sangusantri.app.feature.nahwuquiz.NahwuQuizInstructionRoute
import com.sangusantri.app.feature.nahwuquiz.NahwuQuizLandingRoute
import com.sangusantri.app.feature.nahwuquiz.NahwuQuizPackageDetailRoute
import com.sangusantri.app.feature.nahwuquiz.NahwuQuizPackagesRoute
import com.sangusantri.app.feature.nahwuquiz.NahwuQuizResultRoute
import com.sangusantri.app.feature.nahwuquiz.NahwuQuizSessionRoute
import com.sangusantri.app.feature.quran.QuranEntryRoute
import com.sangusantri.app.feature.quran.hub.QuranHubRoute
import com.sangusantri.app.feature.quran.reader.QuranReaderRoute
import com.sangusantri.app.feature.quran.settings.QuranSettingsRoute
import com.sangusantri.app.feature.quran.source.QuranSourceRoute
import com.sangusantri.app.feature.reader.ReaderEntryRoute
import com.sangusantri.app.feature.reader.ReaderRoute
import com.sangusantri.app.feature.reminder.ReminderRoute
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

/** `0.0.6`, standalone Al-Qur'an Kemenag — Aktivitas' "Lihat semua" for the Quran-reading-history
 * section (QUR-FR-017). */
@Serializable
private data object ActivityQuranHistory : NavKey

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

/** `0.0.4`, Pengingat Amaliyah — never a bottom-nav destination (PRD §7.1), reached only from a
 * Beranda or Aktivitas section entry point. */
@Serializable
private data object Pengingat : NavKey

/** `0.0.5`, Nahwu Quiz — also never a bottom-nav destination (ADR 0013), reached only from
 * Beranda's "Belajar" entry point. */
@Serializable
private data object NahwuQuizLanding : NavKey

@Serializable
private data object NahwuQuizPackages : NavKey

@Serializable
private data class NahwuQuizPackageDetail(
    val packageId: String,
) : NavKey

/** A gate, same pattern as [ReaderGate]: replaced (not left underneath) by [NahwuQuizSession]
 * once "Mulai kuis" is tapped, so back from the session returns to [NahwuQuizPackageDetail], not
 * a stale instruction screen. */
@Serializable
private data class NahwuQuizInstruction(
    val packageId: String,
) : NavKey

@Serializable
private data class NahwuQuizSession(
    val packageId: String,
) : NavKey

@Serializable
private data class NahwuQuizResult(
    val attemptId: String,
) : NavKey

@Serializable
private data class NahwuQuizHistory(
    val packageId: String,
) : NavKey

/** `0.0.6`, standalone Al-Qur'an Kemenag — reached only from a Beranda entry point, never a
 * bottom-nav destination (QUR-FR-001). [QuranEntry] is the same "gate, replaced once resolved"
 * pattern as [ReaderGate]/[NahwuQuizInstruction]. */
@Serializable
private data object QuranEntry : NavKey

@Serializable
private data object QuranHub : NavKey

@Serializable
private data class QuranReader(
    val surahNumber: Int,
    val targetAyat: Int?,
) : NavKey

@Serializable
private data object QuranSettings : NavKey

@Serializable
private data object QuranSource : NavKey

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
fun SanguSantriNavHost(
    modifier: Modifier = Modifier,
    deepLinkContentId: String? = null,
    onDeepLinkConsumed: () -> Unit = {},
) {
    val topLevelBackStack = remember { TopLevelBackStack(Serambi) }
    val isQuranDestination = topLevelBackStack.backStack.lastOrNull().isQuranDestination()

    // A reminder notification tap (MainActivity.EXTRA_REMINDER_CONTENT_ID) opens that amaliyah's
    // reading-mode gate directly, on top of whatever the user was already doing — never replaces
    // the current tab's own back stack, matching how every other content selection navigates.
    LaunchedEffect(deepLinkContentId) {
        if (deepLinkContentId != null) {
            topLevelBackStack.add(ReaderGate(deepLinkContentId))
            onDeepLinkConsumed()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = if (isQuranDestination) QuranBackground else MaterialTheme.colorScheme.background,
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

private fun NavKey?.isQuranDestination(): Boolean =
    this is QuranEntry ||
        this is QuranHub ||
        this is QuranReader ||
        this is QuranSettings ||
        this is QuranSource

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
                actions =
                    SerambiActions(
                        onSetelanClick = { topLevelBackStack.add(Setelan) },
                        onAboutClick = { topLevelBackStack.add(About) },
                        onPengingatClick = { topLevelBackStack.add(Pengingat) },
                        onBelajarClick = { topLevelBackStack.add(NahwuQuizLanding) },
                        onQuranClick = { topLevelBackStack.add(QuranEntry) },
                    ),
            )
        }
        activityEntries(topLevelBackStack)
        entry<Pengingat> {
            ReminderRoute(onBack = { topLevelBackStack.removeLast() })
        }
        nahwuQuizEntries(topLevelBackStack)
        quranEntries(topLevelBackStack)
        entry<Tasbih> {
            TasbihRoute(onHistoryClick = { topLevelBackStack.add(TasbihHistory) })
        }
        entry<TasbihHistory> {
            TasbihHistoryRoute(onBack = { topLevelBackStack.removeLast() })
        }
        readerEntries(topLevelBackStack)
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

/** The mode gate + both readers — split out to keep [sanguSantriEntryProvider] short. */
private fun EntryProviderScope<NavKey>.readerEntries(topLevelBackStack: TopLevelBackStack) {
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
}

/**
 * `0.0.5`, Nahwu Quiz — split out to keep [sanguSantriEntryProvider] short. [NahwuQuizInstruction]
 * is resolved with [TopLevelBackStack.replaceLast] once "Mulai kuis" is tapped (same gate pattern
 * as [readerEntries]'s [ReaderGate]), and [NahwuQuizSession] is likewise replaced by
 * [NahwuQuizResult] on completion — so back navigation never re-enters a stale instruction screen
 * or a completed quiz session.
 */
private fun EntryProviderScope<NavKey>.nahwuQuizEntries(topLevelBackStack: TopLevelBackStack) {
    entry<NahwuQuizLanding> {
        NahwuQuizLandingRoute(
            onBack = { topLevelBackStack.removeLast() },
            onViewPackages = { topLevelBackStack.add(NahwuQuizPackages) },
            onResumeAttempt = { packageId -> topLevelBackStack.add(NahwuQuizInstruction(packageId)) },
        )
    }
    entry<NahwuQuizPackages> {
        NahwuQuizPackagesRoute(
            onBack = { topLevelBackStack.removeLast() },
            onPackageSelected = { packageId -> topLevelBackStack.add(NahwuQuizPackageDetail(packageId)) },
        )
    }
    entry<NahwuQuizPackageDetail> { key ->
        NahwuQuizPackageDetailRoute(
            packageId = key.packageId,
            onBack = { topLevelBackStack.removeLast() },
            onStart = { packageId -> topLevelBackStack.add(NahwuQuizInstruction(packageId)) },
        )
    }
    entry<NahwuQuizInstruction> { key ->
        NahwuQuizInstructionRoute(
            packageId = key.packageId,
            onBack = { topLevelBackStack.removeLast() },
            onStartQuiz = { packageId -> topLevelBackStack.replaceLast(NahwuQuizSession(packageId)) },
        )
    }
    entry<NahwuQuizSession> { key ->
        NahwuQuizSessionRoute(
            packageId = key.packageId,
            onBack = { topLevelBackStack.removeLast() },
            onCompleted = { attemptId -> topLevelBackStack.replaceLast(NahwuQuizResult(attemptId)) },
        )
    }
    entry<NahwuQuizResult> { key ->
        NahwuQuizResultRoute(
            attemptId = key.attemptId,
            onViewHistory = { packageId -> topLevelBackStack.add(NahwuQuizHistory(packageId)) },
            onRetakeQuiz = { packageId -> topLevelBackStack.add(NahwuQuizInstruction(packageId)) },
        )
    }
    entry<NahwuQuizHistory> { key ->
        NahwuQuizHistoryRoute(packageId = key.packageId, onBack = { topLevelBackStack.removeLast() })
    }
}

/**
 * `0.0.6`, standalone Al-Qur'an Kemenag — split out to keep [sanguSantriEntryProvider] short.
 * [QuranEntry] is replaced (not left underneath) by [QuranHub] once ready, same gate pattern as
 * [readerEntries]'s [ReaderGate].
 */
private fun EntryProviderScope<NavKey>.quranEntries(topLevelBackStack: TopLevelBackStack) {
    entry<QuranEntry> {
        QuranEntryRoute(
            onBack = { topLevelBackStack.removeLast() },
            onReady = { topLevelBackStack.replaceLast(QuranHub) },
        )
    }
    entry<QuranHub> {
        QuranHubRoute(
            onBack = { topLevelBackStack.removeLast() },
            onSurahSelected = { surahNumber -> topLevelBackStack.add(QuranReader(surahNumber, targetAyat = null)) },
            onAyatSelected = { surahNumber, ayatNumber ->
                topLevelBackStack.add(QuranReader(surahNumber, targetAyat = ayatNumber))
            },
            onOpenSettings = { topLevelBackStack.add(QuranSettings) },
            onOpenSource = { topLevelBackStack.add(QuranSource) },
        )
    }
    entry<QuranReader> { key ->
        QuranReaderRoute(
            surahNumber = key.surahNumber,
            targetAyat = key.targetAyat,
            onBack = { topLevelBackStack.removeLast() },
            onOpenSettings = { topLevelBackStack.add(QuranSettings) },
        )
    }
    entry<QuranSource> {
        QuranSourceRoute(onBack = { topLevelBackStack.removeLast() })
    }
    entry<QuranSettings> {
        QuranSettingsRoute(
            onBack = { topLevelBackStack.removeLast() },
            onOpenSource = { topLevelBackStack.add(QuranSource) },
        )
    }
}

/** Aktivitas' own root + "Lihat semua" entries — split out to keep [sanguSantriEntryProvider] short. */
private fun EntryProviderScope<NavKey>.activityEntries(topLevelBackStack: TopLevelBackStack) {
    entry<Aktivitas> {
        ActivityRoute(
            onAmaliyahHistoryClick = { topLevelBackStack.add(ActivityAmaliyahHistory) },
            onTasbihHistoryClick = { topLevelBackStack.add(ActivityTasbihHistory) },
            onRemindersClick = { topLevelBackStack.add(Pengingat) },
            onQuranHistoryClick = { topLevelBackStack.add(ActivityQuranHistory) },
        )
    }
    entry<ActivityAmaliyahHistory> {
        ActivityAmaliyahHistoryRoute(onBack = { topLevelBackStack.removeLast() })
    }
    entry<ActivityTasbihHistory> {
        ActivityTasbihHistoryRoute(onBack = { topLevelBackStack.removeLast() })
    }
    entry<ActivityQuranHistory> {
        ActivityQuranHistoryRoute(onBack = { topLevelBackStack.removeLast() })
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
