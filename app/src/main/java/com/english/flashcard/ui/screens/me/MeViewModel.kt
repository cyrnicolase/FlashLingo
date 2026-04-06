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
import kotlinx.coroutines.launch
import java.time.LocalDate
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
                wordRepository.getFavoriteWordCount()
            ) { dailyProgress, masteredCount, favoriteCount ->
                MeUiState(
                    todayLearned = dailyProgress.newWordsLearned,
                    weekStreak = calculateWeekStreak(),
                    totalMastered = masteredCount,
                    favoriteCount = favoriteCount,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private suspend fun calculateWeekStreak(): Int {
        val recentProgress = wordRepository.getRecentProgress(7)
        if (recentProgress.isEmpty()) return 0

        val progressByDate = recentProgress.associateBy { it.date }
        val today = LocalDate.now()
        
        var streak = 0
        var checkDate = today
        
        while (true) {
            val progress = progressByDate[checkDate]
            if (progress != null && (progress.newWordsLearned > 0 || progress.wordsReviewed > 0)) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else if (checkDate == today) {
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }
}
