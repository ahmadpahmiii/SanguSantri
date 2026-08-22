package com.sangusantri.app.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.sangusantri.app.feature.activity.detail.ActivityQuranHistoryRoute
import com.sangusantri.app.feature.activity.detail.ActivityTasbihHistoryRoute
import com.sangusantri.app.feature.explore.ExploreRoute
import com.sangusantri.app.feature.guidedreader.GuidedReaderRoute
import com.sangusantri.app.feature.hijricalendar.HijriCalendarRoute
import com.sangusantri.app.feature.home.SerambiActions
import com.sangusantri.app.feature.home.SerambiRoute
import com.sangusantri.app.feature.nahwuquiz.NahwuQuizHistoryRoute
import com.sangusantri.app.feature.nahwuquiz.NahwuQuizInstructionRoute
import com.sangusantri.app.feature.nahwuquiz.NahwuQuizLandingRoute
import com.sangusantri.app.feature.nahwuquiz.NahwuQuizPackageDetailRoute
import com.sangusantri.app.feature.nahwuquiz.NahwuQuizPackagesRoute
import com.sangusantri.app.feature.nahwuquiz.NahwuQuizResultRoute
import com.sangusantri.app.feature.nahwuquiz.NahwuQuizSessionRoute
import com.sangusantri.app.feature.prayertimes.JadwalSholatRoute
import com.sangusantri.app.feature.prayertimes.LocationRefreshViewModel
import com.sangusantri.app.feature.quran.QuranEntryRoute
import com.sangusantri.app.feature.quran.hub.QuranHubRoute
import com.sangusantri.app.feature.quran.reader.QuranReaderRoute
import com.sangusantri.app.feature.quran.settings.QuranSettingsRoute
import com.sangusantri.app.feature.quran.source.QuranSourceRoute
import com.sangusantri.app.feature.reader.ReaderEntryRoute
import com.sangusantri.app.feature.reader.ReaderRoute
import com.sangusantri.app.feature.reminder.ReminderRoute
import com.sangusantri.app.feature.sholawat.SholawatListRoute
import com.sangusantri.app.feature.sholawat.SholawatReaderRoute
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

/** Jadwal Sholat + Kiblat — a Beranda entry point only, never a bottom-nav destination. Kiblat has
 * no key of its own: the handoff folds it into this screen. */
@Serializable
private data object JadwalSholat : NavKey

/** Searchable, category-driven amaliyah catalogue reached from Beranda. */
@Serializable
private data object Explore : NavKey

/** `0.0.4`, Pengingat Amaliyah — never a bottom-nav destination (PRD §7.1), reached only from a
 * Beranda or Aktivitas section entry point. */
@Serializable
private data object Pengingat : NavKey

/** `0.0.7`, Kalender Hijriah — Beranda-only entry, never a bottom-nav destination
 * (`docs/product/HIJRI_CALENDAR_PRD.md` §4.1's "never add a bottom-navigation item"). */
@Serializable
private data object KalenderHijriah : NavKey

/** `0.0.8`, Sholawat dan Artinya — Beranda-only entry, deliberately not inside [Explore]
 * (`docs/product/SHOLAWAT_PRD.md`). */
@Serializable
private data object SholawatList : NavKey

@Serializable
private data class SholawatReader(
    val contentId: String,
) : NavKey

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
    openPrayerSchedule: Boolean = false,
    onPrayerScheduleConsumed: () -> Unit = {},
) {
    val topLevelBackStack = remember { TopLevelBackStack(Serambi) }

    val context = LocalContext.current
    val locationRefreshViewModel: LocationRefreshViewModel = hiltViewModel()

    // A reminder notification tap (MainActivity.EXTRA_REMINDER_CONTENT_ID) opens that amaliyah's
    // reading-mode gate directly, on top of whatever the user was already doing — never replaces
    // the current tab's own back stack, matching how every other content selection navigates.
    LaunchedEffect(deepLinkContentId) {
        if (deepLinkContentId != null) {
            topLevelBackStack.add(ReaderGate(deepLinkContentId))
            onDeepLinkConsumed()
        }
    }

    // Same rule for the home-screen widget's tap: push [JadwalSholat] on top of wherever the user
    // was, never reset a tab's back stack.
    LaunchedEffect(openPrayerSchedule) {
        if (!openPrayerSchedule) return@LaunchedEffect
        // Guarded: `add` appends unconditionally, so tapping the widget while Jadwal Sholat is
        // already showing would stack a second, identical key on the same tab.
        if (topLevelBackStack.backStack.lastOrNull() != JadwalSholat) {
            topLevelBackStack.add(JadwalSholat)
        }
        // Above the destination on purpose: the tap must re-derive the city and the bearing whether
        // the reader lands on Jadwal Sholat or is already sitting on Beranda. Both render the same
        // Room flows, so neither has to run it — whichever is on screen updates when this writes.
        //
        // Only when the permission is already granted. A widget tap must never raise a permission
        // dialog: the reader tapped a schedule, not a consent prompt, and location stays optional
        // and on-demand (`docs/security/PRIVACY.md`). Without it the app opens exactly as before,
        // with Jadwal Sholat's "Izinkan lokasi" still one tap away.
        val granted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (granted) locationRefreshViewModel.refresh()
        onPrayerScheduleConsumed()
    }

    // No Quran-specific containerColor any more: one app-wide theme means the Quran background and
    // MaterialTheme's background are the same value (MainActivity owns the mode).
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
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
                actions =
                    SerambiActions(
                        onExploreClick = { topLevelBackStack.add(Explore) },
                        onPengingatClick = { topLevelBackStack.add(Pengingat) },
                        onBelajarClick = { topLevelBackStack.add(NahwuQuizLanding) },
                        onQuranClick = { topLevelBackStack.add(QuranEntry) },
                        onContinueAmaliyah = { contentId, mode ->
                            topLevelBackStack.add(
                                when (mode) {
                                    ReaderMode.FULL -> FullReader(contentId)
                                    ReaderMode.GUIDED -> GuidedReader(contentId)
                                },
                            )
                        },
                        onContinueQuran = { surahNumber, ayatNumber ->
                            topLevelBackStack.add(QuranReader(surahNumber, ayatNumber))
                        },
                        onContinueTasbih = { topLevelBackStack.addTopLevel(Tasbih) },
                        onHijriCalendarClick = { topLevelBackStack.add(KalenderHijriah) },
                        onSholawatClick = { topLevelBackStack.add(SholawatList) },
                        onPrayerScheduleClick = { topLevelBackStack.add(JadwalSholat) },
                        // Kiblat lives inside Jadwal Sholat (handoff decision) — same destination.
                        onKiblatClick = { topLevelBackStack.add(JadwalSholat) },
                    ),
            )
        }
        standaloneEntries(topLevelBackStack)
        activityEntries(topLevelBackStack)
        nahwuQuizEntries(topLevelBackStack)
        quranEntries(topLevelBackStack)
        entry<Tasbih> {
            TasbihRoute(onHistoryClick = { topLevelBackStack.add(TasbihHistory) })
        }
        entry<TasbihHistory> {
            TasbihHistoryRoute(onBack = { topLevelBackStack.removeLast() })
        }
        readerEntries(topLevelBackStack)
        entry<JadwalSholat> {
            JadwalSholatRoute(onBack = { topLevelBackStack.removeLast() })
        }
    }

