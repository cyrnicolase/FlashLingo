package com.english.flashcard.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.english.flashcard.domain.model.Word
import com.english.flashcard.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadWords()
    }

    private fun loadWords() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            wordRepository.getAllWords().collect { words ->
                val grouped = words
                    .filter { it.word.isNotBlank() }
                    .groupBy { it.word.first().uppercaseChar() }
                    .toSortedMap()
                _uiState.update {
                    it.copy(
                        words = words,
                        groupedWords = grouped,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            loadWords()
        } else {
            searchWords(query)
        }
    }

    private fun searchWords(query: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            wordRepository.searchWords(query).collect { words ->
                val grouped = words
                    .filter { it.word.isNotBlank() }
                    .groupBy { it.word.first().uppercaseChar() }
                    .toSortedMap()
                _uiState.update {
                    it.copy(
                        words = words,
                        groupedWords = grouped
                    )
                }
            }
        }
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
