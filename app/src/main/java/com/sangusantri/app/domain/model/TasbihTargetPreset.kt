package com.sangusantri.app.domain.model

/** Standalone Tasbih target choice (0.0.2). Deliberately no 99 preset — product requirement. */
enum class TasbihTargetPreset {
    THIRTY_THREE,
    ONE_HUNDRED,
    UNLIMITED,
    CUSTOM,
    ;

    companion object {
        const val THIRTY_THREE_TARGET = 33
        const val ONE_HUNDRED_TARGET = 100

        /** Rejected before dialog dismissal is possible (`ACCESSIBILITY.md`'s numeric-input rule). */
        const val MIN_CUSTOM_TARGET = 1

        /**
         * Documented engineering ceiling for the custom-target dialog (design spec:
         * `[ENGINEERING: define max target value]`). Real amaliyah repetition counts can reach the
         * tens of thousands (Istighosah content already bundled in this app records a 30,000x
         * repetition), so 100,000 leaves real headroom while still rejecting pathological input.
         */
        const val MAX_CUSTOM_TARGET = 100_000
    }
}
