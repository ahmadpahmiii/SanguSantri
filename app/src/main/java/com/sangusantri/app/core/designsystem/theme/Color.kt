package com.sangusantri.app.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// SanguSantri brand green — modern Islamic identity (docs/design/DESIGN_SYSTEM.md).
val SantriGreen10 = Color(0xFF00210E)
val SantriGreen20 = Color(0xFF00391C)
val SantriGreen30 = Color(0xFF00522A)
val SantriGreen40 = Color(0xFF0B6E3B)
val SantriGreen80 = Color(0xFF7FDB9C)
val SantriGreen90 = Color(0xFFA1F5B9)

// Light primary-container tint — design product-alignment pass (docs/design/DESIGN_HANDOFF.md,
// exported node color variable observed as rgb(215,248,223) across every revised reader/tasbih/
// beranda frame: repeat-shortcut pills, the saved-position status pill, the guided counter
// background, secondary pill actions, the active bottom-nav pill, and the active TOC/category
// highlight). Distinct from — and noticeably lighter than — SantriGreen90 above.
val SantriGreen95 = Color(0xFFD7F8DF)

// Warm neutral surfaces — calm, readable, non-ornamental.
val SantriNeutral10 = Color(0xFF1A1C19)
val SantriNeutral90 = Color(0xFFE2E3DD)
val SantriNeutral95 = Color(0xFFF1F1EB)
val SantriNeutral99 = Color(0xFFFBFDF7)

// Card/sheet surface — design product-alignment pass, observed as rgb(255,253,248) on every card,
// bottom sheet, and dialog in the revised exports; warmer and distinct from SantriNeutral95/99.
val SantriSurface = Color(0xFFFFFDF8)

// Secondary/muted text — design product-alignment pass, observed as rgb(89,96,90) for every
// secondary label (translations, captions, step counts) in the revised exports. No existing
// token matched this tone; previously this role fell through to Material 3's unbranded default.
val SantriNeutral40 = Color(0xFF59605A)

// Hairline border / drag handle — design product-alignment pass, observed as rgb(195,200,192) for
// every card/sheet stroke and bottom-sheet drag handle in the revised exports. Previously this
// role fell through to Material 3's unbranded default.
val SantriOutline = Color(0xFFC3C8C0)

val SantriError40 = Color(0xFFBA1A1A)
val SantriError80 = Color(0xFFFFB4AB)
val SantriError90 = Color(0xFFFFDAD6)
val SantriError10 = Color(0xFF410002)

// Standalone Al-Qur'an reading-room roles (`0.0.6`). These extend the one canonical
// SanguSantri token source; they are not a second application theme. Dark was the feature's
// original (and still default) mode — ADR 0016 decision #12; a light mode was added by product
// decision as a user-controlled toggle (`docs/decisions/0016-standalone-quran-kemenag-direct-api.md`
// 2026-08-10 amendment), so every role below now has a Dark/Light pair resolved live by
// `QuranColorScheme.kt`'s `LocalQuranThemeMode`-aware properties — the bare `Quran*` names UI
// screens actually import.
val QuranBackgroundDark = Color(0xFF050806)
val QuranSurfaceDark = Color(0xFF101713)
val QuranSurfaceHighDark = Color(0xFF1A1C19)
val QuranPrimaryDark = SantriGreen80
val QuranOnPrimaryDark = SantriGreen20
val QuranPrimaryContainerDark = SantriGreen20
val QuranOnPrimaryContainerDark = SantriGreen90
val QuranArabicTextDark = SantriNeutral95
val QuranTranslationTextDark = Color(0xFFC3C8C0)
val QuranMutedTextDark = Color(0xFF95A099)
val QuranOutlineDark = Color(0xFF2D3933)
val QuranErrorDark = SantriError80

