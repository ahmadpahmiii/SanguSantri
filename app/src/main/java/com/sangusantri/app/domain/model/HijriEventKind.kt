package com.sangusantri.app.domain.model

/**
 * The five agenda categories required by `docs/product/HIJRI_CALENDAR_PRD.md` §5.3 (CAL-FR-006).
 * Drives both dot colour (§7.2: [FASTING] is amber, everything else is coral) and the two agenda
 * filters — [FASTING] is "Puasa"; every other kind falls under "Hari besar & libur". An item is
 * never both [FASTING] and [FASTING_PROHIBITED] (CAL-FR-006's "prevent a dual representation" rule).
 */
enum class HijriEventKind {
    FASTING,
    FASTING_PROHIBITED,
    RELIGIOUS_OBSERVANCE,
    NATIONAL_HOLIDAY,
    COLLECTIVE_LEAVE,
}
