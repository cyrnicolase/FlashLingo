package com.english.flashcard.ui.screens.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.english.flashcard.domain.model.Word
import com.english.flashcard.domain.repository.WordRepository
import com.english.flashcard.domain.usecase.UpdateWordAfterAnswerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearningViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val updateWordAfterAnswerUseCase: UpdateWordAfterAnswerUseCase
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

    private var flashcardPhase = true
    private var currentWord: Word? = null

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
        currentIndex = 0
        correctCount = 0
        totalAnswered = 0
        flashcardPhase = true

        viewModelScope.launch {
            _state.value = LearningState.Loading

            when (type) {
                LearningType.Today -> {
                    newWords = wordRepository.getNewWordsForToday(10).first()
                    reviewWords = wordRepository.getWordsForReview().first()
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
            }

            val allWords = newWords + reviewWords
            if (allWords.isEmpty()) {
                _state.value = LearningState.Empty(type)
            } else {
                showFlashcard(allWords[0], allWords.size)
            }
        }
    }

    private fun showFlashcard(word: Word, total: Int) {
        currentWord = word
        flashcardPhase = true
        _state.value = LearningState.Flashcard(
            word = word,
            progress = "${currentIndex + 1}/$total"
        )
    }

    fun onFlashcardFlip() {
        val current = _state.value
        if (current is LearningState.Flashcard) {
            _state.value = current.copy(isFlipped = !current.isFlipped)
        }
    }

    fun onNextFlashcard() {
        val allWords = newWords + reviewWords
        currentIndex++

        if (currentIndex >= allWords.size) {
            finishLearning()
        } else {
            val word = allWords[currentIndex]
            flashcardPhase = false
            showQuiz(word, allWords.size)
        }
    }

    private fun showQuiz(word: Word, total: Int) {
        currentWord = word
        val options = generateQuizOptions(word)
        _state.value = LearningState.Quiz(
            word = word,
            options = options,
            progress = "${currentIndex + 1}/$total"
        )
    }

    private fun generateQuizOptions(correctWord: Word): List<String> {
        val allWords = newWords + reviewWords
        val sameLetterWords = allWords.filter {
            it.id != correctWord.id && it.word.first().lowercaseChar() == correctWord.word.first().lowercaseChar()
        }
        val otherWords = allWords.filter {
            it.id != correctWord.id && it.word.first().lowercaseChar() != correctWord.word.first().lowercaseChar()
        }

        val distractors = if (sameLetterWords.size >= 3) {
            sameLetterWords.shuffled().take(3).map { it.meaning }
        } else {
            val needed = 3 - sameLetterWords.size
            (sameLetterWords + otherWords.shuffled().take(needed)).shuffled().map { it.meaning }
        }

        val options = (distractors + correctWord.meaning).shuffled()
        return options
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

            viewModelScope.launch {
                updateWordAfterAnswerUseCase(current.word, isCorrect)
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
        val allWords = newWords + reviewWords
        currentIndex++

        if (currentIndex >= allWords.size) {
            finishLearning()
        } else {
            val word = allWords[currentIndex]
            showQuiz(word, allWords.size)
        }
    }

    private fun finishLearning() {
        val durationSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        val accuracy = if (totalAnswered > 0) correctCount.toFloat() / totalAnswered else 0f

        _state.value = LearningState.Completed(
            totalWords = totalAnswered,
            correctCount = correctCount,
            accuracy = accuracy,
            durationSeconds = durationSeconds
        )
    }
}