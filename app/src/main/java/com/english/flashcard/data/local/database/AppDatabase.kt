package com.english.flashcard.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.english.flashcard.data.local.database.dao.DailyProgressDao
import com.english.flashcard.data.local.database.dao.WordDao
import com.english.flashcard.data.local.database.entity.DailyProgressEntity
import com.english.flashcard.data.local.database.entity.WordEntity

@Database(
    entities = [WordEntity::class, DailyProgressEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun dailyProgressDao(): DailyProgressDao
    
    companion object {
        const val DATABASE_NAME = "flashcard_database"
    }
}
