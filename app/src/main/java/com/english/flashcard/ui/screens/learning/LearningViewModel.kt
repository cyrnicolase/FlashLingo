package com.english.flashcard.ui.screens.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.english.flashcard.data.local.preferences.UserPreferences
import com.english.flashcard.domain.model.DailyProgress
import com.english.flashcard.domain.model.Word
import com.english.flashcard.domain.repository.WordRepository
import com.english.flashcard.domain.usecase.UpdateWordAfterAnswerUseCase
import com.english.flashcard.ui.navigation.LearningType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class LearningViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val updateWordAfterAnswerUseCase: UpdateWordAfterAnswerUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow<LearningState>(LearningState.Loading)
    val state: StateFlow<LearningState> = _state.asStateFlow()

    private var newWords: List<Word> = emptyList()
    private var reviewWords: List<Word> = emptyList()
    private var currentIndex = 0
    private var correctCount = 0
    private var totalAnswered = 0
    private var startTime = 0L
    private var learningType: LearningType = LearningType.Today
    private var learningDate: LocalDate = LocalDate.now()
    private var flashcardIndex = 0
    private var currentWord: Word? = null
    private var shuffledAllWords: List<Word> = emptyList()

    private val encouragingMessages = listOf(
        "加油！再想想！",
        "别灰心，继续加油！",
        "这词有点难，记住了！",
        "继续保持，你能行！",
        "温故知新，再接再厉！",
        "差一点就对了！",
        "熟能生巧，多练几遍！",
        "记忆需要反复，加油！"
    )

    fun startLearning(type: LearningType) {
        learningType = type
        startTime = System.currentTimeMillis()
        learningDate = LocalDate.now()
        currentIndex = 0
        correctCount = 0
        totalAnswered = 0
        flashcardIndex = 0

        viewModelScope.launch {
            _state.value = LearningState.Loading

            when (type) {
                LearningType.Today -> {
                    val dailyNewWords = userPreferences.dailyNewWords.first()
                    newWords = wordRepository.getNewWordsForToday(dailyNewWords).first()
                    reviewWords = emptyList()
                }
                LearningType.Review -> {
                    reviewWords = wordRepository.getWordsForReview().first()
                    newWords = emptyList()
                }
                LearningType.WrongWords -> {
                    reviewWords = wordRepository.getWrongWords().first()
                    newWords = emptyList()
                }
                LearningType.Favorites -> {
                    reviewWords = wordRepository.getFavoriteWords().first()
                    newWords = emptyList()
                }
                LearningType.Test -> {
                    val testCount = userPreferences.testWordCount.first()
                    shuffledAllWords = wordRepository.getRandomWords(testCount).first()
                    if (shuffledAllWords.isEmpty()) {
                        _state.value = LearningState.Empty(type)
                        return@launch
                    }
                    currentIndex = 0
                    showQuiz(shuffledAllWords[0], shuffledAllWords.size, shuffledAllWords)
                    return@launch
                }
            }

            shuffledAllWords = (newWords + reviewWords).shuffled()
            if (shuffledAllWords.isEmpty()) {
                _state.value = LearningState.Empty(type)
            } else {
                showFlashcard(shuffledAllWords[0], shuffledAllWords.size)
            }
        }
    }

    private fun showFlashcard(word: Word, total: Int) {
        currentWord = word
        _state.value = LearningState.Flashcard(
            word = word,
            progress = "${flashcardIndex + 1}/$total",
            isFirst = flashcardIndex == 0,
            isLast = flashcardIndex >= total - 1
        )
    }

    fun onFlashcardFlip() {
        val current = _state.value
        if (current is LearningState.Flashcard) {
            _state.value = current.copy(isFlipped = !current.isFlipped)
        }
    }

    fun onPreviousFlashcard() {
        if (shuffledAllWords.isEmpty()) return
        if (flashcardIndex > 0) {
            flashcardIndex--
            val word = shuffledAllWords[flashcardIndex]
            showFlashcard(word, shuffledAllWords.size)
        }
    }

    fun onNextFlashcard() {
        flashcardIndex++

        if (flashcardIndex >= shuffledAllWords.size) {
            if (learningType == LearningType.Today) {
                currentIndex = 0
                val quizWords = shuffledAllWords.shuffled()
                if (quizWords.isNotEmpty()) {
                    showQuiz(quizWords[0], quizWords.size, quizWords)
                } else {
                    finishLearning()
                }
            } else {
                finishLearning()
            }
        } else {
            val word = shuffledAllWords[flashcardIndex]
            showFlashcard(word, shuffledAllWords.size)
        }
    }

    private var quizWords: List<Word> = emptyList()

    private fun showQuiz(word: Word, total: Int, sourceList: List<Word>) {
        quizWords = sourceList
        currentWord = word
        val options = generateQuizOptions(word, sourceList)
        _state.value = LearningState.Quiz(
            word = word,
            options = options,
            progress = "${currentIndex + 1}/$total",
            isFirst = currentIndex == 0,
            isLast = currentIndex >= total - 1
        )
    }

    private fun generateQuizOptions(correctWord: Word, sourceList: List<Word>): List<String> {
        val sameLetterWords = sourceList.filter {
            it.id != correctWord.id && it.word.isNotEmpty() && it.word.first().lowercaseChar() == correctWord.word.first().lowercaseChar()
        }
        val otherWords = sourceList.filter {
            it.id != correctWord.id && (it.word.isEmpty() || it.word.first().lowercaseChar() != correctWord.word.first().lowercaseChar())
        }

        val sameLetterShuffled = sameLetterWords.shuffled()
        val otherShuffled = otherWords.shuffled()

        val distractors = if (sameLetterShuffled.size >= 3) {
            sameLetterShuffled.take(3).map { it.meaning }
        } else {
            val needed = 3 - sameLetterShuffled.size
            (sameLetterShuffled + otherShuffled.take(needed)).map { it.meaning }
        }

        return (distractors + correctWord.meaning).shuffled()
    }

    fun onSelectAnswer(index: Int) {
        val current = _state.value
        if (current is LearningState.Quiz && !current.showFeedback) {
            val selectedMeaning = current.options[index]
            val isCorrect = selectedMeaning == current.word.meaning
            val correctIndex = current.options.indexOf(current.word.meaning)

            if (isCorrect) {
                correctCount++
            }
            totalAnswered++

            if (learningType != LearningType.Test) {
                viewModelScope.launch {
                    updateWordAfterAnswerUseCase(current.word, isCorrect)
                }
            }

            val encouragingMessage = if (!isCorrect) {
                encouragingMessages.random()
            } else null

            _state.value = current.copy(
                selectedIndex = index,
                correctIndex = correctIndex,
                isCorrect = isCorrect,
                showFeedback = true,
                encouragingMessage = encouragingMessage
            )
        }
    }

    fun onNextQuestion() {
        currentIndex++

        if (currentIndex >= quizWords.size) {
            finishLearning()
        } else {
            val word = quizWords[currentIndex]
            showQuiz(word, quizWords.size, quizWords)
        }
    }

    fun onPreviousQuestion() {
        if (quizWords.isEmpty()) return
        if (currentIndex > 0) {
            currentIndex--
            val word = quizWords[currentIndex]
            showQuiz(word, quizWords.size, quizWords)
        } else if (shuffledAllWords.isNotEmpty()) {
            flashcardIndex = shuffledAllWords.size - 1
            showFlashcard(shuffledAllWords[flashcardIndex], shuffledAllWords.size)
        }
    }

    private fun finishLearning() {
        val durationSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        val accuracy = if (totalAnswered > 0) correctCount.toFloat() / totalAnswered else 0f

        if (learningType != LearningType.Test) {
            viewModelScope.launch {
                try {
                    val existingProgress = wordRepository.getDailyProgressFlow(learningDate.toString()).first()
                    
                    val updatedProgress = DailyProgress(
                        id = existingProgress?.id ?: 0,
                        date = learningDate,
                        newWordsLearned = (existingProgress?.newWordsLearned ?: 0) + newWords.size,
                        wordsReviewed = (existingProgress?.wordsReviewed ?: 0) + totalAnswered,
                        correctCount = (existingProgress?.correctCount ?: 0) + correctCount,
                        totalCount = (existingProgress?.totalCount ?: 0) + totalAnswered
                    )
                    wordRepository.saveDailyProgress(updatedProgress)
                } catch (e: Exception) {
                    android.util.Log.e("LearningViewModel", "Failed to save progress", e)
                }
            }
        }

        _state.value = LearningState.Completed(
            totalWords = totalAnswered,
            correctCount = correctCount,
            accuracy = accuracy,
            durationSeconds = durationSeconds
        )
    }

    fun toggleFavorite() {
        currentWord?.let { word ->
            viewModelScope.launch {
                wordRepository.toggleFavorite(word.id)
                val updatedWord = word.copy(isFavorite = !word.isFavorite)
                currentWord = updatedWord
                updateWordInLists(updatedWord)
                refreshCurrentState(updatedWord)
            }
        }
    }

    fun removeFromWrongWords() {
        currentWord?.let { word ->
            viewModelScope.launch {
                wordRepository.removeFromWrongWords(word.id)
                shuffledAllWords = shuffledAllWords.filter { it.id != word.id }
                if (shuffledAllWords.isEmpty()) {
                    _state.value = LearningState.Empty(learningType)
                } else {
                    if (flashcardIndex >= shuffledAllWords.size) {
                        flashcardIndex = shuffledAllWords.size - 1
                    }
                    val nextWord = shuffledAllWords[flashcardIndex]
                    showFlashcard(nextWord, shuffledAllWords.size)
                }
            }
        }
    }

    private fun updateWordInLists(word: Word) {
        newWords = newWords.map { if (it.id == word.id) word else it }
        reviewWords = reviewWords.map { if (it.id == word.id) word else it }
        shuffledAllWords = shuffledAllWords.map { if (it.id == word.id) word else it }
        quizWords = quizWords.map { if (it.id == word.id) word else it }
    }

    private fun refreshCurrentState(word: Word) {
        val current = _state.value
        _state.value = when (current) {
            is LearningState.Flashcard -> current.copy(word = word)
            is LearningState.Quiz -> current.copy(word = word)
            else -> current
        }
    }
}