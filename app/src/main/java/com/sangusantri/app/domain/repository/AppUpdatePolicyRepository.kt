package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.AppUpdatePolicy

/** Fetches the in-app update policy (ADR 0017). `null` means fetch or parse failed — callers fail open. */
interface AppUpdatePolicyRepository {
    suspend fun fetchPolicy(): AppUpdatePolicy?
}
