package com.sangusantri.app.domain.model

/**
 * The five-day Pancawara (Javanese market week) cycle — Kalender Hijriah (`0.0.7`, CAL-FR-004).
 * Only these five names are ever surfaced; weton, neptu, and primbon are explicitly out of scope
 * (`docs/product/HIJRI_CALENDAR_PRD.md` §4.2). Ordinal order matches the cycle's own forward
 * sequence, which [PasaranCalculator] relies on.
 */
enum class Pasaran {
    LEGI,
    PAHING,
    PON,
    WAGE,
    KLIWON,
}
