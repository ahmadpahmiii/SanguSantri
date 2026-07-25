package com.sangusantri.app.domain.model

/** A version together with its approval and ordered steps — one canonical read for any reader mode. */
data class AmaliyahVersionDetail(
    val version: AmaliyahVersion,
    val approval: Approval,
    val steps: List<AmaliyahStep>,
)
