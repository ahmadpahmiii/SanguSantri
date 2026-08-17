package com.sangusantri.app.domain.model

import java.time.Duration
import java.time.LocalTime

/** The six rows Jadwal Sholat shows, in the order they occur. Imsak is not a prayer, but it is part
 * of the same published daily schedule and the design lists it first. */
enum class PrayerName {
    IMSAK,
    SUBUH,
    ZUHUR,
    ASAR,
    MAGRIB,
    ISYA,
}

data class PrayerTime(
    val name: PrayerName,
    val time: LocalTime,
    /** Per-prayer reminder flag (handoff §2 — each row's bell toggles independently). */
    val notificationEnabled: Boolean = name != PrayerName.IMSAK,
)

/**
 * One day's published prayer schedule for one place.
 *
 * [times] are the six entries the design lists, carried through exactly as published by the source
 * — this app never recomputes or adjusts a prayer time. [source] names the publisher so the screen
 * can attribute it.
 */
data class PrayerSchedule(
    val times: List<PrayerTime>,
    val location: String,
    val source: String,
) {
    /**
     * The next entry due after [now], rolling over into tomorrow once the day's last has passed —
     * after Isya the next entry really is tomorrow's Imsak, and the block must say so rather than
     * printing a time that already went by this morning.
     */
    fun nextAfter(now: LocalTime): PrayerTime? = times.firstOrNull { it.time > now } ?: times.firstOrNull()

    /** `true` when [nextAfter] has wrapped into tomorrow. */
    fun nextIsTomorrow(now: LocalTime): Boolean = times.none { it.time > now }

    /** The most recent entry at or before [now]; the day's last entry once [now] is before the
     * first, since that is what is still "current" in the small hours. */
    fun currentAt(now: LocalTime): PrayerTime? = times.lastOrNull { it.time <= now } ?: times.lastOrNull()

    fun remainingUntilNext(now: LocalTime): Duration? {
        val next = nextAfter(now) ?: return null
        val direct = Duration.between(now, next.time)
        return if (direct.isNegative || direct.isZero) direct.plusDays(1) else direct
    }

    /**
     * How far [now] has travelled from the previous entry towards the next, `0f..1f` — the 2dp
     * position line under the next-prayer block. Handles the overnight span between the day's last
     * entry and tomorrow's first, which is where a naive same-day calculation pins the line at 100%
     * all evening.
     */
    fun elapsedFractionAt(now: LocalTime): Float {
        val previous = currentAt(now)
        val next = nextAfter(now)
        val span = if (previous == null || next == null) 0L else spanMinutes(previous.time, next.time)
        return if (span <= 0L || previous == null) {
            0f
        } else {
            (spanMinutes(previous.time, now).toFloat() / span).coerceIn(0f, 1f)
        }
    }

    /** Minutes from [from] to [to], counting forward through midnight when [to] is not later. */
    private fun spanMinutes(
        from: LocalTime,
        to: LocalTime,
    ): Long {
        val minutes = Duration.between(from, to).toMinutes()
        return if (minutes < 0L) minutes + MINUTES_PER_DAY else minutes
    }

    private companion object {
        const val MINUTES_PER_DAY = 24L * 60L
    }
}
