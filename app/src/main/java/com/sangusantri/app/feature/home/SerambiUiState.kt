package com.sangusantri.app.feature.home

import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.PrayerSchedule
import com.sangusantri.app.domain.model.ReaderMode
import com.sangusantri.app.domain.model.Reminder
import java.time.LocalTime

/** Beranda state. Independent sections decide their own visibility from this genuine local data. */
sealed interface SerambiUiState {
    data object Loading : SerambiUiState

    data class Loaded(
        val items: List<Content>,
        val nearestReminder: Reminder? = null,
        val hasNahwuQuizContent: Boolean = false,
        val hasActiveNahwuQuiz: Boolean = false,
        val hasSholawatContent: Boolean = false,
        val resumeItem: SerambiResumeItem? = null,
        /** `null` until a prayer-time source is wired — the next-prayer block then does not render
         * at all, rather than showing times nobody should pray by. */
        val prayerSchedule: PrayerSchedule? = null,
        /** Ticks once a minute so the countdown, the position line, and the highlighted prayer stay
         * honest without the block recomposing every second. */
        val now: LocalTime = LocalTime.MIDNIGHT,
    ) : SerambiUiState {
        val featuredItems: List<Content> get() = items.take(MAX_FEATURED_ITEMS)

        private companion object {
            const val MAX_FEATURED_ITEMS = 4
        }
    }
}

/** The single most relevant, locally-backed activity that Beranda can resume. */
sealed interface SerambiResumeItem {
    val lastActivityAtEpochMillis: Long
    val progress: SerambiResumeProgress?
    val dismissFingerprint: String

    data class Amaliyah(
        val contentId: String,
        val title: String,
        val mode: ReaderMode,
        val current: Int,
        val total: Int,
        override val lastActivityAtEpochMillis: Long,
    ) : SerambiResumeItem {
        override val progress = SerambiResumeProgress(current, total)
        override val dismissFingerprint =
            "amaliyah:$contentId:${mode.name}:$current:$total"
    }

    data class Quran(
        val surahNumber: Int,
        val surahName: String,
        val ayatNumber: Int,
        val totalAyat: Int,
        override val lastActivityAtEpochMillis: Long,
    ) : SerambiResumeItem {
        override val progress = SerambiResumeProgress(ayatNumber, totalAyat)
        override val dismissFingerprint =
            "quran:$surahNumber:$ayatNumber:$totalAyat"
    }

    data class Tasbih(
        val sessionName: String?,
        val currentCount: Int,
        val targetCount: Int?,
        override val lastActivityAtEpochMillis: Long,
    ) : SerambiResumeItem {
        override val progress = targetCount?.let { SerambiResumeProgress(currentCount, it) }
        override val dismissFingerprint =
            "tasbih:$currentCount:${targetCount ?: "unlimited"}"
    }
}

data class SerambiResumeProgress(
    val current: Int,
    val total: Int,
) {
    val fraction: Float
        get() = (current.toFloat() / total.coerceAtLeast(1)).coerceIn(0f, 1f)
}
