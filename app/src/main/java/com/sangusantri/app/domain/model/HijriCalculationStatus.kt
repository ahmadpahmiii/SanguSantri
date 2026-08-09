package com.sangusantri.app.domain.model

/**
 * The authority boundary from `docs/product/HIJRI_CALENDAR_PRD.md` §3.2: [UMM_AL_QURA_CALCULATION]
 * ("Perhitungan Umm al-Qura") is Android's deterministic `HijrahDate` output, never claimed as an
 * Indonesian sidang-isbat decision. [OFFICIAL_CONFIRMED] ("Dikonfirmasi dari sumber resmi") is
 * reserved for a record backed by a published Kemenag determination or a sourced Gregorian official-
 * date record — never inferred or upgraded automatically.
 */
enum class HijriCalculationStatus {
    UMM_AL_QURA_CALCULATION,
    OFFICIAL_CONFIRMED,
}
