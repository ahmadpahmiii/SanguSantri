package com.sangusantri.app.core.validation

/**
 * The result of checking a parsed payload's structure before anything is written to Room.
 *
 * There used to be three of these — `ContentValidation`, `QuranValidation`, `NahwuQuizValidation` —
 * declared in three packages and structurally identical down to the field name. Three types meant
 * three `is …Invalid` checks that could not share a helper, which is exactly why validation
 * failures could not travel in the same channel as transport failures
 * ([com.sangusantri.app.core.network.validate] now does that).
 *
 * This is *technical* validation only. It answers "can this app safely persist and render these
 * bytes", never "is this religious content correct" — nothing here may repair, merge, normalise or
 * invent content (`CLAUDE.md` §Content safety). An invalid payload is rejected whole; it is never
 * partially accepted.
 */
sealed interface Validation {
    data object Valid : Validation

    data class Invalid(val reason: String) : Validation

    companion object {
        /**
         * Builds a result from the "first problem found, or null" idiom every validator in this
         * codebase already uses internally, so none of them has to spell out
         * `reason?.let(::Invalid) ?: Valid` again.
         */
        fun of(reason: String?): Validation = reason?.let(::Invalid) ?: Valid
    }
}
