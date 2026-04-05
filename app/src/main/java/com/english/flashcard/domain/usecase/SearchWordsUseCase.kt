package com.english.flashcard.domain.usecase

import com.english.flashcard.domain.model.Word
import com.english.flashcard.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    operator fun invoke(query: String): Flow<List<Word>> {
        return wordRepository.searchWords(query)
    }
}
