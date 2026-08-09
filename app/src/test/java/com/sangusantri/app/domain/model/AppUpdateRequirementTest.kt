package com.sangusantri.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppUpdateRequirementTest {
    private val policy =
        AppUpdatePolicy(
            minimumVersionCode = 4,
            forceUpdateVersionCodes = setOf(1, 2),
        )

    @Test
    fun belowMinimumVersionForcesUpdate() {
        val requirement = decideAppUpdateRequirement(installedVersionCode = 3, policy = policy)

        assertEquals(AppUpdateRequirement.FORCE, requirement)
    }

    @Test
    fun listedForceVersionForcesUpdateEvenAboveMinimum() {
        val requirement =
            decideAppUpdateRequirement(
                installedVersionCode = 2,
                policy = policy.copy(minimumVersionCode = 1),
            )

        assertEquals(AppUpdateRequirement.FORCE, requirement)
    }

    @Test
    fun sameAsMinimumVersionIsFlexible() {
        val requirement = decideAppUpdateRequirement(installedVersionCode = 4, policy = policy)

        assertEquals(AppUpdateRequirement.FLEXIBLE, requirement)
    }

    @Test
    fun aboveMinimumVersionIsFlexible() {
        val requirement = decideAppUpdateRequirement(installedVersionCode = 5, policy = policy)

        assertEquals(AppUpdateRequirement.FLEXIBLE, requirement)
    }
}
