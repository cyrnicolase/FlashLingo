package com.english.flashcard.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.english.flashcard.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                userPreferences.isDarkMode,
                userPreferences.dailyNewWords,
                userPreferences.testWordCount
            ) { isDarkMode, dailyNewWords, testWordCount ->
                SettingsUiState(
                    isDarkMode = isDarkMode,
                    dailyNewWords = dailyNewWords,
                    testWordCount = testWordCount,
                    version = "1.0.0"
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setTestWordCount(count: Int) {
        viewModelScope.launch {
            userPreferences.setTestWordCount(count)
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            userPreferences.setDarkMode(!_uiState.value.isDarkMode)
        }
    }

    fun setDailyNewWords(count: Int) {
        viewModelScope.launch {
            userPreferences.setDailyNewWords(count)
        }
    }
}