package com.sangusantri.app.feature.quran

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.LocalQuranThemeMode
import com.sangusantri.app.domain.model.QuranThemeMode

/** The quick one-tap Light/Dark switch shown in the hub and reader top bars (2026-08-10 addition,
 * ADR 0016 amendment) — the icon shown is the mode a tap switches *to* (a sun while dark, a moon
 * while light), the common convention for this control. Tampilan Al-Qur'an settings offers the
 * same choice as an explicit segmented control for anyone who prefers not to use the quick toggle.
 */
@Composable
fun QuranThemeToggleButton(onClick: () -> Unit) {
    val isDark = LocalQuranThemeMode.current == QuranThemeMode.DARK
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
            contentDescription =
                stringResource(
                    if (isDark) {
                        R.string.quran_theme_toggle_to_light_content_description
                    } else {
                        R.string.quran_theme_toggle_to_dark_content_description
                    },
                ),
        )
    }
}
