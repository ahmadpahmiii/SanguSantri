package com.sangusantri.app.data.remote.quran.dto

import com.sangusantri.app.core.network.ApiEnvelope
import kotlinx.serialization.Serializable

/**
 * The observed LPMQ Kemenag response envelope, shared by all three endpoints
 * (`docs/engineering/QURAN_API_CONTRACT_DRAFT.md`): `{ "code": 200, "res": "success", "data": [] }`.
 * HTTP status handling, non-success bodies, and nullability remain unverified beyond this shape,
 * so [code]/[res] are checked before [data] is trusted — by
 * [unwrap][com.sangusantri.app.core.network.unwrap], through the [ApiEnvelope] contract.
 */
@Serializable
data class QuranEnvelopeDto<T>(val code: Int, val res: String, val data: T) : ApiEnvelope<T> {
    override val payload: T get() = data

    override val envelopeFailure: String?
        get() = if (code == SUCCESS_CODE && res == SUCCESS_RES) null else "unsuccessful envelope (code=$code, res=$res)"

    private companion object {
        const val SUCCESS_CODE = 200
        const val SUCCESS_RES = "success"
    }
}
