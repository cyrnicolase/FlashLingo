package com.english.flashcard.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.english.flashcard.data.local.preferences.UserPreferences
import com.english.flashcard.domain.repository.WordRepository
import com.english.flashcard.domain.usecase.GetDailyProgressUseCase
import com.english.flashcard.domain.usecase.SyncWordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val getDailyProgressUseCase: GetDailyProgressUseCase,
    private val syncWordsUseCase: SyncWordsUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkOnboarding()
        loadData()
        syncOnStart()
    }

    private fun checkOnboarding() {
        viewModelScope.launch {
            val isOnboardingShown = userPreferences.isOnboardingShown.first()
            _uiState.update { it.copy(showOnboarding = !isOnboardingShown) }
        }
    }

    fun dismissOnboarding() {
        viewModelScope.launch {
            userPreferences.setOnboardingShown(true)
            _uiState.update { it.copy(showOnboarding = false) }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val today = LocalDate.now()
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.CHINA)
                val dateString = today.format(dateFormatter)

                val dailyNewWords = userPreferences.dailyNewWords.first()
                val todayNewWords = wordRepository.getNewWordsForToday(dailyNewWords).first().size
                val todayReviewWords = wordRepository.getWordsForReview().first().size
                val wrongWordCount = wordRepository.getWrongWords().first().size
                val favoriteCount = wordRepository.getFavoriteWords().first().size
                val masteredCount = wordRepository.getMasteredWordCount().first()

                getDailyProgressUseCase().collect { progress ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            todayDate = dateString,
                            todayNewWords = todayNewWords,
                            todayReviewWords = todayReviewWords,
                            todayProgress = progress,
                            wrongWordCount = wrongWordCount,
                            favoriteCount = favoriteCount,
                            masteredCount = masteredCount
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "加载数据失败"
                    )
                }
            }
        }
    }

    fun syncOnStart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                syncWordsUseCase()
                _uiState.update { it.copy(isSyncing = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        error = e.message ?: "同步失败"
                    )
                }
            }
        }
    }
}
