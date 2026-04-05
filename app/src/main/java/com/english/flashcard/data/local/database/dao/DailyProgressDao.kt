package com.english.flashcard.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.english.flashcard.data.local.database.entity.DailyProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyProgressDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: DailyProgressEntity): Long
    
    @Query("SELECT * FROM daily_progress WHERE date = :date LIMIT 1")
    suspend fun getProgressByDate(date: String): DailyProgressEntity?
    
    @Query("SELECT * FROM daily_progress WHERE date = :date LIMIT 1")
    fun getProgressByDateFlow(date: String): Flow<DailyProgressEntity?>
    
    @Query("SELECT * FROM daily_progress ORDER BY date DESC LIMIT :days")
    suspend fun getRecentProgress(days: Int): List<DailyProgressEntity>
    
    @Query("SELECT COUNT(*) FROM daily_progress WHERE newWordsLearned > 0 OR wordsReviewed > 0")
    suspend fun getTotalStudyDays(): Int
}
