package com.english.flashcard.domain.usecase

import com.english.flashcard.domain.model.Word
import com.english.flashcard.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReviewWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(): Flow<List<Word>> {
        return wordRepository.getWordsForReview()
    }
}
