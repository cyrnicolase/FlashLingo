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
        val today = LocalDate.now().toString()
        return wordRepository.getDailyProgressFlow(today).map { progress ->
            progress ?: DailyProgress(
                id = 0,
                date = LocalDate.parse(today),
                newWordsLearned = 0,
                wordsReviewed = 0,
                correctCount = 0,
                totalCount = 0
            )
        }
    }
}
