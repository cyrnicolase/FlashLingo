package com.english.flashcard.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_progress",
    indices = [Index(value = ["date"], unique = true)]
)
data class DailyProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "date")
    val date: String,
    
    @ColumnInfo(name = "newWordsLearned")
    val newWordsLearned: Int = 0,
    
    @ColumnInfo(name = "wordsReviewed")
    val wordsReviewed: Int = 0,
    
    @ColumnInfo(name = "correctCount")
    val correctCount: Int = 0,
    
    @ColumnInfo(name = "totalCount")
    val totalCount: Int = 0
)
