package com.sangusantri.app.feature.quran.reader

data class QuranAyatActionSheetActions(
    /** "Putar dari ayat ini" — plays this ayah and continues through the surah (`4b`). */
    val onPlayFromHere: () -> Unit,
    /** "Putar ayat ini saja" — stops at the end of this ayah. */
    val onPlaySingle: () -> Unit,
    /** "Ulangi ayat ini" — replays this ayah three times before moving on. */
    val onRepeatAyat: () -> Unit,
    val onToggleBookmark: () -> Unit,
    val onOpenTafsir: () -> Unit,
    val onMarkLastRead: () -> Unit,
    val onShowPosition: () -> Unit,
)
