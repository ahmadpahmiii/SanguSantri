package com.sangusantri.app.domain.model

/**
 * Guided Reader advancement behaviour once a step's repetition target is reached (PRD 8.4/FR-005).
 * [MANUAL] enables Continue and waits for the user; [AUTOMATIC] advances after briefly reflecting
 * the completed state.
 */
enum class GuidedProgressionMode {
    MANUAL,
    AUTOMATIC,
}
