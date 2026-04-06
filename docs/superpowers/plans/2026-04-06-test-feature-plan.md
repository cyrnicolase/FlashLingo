# 开始测试功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在首页新增「开始测试」入口，随机抽取词库中的单词进行测试，测试结果不计入今日学习进度。

**Architecture:** 新增 `LearningType.Test` 枚举值，复用现有 `LearningScreen`，通过 `LearningViewModel` 区分行为：Test 模式随机选词、直接进入 Quiz、不更新每日进度。

**Tech Stack:** Jetpack Compose, Hilt, Room, DataStore, Navigation Compose

---

## 文件改动总览

| 文件 | 改动 |
|------|------|
| `Screen.kt` | LearningType 新增 Test |
| `UserPreferences.kt` | 新增 test_word_count |
| `SettingsUiState.kt` | 新增 testWordCount |
| `SettingsViewModel.kt` | 加载和保存 testWordCount |
| `SettingsScreen.kt` | 新增测试词数设置项 |
| `HomeScreen.kt` | 新增开始测试按钮 |
| `HomeUiState.kt` | 新增导航回调 |
| `NavGraph.kt` | 传递 testWordCount 到 LearningScreen |
| `LearningScreen.kt` | 新增 testWordCount 参数 |
| `LearningViewModel.kt` | Test 模式逻辑 |
| `WordRepository.kt` | 新增 getRandomWords 方法 |
| `WordRepositoryImpl.kt` | 实现 getRandomWords |
| `WordDao.kt` | 新增随机查询方法 |

---

## Task 1: 添加 Test 类型和偏好设置

### Screen.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/ui/navigation/Screen.kt:22-27`

- [ ] **Step 1: 修改 LearningType 枚举**

```kotlin
enum class LearningType(val value: String) {
    Today("today"),
    Review("review"),
    WrongWords("wrong"),
    Favorites("favorites"),
    Test("test")
}
```

---

### UserPreferences.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/data/local/preferences/UserPreferences.kt`

- [ ] **Step 1: 新增 KEY_TEST_WORD_COUNT 常量**

在 `companion object` 中添加:
```kotlin
private val KEY_TEST_WORD_COUNT = intPreferencesKey("test_word_count")
```

- [ ] **Step 2: 新增 testWordCount Flow**

```kotlin
val testWordCount: Flow<Int> = dataStore.data.map { preferences ->
    preferences[KEY_TEST_WORD_COUNT] ?: 10
}
```

- [ ] **Step 3: 新增 setTestWordCount 方法**

```kotlin
suspend fun setTestWordCount(count: Int) {
    dataStore.edit { preferences ->
        preferences[KEY_TEST_WORD_COUNT] = count
    }
}
```

---

### SettingsUiState.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/ui/screens/settings/SettingsUiState.kt`

- [ ] **Step 1: 新增 testWordCount 字段**

```kotlin
data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val dailyNewWords: Int = 20,
    val testWordCount: Int = 10,
    val version: String = "1.0.0"
)
```

---

## Task 2: 设置页面新增测试词数选项

### SettingsViewModel.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/ui/screens/settings/SettingsViewModel.kt`

- [ ] **Step 1: 修改 loadSettings 中的 combine**

```kotlin
combine(
    userPreferences.isDarkMode,
    userPreferences.dailyNewWords,
    userPreferences.testWordCount
) { isDarkMode, dailyNewWords, testWordCount ->
    SettingsUiState(
        isDarkMode = isDarkMode,
        dailyNewWords = dailyNewWords,
        testWordCount = testWordCount,
        version = "1.0.0"
    )
}
```

- [ ] **Step 2: 新增 setTestWordCount 方法**

```kotlin
fun setTestWordCount(count: Int) {
    viewModelScope.launch {
        userPreferences.setTestWordCount(count)
    }
}
```

---

### SettingsScreen.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/ui/screens/settings/SettingsScreen.kt`

- [ ] **Step 1: 在学习设置区新增测试词数项**

在 `DailyNewWordsItem` 下方添加:
```kotlin
TestWordCountItem(
    currentValue = uiState.testWordCount,
    onValueChange = { viewModel.setTestWordCount(it) }
)
```

- [ ] **Step 2: 新增 TestWordCountItem 组件**

```kotlin
@Composable
private fun TestWordCountItem(
    currentValue: Int,
    onValueChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(5, 10, 15, 20, 30)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "测试词数",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = currentValue.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString()) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
```

---

## Task 3: 首页新增开始测试入口

### HomeUiState.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/ui/screens/home/HomeUiState.kt`

- [ ] **Step 1: 新增导航回调**

```kotlin
data class HomeUiState(
    val showOnboarding: Boolean = false,
    val todayDate: String = "",
    val todayNewWords: Int = 0,
    val todayProgress: DailyProgress? = null,
    val todayProgressPercent: Int = 0,
    val wrongWordCount: Int = 0,
    val favoriteCount: Int = 0,
    val masteredCount: Int = 0,
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val error: String? = null
)
```

