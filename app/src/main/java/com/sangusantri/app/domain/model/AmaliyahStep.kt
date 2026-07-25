package com.sangusantri.app.domain.model

/**
 * One ordered reading step within an [AmaliyahVersion] (PRD 10.1, 10.2, 11.1).
 * Renders identically for full reading mode and guided reading mode (PRD 3.5) —
 * the two reader modes must never fork this model.
 */
data class AmaliyahStep(
    val id: String,
    val versionId: String,
    val position: Int,
    val stepType: StepType,
    val titleId: String?,
    val titleAr: String?,
    val arabicText: String?,
    val translationId: String?,
    val instructionId: String?,
    val instructionAr: String?,
    val repeatTarget: Int?,
    val quranSurahNumber: Int?,
    val quranAyahStart: Int?,
    val quranAyahEnd: Int?,
    val audioGroupId: String?,
)
