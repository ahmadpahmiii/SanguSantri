package com.sangusantri.app.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

/**
 * Multiple-backstacks helper for the bottom navigation shell — one back stack per top-level
 * destination (Beranda/Tasbih, later Aktivitas), a single flattened [backStack] for [NavDisplay]
 * to render, and the currently selected [topLevelKey]. This is the exact pattern from
 * `android/nav3-recipes`' "Common UI" recipe (bottom nav + multiple back stacks), which
 * `docs/engineering/CODING_STANDARD.md` names as the reference for Compose navigation in this
 * project — it wraps the existing Navigation 3 [NavKey]/`NavDisplay` system, it is not a second
 * navigation framework.
 *
 * Switching tabs ([addTopLevel]) never duplicates a [NavKey]: an existing tab's stack is reused
 * (moved to the end so it renders as current) rather than recreated, so each tab's state survives
 * switching away and back. Popping within a tab ([removeLast]) only ever affects that tab's own
 * stack, so back navigation from a child flow returns to its own root, never another tab's screen.
 */
class TopLevelBackStack(
    startKey: NavKey,
) {
    private val topLevelStacks: LinkedHashMap<NavKey, SnapshotStateList<NavKey>> =
        linkedMapOf(startKey to mutableStateListOf(startKey))

    var topLevelKey: NavKey by mutableStateOf(startKey)
        private set

    val backStack: SnapshotStateList<NavKey> = mutableStateListOf(startKey)

    /** True when the currently displayed tab is showing only its own root (no child flow pushed). */
    val isAtTopLevelRoot: Boolean
        get() = topLevelStacks[topLevelKey]?.size == 1

    private fun updateBackStack() {
        backStack.clear()
        backStack.addAll(topLevelStacks.flatMap { it.value })
    }

    fun addTopLevel(key: NavKey) {
        val existingStack = topLevelStacks.remove(key)
        topLevelStacks[key] = existingStack ?: mutableStateListOf(key)
        topLevelKey = key
        updateBackStack()
    }

    fun add(key: NavKey) {
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    /** Pops the current tab's top entry and pushes [key] in its place (in-place mode switches). */
    fun replaceLast(key: NavKey) {
        topLevelStacks[topLevelKey]?.let { stack ->
            stack.removeLastOrNull()
            stack.add(key)
        }
        updateBackStack()
    }

    /**
     * Pops within the current tab's stack. Popping a tab's own root removes that tab entirely and
     * falls back to whichever tab was previously active (`nav3-recipes`' "Common UI" recipe
     * behavior) — so repeatedly pressing back eventually collapses through every visited tab back
     * to the start tab, then exits, standard Android multi-tab back behavior. `NavDisplay` itself
     * only invokes `onBack` while the flattened [backStack] has more than one entry (matching this
     * app's pre-existing single-stack `SanguSantriNavHost` behavior), so [topLevelStacks] can never
     * actually go empty here — `lastOrNull()` is still used defensively rather than `last()`.
     */
    fun removeLast() {
        val stack = topLevelStacks[topLevelKey] ?: return
        val removedKey = stack.removeLastOrNull()
        if (removedKey != null) {
            topLevelStacks.remove(removedKey)
        }
        topLevelStacks.keys.lastOrNull()?.let { topLevelKey = it }
        updateBackStack()
    }
}
