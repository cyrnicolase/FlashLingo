package com.english.flashcard.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.english.flashcard.domain.model.Word
import com.english.flashcard.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavoriteWords()
    }

    private fun loadFavoriteWords() {
        viewModelScope.launch {
            wordRepository.getFavoriteWords().collect { words ->
                _uiState.update {
                    it.copy(
                        favoriteWords = words,
                        filteredWords = words,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterWords()
    }

    fun onLetterFilterChange(letter: Char?) {
        _uiState.update { it.copy(selectedLetter = letter) }
        filterWords()
    }

    private fun filterWords() {
        val currentState = _uiState.value
        var filtered = currentState.favoriteWords

        if (currentState.selectedLetter != null) {
            filtered = filtered.filter {
                it.word.first().uppercaseChar() == currentState.selectedLetter
            }
        }

        if (currentState.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.word.contains(currentState.searchQuery, ignoreCase = true) ||
                        it.meaning.contains(currentState.searchQuery, ignoreCase = true)
            }
        }

        _uiState.update { it.copy(filteredWords = filtered) }
    }

    fun onWordClick(word: Word) {
        _uiState.update {
            it.copy(
                selectedWord = word,
                showBottomSheet = true
            )
        }
    }

    fun onDismissBottomSheet() {
        _uiState.update {
            it.copy(
                selectedWord = null,
                showBottomSheet = false
            )
        }
    }

    fun onToggleFavorite(wordId: Long) {
        viewModelScope.launch {
            wordRepository.toggleFavorite(wordId)
        }
    }
}
