package com.english.flashcard.domain.usecase

import com.english.flashcard.domain.model.Word
import com.english.flashcard.domain.repository.WordRepository
import java.time.LocalDateTime
import javax.inject.Inject

class UpdateWordAfterAnswerUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(word: Word, isCorrect: Boolean) {
        val now = LocalDateTime.now()
        val updatedWord = if (isCorrect) {
            val newStreak = word.correctStreak + 1
            val cappedStreak = newStreak.coerceAtMost(Int.MAX_VALUE - 1)
            val isNowMastered = cappedStreak >= 2
            val nextReview = calculateNextReview(cappedStreak, now)
            word.copy(
                correctStreak = cappedStreak,
                isMastered = isNowMastered,
                wrongCount = 0,
                lastReviewAt = now,
                nextReviewAt = nextReview
            )
        } else {
            word.copy(
                correctStreak = 0,
                wrongCount = word.wrongCount + 1,
                isMastered = false,
                lastReviewAt = now,
                nextReviewAt = now.plusDays(1)
            )
        }
        wordRepository.updateWord(updatedWord)
    }

    private fun calculateNextReview(streak: Int, from: LocalDateTime): LocalDateTime {
        return when (streak) {
            1 -> from.plusDays(1)
            2 -> from.plusDays(3)
            else -> from.plusDays(7)
        }
    }
}
