package com.english.flashcard.ui.screens.learning

import com.english.flashcard.domain.model.Word
import com.english.flashcard.ui.navigation.LearningType

sealed class LearningState {
    object Loading : LearningState()
    data class Empty(val type: LearningType) : LearningState()
    data class Flashcard(
        val word: Word,
        val progress: String,
        val isFlipped: Boolean = false,
        val isFirst: Boolean = true,
        val isLast: Boolean = false
    ) : LearningState()
    data class Quiz(
        val word: Word,
        val options: List<String>,
        val progress: String,
        val selectedIndex: Int? = null,
        val correctIndex: Int? = null,
        val isCorrect: Boolean? = null,
        val showFeedback: Boolean = false,
        val encouragingMessage: String? = null,
        val isFirst: Boolean = true,
        val isLast: Boolean = false
    ) : LearningState()
    data class Completed(
        val totalWords: Int,
        val correctCount: Int,
        val accuracy: Float,
        val durationSeconds: Int
    ) : LearningState()
}