---

### HomeScreen.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/ui/screens/home/HomeScreen.kt:46-50`

- [ ] **Step 1: 新增 onStartTest 回调参数**

```kotlin
@Composable
fun HomeScreen(
    onStartLearning: () -> Unit = {},
    onStartTest: () -> Unit = {},
    onNavigateToWrongWords: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
)
```

- [ ] **Step 2: 在 TodayTaskCard 中添加开始测试按钮**

在 `TodayTaskCard` 组件的 `开始学习` 按钮下方添加:
```kotlin
Spacer(modifier = Modifier.height(8.dp))

Box(
    modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.secondaryContainer)
        .clickable { onStartTest() }
        .padding(vertical = 14.dp),
    contentAlignment = Alignment.Center
) {
    Text(
        text = "开始测试",
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSecondaryContainer
    )
}
```

- [ ] **Step 3: 更新 TodayTaskCard 参数**

```kotlin
@Composable
private fun TodayTaskCard(
    date: String,
    newWords: Int,
    progressPercent: Int,
    onStartLearning: () -> Unit,
    onStartTest: () -> Unit
)
```

调用处传入 `onStartTest = onStartTest`

---

### NavGraph.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/ui/navigation/NavGraph.kt:36-46`

- [ ] **Step 1: HomeScreen 新增 onStartTest**

```kotlin
HomeScreen(
    onStartLearning = {
        navController.navigate(Screen.Learning.createRoute(LearningType.Today))
    },
    onStartTest = {
        navController.navigate(Screen.Learning.createRoute(LearningType.Test))
    },
    onNavigateToWrongWords = {
        navController.navigate(Screen.Learning.createRoute(LearningType.WrongWords))
    },
    onNavigateToFavorites = {
        navController.navigate(Screen.Favorites.route)
    }
)
```

---

## Task 4: LearningScreen 支持 Test 模式

### LearningScreen.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/ui/screens/learning/LearningScreen.kt`

- [ ] **Step 1: 新增 testWordCount 参数**

```kotlin
@Composable
fun LearningScreen(
    learningType: String,
    testWordCount: Int = 10,
    onClose: () -> Unit = {},
    onComplete: (totalWords: Int, correctCount: Int, accuracy: Float, duration: Long) -> Unit = { _, _, _, _ -> }
)
```

- [ ] **Step 2: 将 testWordCount 传给 ViewModel**

在调用 `startLearning` 时或 ViewModel 初始化时传递。

---

### LearningViewModel.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/ui/screens/learning/LearningViewModel.kt`

- [ ] **Step 1: 新增 testWordCount 变量**

```kotlin
private var testWordCount: Int = 10
```

- [ ] **Step 2: 修改 startLearning 方法签名**

```kotlin
fun startLearning(type: LearningType, testWordCount: Int = 10) {
    this.testWordCount = testWordCount
    learningType = type
    startTime = System.currentTimeMillis()
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
                val randomWords = wordRepository.getRandomWords(testWordCount).first()
                shuffledAllWords = randomWords
                if (shuffledAllWords.isEmpty()) {
                    _state.value = LearningState.Empty(type)
                } else {
                    currentIndex = 0
                    showQuiz(shuffledAllWords[0], shuffledAllWords.size, shuffledAllWords)
                }
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
```

- [ ] **Step 3: 修改 finishLearning 方法**

```kotlin
private fun finishLearning() {
    val durationSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
    val accuracy = if (totalAnswered > 0) correctCount.toFloat() / totalAnswered else 0f

    if (learningType != LearningType.Test) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val existingProgress = wordRepository.getDailyProgressFlow(today.toString()).first()
            
            val updatedProgress = DailyProgress(
                id = existingProgress?.id ?: 0,
                date = today,
                newWordsLearned = (existingProgress?.newWordsLearned ?: 0) + newWords.size,
                wordsReviewed = (existingProgress?.wordsReviewed ?: 0) + totalAnswered,
                correctCount = (existingProgress?.correctCount ?: 0) + correctCount,
                totalCount = (existingProgress?.totalCount ?: 0) + totalAnswered
            )
            wordRepository.saveDailyProgress(updatedProgress)
        }
    }

    _state.value = LearningState.Completed(
        totalWords = totalAnswered,
        correctCount = correctCount,
        accuracy = accuracy,
        durationSeconds = durationSeconds
    )
}
```

- [ ] **Step 4: 修改 onSelectAnswer 方法**

跳过 Test 模式的单词更新:

```kotlin
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
```

---

## Task 5: WordRepository 新增随机查询

### WordRepository.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/domain/repository/WordRepository.kt`

- [ ] **Step 1: 新增 getRandomWords 方法**

