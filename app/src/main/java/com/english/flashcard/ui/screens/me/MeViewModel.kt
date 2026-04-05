package com.english.flashcard.ui.screens.me

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.english.flashcard.domain.repository.WordRepository
import com.english.flashcard.domain.usecase.GetDailyProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val getDailyProgressUseCase: GetDailyProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeUiState())
    val uiState: StateFlow<MeUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            combine(
                getDailyProgressUseCase(),
                wordRepository.getMasteredWordCount(),
                wordRepository.getFavoriteWords()
            ) { dailyProgress, masteredCount, favorites ->
                MeUiState(
                    todayLearned = dailyProgress.totalWords,
                    weekStreak = calculateWeekStreak(),
                    totalMastered = masteredCount,
                    favoriteCount = favorites.size,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun calculateWeekStreak(): Int {
        return 0
    }
}
