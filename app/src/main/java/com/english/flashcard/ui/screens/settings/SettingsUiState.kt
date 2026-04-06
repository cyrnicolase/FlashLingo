package com.english.flashcard.ui.screens.settings

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val dailyNewWords: Int = 20,
    val testWordCount: Int = 10,
    val version: String = "1.0.0"
)