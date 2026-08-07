package com.sangusantri.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.repository.ContentRepository
import com.sangusantri.app.domain.repository.NahwuQuizRepository
import com.sangusantri.app.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Beranda is fully dynamic and catalog-driven (ADR 0015): no hardcoded amaliyah list. New content
 * appears here as soon as a sync/bootstrap pass commits it to Room — no APK update required.
 * Combines [ReminderRepository]'s nearest reminder (`0.0.4`) and [NahwuQuizRepository]'s package
 * existence (`0.0.5`) rather than duplicating either, per
 * `docs/engineering/ARCHITECTURE.md`'s per-concern-repository convention.
 */
@HiltViewModel
class SerambiViewModel
    @Inject
    constructor(
        contentRepository: ContentRepository,
        reminderRepository: ReminderRepository,
        nahwuQuizRepository: NahwuQuizRepository,
    ) : ViewModel() {
        val uiState: StateFlow<SerambiUiState> =
            combine(
                contentRepository.observeActiveContent(),
                reminderRepository.observeNearestEnabled(),
                nahwuQuizRepository.observePackageSummaries().map { it.isNotEmpty() },
            ) { items, nearestReminder, hasNahwuQuizContent ->
                SerambiUiState.Loaded(
                    items = items,
                    nearestReminder = nearestReminder,
                    hasNahwuQuizContent = hasNahwuQuizContent,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = SerambiUiState.Loading,
            )

        private companion object {
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
