package com.sangusantri.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.Content
import com.sangusantri.app.domain.model.Reminder
import com.sangusantri.app.domain.repository.ContentRepository
import com.sangusantri.app.domain.repository.NahwuQuizRepository
import com.sangusantri.app.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Builds Beranda exclusively from local repositories; Room remains the source of truth. */
@HiltViewModel
class SerambiViewModel
    @Inject
    constructor(
        contentRepository: ContentRepository,
        reminderRepository: ReminderRepository,
        nahwuQuizRepository: NahwuQuizRepository,
        private val resumeCoordinator: SerambiResumeCoordinator,
    ) : ViewModel() {
    private val activeContent = contentRepository.observeActiveContent()

    private val baseData: Flow<BaseData> =
            combine(
                activeContent,
                reminderRepository.observeNearestEnabled(),
                nahwuQuizRepository.observePackageSummaries().map { it.isNotEmpty() },
                nahwuQuizRepository.observeActiveAttempt(),
            ) { items, nearestReminder, hasNahwuQuizContent, activeQuiz ->
                BaseData(items, nearestReminder, hasNahwuQuizContent, activeQuiz != null)
            }

    val uiState: StateFlow<SerambiUiState> =
        combine(baseData, resumeCoordinator.observe(activeContent)) { base, resumeItem ->
                SerambiUiState.Loaded(
                    items = base.items,
                    nearestReminder = base.nearestReminder,
                    hasNahwuQuizContent = base.hasNahwuQuizContent,
                    hasActiveNahwuQuiz = base.hasActiveNahwuQuiz,
                    resumeItem = resumeItem,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = SerambiUiState.Loading,
            )

    fun dismissResume(fingerprint: String) {
        viewModelScope.launch { resumeCoordinator.dismiss(fingerprint) }
    }

    private data class BaseData(
        val items: List<Content>,
        val nearestReminder: Reminder?,
        val hasNahwuQuizContent: Boolean,
        val hasActiveNahwuQuiz: Boolean,
    )

        private companion object {
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
