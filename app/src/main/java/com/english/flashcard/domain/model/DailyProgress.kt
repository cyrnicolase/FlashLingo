package com.english.flashcard.domain.model

import java.time.LocalDate

data class DailyProgress(
    val id: Long = 0,
    val date: LocalDate,
    val newWordsLearned: Int = 0,
    val wordsReviewed: Int = 0,
    val correctCount: Int = 0,
    val totalCount: Int = 0
) {
    val accuracy: Float
        get() = if (totalCount > 0) correctCount.toFloat() / totalCount else 0f

    val totalWords: Int
        get() = newWordsLearned + wordsReviewed
}
