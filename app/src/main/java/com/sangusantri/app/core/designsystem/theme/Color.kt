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
// SanguSantri token source; they are not a second application theme.
val QuranBackground = Color(0xFF050806)
val QuranSurface = Color(0xFF101713)
val QuranSurfaceHigh = Color(0xFF1A1C19)
val QuranPrimary = SantriGreen80
val QuranOnPrimary = SantriGreen20
val QuranPrimaryContainer = SantriGreen20
val QuranOnPrimaryContainer = SantriGreen90
val QuranArabicText = SantriNeutral95
val QuranTranslationText = Color(0xFFC3C8C0)
val QuranMutedText = Color(0xFF95A099)
val QuranOutline = Color(0xFF2D3933)
val QuranScrim = Color(0xA3000000)
val QuranError = SantriError80

// Terakhir dibaca card gradient start (design-export/quran/01-quran-hub-surah.html
// `.continue{background:linear-gradient(135deg,#07351f,#101713)}`); the end stop reuses
// QuranSurface so the card fades from a tinted highlight back into the hub's own surface tone.
val QuranContinueCardGradientStart = Color(0xFF07351F)

// Initial-preparation determinate progress track (design-export/quran/05b-initial-preparation.html
// `.big-progress{background:#26312b}`) — distinct from the default Material surfaceVariant track.
val QuranEntryProgressTrackColor = Color(0xFF26312B)
