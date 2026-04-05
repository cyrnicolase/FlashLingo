package com.english.flashcard.ui.screens.settings

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val dailyNewWords: Int = 20,
    val version: String = "1.0.0"
)