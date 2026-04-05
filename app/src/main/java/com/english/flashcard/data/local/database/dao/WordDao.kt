package com.english.flashcard.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.english.flashcard.data.local.database.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)
    
    @Update
    suspend fun updateWord(word: WordEntity)
    
    @Delete
    suspend fun deleteWord(word: WordEntity)
    
    @Query("SELECT * FROM words ORDER BY createdAt ASC")
    fun getAllWords(): Flow<List<WordEntity>>
    
    @Query("SELECT * FROM words WHERE isFavorite = 1 ORDER BY word ASC")
    fun getFavoriteWords(): Flow<List<WordEntity>>
    
    @Query("SELECT * FROM words WHERE wrongCount > 0 AND correctStreak < 3 ORDER BY lastReviewAt ASC")
    fun getWrongWords(): Flow<List<WordEntity>>
    
    @Query("SELECT * FROM words WHERE word LIKE '%' || :query || '%' OR meaning LIKE '%' || :query || '%' ORDER BY word ASC")
    fun searchWords(query: String): Flow<List<WordEntity>>
    
    @Query("SELECT * FROM words WHERE id = :id")
    fun getWordById(id: Long): Flow<WordEntity?>
    
    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordByIdOnce(id: Long): WordEntity?
    
    @Query("SELECT * FROM words WHERE word = :word LIMIT 1")
    suspend fun getWordByWord(word: String): WordEntity?
    
    @Query("SELECT * FROM words WHERE nextReviewAt <= :currentTime ORDER BY nextReviewAt ASC LIMIT :limit")
    suspend fun getWordsForReview(currentTime: Long, limit: Int): List<WordEntity>
    
    @Query("SELECT * FROM words WHERE lastReviewAt IS NULL ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getNewWords(limit: Int): List<WordEntity>
    
    @Query("SELECT COUNT(*) FROM words")
    fun getWordCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM words WHERE isMastered = 1")
    fun getMasteredWordCount(): Flow<Int>
    
    @Query("SELECT * FROM words WHERE word LIKE :prefix || '%' ORDER BY word ASC")
    suspend fun getWordsByPrefix(prefix: String): List<WordEntity>
    
    @Query("DELETE FROM words WHERE id IN (:ids)")
    suspend fun deleteWords(ids: List<Long>)
    
    @Query("UPDATE words SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)
}
