package com.english.flashcard.ui.screens.library

import com.english.flashcard.domain.model.Word

data class LibraryUiState(
    val words: List<Word> = emptyList(),
    val groupedWords: Map<Char, List<Word>> = emptyMap(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val selectedWord: Word? = null,
    val showBottomSheet: Boolean = false
)
