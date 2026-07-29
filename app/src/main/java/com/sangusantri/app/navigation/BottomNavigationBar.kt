package com.sangusantri.app.navigation

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import com.sangusantri.app.core.designsystem.theme.SanguSantriDimensions

/**
 * The single bottom navigation bar for the whole app (Beranda|Tasbih at 0.0.2, Beranda|Aktivitas|
 * Tasbih from 0.0.3) — bottom bar only, on every window-size class including expanded/tablet, by
 * explicit product/tech-lead decision superseding `docs/design/DESIGN_SYSTEM.md`'s previously
 * documented adaptive nav-rail plan (see the bottom-navigation-only ADR amendment). Label is always
 * visible; the selected item gets both a tonal pill indicator and a filled/outlined icon swap, so
 * selection state is never colour-only (`docs/design/ACCESSIBILITY.md`).
 */
@Composable
fun BottomNavigationBar(
    destinations: List<RootDestination>,
    selectedKey: NavKey,
    onSelect: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        destinations.forEach { destination ->
            val isSelected = destination.key == selectedKey
            NavigationBarItem(
                modifier = Modifier.heightIn(min = SanguSantriDimensions.minimumTouchTarget),
                selected = isSelected,
                onClick = { onSelect(destination.key) },
                alwaysShowLabel = true,
                icon = { destination.icon(isSelected) },
                label = { Text(text = destination.label) },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            )
        }
    }
}
