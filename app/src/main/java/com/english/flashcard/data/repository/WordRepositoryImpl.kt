package com.english.flashcard.data.repository

import com.english.flashcard.data.local.database.dao.WordDao
import com.english.flashcard.data.local.database.entity.WordEntity
import com.english.flashcard.domain.model.Word
import com.english.flashcard.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao
) : WordRepository {
    
    override fun getAllWords(): Flow<List<Word>> =
        wordDao.getAllWords().map { entities ->
            entities.map { it.toDomain() }
        }
    
    override fun getFavoriteWords(): Flow<List<Word>> =
        wordDao.getFavoriteWords().map { entities ->
            entities.map { it.toDomain() }
        }
    
    override fun getWrongWords(): Flow<List<Word>> =
        wordDao.getWrongWords().map { entities ->
            entities.map { it.toDomain() }
        }
    
    override fun searchWords(query: String): Flow<List<Word>> =
        wordDao.searchWords(query).map { entities ->
            entities.map { it.toDomain() }
        }
    
    override fun getWordById(id: Long): Flow<Word?> =
        wordDao.getWordById(id).map { it?.toDomain() }
    
    override fun getNewWordsForToday(limit: Int): Flow<List<Word>> = flow {
        emit(wordDao.getNewWords(limit).map { it.toDomain() })
    }
    
    override fun getWordsForReview(): Flow<List<Word>> = flow {
        emit(wordDao.getWordsForReview(System.currentTimeMillis(), 50).map { it.toDomain() })
    }
    
    override suspend fun updateWord(word: Word) {
        wordDao.updateWord(word.toEntity())
    }
    
    override suspend fun toggleFavorite(wordId: Long) {
        val word = wordDao.getWordByIdOnce(wordId)
        word?.let {
            wordDao.updateFavorite(wordId, !it.isFavorite)
        }
    }
    
    override suspend fun insertWords(words: List<Word>) {
        wordDao.insertWords(words.map { it.toEntity() })
    }
    
    override suspend fun deleteWords(wordIds: List<Long>) {
        wordDao.deleteWords(wordIds)
    }
    
    override fun getWordCount(): Flow<Int> =
        wordDao.getWordCount()
    
    override fun getMasteredWordCount(): Flow<Int> =
        wordDao.getMasteredWordCount()
    
    override fun getWordsByPrefix(prefix: String): Flow<List<Word>> = flow {
        emit(wordDao.getWordsByPrefix(prefix).map { it.toDomain() })
    }
}

fun WordEntity.toDomain(): Word = Word(
    id = id,
    word = word,
    phonetic = phonetic,
    meaning = meaning,
    example = example,
    isFavorite = isFavorite,
    isMastered = isMastered,
    correctStreak = correctStreak,
    wrongCount = wrongCount,
    lastReviewAt = if (lastReviewAt != null && lastReviewAt > 0) {
        java.time.LocalDateTime.ofEpochSecond(lastReviewAt, 0, java.time.ZoneOffset.UTC)
    } else null,
    nextReviewAt = if (nextReviewAt != null && nextReviewAt > 0) {
        java.time.LocalDateTime.ofEpochSecond(nextReviewAt, 0, java.time.ZoneOffset.UTC)
    } else null,
    createdAt = if (createdAt > 0) {
        java.time.LocalDateTime.ofEpochSecond(createdAt, 0, java.time.ZoneOffset.UTC)
    } else java.time.LocalDateTime.now()
)

fun Word.toEntity(): WordEntity = WordEntity(
    id = id,
    word = word,
    phonetic = phonetic,
    meaning = meaning,
    example = example,
    isFavorite = isFavorite,
    isMastered = isMastered,
    correctStreak = correctStreak,
    wrongCount = wrongCount,
    lastReviewAt = lastReviewAt?.toEpochSecond(java.time.ZoneOffset.UTC),
    nextReviewAt = nextReviewAt?.toEpochSecond(java.time.ZoneOffset.UTC),
    createdAt = createdAt.toEpochSecond(java.time.ZoneOffset.UTC)
)
