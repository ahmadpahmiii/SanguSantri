package com.sangusantri.app.feature.tasbih

/** Which modal dialog (if any) is currently shown above [TasbihScreen] — mutually exclusive, so one
 * state instead of two independent booleans. */
internal enum class TasbihDialog { NONE, CUSTOM_TARGET, RESET_CONFIRMATION }
