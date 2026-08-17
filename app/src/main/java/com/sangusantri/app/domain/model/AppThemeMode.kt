package com.sangusantri.app.domain.model

/**
 * The app's light/dark appearance. Originally a Quran-only reading-surface setting (QUR-FR-015
 * amendment, ADR 0016); the Beranda/Quran revamp made one theme apply to every screen, so the
 * reader and the rest of the app can no longer drift apart.
 *
 * The persisted value is nullable — `null` means the user has never chosen, and the app follows the
 * system setting until they do. There is deliberately no `SYSTEM` case: the design's control is a
 * binary sun/moon toggle, so every mode this enum can hold is one the UI can actually show as
 * selected.
 */
enum class AppThemeMode {
    LIGHT,
    DARK,
}
