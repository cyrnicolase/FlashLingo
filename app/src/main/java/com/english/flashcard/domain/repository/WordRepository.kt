package com.english.flashcard.domain.repository

import com.english.flashcard.domain.model.Word
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    fun getAllWords(): Flow<List<Word>>
    fun getFavoriteWords(): Flow<List<Word>>
    fun getWrongWords(): Flow<List<Word>>
    fun searchWords(query: String): Flow<List<Word>>
    fun getWordById(id: Long): Flow<Word?>
    fun getNewWordsForToday(limit: Int): Flow<List<Word>>
    fun getWordsForReview(): Flow<List<Word>>
    suspend fun updateWord(word: Word)
    suspend fun toggleFavorite(wordId: Long)
    suspend fun insertWords(words: List<Word>)
    suspend fun deleteWords(wordIds: List<Long>)
    fun getWordCount(): Flow<Int>
    fun getMasteredWordCount(): Flow<Int>
    fun getWordsByPrefix(prefix: String): Flow<List<Word>>
}
