package com.sangusantri.app.data.remote.update

import com.sangusantri.app.domain.model.AppUpdatePolicy
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class AppUpdatePolicyDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun decodesTheConsoleConfiguredJsonIntoTheDomainModel() {
        val raw = """{"minimum_version_code": 4, "force_update_versions": [1, 2]}"""

        val policy = json.decodeFromString<AppUpdatePolicyDto>(raw).toDomain()

        assertEquals(
            AppUpdatePolicy(minimumVersionCode = 4, forceUpdateVersionCodes = setOf(1, 2)),
            policy,
        )
    }

    @Test
    fun missingForceUpdateVersionsDefaultsToEmpty() {
        val raw = """{"minimum_version_code": 4}"""

        val policy = json.decodeFromString<AppUpdatePolicyDto>(raw).toDomain()

        assertEquals(
            AppUpdatePolicy(minimumVersionCode = 4, forceUpdateVersionCodes = emptySet()),
            policy,
        )
    }
}
