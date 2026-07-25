package com.sangusantri.app.domain.model

/** A public or pesantren-specific practice, for example Tahlil or Istighosah (PRD 10.1, 11.1). */
data class Amaliyah(
    val id: String,
    val slug: String,
    val titleId: String,
    val titleAr: String,
    val descriptionId: String?,
    val descriptionAr: String?,
    val category: String,
)
