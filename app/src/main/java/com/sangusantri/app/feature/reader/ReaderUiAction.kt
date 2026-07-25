package com.sangusantri.app.feature.reader

/** User-initiated intents the Full Reader sends to [ReaderViewModel] (unidirectional data flow). */
sealed interface ReaderUiAction {
    data class ScrollPositionChanged(
        val itemIndex: Int,
        val itemOffset: Int,
    ) : ReaderUiAction

    /** Bypasses the debounce — dispatched on `Lifecycle.Event.ON_STOP` so exit progress isn't lost. */
    data class PersistPositionNow(
        val itemIndex: Int,
        val itemOffset: Int,
    ) : ReaderUiAction

    data class SetArabicFontSize(
        val sp: Int,
    ) : ReaderUiAction

    data class SetTranslationFontSize(
        val sp: Int,
    ) : ReaderUiAction

    data class SetArabicLineSpacing(
        val multiplier: Float,
    ) : ReaderUiAction

    data class SetTranslationLineSpacing(
        val multiplier: Float,
    ) : ReaderUiAction

    data class SetShowTranslation(
        val show: Boolean,
    ) : ReaderUiAction

    data object Retry : ReaderUiAction

    /** Switches to the Guided Reader at the currently visible step (FR-016). */
    data object SwitchToGuided : ReaderUiAction
}
