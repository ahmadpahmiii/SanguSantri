package com.sangusantri.app.domain.model

/** A multiple-choice answer slot (`0.0.5`, Nahwu Quiz) — always exactly four per question, per
 * `docs/design/figma-export/future-releases/05-release-0.0.5-nahwu-quiz.md`. Stored as a native
 * Room enum column (`TasbihSessionEntity.targetPreset` precedent), never a fifth free-form option. */
enum class NahwuQuizOptionKey {
    A,
    B,
    C,
    D,
}
