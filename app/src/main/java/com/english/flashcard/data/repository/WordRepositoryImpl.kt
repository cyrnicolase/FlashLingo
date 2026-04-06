package com.english.flashcard.data.repository

import com.english.flashcard.data.local.database.dao.DailyProgressDao
import com.english.flashcard.data.local.database.dao.WordDao
import com.english.flashcard.data.local.database.entity.DailyProgressEntity
import com.english.flashcard.data.local.database.entity.WordEntity
import com.english.flashcard.domain.model.DailyProgress
import com.english.flashcard.domain.model.Phrase
import com.english.flashcard.domain.model.Sentence
import com.english.flashcard.domain.model.Translation
import com.english.flashcard.domain.model.Word
import com.english.flashcard.domain.repository.WordRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

private val gson = Gson()

@Singleton
class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao,
    private val dailyProgressDao: DailyProgressDao
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
    
    override fun getNewWordsForToday(limit: Int): Flow<List<Word>> = flow {
        emit(wordDao.getNewWords(limit).map { it.toDomain() })
    }
    
    override fun getWordsForReview(): Flow<List<Word>> = flow {
        emit(wordDao.getWordsForReview(System.currentTimeMillis(), 50).map { it.toDomain() })
    }
    
    override fun getRandomWords(count: Int): Flow<List<Word>> = flow {
        emit(wordDao.getRandomWords(count).map { it.toDomain() })
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

    override suspend fun removeFromWrongWords(wordId: Long) {
        wordDao.resetWrongCount(wordId)
    }
    
    override suspend fun insertWords(words: List<Word>) {
        wordDao.insertWords(words.map { it.toEntity() })
    }
    
    override suspend fun deleteWords(wordIds: List<Long>) {
        wordDao.deleteWords(wordIds)
    }
    
    override fun getFavoriteWordCount(): Flow<Int> =
        wordDao.getFavoriteWordCount()

    override fun getMasteredWordCount(): Flow<Int> =
        wordDao.getMasteredWordCount()

    override suspend fun saveDailyProgress(progress: DailyProgress) {
        dailyProgressDao.insertOrUpdate(progress.toEntity())
    }

    override fun getDailyProgressFlow(date: String): Flow<DailyProgress?> =
        dailyProgressDao.getProgressByDateFlow(date).map { it?.toDomain() }

    override suspend fun getRecentProgress(days: Int): List<DailyProgress> =
        dailyProgressDao.getRecentProgress(days).map { it.toDomain() }
}

fun WordEntity.toDomain(): Word {
    val phraseList: List<Phrase> = try {
        gson.fromJson(phrases, object : TypeToken<List<Phrase>>() {}.type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    
    val translationsList: List<Translation> = try {
        gson.fromJson(translations, object : TypeToken<List<Translation>>() {}.type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    
    val sentencesList: List<Sentence> = try {
        gson.fromJson(sentences, object : TypeToken<List<Sentence>>() {}.type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    
    return Word(
        id = id,
        word = word,
        phonetic = phonetic,
        meaning = meaning,
        isFavorite = isFavorite,
        isMastered = isMastered,
        correctStreak = correctStreak,
        wrongCount = wrongCount,
        lastReviewAt = if (lastReviewAt != null && lastReviewAt > 0) {
            LocalDateTime.ofEpochSecond(lastReviewAt, 0, ZoneOffset.UTC)
        } else null,
        nextReviewAt = if (nextReviewAt != null && nextReviewAt > 0) {
            LocalDateTime.ofEpochSecond(nextReviewAt, 0, ZoneOffset.UTC)
        } else null,
        createdAt = if (createdAt > 0) {
            LocalDateTime.ofEpochSecond(createdAt, 0, ZoneOffset.UTC)
        } else LocalDateTime.now(),
        phrases = phraseList,
        translations = translationsList,
        sentences = sentencesList,
        partOfSpeech = partOfSpeech
    )
}

fun Word.toEntity(): WordEntity = WordEntity(
    id = id,
    word = word,
    phonetic = phonetic,
    meaning = meaning,
    isFavorite = isFavorite,
    isMastered = isMastered,
    correctStreak = correctStreak,
    wrongCount = wrongCount,
    lastReviewAt = lastReviewAt?.toEpochSecond(ZoneOffset.UTC),
    nextReviewAt = nextReviewAt?.toEpochSecond(ZoneOffset.UTC),
    createdAt = createdAt.toEpochSecond(ZoneOffset.UTC),
    phrases = gson.toJson(phrases),
    translations = gson.toJson(translations),
    sentences = gson.toJson(sentences),
    partOfSpeech = partOfSpeech
)

fun DailyProgressEntity.toDomain(): DailyProgress = DailyProgress(
    id = id,
    date = LocalDate.parse(date),
    newWordsLearned = newWordsLearned,
    wordsReviewed = wordsReviewed,
    correctCount = correctCount,
    totalCount = totalCount
)

fun DailyProgress.toEntity(): DailyProgressEntity = DailyProgressEntity(
    id = id,
    date = date.toString(),
    newWordsLearned = newWordsLearned,
    wordsReviewed = wordsReviewed,
    correctCount = correctCount,
    totalCount = totalCount
)
