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
// every card/sheet stroke and bottom-sheet drag handle in the revised exports.
val SantriOutline = Color(0xFFC3C8C0)

// ---------------------------------------------------------------------------------------------
// App-wide palette — Beranda/Quran revamp handoff (design_handoff_beranda_quran_revamp/README.md
// §"New app-wide palette"). One colour family for the whole app: Beranda, Jadwal Sholat,
// Aktivitas, Tasbih, Amaliyah, and the Quran reading surfaces all resolve to the roles below, so
// moving between them never reads as two applications. Light is warm mushaf paper rather than a
// green-tinted white; dark is a raised near-black with a desaturated accent (the OLED halation
// fix that started the revamp). `Theme.kt` maps these onto Material 3's scheme — no screen reads
// them directly — and the `Quran*` roles further down alias them so the reader stays in family.
// ---------------------------------------------------------------------------------------------
val SantriBackgroundLight = Color(0xFFF7F4EC)
val SantriBackgroundDark = Color(0xFF0E1210)

val SantriSurfaceLight = SantriSurface
val SantriSurfaceDark = Color(0xFF161B18)

val SantriTextLight = SantriNeutral10
val SantriTextDark = Color(0xFFE8E5DA)

val SantriMutedTextLight = Color(0xFF6B7268)
val SantriMutedTextDark = Color(0xFF8A938B)

val SantriTranslationTextLight = SantriNeutral40
val SantriTranslationTextDark = Color(0xFFB4BBB2)

val SantriOutlineLight = Color(0xFFE3DFD2)
val SantriOutlineDark = Color(0xFF283029)

val SantriPrimaryLight = SantriGreen40
val SantriPrimaryDark = Color(0xFF6FA88A)

val SantriOnPrimaryLight = SantriNeutral99
val SantriOnPrimaryDark = Color(0xFF08150E)

// "Tint" is the quiet filled role — icon tiles, chips, selected segments, the ayat-number circle.
// Deliberately not a filled-button colour; onTint is the text/icon that sits on it.
val SantriTintLight = Color(0xFFE6F1E4)
val SantriTintDark = Color(0xFF1B2A21)

val SantriOnTintLight = Color(0xFF0A3A20)
val SantriOnTintDark = Color(0xFFC6DCCD)

// "Block" roles — the one dark green panel the revamp allows itself (Beranda's next-prayer block
// and Jadwal Sholat's countdown). Deliberately its own small role group rather than Material
// roles: it is a deep green surface in *both* themes, so nothing in `colorScheme` describes it.
// Light keeps a solid panel with no border; dark softens the green and adds a hairline so the
// panel still separates from the canvas. Resolved per mode by `BlockColorScheme.kt`.
val SantriBlockBackgroundLight = Color(0xFF0A3A20)
val SantriBlockBackgroundDark = Color(0xFF16241C)

val SantriBlockBorderDark = Color(0xFF2A3B31)

val SantriBlockTextLight = Color(0xFFC8DFCD)
val SantriBlockTextDark = Color(0xFFB7CFBF)

val SantriBlockStrongLight = Color(0xFFFCFDF9)
val SantriBlockStrongDark = SantriTextDark

val SantriBlockDimLight = Color(0xB8C8DFCD)
val SantriBlockDimDark = Color(0xFF8FA895)

val SantriBlockTrackLight = Color(0x33C8DFCD)
val SantriBlockTrackDark = Color(0x29B7CFBF)

val SantriBlockFillLight = Color(0xFF8FC9A2)
val SantriBlockFillDark = SantriPrimaryDark

val SantriBlockChipBackgroundLight = Color(0x1FC8DFCD)
val SantriBlockChipBackgroundDark = Color(0x246FA88A)

val SantriBlockChipTextLight = Color(0xFFDDEDE0)
val SantriBlockChipTextDark = SantriOnTintDark

val SantriError40 = Color(0xFFBA1A1A)
val SantriError80 = Color(0xFFFFB4AB)
val SantriError90 = Color(0xFFFFDAD6)
val SantriError10 = Color(0xFF410002)

// Standalone Al-Qur'an reading-room roles (`0.0.6`). These are aliases of the app-wide palette
// above, not a second application theme — the Beranda/Quran revamp made the reader and the rest of
// the app one colour family, so every role below now points at its app-wide equivalent and only
// the reader-specific roles (surface-high, Arabic text, entry progress track) carry their own
// value. The bare `Quran*` names UI screens import are resolved per mode in `QuranColorScheme.kt`.
//
// The dark values changed in the revamp (README §"Quran dark, changed"): primary #7FDB9C→#6FA88A,
// background #050806→#0E1210, surface #101713→#161B18, Arabic text #F1F1EB→#E8E5DA, outline
// #2D3933→#283029, primary container #00391C→#1B2A21, on-primary-container #A1F5B9→#C6DCCD.
// Arabic-on-canvas contrast moves from ≈17.7:1 to ≈14.8:1 — still far above AA, with less
// halation on OLED.
val QuranBackgroundDark = SantriBackgroundDark
val QuranSurfaceDark = SantriSurfaceDark
val QuranSurfaceHighDark = Color(0xFF1A1C19)
val QuranPrimaryDark = SantriPrimaryDark
val QuranOnPrimaryDark = SantriOnPrimaryDark
val QuranPrimaryContainerDark = SantriTintDark
val QuranOnPrimaryContainerDark = SantriOnTintDark
val QuranArabicTextDark = SantriTextDark
val QuranTranslationTextDark = SantriTranslationTextDark
val QuranMutedTextDark = SantriMutedTextDark
val QuranOutlineDark = SantriOutlineDark
val QuranErrorDark = SantriError80

// Terakhir dibaca card gradient start (design-export/quran/01-quran-hub-surah.html
// `.continue{background:linear-gradient(135deg,#07351f,#101713)}`); the end stop reuses
// QuranSurface so the card fades from a tinted highlight back into the hub's own surface tone.
val QuranContinueCardGradientStartDark = Color(0xFF07351F)

// Initial-preparation determinate progress track (design-export/quran/05b-initial-preparation.html
// `.big-progress{background:#26312b}`) — distinct from the default Material surfaceVariant track.
val QuranEntryProgressTrackColorDark = Color(0xFF26312B)

// Light mode (2026-08-10 addition) — a warm "mushaf paper" reading surface rather than stark
// white, mirroring the softness of the dark palette instead of a harsh inversion. Now aliases of
// the app-wide light palette for the same one-family reason as the dark roles above; the revamp
// changed background #FBFDF7→#F7F4EC, outline →#E3DFD2, primary container #D7F8DF→#E6F1E4, and
// on-primary-container #00391C→#0A3A20 (README §"Quran light, changed").
val QuranBackgroundLight = SantriBackgroundLight
val QuranSurfaceLight = SantriSurfaceLight
val QuranSurfaceHighLight = Color(0xFFFFFFFF)
val QuranPrimaryLight = SantriPrimaryLight
val QuranOnPrimaryLight = SantriOnPrimaryLight
val QuranPrimaryContainerLight = SantriTintLight
val QuranOnPrimaryContainerLight = SantriOnTintLight
val QuranArabicTextLight = SantriTextLight
val QuranMutedTextLight = SantriMutedTextLight
val QuranTranslationTextLight = SantriTranslationTextLight
val QuranOutlineLight = SantriOutlineLight
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