```kotlin
fun getRandomWords(count: Int): Flow<List<Word>>
```

---

### WordDao.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/data/local/database/dao/WordDao.kt`

- [ ] **Step 1: 新增 getRandomWords 方法**

```kotlin
@Query("SELECT * FROM words ORDER BY RANDOM() LIMIT :count")
fun getRandomWords(count: Int): Flow<List<WordEntity>>
```

---

### WordRepositoryImpl.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/data/repository/WordRepositoryImpl.kt`

- [ ] **Step 1: 实现 getRandomWords**

```kotlin
override fun getRandomWords(count: Int): Flow<List<Word>> {
    return wordDao.getRandomWords(count).map { entities ->
        entities.map { it.toDomain() }
    }
}
```

---

## Task 6: 传递 testWordCount 到 LearningScreen

### 方案

修改 `Screen.Learning` 路由支持 `testWordCount` 参数，`HomeScreen` 从 `HomeViewModel` 获取配置后传递。

### Screen.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/ui/navigation/Screen.kt`

- [ ] **Step 1: 修改 Learning route 和 createRoute**

```kotlin
data object Learning : Screen("learning/{learningType}") {
    fun createRoute(learningType: LearningType, testWordCount: Int = 0): String = 
        "learning/${learningType.value}?testWordCount=$testWordCount"
}
```

- [ ] **Step 2: 新增 TestWordCount 参数**

在 `Screen.kt` 中添加:
```kotlin
const val DEFAULT_TEST_WORD_COUNT = 10
```

### NavGraph.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/ui/navigation/NavGraph.kt`

- [ ] **Step 1: 修改 Learning route 匹配带参数的路由**

```kotlin
composable(
    route = Screen.Learning.route + "?testWordCount={testWordCount}",
    arguments = listOf(
        navArgument("learningType") { type = NavType.StringType },
        navArgument("testWordCount") { 
            type = NavType.IntType 
            defaultValue = 0 
        }
    )
) { backStackEntry ->
    val learningType = backStackEntry.arguments?.getString("learningType") ?: "today"
    val testWordCount = backStackEntry.arguments?.getInt("testWordCount") ?: 10
    LearningScreen(
        learningType = learningType,
        testWordCount = testWordCount,
        onClose = { navController.popBackStack() },
        onComplete = { totalWords, correctCount, accuracy, duration ->
            navController.navigate(
                Screen.Completion.createRoute(totalWords, correctCount, accuracy, duration)
            ) {
                popUpTo(Screen.Home.route)
            }
        }
    )
}
```

### HomeViewModel.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/ui/screens/home/HomeViewModel.kt`

- [ ] **Step 1: 新增 getTestWordCount 方法**

```kotlin
fun getTestWordCount(): Int {
    return userPreferences.testWordCount.first()
}
```

### HomeScreen.kt

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/ui/screens/home/HomeScreen.kt`

- [ ] **Step 1: 在点击开始测试时获取 testWordCount 并导航**

```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.secondaryContainer)
        .clickable { 
            onStartTest(viewModel.getTestWordCount())
        }
        .padding(vertical = 14.dp),
    contentAlignment = Alignment.Center
) {
    Text(
        text = "开始测试",
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSecondaryContainer
    )
}
```

- [ ] **Step 2: 修改 TodayTaskCard 参数**

```kotlin
@Composable
private fun TodayTaskCard(
    date: String,
    newWords: Int,
    progressPercent: Int,
    onStartLearning: () -> Unit,
    onStartTest: (testWordCount: Int) -> Unit
)
```

- [ ] **Step 3: 更新调用处**

```kotlin
TodayTaskCard(
    date = uiState.todayDate,
    newWords = uiState.todayNewWords,
    progressPercent = uiState.todayProgressPercent,
    onStartLearning = onStartLearning,
    onStartTest = onStartTest
)
```

### NavGraph.kt 中 HomeScreen 调用处

**Files:**
- Modify: `app/src/main/java/com/english/flashcard/ui/navigation/NavGraph.kt:36-46`

- [ ] **Step 1: 更新 HomeScreen 回调**

```kotlin
HomeScreen(
    onStartLearning = {
        navController.navigate(Screen.Learning.createRoute(LearningType.Today))
    },
    onStartTest = { testWordCount ->
        navController.navigate(Screen.Learning.createRoute(LearningType.Test, testWordCount))
    },
    onNavigateToWrongWords = {
        navController.navigate(Screen.Learning.createRoute(LearningType.WrongWords))
    },
    onNavigateToFavorites = {
        navController.navigate(Screen.Favorites.route)
    }
)
```

---

## 验证

- [ ] 运行 `make debug` 确保编译通过
- [ ] 运行 `make lint` 确保无 lint 错误

---

## 提交

```bash
git add .
git commit -m "feat: add test mode with random word quiz"
```
