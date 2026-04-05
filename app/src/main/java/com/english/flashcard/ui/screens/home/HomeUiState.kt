package com.english.flashcard.ui.screens.home

import com.english.flashcard.domain.model.DailyProgress

data class HomeUiState(
    val showOnboarding: Boolean = false,
    val todayDate: String = "",
    val todayNewWords: Int = 0,
    val todayProgress: DailyProgress? = null,
    val wrongWordCount: Int = 0,
    val favoriteCount: Int = 0,
    val masteredCount: Int = 0,
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val error: String? = null
) {
    val todayTotalTasks: Int
        get() = todayNewWords

    val todayCompletedTasks: Int
        get() = todayProgress?.newWordsLearned ?: 0

    val todayProgressPercent: Int
        get() = if (todayTotalTasks > 0) {
            ((todayCompletedTasks.toFloat() / todayTotalTasks) * 100).toInt().coerceIn(0, 100)
        } else 0
}