// Terakhir dibaca card gradient start (design-export/quran/01-quran-hub-surah.html
// `.continue{background:linear-gradient(135deg,#07351f,#101713)}`); the end stop reuses
// QuranSurface so the card fades from a tinted highlight back into the hub's own surface tone.
val QuranContinueCardGradientStartDark = Color(0xFF07351F)

// Initial-preparation determinate progress track (design-export/quran/05b-initial-preparation.html
// `.big-progress{background:#26312b}`) — distinct from the default Material surfaceVariant track.
val QuranEntryProgressTrackColorDark = Color(0xFF26312B)

// Light mode (2026-08-10 addition) — a warm "mushaf paper" reading surface rather than stark
// white, mirroring the softness of the dark palette instead of a harsh inversion. Reuses the
// app's own established light tokens (SantriGreen40/95/20, SantriNeutral10/40/99, SantriSurface,
// SantriOutline, SantriError40) everywhere their role matches exactly, for brand consistency with
// the rest of the app's light theme (`Theme.kt`'s `LightColorScheme`) — new hex values are added
// only for the two roles with no existing equivalent (QuranMutedTextLight,
// QuranEntryProgressTrackColorLight). Every text-on-surface pairing below was contrast-checked
// (WCAG relative luminance) at ≥4.8:1, comfortably above the 4.5:1 AA threshold for body text.
val QuranBackgroundLight = SantriNeutral99
val QuranSurfaceLight = SantriSurface
val QuranSurfaceHighLight = Color(0xFFFFFFFF)
val QuranPrimaryLight = SantriGreen40
val QuranOnPrimaryLight = SantriNeutral99
val QuranPrimaryContainerLight = SantriGreen95
val QuranOnPrimaryContainerLight = SantriGreen20
val QuranArabicTextLight = SantriNeutral10

// Dedicated (not reused) — SantriNeutral40 is already spoken for as QuranTranslationTextLight;
// this needs to sit visibly lighter than that while still clearing 4.5:1 against
// QuranBackgroundLight/QuranSurfaceLight (measured 4.84:1).
val QuranMutedTextLight = Color(0xFF6B7268)
val QuranTranslationTextLight = SantriNeutral40
val QuranOutlineLight = SantriOutline
val QuranErrorLight = SantriError40
val QuranContinueCardGradientStartLight = SantriGreen95

// Dedicated — a soft sage track distinct from Material's default surfaceVariant, echoing the
// desaturated-green feel of QuranEntryProgressTrackColorDark rather than a flat neutral gray.
val QuranEntryProgressTrackColorLight = Color(0xFFE3E8DF)

// Dims behind Quran bottom sheets/dialogs in both modes — a scrim always darkens, so one shared
// value (not a Dark/Light pair) is correct here.
val QuranScrim = Color(0xA3000000)

// Kalender Hijriah (`0.0.7`) event-semantic roles — figma-export/hijri-calendar/
// 01-calendar-overview-light.html and 02-calendar-overview-dark.html `:root`/`body.dark` CSS
// variables. These extend the one canonical SanguSantri token source (same rule as the Quran roles
// above); teal marks selection, amber marks fasting, coral marks a religious observance, fasting
// prohibition, or official holiday (PRD §7.2). Never used for anything outside Kalender Hijriah.
val HijriTealLight = Color(0xFF176B5C)
val HijriTealSoftLight = Color(0xFFDCEFE9)
val HijriAmberLight = Color(0xFFD89713)
val HijriAmberSoftLight = Color(0xFFFFF1CB)
val HijriCoralLight = Color(0xFFC94E4E)
val HijriCoralSoftLight = Color(0xFFFBE3E1)

val HijriTealDark = Color(0xFF62D2B8)
val HijriTealSoftDark = Color(0xFF193E35)
val HijriAmberDark = Color(0xFFF3BE4E)
val HijriAmberSoftDark = Color(0xFF473918)
val HijriCoralDark = Color(0xFFF27E78)
val HijriCoralSoftDark = Color(0xFF4A2626)
