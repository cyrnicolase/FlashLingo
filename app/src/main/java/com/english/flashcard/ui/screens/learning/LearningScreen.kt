package com.english.flashcard.ui.screens.learning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.english.flashcard.ui.components.EmptyState
import com.english.flashcard.ui.components.FlashCard
import com.english.flashcard.ui.components.QuizOption
import com.english.flashcard.ui.navigation.LearningType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    learningType: String,
    onClose: () -> Unit,
    onComplete: (Int, Int, Float, Long) -> Unit,
    viewModel: LearningViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val type = LearningType.entries.find { it.value == learningType } ?: LearningType.Today

    LaunchedEffect(type) {
        viewModel.startLearning(type)
    }

    LaunchedEffect(state) {
        if (state is LearningState.Completed) {
            val completed = state as LearningState.Completed
            onComplete(completed.totalWords, completed.correctCount, completed.accuracy, completed.durationSeconds.toLong())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("学习") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val currentState = state) {
                is LearningState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is LearningState.Empty -> {
                    EmptyState(
                        emoji = "📚",
                        title = "没有要学习的内容",
                        message = getEmptyMessage(currentState.type)
                    )
                }
                is LearningState.Flashcard -> {
                    FlashcardContent(
                        state = currentState,
                        onFlip = { viewModel.onFlashcardFlip() },
                        onNext = { viewModel.onNextFlashcard() },
                        onPrevious = { viewModel.onPreviousFlashcard() },
                        isFavorite = currentState.word.isFavorite,
                        onToggleFavorite = { viewModel.toggleFavorite() }
                    )
                }
                is LearningState.Quiz -> {
                    QuizContent(
                        state = currentState,
                        onSelectAnswer = { viewModel.onSelectAnswer(it) },
                        onNext = { viewModel.onNextQuestion() },
                        onPrevious = { viewModel.onPreviousQuestion() },
                        isFavorite = currentState.word.isFavorite,
                        onToggleFavorite = { viewModel.toggleFavorite() }
                    )
                }
                is LearningState.Completed -> {
                    CompletionContent(state = currentState)
                }
            }
        }
    }
}

@Composable
private fun FlashcardContent(
    state: LearningState.Flashcard,
    onFlip: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.progress,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "取消收藏" else "收藏",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        FlashCard(
            word = state.word,
            isFlipped = state.isFlipped,
            onFlip = onFlip,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            state.isFirst -> {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("下一张")
                }
            }
            state.isLast -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onPrevious,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("上一张")
                    }
                    Button(
                        onClick = onNext,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("进入测试")
                    }
                }
            }
            else -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onPrevious,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("上一张")
                    }
                    Button(
                        onClick = onNext,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("下一张")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizContent(
    state: LearningState.Quiz,
    onSelectAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.progress,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "取消收藏" else "收藏",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = state.word.word,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (state.word.phonetic.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.word.phonetic,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            state.options.forEachIndexed { index, option ->
                QuizOption(
                    text = option,
                    index = index,
                    isSelected = state.selectedIndex == index,
                    isCorrect = if (state.showFeedback) index == state.correctIndex else null,
                    showResult = state.showFeedback,
                    onClick = { if (!state.showFeedback) onSelectAnswer(index) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (state.showFeedback) {
            Spacer(modifier = Modifier.height(16.dp))

            if (state.isCorrect == true) {
                Text(
                    text = "正确！",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                state.encouragingMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                state.isFirst && state.isLast -> {
                    Button(
                        onClick = onPrevious,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("完成")
                    }
                }
                state.isLast -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = onPrevious,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("上一题")
                        }
                        Button(
                            onClick = onNext,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("完成")
                        }
                    }
                }
                state.isFirst -> {
                    Button(
                        onClick = onNext,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("下一题")
                    }
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = onPrevious,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("上一题")
                        }
                        Button(
                            onClick = onNext,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("下一题")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionContent(state: LearningState.Completed) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "学习完成！",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("总题数", state.totalWords.toString())
            StatItem("正确", state.correctCount.toString())
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("正确率", "${(state.accuracy * 100).toInt()}%")
            StatItem("用时", "${state.durationSeconds}秒")
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getEmptyMessage(type: LearningType): String {
    return when (type) {
        LearningType.Today -> "今天没有需要学习的新单词"
        LearningType.Review -> "没有需要复习的单词"
        LearningType.WrongWords -> "恭喜！没有错题了"
        LearningType.Favorites -> "收藏夹是空的"
    }
}