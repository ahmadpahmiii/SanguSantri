package com.sangusantri.app.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.sangusantri.app.domain.model.AppThemeMode

/**
 * The dark green "block" panel's colour roles, resolved against the app-wide [LocalAppThemeMode] —
 * the same pattern `QuranColorScheme.kt` uses for the reader's named roles. Material 3's scheme has
 * no equivalent: this panel stays a deep green surface in both themes rather than inverting, so it
 * cannot borrow `primaryContainer` or `surface` without one of the two modes going wrong.
 *
 * Used by Beranda's next-prayer block and Jadwal Sholat's countdown, and nowhere else — the revamp
 * allows the app exactly one such panel per screen (handoff §Elevation: hierarchy comes from
 * typography, one dark block, hairlines, and tint).
 */
object BlockColors {
    val background: Color
        @Composable get() = blockColor(SantriBlockBackgroundDark, SantriBlockBackgroundLight)

    /** Light has no border — the solid panel separates on its own. */
    val border: Color?
        @Composable get() = if (isDark()) SantriBlockBorderDark else null

    val text: Color
        @Composable get() = blockColor(SantriBlockTextDark, SantriBlockTextLight)

    /** Prayer name, time, countdown — the elements meant to be read from across the room. */
    val strong: Color
        @Composable get() = blockColor(SantriBlockStrongDark, SantriBlockStrongLight)

    val dim: Color
        @Composable get() = blockColor(SantriBlockDimDark, SantriBlockDimLight)

    val track: Color
        @Composable get() = blockColor(SantriBlockTrackDark, SantriBlockTrackLight)

    val fill: Color
        @Composable get() = blockColor(SantriBlockFillDark, SantriBlockFillLight)

    val chipBackground: Color
        @Composable get() = blockColor(SantriBlockChipBackgroundDark, SantriBlockChipBackgroundLight)

    val chipText: Color
        @Composable get() = blockColor(SantriBlockChipTextDark, SantriBlockChipTextLight)
}

@Composable
private fun isDark(): Boolean = LocalAppThemeMode.current == AppThemeMode.DARK

@Composable
private fun blockColor(
    dark: Color,
    light: Color,
): Color = if (isDark()) dark else light
