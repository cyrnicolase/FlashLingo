package com.english.flashcard.data.repository

import com.english.flashcard.data.local.preferences.UserPreferences
import com.english.flashcard.data.remote.api.WordApi
import com.english.flashcard.domain.model.Word
import com.english.flashcard.domain.repository.SyncRepository
import com.english.flashcard.domain.repository.WordRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val wordApi: WordApi,
    private val wordRepository: WordRepository,
    private val userPreferences: UserPreferences
) : SyncRepository {
    
    override suspend fun syncWords(): Result<Unit> {
        return try {
            val lastSync = userPreferences.getLastSyncTimestamp()
            val response = wordApi.syncWords(lastSync)
            
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) {
                    val words = data.words.map { dto ->
                        Word(
                            id = 0,
                            word = dto.word,
                            phonetic = dto.phonetic,
                            meaning = dto.meaning,
                            example = dto.example ?: "",
                            isFavorite = false,
                            isMastered = false,
                            correctStreak = 0,
                            wrongCount = 0,
                            lastReviewAt = null,
                            nextReviewAt = null,
                            createdAt = java.time.LocalDateTime.now()
                        )
                    }
                    wordRepository.insertWords(words)
                    
                    if (data.deleted.isNotEmpty()) {
                        wordRepository.deleteWords(data.deleted.map { it.hashCode().toLong() })
                    }
                    
                    userPreferences.setLastSyncTimestamp(data.syncTimestamp)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Sync failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
