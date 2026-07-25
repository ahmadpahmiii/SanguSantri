package com.sangusantri.app.domain.model

/**
 * Kyai/sesepuh approval record for a content version (PRD 6.5). Only the fields
 * safe for on-device, user-facing display are modelled here; the private signed
 * document itself stays server-side (PRD 6.6) and is never bundled or synced.
 */
data class Approval(
    val id: String,
    val approverName: String,
    val approverRole: String,
    val institutionName: String?,
    val approvalDate: String,
    val approvalScope: String,
    val publicDocumentStorageKey: String?,
    val documentReferenceNumber: String?,
    val status: ApprovalStatus,
)
