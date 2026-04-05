package com.english.flashcard.domain.usecase

import com.english.flashcard.domain.repository.WordRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(wordId: Long) {
        wordRepository.toggleFavorite(wordId)
    }
}
