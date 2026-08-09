package com.sangusantri.app.domain.model

/**
 * The minimum per-record provenance `docs/product/HIJRI_CALENDAR_PRD.md` §5.3/CAL-FR-008 requires:
 * publisher, title, URL, and the source year, plus this bundle's own version and editorial
 * acceptance note. Every [HijriCalendarEvent] carries one — provenance is never optional for a
 * production agenda item.
 */
data class HijriEventProvenance(
    val bundleVersion: Int,
    val sourcePublisher: String,
    val sourceTitle: String,
    val sourceUrl: String,
    val sourceYear: Int?,
    val editorialNote: String,
)
