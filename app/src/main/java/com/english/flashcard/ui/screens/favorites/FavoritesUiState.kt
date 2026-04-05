package com.english.flashcard.ui.screens.library

import com.english.flashcard.domain.model.Word

data class FavoritesUiState(
    val favoriteWords: List<Word> = emptyList(),
    val filteredWords: List<Word> = emptyList(),
    val selectedLetter: Char? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val selectedWord: Word? = null,
    val showBottomSheet: Boolean = false
)
