package com.english.flashcard.domain.usecase

import com.english.flashcard.domain.model.Word
import com.english.flashcard.domain.repository.WordRepository
import javax.inject.Inject

class RemoveFromWrongWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(word: Word) {
        val updatedWord = word.copy(
            correctStreak = 3,
            wrongCount = 0,
            isMastered = true
        )
        wordRepository.updateWord(updatedWord)
    }
}
