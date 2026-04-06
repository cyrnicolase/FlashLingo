package com.english.flashcard.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.english.flashcard.data.local.database.dao.DailyProgressDao
import com.english.flashcard.data.local.database.dao.WordDao
import com.english.flashcard.data.local.database.entity.DailyProgressEntity
import com.english.flashcard.data.local.database.entity.WordEntity

@Database(
    entities = [WordEntity::class, DailyProgressEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun dailyProgressDao(): DailyProgressDao
    
    companion object {
        const val DATABASE_NAME = "flashcard_database"
        
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE words ADD COLUMN phrases TEXT DEFAULT '[]'")
            }
        }
        
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE words ADD COLUMN partOfSpeech TEXT DEFAULT ''")
            }
        }
        
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE words ADD COLUMN translations TEXT DEFAULT '[]'")
            }
        }
        
        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE words ADD COLUMN sentences TEXT DEFAULT '[]'")
            }
        }
        
        val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX IF NOT EXISTS index_words_wrongCount_correctStreak ON words (wrongCount, correctStreak)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_words_lastReviewAt ON words (lastReviewAt)")
            }
        }
        
        fun getMigrations(): Array<Migration> = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6
        )
    }
}
