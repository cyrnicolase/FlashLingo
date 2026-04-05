package com.english.flashcard.domain.model

import java.time.LocalDateTime

data class Word(
    val id: Long = 0,
    val word: String,
    val phonetic: String = "",
    val meaning: String,
    val example: String = "",
    val isFavorite: Boolean = false,
    val isMastered: Boolean = false,
    val correctStreak: Int = 0,
    val wrongCount: Int = 0,
    val lastReviewAt: LocalDateTime? = null,
    val nextReviewAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    val isNew: Boolean
        get() = lastReviewAt == null

    val isInWrongList: Boolean
        get() = wrongCount > 0 && correctStreak < 3

    val needsReview: Boolean
        get() = nextReviewAt != null && LocalDateTime.now() >= nextReviewAt
}
