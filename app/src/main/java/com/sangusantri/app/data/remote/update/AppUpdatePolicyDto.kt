package com.sangusantri.app.data.remote.update

import com.sangusantri.app.domain.model.AppUpdatePolicy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The Firebase Remote Config `in_app_update` JSON parameter (ADR 0017). */
@Serializable
data class AppUpdatePolicyDto(
    @SerialName("minimum_version_code") val minimumVersionCode: Int,
    @SerialName("force_update_versions") val forceUpdateVersions: List<Int> = emptyList(),
)

fun AppUpdatePolicyDto.toDomain(): AppUpdatePolicy =
    AppUpdatePolicy(
        minimumVersionCode = minimumVersionCode,
        forceUpdateVersionCodes = forceUpdateVersions.toSet(),
    )
