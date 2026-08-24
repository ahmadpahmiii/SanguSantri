package com.sangusantri.app.core.telemetry

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.sangusantri.app.core.telemetry.Breadcrumb.action
import com.sangusantri.app.core.telemetry.Breadcrumb.label

/**
 * The trail Crashlytics attaches to the next crash report: which screen the reader was on, and what
 * they last did, so an obfuscated release stack trace does not have to be read cold.
 *
 * **Names only, never values.** `docs/security/PRIVACY.md` commits that Crashlytics is not an
 * analytics channel for devotional behaviour, so a breadcrumb records `FullReader` and
 * `IncrementCounter` — never which amaliyah, which surah or ayah, which dzikir, or any count.
 * That is also all a crash needs: the stack trace already says *where* it broke, the breadcrumb
 * says how the reader got there.
 *
 * [label] reads the name off `toString()` rather than `simpleName` deliberately. R8 obfuscates
 * class names in release, but the string literal Kotlin bakes into a `data class`/`data object`
 * `toString()` survives it — so a breadcrumb stays readable in exactly the builds that need it.
 * Everything from the first `(` is the argument list, which is the part that must not be sent.
 */
object Breadcrumb {
    /**
     * The reader moved to [key]. Wired once at the navigation host, so it covers every destination
     * that exists now and every one added later without further wiring.
     */
    fun screen(key: Any) = record(KEY_SCREEN, "screen", key)

    /**
     * The reader did [action]. Wired into each `onAction`, so one line covers that screen's whole
     * action set.
     */
    fun action(action: Any) = record(KEY_ACTION, "action", action)

    private fun record(
        customKey: String,
        prefix: String,
        value: Any,
    ) {
        val name = label(value)
        FirebaseCrashlytics.getInstance().apply {
            // The custom key answers "where was the reader?" at a glance on the report's Keys tab;
            // the log line keeps the ordered trail of everything that came before it.
            setCustomKey(customKey, name)
            log("$prefix: $name")
        }
    }

    /** `SelectPreset(preset=THIRTY_THREE)` -> `SelectPreset`; a non-data class falls back to its
     * (obfuscated) type without the identity hash, which at least stays stable across reports. */
    private fun label(value: Any): String = value.toString().substringBefore('(').substringBefore('@')

    private const val KEY_SCREEN = "screen"
    private const val KEY_ACTION = "last_action"
}
