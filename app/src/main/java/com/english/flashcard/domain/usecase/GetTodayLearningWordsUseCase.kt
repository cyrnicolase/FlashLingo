package com.english.flashcard.domain.usecase

import com.english.flashcard.domain.model.Word
import com.english.flashcard.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class TodayLearningWords(
    val newWords: List<Word>,
    val reviewWords: List<Word>
)

class GetTodayLearningWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(): Flow<TodayLearningWords> {
        return combine(
            wordRepository.getNewWordsForToday(10),
            wordRepository.getWordsForReview()
        ) { newWords, reviewWords ->
            TodayLearningWords(
                newWords = newWords,
                reviewWords = reviewWords
            )
        }
    }
}
