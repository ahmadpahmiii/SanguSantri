package com.sangusantri.app.domain.model

/** Publication lifecycle of an immutable amaliyah version (PRD 11.1). */
enum class AmaliyahVersionStatus {
    DRAFT,
    IN_REVIEW,
    APPROVED,
    PUBLISHED,
    REVOKED,
}
