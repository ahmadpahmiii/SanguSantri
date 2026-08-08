package com.sangusantri.app.data.remote.quran.dto

import kotlinx.serialization.Serializable

/**
 * The observed LPMQ Kemenag response envelope, shared by all three endpoints
 * (`docs/engineering/QURAN_API_CONTRACT_DRAFT.md`): `{ "code": 200, "res": "success", "data": [] }`.
 * HTTP status handling, non-success bodies, and nullability remain unverified beyond this shape —
 * [QuranValidator][com.sangusantri.app.data.remote.quran.QuranValidator] checks [code]/[res] before
 * any [data] is trusted.
 */
@Serializable
data class QuranEnvelopeDto<T>(
    val code: Int,
    val res: String,
    val data: T,
)
