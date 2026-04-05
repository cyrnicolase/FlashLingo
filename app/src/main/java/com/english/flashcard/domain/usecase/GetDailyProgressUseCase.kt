package com.english.flashcard.domain.usecase

import com.english.flashcard.domain.model.DailyProgress
import com.english.flashcard.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetDailyProgressUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(): Flow<DailyProgress> {
        return wordRepository.getAllWords().map { words ->
            val today = LocalDate.now()
            // Filter words that were reviewed/created today based on lastReviewAt
            val todayWords = words.filter { word ->
                word.lastReviewAt?.toLocalDate() == today || word.createdAt.toLocalDate() == today
            }
            DailyProgress(
                id = 0,
                date = today,
                newWordsLearned = todayWords.count { it.isNew },
                wordsReviewed = todayWords.count { !it.isNew },
                correctCount = todayWords.sumOf { it.correctStreak },
                totalCount = todayWords.size
            )
        }
    }
}
