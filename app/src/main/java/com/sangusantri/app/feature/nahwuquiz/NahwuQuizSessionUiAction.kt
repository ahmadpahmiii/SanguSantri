package com.sangusantri.app.feature.nahwuquiz

import com.sangusantri.app.domain.model.NahwuQuizOptionKey

sealed interface NahwuQuizSessionUiAction {
    data class SelectOption(
        val option: NahwuQuizOptionKey,
    ) : NahwuQuizSessionUiAction

    data object Submit : NahwuQuizSessionUiAction

    data object Continue : NahwuQuizSessionUiAction

    data object Retry : NahwuQuizSessionUiAction
}