/** Beranda-reached, non-bottom-nav destinations with no further sub-entries of their own —
 * split out to keep [sanguSantriEntryProvider] short. */
private fun EntryProviderScope<NavKey>.standaloneEntries(topLevelBackStack: TopLevelBackStack) {
    entry<Explore> {
        ExploreRoute(
            onBack = { topLevelBackStack.removeLast() },
            onContentSelected = { contentId -> topLevelBackStack.add(ReaderGate(contentId)) },
        )
    }
    entry<Pengingat> {
        ReminderRoute(onBack = { topLevelBackStack.removeLast() })
    }
    entry<KalenderHijriah> {
        HijriCalendarRoute(onBack = { topLevelBackStack.removeLast() })
    }
    entry<SholawatList> {
        SholawatListRoute(
            onBack = { topLevelBackStack.removeLast() },
            onSholawatSelected = { contentId -> topLevelBackStack.add(SholawatReader(contentId)) },
        )
    }
    entry<SholawatReader> { key ->
        SholawatReaderRoute(contentId = key.contentId, onBack = { topLevelBackStack.removeLast() })
    }
}

/**
 * The mode gate + both readers — split out to keep [sanguSantriEntryProvider] short.
 * [replaceTopEntryWithReader], local to this function since it is only used here, pops the
 * current tab's top entry and pushes the given reader in its place — used both for the mode gate
 * (resolving [ReaderGate] into a reader) and the in-reader mode switch (replacing [FullReader]
 * with [GuidedReader] or vice versa, FR-016). Popping first means repeated switching never
 * accumulates duplicate backstack entries, and back navigation from either reader always lands on
 * [Serambi], never on a stale gate or the previous reader mode.
 */
private fun EntryProviderScope<NavKey>.readerEntries(topLevelBackStack: TopLevelBackStack) {
    fun replaceTopEntryWithReader(
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

    entry<ReaderGate> { key ->
        ReaderEntryRoute(
            contentId = key.contentId,
            onBack = { topLevelBackStack.removeLast() },
            onModeResolved = { mode -> replaceTopEntryWithReader(key.contentId, mode) },
        )
    }
    entry<FullReader> { key ->
        ReaderRoute(
            contentId = key.contentId,
            onBack = { topLevelBackStack.removeLast() },
            onSwitchToGuided = {
                replaceTopEntryWithReader(key.contentId, ReaderMode.GUIDED)
            },
        )
    }
    entry<GuidedReader> { key ->
        GuidedReaderRoute(
            contentId = key.contentId,
            onBack = { topLevelBackStack.removeLast() },
            onSwitchToFull = {
                replaceTopEntryWithReader(key.contentId, ReaderMode.FULL)
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
    // No transition on the reader: mushaf page turns swap the whole destination when reading crosses
    // a surah boundary, and NavDisplay's default enter/exit would play a screen animation on top of a
    // gesture that has already visually completed. The surah must simply be there.
    entry<QuranReader>(
        metadata =
            NavDisplay.transitionSpec { EnterTransition.None togetherWith ExitTransition.None } +
                NavDisplay.popTransitionSpec { EnterTransition.None togetherWith ExitTransition.None } +
                NavDisplay.predictivePopTransitionSpec { _: Int ->
                    EnterTransition.None togetherWith ExitTransition.None
                },
    ) { key ->
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
