package com.english.flashcard.ui.screens.me

data class MeUiState(
    val todayLearned: Int = 0,
    val weekStreak: Int = 0,
    val totalMastered: Int = 0,
    val favoriteCount: Int = 0,
    val isLoading: Boolean = true
)
