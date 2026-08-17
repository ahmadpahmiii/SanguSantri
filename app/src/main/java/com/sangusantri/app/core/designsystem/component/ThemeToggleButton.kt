package com.sangusantri.app.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.LocalAppThemeMode
import com.sangusantri.app.domain.model.AppThemeMode

/** The quick one-tap Light/Dark switch shown in the hub and reader top bars (2026-08-10 addition,
 * ADR 0016 amendment; app-wide rather than Quran-only since the Beranda/Quran revamp) — the icon
 * shown is the mode a tap switches *to* (a sun while dark, a moon while light), the common
 * convention for this control. Tampilan Al-Qur'an settings offers the same choice as an explicit
 * segmented control for anyone who prefers not to use the quick toggle.
 *
 * The button resolves the target mode itself from [LocalAppThemeMode] and passes it to [onSelect],
 * so a first tap while the app is still following the system persists what the user can actually
 * see, rather than flipping a value nobody has set.
 */
@Composable
fun ThemeToggleButton(onSelect: (AppThemeMode) -> Unit) {
    val isDark = LocalAppThemeMode.current == AppThemeMode.DARK
    IconButton(onClick = { onSelect(if (isDark) AppThemeMode.LIGHT else AppThemeMode.DARK) }) {
        Icon(
            imageVector = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
            contentDescription =
                stringResource(
                    if (isDark) {
                        R.string.theme_toggle_to_light_content_description
                    } else {
                        R.string.theme_toggle_to_dark_content_description
                    },
                ),
        )
    }
}
