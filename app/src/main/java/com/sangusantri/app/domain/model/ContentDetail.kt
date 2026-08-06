package com.sangusantri.app.domain.model

/** A content item together with its ordered steps — the one canonical read for any reader mode. */
data class ContentDetail(
    val content: Content,
    val steps: List<ContentStep>,
)
