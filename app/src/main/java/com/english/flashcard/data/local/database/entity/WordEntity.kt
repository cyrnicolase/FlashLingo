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
        Index(value = ["isMastered"]),
        Index(value = ["wrongCount", "correctStreak"]),
        Index(value = ["lastReviewAt"])
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
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "phrases")
    val phrases: String = "[]",
    
    @ColumnInfo(name = "translations")
    val translations: String = "[]",
    
    @ColumnInfo(name = "sentences")
    val sentences: String = "[]",
    
    @ColumnInfo(name = "partOfSpeech")
    val partOfSpeech: String = ""
)
