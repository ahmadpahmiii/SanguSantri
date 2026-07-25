package com.sangusantri.app.domain.model

/**
 * A named reading arrangement of an [Amaliyah], for example "Umum" (PRD 10.1, 11.1).
 * Release 0.0.1 only publishes one default PUBLIC variant per amaliyah (PRD FR-003).
 */
data class AmaliyahVariant(
    val id: String,
    val amaliyahId: String,
    val slug: String,
    val nameId: String,
    val nameAr: String,
    val ownerType: OwnerType,
    val pondokId: String?,
    val visibility: Visibility,
    val isDefault: Boolean,
)
