package com.english.flashcard.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "words",
    indices = [
        Index(value = ["word"], unique = true),
        Index(value = ["isFavorite"]),
        Index(value = ["nextReviewAt"]),
        Index(value = ["isMastered"])
    ]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "word")
    val word: String,
    
    @ColumnInfo(name = "phonetic")
    val phonetic: String = "",
    
    @ColumnInfo(name = "meaning")
    val meaning: String,
    
    @ColumnInfo(name = "example")
    val example: String = "",
    
    @ColumnInfo(name = "isFavorite")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "isMastered")
    val isMastered: Boolean = false,
    
    @ColumnInfo(name = "correctStreak")
    val correctStreak: Int = 0,
    
    @ColumnInfo(name = "wrongCount")
    val wrongCount: Int = 0,
    
    @ColumnInfo(name = "lastReviewAt")
    val lastReviewAt: Long? = null,
    
    @ColumnInfo(name = "nextReviewAt")
    val nextReviewAt: Long? = null,
    
    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis()
)
