package com.english.flashcard.domain.repository

import com.english.flashcard.domain.model.DailyProgress
import com.english.flashcard.domain.model.Word
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    fun getAllWords(): Flow<List<Word>>
    fun getFavoriteWords(): Flow<List<Word>>
    fun getWrongWords(): Flow<List<Word>>
    fun searchWords(query: String): Flow<List<Word>>
    fun getNewWordsForToday(limit: Int): Flow<List<Word>>
    fun getWordsForReview(): Flow<List<Word>>
    fun getRandomWords(count: Int): Flow<List<Word>>
    suspend fun updateWord(word: Word)
    suspend fun toggleFavorite(wordId: Long)
    suspend fun removeFromWrongWords(wordId: Long)
    suspend fun insertWords(words: List<Word>)
    suspend fun deleteWords(wordIds: List<Long>)
    fun getFavoriteWordCount(): Flow<Int>
    fun getMasteredWordCount(): Flow<Int>
    suspend fun saveDailyProgress(progress: DailyProgress)
    fun getDailyProgressFlow(date: String): Flow<DailyProgress?>
    suspend fun getRecentProgress(days: Int): List<DailyProgress>
}
