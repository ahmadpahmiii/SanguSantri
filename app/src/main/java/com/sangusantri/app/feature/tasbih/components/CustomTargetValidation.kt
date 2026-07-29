package com.sangusantri.app.feature.tasbih.components

import com.sangusantri.app.domain.model.TasbihTargetPreset

/** Custom Tasbih target dialog validation states (design spec, `02-release-0.0.2-tasbih.md`). */
internal enum class CustomTargetValidation { VALID, EMPTY, ZERO, NEGATIVE, NON_NUMERIC, TOO_LARGE }

internal fun validateCustomTarget(raw: String): CustomTargetValidation {
    val trimmed = raw.trim()
    val value = trimmed.toIntOrNull()
    return when {
        trimmed.isEmpty() -> CustomTargetValidation.EMPTY
        value == null -> CustomTargetValidation.NON_NUMERIC
        value == 0 -> CustomTargetValidation.ZERO
        value < TasbihTargetPreset.MIN_CUSTOM_TARGET -> CustomTargetValidation.NEGATIVE
        value > TasbihTargetPreset.MAX_CUSTOM_TARGET -> CustomTargetValidation.TOO_LARGE
        else -> CustomTargetValidation.VALID
    }
}
