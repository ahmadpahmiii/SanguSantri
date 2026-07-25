package com.sangusantri.app.feature.reader

import com.sangusantri.app.domain.model.Approval
import com.sangusantri.app.domain.model.ApprovalStatus

/**
 * Compact, user-facing approval status shared by the Full and Guided Reader (PRD 6.5, Milestone 5):
 * "Approved by <name>" when real approval metadata exists, a neutral development-only status while
 * it is still pending, or nothing at all in release builds — never a fake or placeholder approver.
 */
sealed interface ApprovalDisplay {
    data class Approved(
        val approverLabel: String,
    ) : ApprovalDisplay

    /** Shown only in development builds — release builds fall back to [Hidden] instead. */
    data object Pending : ApprovalDisplay

    /** Nothing valid to show yet, and this is not a development build — display nothing. */
    data object Hidden : ApprovalDisplay
}

fun Approval.toApprovalDisplay(isDebugBuild: Boolean): ApprovalDisplay =
    when {
        status == ApprovalStatus.APPROVED && approverName.isNotBlank() ->
            ApprovalDisplay.Approved(institutionName?.takeIf { it.isNotBlank() } ?: approverName)

        isDebugBuild -> ApprovalDisplay.Pending
        else -> ApprovalDisplay.Hidden
    }
