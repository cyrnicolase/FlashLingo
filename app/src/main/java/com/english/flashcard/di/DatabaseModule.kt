package com.english.flashcard.di

import android.content.Context
import androidx.room.Room
import com.english.flashcard.data.local.database.AppDatabase
import com.english.flashcard.data.local.database.dao.DailyProgressDao
import com.english.flashcard.data.local.database.dao.WordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideWordDao(database: AppDatabase): WordDao {
        return database.wordDao()
    }

    @Provides
    @Singleton
    fun provideDailyProgressDao(database: AppDatabase): DailyProgressDao {
        return database.dailyProgressDao()
    }
}
