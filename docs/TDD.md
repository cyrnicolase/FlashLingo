# 程序员英语词汇背诵 APP 技术设计文档

## 1. 项目概述

### 1.1 文档目的

本文档为程序员英语词汇背诵 APP 的技术设计文档，详细描述项目架构、模块设计、数据库设计、API 接口设计等核心技术方案，作为后续开发的蓝图和依据。

### 1.2 技术栈

| 技术项 | 选择 | 版本 |
|--------|------|------|
| 语言 | Kotlin | 1.9.x |
| 最低 Android 版本 | API 24 | Android 7.0 |
| UI 框架 | Jetpack Compose | BOM 2024.02.00 |
| 架构 | MVVM + Clean Architecture | - |
| 依赖注入 | Hilt | 2.50 |
| 数据库 | Room | 2.6.1 |
| 网络 | Retrofit + OkHttp | 2.9.0 / 4.12.0 |
| 状态管理 | ViewModel + StateFlow | - |
| 主题 | Material Design 3 | - |
| 导航 | Compose Navigation | 2.7.7 |
| 协程 | Kotlin Coroutines | 1.7.3 |

### 1.3 模块架构

```
┌─────────────────────────────────────────────────────────────┐
│                      APP Module                              │
├─────────────────────────────────────────────────────────────┤
│  UI Layer (Compose)                                          │
│  ├── screens/         页面                                   │
│  │   ├── home/        首页                                   │
│  │   ├── library/     单词库                                 │
│  │   ├── favorites/   收藏                                   │
│  │   └── learning/    学习（闪卡+测试）                      │
│  ├── components/     通用组件                                │
│  │   ├── FlashCard.kt         闪卡组件                        │
│  │   ├── QuizOption.kt        测试选项组件                    │
│  │   ├── WordBottomSheet.kt   单词详情底部弹窗                │
│  │   └── CompletionScreen.kt  学习完成庆祝页                  │
│  └── navigation/      导航配置                               │
│                                                             │
│  Domain Layer (Business Logic)                              │
│  ├── model/           领域模型                               │
│  │   ├── Word.kt      单词模型                              │
│  │   └── DailyProgress.kt  每日进度模型                      │
│  ├── repository/      仓库接口                              │
│  │   ├── WordRepository.kt                                 │
│  │   └── SyncRepository.kt                                 │
│  └── usecase/         用例                                   │
│      ├── GetTodayLearningWordsUseCase.kt                    │
│      ├── UpdateWordMasteryUseCase.kt                        │
│      └── SyncWordsUseCase.kt                                │
│                                                             │
│  Data Layer (Data Access)                                    │
│  ├── local/           本地数据                               │
│  │   ├── database/    Room 数据库                           │
│  │   │   ├── AppDatabase.kt                                │
│  │   │   ├── dao/          Data Access Objects              │
│  │   │   └── entity/       数据库实体                        │
│  │   └── preferences/   DataStore 偏好设置                  │
│  ├── remote/          远程数据                               │
│  │   ├── api/         Retrofit API 接口                     │
│  │   └── dto/         数据传输对象                          │
│  └── repository/      仓库实现                              │
│      ├── WordRepositoryImpl.kt                              │
│      └── SyncRepositoryImpl.kt                              │
│                                                             │
│  DI Layer (Dependency Injection)                             │
│  ├── AppModule.kt      应用级模块                           │
│  ├── DatabaseModule.kt 数据库模块                           │
│  ├── NetworkModule.kt  网络模块                             │
│  └── RepositoryModule.kt 仓库模块                           │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. 项目结构

### 2.1 目录结构

```
app/
├── src/main/
│   ├── java/com/english/flashcard/
│   │   ├── FlashCardApp.kt              # Application 类
│   │   │
│   │   ├── ui/
│   │   │   ├── MainActivity.kt          # 应用入口
│   │   │   ├── theme/
│   │   │   │   ├── Color.kt             # 颜色定义
│   │   │   │   ├── Type.kt              # 字体定义
│   │   │   │   ├── Shape.kt             # 形状定义
│   │   │   │   └── Theme.kt            # 主题配置
│   │   │   │
│   │   │   ├── navigation/
│   │   │   │   └── NavGraph.kt         # 导航图配置
│   │   │   │
│   │   │   ├── components/              # 通用组件（非完整页面）
│   │   │   │   ├── FlashCard.kt        # 闪卡组件
│   │   │   │   ├── QuizOption.kt       # 测试选项组件
│   │   │   │   ├── WordBottomSheet.kt  # 单词详情底部弹窗
│   │   │   │   ├── WordCard.kt         # 单词列表项
│   │   │   │   ├── EmptyState.kt       # 空状态组件
│   │   │   │   └── LoadingState.kt     # 加载状态组件
│   │   │   │
│   │   │   ├── screens/
│   │   │   │   ├── home/
│   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   ├── HomeViewModel.kt
│   │   │   │   │   └── model/
│   │   │   │   │       └── HomeUiState.kt
│   │   │   │   │
│   │   │   │   ├── library/
│   │   │   │   │   ├── LibraryScreen.kt
│   │   │   │   │   ├── LibraryViewModel.kt
│   │   │   │   │   └── model/
│   │   │   │   │       └── LibraryUiState.kt
│   │   │   │   │
│   │   │   │   ├── favorites/
│   │   │   │   │   ├── FavoritesScreen.kt
│   │   │   │   │   ├── FavoritesViewModel.kt
│   │   │   │   │   └── model/
│   │   │   │   │       └── FavoritesUiState.kt
│   │   │   │   │
│   │   │   │   ├── me/
│   │   │   │   │   ├── MeScreen.kt
│   │   │   │   │   ├── MeViewModel.kt
│   │   │   │   │   └── model/
│   │   │   │   │       └── MeUiState.kt
│   │   │   │   │
│   │   │   │   ├── learning/
│   │   │   │   │   ├── LearningScreen.kt
│   │   │   │   │   ├── LearningViewModel.kt
│   │   │   │   │   └── model/
│   │   │   │   │       └── LearningState.kt    # 合并后的学习状态
│   │   │   │   │
│   │   │   │   ├── completion/
│   │   │   │   │   └── CompletionScreen.kt    # 学习完成页（完整页面）
│   │   │   │   │
│   │   │   │   ├── onboarding/
│   │   │   │   │   └── OnboardingScreen.kt   # 首次引导页（完整页面）
│   │   │   │   │
│   │   │   │   └── settings/
│   │   │   │       ├── SettingsScreen.kt
│   │   │   │       ├── SettingsViewModel.kt
│   │   │   │       └── model/
│   │   │   │           └── SettingsUiState.kt
│   │   │   │
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── Word.kt
│   │   │   │   └── DailyProgress.kt
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── WordRepository.kt
│   │   │   │   └── SyncRepository.kt
│   │   │   │
│   │   │   └── usecase/
│   │   │       ├── GetTodayLearningWordsUseCase.kt
│   │   │       ├── GetReviewWordsUseCase.kt
│   │   │       ├── GetWrongWordsUseCase.kt
│   │   │       ├── GetFavoriteWordsUseCase.kt
│   │   │       ├── UpdateWordAfterAnswerUseCase.kt
│   │   │       ├── RemoveFromWrongWordsUseCase.kt
│   │   │       ├── ToggleFavoriteUseCase.kt
│   │   │       ├── SearchWordsUseCase.kt
│   │   │       ├── SyncWordsUseCase.kt
│   │   │       └── GetDailyProgressUseCase.kt
│   │   │
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   ├── database/
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── WordDao.kt
│   │   │   │   │   │   └── DailyProgressDao.kt
│   │   │   │   │   └── entity/
│   │   │   │   │       ├── WordEntity.kt
│   │   │   │   │       └── DailyProgressEntity.kt
│   │   │   │   │
│   │   │   │   └── preferences/
│   │   │   │       └── UserPreferences.kt  # DataStore
│   │   │   │
│   │   │   ├── remote/
│   │   │   │   ├── api/
│   │   │   │   │   └── WordApi.kt
│   │   │   │   │
│   │   │   │   └── dto/
│   │   │   │       ├── SyncResponse.kt
│   │   │   │       └── WordDto.kt
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── WordRepositoryImpl.kt
│   │   │   │   └── SyncRepositoryImpl.kt
│   │   │   │
│   │   │   └── util/
│   │   │       └── Resource.kt            # 统一错误处理封装
│   │   │
│   │   └── di/
│   │       ├── AppModule.kt
│   │       ├── DatabaseModule.kt
│   │       ├── NetworkModule.kt
│   │       └── RepositoryModule.kt
│   │
│   ├── res/
│   │   ├── values/
│   │   │   ├── strings.xml
│   │   │   └── colors.xml
│   │   │
│   │   └── raw/
│   │       └── words.json               # 内置词库 JSON
│   │
│   └── AndroidManifest.xml
│
├── build.gradle.kts (project)
├── build.gradle.kts (app)
└── settings.gradle.kts
```

### 2.2 关键类说明

| 类名 | 包路径 | 职责 |
|------|--------|------|
| FlashCardApp | di | Application 类，初始化 Hilt |
| MainActivity | ui | 应用入口，Compose 生命周期管理 |
| AppDatabase | data.local.database | Room 数据库单例 |
| WordDao | data.local.database.dao | 单词 CRUD 操作 |
| WordEntity | data.local.database.entity | 单词数据库实体 |
| WordRepository | domain.repository | 单词仓库接口 |
| WordRepositoryImpl | data.repository | 单词仓库实现 |
| WordApi | data.remote.api | Retrofit API 接口 |
| SyncResponse | data.remote.dto | 同步响应 DTO |
| HomeViewModel | ui.screens.home | 首页 ViewModel |
| HomeUiState | ui.screens.home.model | 首页 UI 状态 |
| LearningViewModel | ui.screens.learning | 学习页 ViewModel |
| LearningState | ui.screens.learning.model | 学习状态（合并后） |
| CompletionScreen | ui.screens.completion | 学习完成页 |
| OnboardingScreen | ui.screens.onboarding | 首次引导页 |
| SettingsViewModel | ui.screens.settings | 设置页 ViewModel |
| SettingsUiState | ui.screens.settings.model | 设置页 UI 状态 |
| Resource | data.util | 统一错误处理封装 |

### 2.2 关键类说明

| 类名 | 包路径 | 职责 |
|------|--------|------|
| FlashCardApp | di | Application 类，初始化 Hilt |
| AppDatabase | data.local.database | Room 数据库单例 |
| WordDao | data.local.database.dao | 单词 CRUD 操作 |
| WordEntity | data.local.database.entity | 单词数据库实体 |
| WordRepository | domain.repository | 单词仓库接口 |
| WordRepositoryImpl | data.repository | 单词仓库实现 |
| WordApi | data.remote.api | Retrofit API 接口 |
| SyncResponse | data.remote.dto | 同步响应 DTO |
| HomeViewModel | ui.screens.home | 首页 ViewModel |
| LearningViewModel | ui.screens.learning | 学习页 ViewModel |

---

## 3. 数据库设计

### 3.1 ER 图

```
┌─────────────────────────┐     ┌─────────────────────────┐
│      WordEntity         │     │  DailyProgressEntity    │
├─────────────────────────┤     ├─────────────────────────┤
│ id: Long (PK, auto)     │     │ id: Long (PK, auto)     │
│ word: String (unique)  │     │ date: String (unique)   │
│ phonetic: String        │     │ newWordsLearned: Int    │
│ meaning: String         │     │ wordsReviewed: Int     │
│ example: String?        │     │ correctCount: Int      │
│ isFavorite: Boolean     │     │ totalCount: Int         │
│ isMastered: Boolean     │     └─────────────────────────┘
│ correctStreak: Int      │
│ wrongCount: Int         │
│ lastReviewAt: Long      │
│ nextReviewAt: Long      │
│ createdAt: Long         │
└─────────────────────────┘
```

### 3.2 WordEntity 表结构

```kotlin
@Entity(
    tableName = "words",
    indices = [
        Index(value = ["word"], unique = true),
        Index(value = ["isFavorite"]),
        Index(value = ["nextReviewAt"]),
        Index(value = ["isMastered"])
    ]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "word")
    val word: String,
    
    @ColumnInfo(name = "phonetic")
    val phonetic: String,
    
    @ColumnInfo(name = "meaning")
    val meaning: String,
    
    @ColumnInfo(name = "example")
    val example: String? = null,
    
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "is_mastered")
    val isMastered: Boolean = false,
    
    @ColumnInfo(name = "correct_streak")
    val correctStreak: Int = 0,
    
    @ColumnInfo(name = "wrong_count")
    val wrongCount: Int = 0,
    
    @ColumnInfo(name = "last_review_at")
    val lastReviewAt: Long = 0,
    
    @ColumnInfo(name = "next_review_at")
    val nextReviewAt: Long = 0,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
```

### 3.3 DailyProgressEntity 表结构

```kotlin
@Entity(
    tableName = "daily_progress",
    indices = [Index(value = ["date"], unique = true)]
)
data class DailyProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "date")
    val date: String,  // YYYY-MM-DD format
    
    @ColumnInfo(name = "new_words_learned")
    val newWordsLearned: Int = 0,
    
    @ColumnInfo(name = "words_reviewed")
    val wordsReviewed: Int = 0,
    
    @ColumnInfo(name = "correct_count")
    val correctCount: Int = 0,
    
    @ColumnInfo(name = "total_count")
    val totalCount: Int = 0
)
```

### 3.4 DAO 接口设计

#### WordDao

```kotlin
@Dao
interface WordDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)
    
    @Update
    suspend fun updateWord(word: WordEntity)
    
    @Delete
    suspend fun deleteWord(word: WordEntity)
    
    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Long): WordEntity?
    
    @Query("SELECT * FROM words WHERE word = :word LIMIT 1")
    suspend fun getWordByWord(word: String): WordEntity?
    
    @Query("SELECT * FROM words ORDER BY createdAt ASC")
    fun getAllWords(): Flow<List<WordEntity>>
    
    @Query("SELECT * FROM words WHERE isFavorite = 1 ORDER BY word ASC")
    fun getFavoriteWords(): Flow<List<WordEntity>>
    
    @Query("SELECT * FROM words WHERE correctStreak < 2 AND wrongCount > 0 ORDER BY lastReviewAt ASC")
    fun getWrongWords(): Flow<List<WordEntity>>
    
    @Query("SELECT * FROM words WHERE nextReviewAt <= :currentTime ORDER BY nextReviewAt ASC LIMIT :limit")
    suspend fun getWordsForReview(currentTime: Long, limit: Int): List<WordEntity>
    
    @Query("SELECT * FROM words WHERE createdAt > 0 AND id NOT IN (SELECT id FROM words WHERE correctStreak > 0) ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getNewWords(limit: Int): List<WordEntity>
    
    @Query("SELECT * FROM words WHERE word LIKE '%' || :query || '%' OR meaning LIKE '%' || :query || '%' ORDER BY word ASC")
    fun searchWords(query: String): Flow<List<WordEntity>>
    
    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int
    
    @Query("SELECT COUNT(*) FROM words WHERE correctStreak > 0")
    suspend fun getMasteredWordCount(): Int
    
    @Query("SELECT * FROM words WHERE word LIKE :prefix || '%' ORDER BY word ASC")
    suspend fun getWordsByPrefix(prefix: String): List<WordEntity>
    
    @Query("DELETE FROM words WHERE word IN (:words)")
    suspend fun deleteWords(words: List<String>)
    
    @Query("UPDATE words SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)
}
```

#### DailyProgressDao

```kotlin
@Dao
interface DailyProgressDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: DailyProgressEntity): Long
    
    @Query("SELECT * FROM daily_progress WHERE date = :date LIMIT 1")
    suspend fun getProgressByDate(date: String): DailyProgressEntity?
    
    @Query("SELECT * FROM daily_progress WHERE date = :date LIMIT 1")
    fun getProgressByDateFlow(date: String): Flow<DailyProgressEntity?>
    
    @Query("SELECT * FROM daily_progress ORDER BY date DESC LIMIT :days")
    suspend fun getRecentProgress(days: Int): List<DailyProgressEntity>
    
    @Query("SELECT COUNT(*) FROM daily_progress WHERE newWordsLearned > 0 OR wordsReviewed > 0")
    suspend fun getTotalStudyDays(): Int
}
```

---

## 4. API 接口设计

### 4.1 Retrofit 服务接口

```kotlin
interface WordApi {
    
    @GET("/api/v1/words/sync")
    suspend fun syncWords(
        @Query("since") since: Long? = null
    ): Response<SyncResponse>
    
    @POST("/api/v1/words/import")
    suspend fun importWords(
        @Body request: ImportWordsRequest
    ): Response<ImportWordsResponse>
}
```

### 4.2 DTO 数据传输对象

```kotlin
// 同步响应
data class SyncResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: SyncData?
)

data class SyncData(
    @SerializedName("words")
    val words: List<WordDto>,
    @SerializedName("deleted")
    val deleted: List<String>,
    @SerializedName("syncTimestamp")
    val syncTimestamp: Long
)

// 单词 DTO
data class WordDto(
    @SerializedName("word")
    val word: String,
    @SerializedName("phonetic")
    val phonetic: String,
    @SerializedName("meaning")
    val meaning: String,
    @SerializedName("example")
    val example: String?
)

// 导入请求
data class ImportWordsRequest(
    @SerializedName("words")
    val words: List<WordDto>
)

// 导入响应
data class ImportWordsResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: ImportData?
)

data class ImportData(
    @SerializedName("imported")
    val imported: Int,
    @SerializedName("duplicates")
    val duplicates: Int
)
```

### 4.3 OkHttp 拦截器配置

```kotlin
// 添加日志拦截器（Debug 构建）
// 添加 Auth 拦截器（预留）
// 添加错误处理拦截器
```

---

## 5. 领域模型

### 5.1 Word 模型（Domain）

```kotlin
data class Word(
    val id: Long,
    val word: String,
    val phonetic: String,
    val meaning: String,
    val example: String?,
    val isFavorite: Boolean,
    val isMastered: Boolean,
    val correctStreak: Int,
    val wrongCount: Int,
    val lastReviewAt: Long,
    val nextReviewAt: Long,
    val createdAt: Long
) {
    val isNew: Boolean
        get() = correctStreak == 0 && wrongCount == 0
    
    val isInWrongList: Boolean
        get() = wrongCount > 0 && correctStreak < 3
    
    val needsReview: Boolean
        get() = nextReviewAt <= System.currentTimeMillis()
}

// 与 Entity 互转
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
    lastReviewAt = lastReviewAt,
    nextReviewAt = nextReviewAt,
    createdAt = createdAt
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
    lastReviewAt = lastReviewAt,
    nextReviewAt = nextReviewAt,
    createdAt = createdAt
)
```

### 5.2 DailyProgress 模型（Domain）

```kotlin
data class DailyProgress(
    val id: Long,
    val date: String,
    val newWordsLearned: Int,
    val wordsReviewed: Int,
    val correctCount: Int,
    val totalCount: Int
) {
    val accuracy: Float
        get() = if (totalCount > 0) correctCount.toFloat() / totalCount else 0f
    
    val totalWords: Int
        get() = newWordsLearned + wordsReviewed
}

fun DailyProgressEntity.toDomain(): DailyProgress = DailyProgress(
    id = id,
    date = date,
    newWordsLearned = newWordsLearned,
    wordsReviewed = wordsReviewed,
    correctCount = correctCount,
    totalCount = totalCount
)
```

---

## 6. Repository 接口与实现

### 6.1 WordRepository

```kotlin
interface WordRepository {
    
    fun getAllWords(): Flow<List<Word>>
    
    fun getFavoriteWords(): Flow<List<Word>>
    
    fun getWrongWords(): Flow<List<Word>>
    
    fun searchWords(query: String): Flow<List<Word>>
    
    suspend fun getWordById(id: Long): Word?
    
    suspend fun getNewWordsForToday(limit: Int): List<Word>
    
    suspend fun getWordsForReview(limit: Int): List<Word>
    
    suspend fun updateWord(word: Word)
    
    suspend fun toggleFavorite(wordId: Long, isFavorite: Boolean)
    
    suspend fun insertWords(words: List<Word>)
    
    suspend fun deleteWords(words: List<String>)
    
    suspend fun getWordCount(): Int
    
    suspend fun getMasteredWordCount(): Int
    
    suspend fun getWordsByPrefix(prefix: String): List<Word>
}
```

### 6.2 WordRepositoryImpl

```kotlin
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
    
    override suspend fun getWordById(id: Long): Word? =
        wordDao.getWordById(id)?.toDomain()
    
    override suspend fun getNewWordsForToday(limit: Int): List<Word> =
        wordDao.getNewWords(limit).map { it.toDomain() }
    
    override suspend fun getWordsForReview(limit: Int): List<Word> {
        val currentTime = System.currentTimeMillis()
        return wordDao.getWordsForReview(currentTime, limit).map { it.toDomain() }
    }
    
    override suspend fun updateWord(word: Word) {
        wordDao.updateWord(word.toEntity())
    }
    
    override suspend fun toggleFavorite(wordId: Long, isFavorite: Boolean) {
        wordDao.updateFavorite(wordId, isFavorite)
    }
    
    override suspend fun insertWords(words: List<Word>) {
        wordDao.insertWords(words.map { it.toEntity() })
    }
    
    override suspend fun deleteWords(words: List<String>) {
        wordDao.deleteWords(words)
    }
    
    override suspend fun getWordCount(): Int =
        wordDao.getWordCount()
    
    override suspend fun getMasteredWordCount(): Int =
        wordDao.getMasteredWordCount()
    
    override suspend fun getWordsByPrefix(prefix: String): List<Word> =
        wordDao.getWordsByPrefix(prefix).map { it.toDomain() }
}
```

### 6.3 SyncRepository

```kotlin
interface SyncRepository {
    suspend fun syncWords(): Result<Unit>
    suspend fun getLastSyncTimestamp(): Long
    suspend fun setLastSyncTimestamp(timestamp: Long)
}
```

```kotlin
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
                    // 处理新增/更新
                    val words = data.words.map { dto ->
                        Word(
                            id = 0,
                            word = dto.word,
                            phonetic = dto.phonetic,
                            meaning = dto.meaning,
                            example = dto.example,
                            isFavorite = false,
                            isMastered = false,
                            correctStreak = 0,
                            wrongCount = 0,
                            lastReviewAt = 0,
                            nextReviewAt = 0,
                            createdAt = System.currentTimeMillis()
                        )
                    }
                    wordRepository.insertWords(words)
                    
                    // 处理删除
                    if (data.deleted.isNotEmpty()) {
                        wordRepository.deleteWords(data.deleted)
                    }
                    
                    // 保存同步时间戳
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
    
    override suspend fun getLastSyncTimestamp(): Long =
        userPreferences.getLastSyncTimestamp()
    
    override suspend fun setLastSyncTimestamp(timestamp: Long) {
        userPreferences.setLastSyncTimestamp(timestamp)
    }
}
```

---

## 7. UseCase 设计

### 7.1 GetTodayLearningWordsUseCase

```kotlin
class GetTodayLearningWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val userPreferences: UserPreferences
) {
    suspend operator fun invoke(): TodayLearningWords {
        val dailyNewWordsLimit = userPreferences.getDailyNewWordsLimit()
        val newWords = wordRepository.getNewWordsForToday(dailyNewWordsLimit)
        val reviewWords = wordRepository.getWordsForReview(50)
        
        return TodayLearningWords(
            newWords = newWords,
            reviewWords = reviewWords,
            totalNewWords = newWords.size,
            totalReviewWords = reviewWords.size
        )
    }
}

data class TodayLearningWords(
    val newWords: List<Word>,
    val reviewWords: List<Word>,
    val totalNewWords: Int,
    val totalReviewWords: Int
) {
    val isEmpty: Boolean
        get() = newWords.isEmpty() && reviewWords.isEmpty()
}
```

### 7.2 UpdateWordAfterAnswerUseCase

```kotlin
class UpdateWordAfterAnswerUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(word: Word, isCorrect: Boolean): Word {
        val now = System.currentTimeMillis()
        
        val updatedWord = if (isCorrect) {
            val newStreak = word.correctStreak + 1
            val isMastered = newStreak >= 2
            val nextReviewAt = calculateNextReviewAt(word, newStreak)
            
            word.copy(
                correctStreak = newStreak,
                isMastered = isMastered,
                lastReviewAt = now,
                nextReviewAt = nextReviewAt
            )
        } else {
            word.copy(
                correctStreak = 0,
                wrongCount = word.wrongCount + 1,
                isMastered = false,
                lastReviewAt = now,
                nextReviewAt = now + TimeUnit.DAYS.toMillis(1)
            )
        }
        
        wordRepository.updateWord(updatedWord)
        return updatedWord
    }
    
    private fun calculateNextReviewAt(word: Word, newStreak: Int): Long {
        val now = System.currentTimeMillis()
        return when {
            newStreak == 1 -> now + TimeUnit.DAYS.toMillis(1)
            newStreak >= 2 -> now + TimeUnit.DAYS.toMillis(3)
            else -> now + TimeUnit.DAYS.toMillis(1)
        }
    }
}
```

### 7.3 其他 UseCase

| UseCase | 职责 |
|---------|------|
| GetReviewWordsUseCase | 获取今日待复习单词 |
| GetWrongWordsUseCase | 获取错词列表 |
| GetFavoriteWordsUseCase | 获取收藏列表 |
| RemoveFromWrongWordsUseCase | 错词连续答对3次后移除 |
| ToggleFavoriteUseCase | 切换收藏状态 |
| SearchWordsUseCase | 搜索单词 |
| SyncWordsUseCase | 同步词汇 |
| GetDailyProgressUseCase | 获取每日进度 |

### 7.4 GetDailyProgressUseCase 实现

```kotlin
class GetDailyProgressUseCase @Inject constructor(
    private val dailyProgressDao: DailyProgressDao
) {
    suspend fun getTodayProgress(): DailyProgress? {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return dailyProgressDao.getProgressByDate(today)?.toDomain()
    }
    
    suspend fun updateTodayProgress(
        newWordsLearned: Int = 0,
        wordsReviewed: Int = 0,
        correctCount: Int = 0,
        totalCount: Int = 0
    ) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val existing = dailyProgressDao.getProgressByDate(today)
        
        val progress = DailyProgressEntity(
            id = existing?.id ?: 0,
            date = today,
            newWordsLearned = (existing?.newWordsLearned ?: 0) + newWordsLearned,
            wordsReviewed = (existing?.wordsReviewed ?: 0) + wordsReviewed,
            correctCount = (existing?.correctCount ?: 0) + correctCount,
            totalCount = (existing?.totalCount ?: 0) + totalCount
        )
        
        dailyProgressDao.insertOrUpdate(progress)
    }
}
```

---

## 8. ViewModel 设计

### 8.1 HomeViewModel

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val dailyProgressUseCase: GetDailyProgressUseCase,
    private val syncWordsUseCase: SyncWordsUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        checkOnboarding()
        loadData()
        syncOnStart()
    }
    
    private fun checkOnboarding() {
        viewModelScope.launch {
            userPreferences.isOnboardingShown().collect { shown ->
                _uiState.update { it.copy(showOnboarding = !shown) }
            }
        }
    }
    
    fun dismissOnboarding() {
        viewModelScope.launch {
            userPreferences.setOnboardingShown(true)
            _uiState.update { it.copy(showOnboarding = false) }
        }
    }
    
    fun loadData() {
        viewModelScope.launch {
            val todayDate = SimpleDateFormat("yyyy年M月d日", Locale.getDefault()).format(Date())
            
            val dailyNewWordsLimit = userPreferences.getDailyNewWordsLimit()
            val newWords = wordRepository.getNewWordsForToday(dailyNewWordsLimit)
            val reviewWords = wordRepository.getWordsForReview(50)
            
            val todayProgress = dailyProgressUseCase.getTodayProgress()
            val wrongWordCount = wordRepository.getWrongWords().first().size
            val favoriteCount = wordRepository.getFavoriteWords().first().size
            val masteredCount = wordRepository.getMasteredWordCount()
            
            _uiState.update {
                it.copy(
                    todayDate = todayDate,
                    todayNewWords = newWords.size,
                    todayReviewWords = reviewWords.size,
                    todayProgress = todayProgress,
                    wrongWordCount = wrongWordCount,
                    favoriteCount = favoriteCount,
                    masteredCount = masteredCount,
                    isLoading = false
                )
            }
        }
    }
    
    private fun syncOnStart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            syncWordsUseCase()
            _uiState.update { it.copy(isSyncing = false) }
            loadData()
        }
    }
}

data class HomeUiState(
    val showOnboarding: Boolean = false,
    val todayDate: String = "",
    val todayNewWords: Int = 0,
    val todayReviewWords: Int = 0,
    val todayProgress: DailyProgress? = null,
    val wrongWordCount: Int = 0,
    val favoriteCount: Int = 0,
    val masteredCount: Int = 0,
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val error: String? = null
) {
    val todayTotalTasks: Int
        get() = todayNewWords + todayReviewWords
    
    val todayCompletedTasks: Int
        get() = todayProgress?.let { it.newWordsLearned + it.wordsReviewed } ?: 0
    
    val todayProgressPercent: Int
        get() = if (todayTotalTasks > 0) {
            (todayCompletedTasks * 100 / todayTotalTasks).coerceAtMost(100)
        } else 100
}
```

### 8.2 LearningViewModel

```kotlin
@HiltViewModel
class LearningViewModel @Inject constructor(
    private val getTodayLearningWordsUseCase: GetTodayLearningWordsUseCase,
    private val getReviewWordsUseCase: GetReviewWordsUseCase,
    private val getWrongWordsUseCase: GetWrongWordsUseCase,
    private val updateWordAfterAnswerUseCase: UpdateWordAfterAnswerUseCase,
    private val wordRepository: WordRepository,
    private val getDailyProgressUseCase: GetDailyProgressUseCase
) : ViewModel() {
    
    private val _learningState = MutableStateFlow<LearningState>(LearningState.Loading)
    val learningState: StateFlow<LearningState> = _learningState.asStateFlow()
    
    private var newWords: List<Word> = emptyList()
    private var reviewWords: List<Word> = emptyList()
    private var currentWords: List<Word> = emptyList()
    private var newWordsCompleted = 0
    private var reviewWordsCompleted = 0
    private var currentIndex = 0
    private var correctCount = 0
    private var totalAnswered = 0
    private var learningStartTime = 0L
    private var currentPhase: LearningPhase = LearningPhase.None
    
    enum class LearningPhase {
        None,
        Flashcard,      // 闪卡阶段（新词）
        QuizNewWords,   // 测试阶段（新词）
        QuizReview      // 测试阶段（复习）
    }
    
    fun startLearning(learningType: LearningType) {
        learningStartTime = System.currentTimeMillis()
        currentIndex = 0
        correctCount = 0
        totalAnswered = 0
        newWordsCompleted = 0
        reviewWordsCompleted = 0
        
        viewModelScope.launch {
            when (learningType) {
                LearningType.Today -> {
                    val todayWords = getTodayLearningWordsUseCase()
                    newWords = todayWords.newWords
                    reviewWords = todayWords.reviewWords
                    
                    when {
                        newWords.isEmpty() && reviewWords.isEmpty() -> {
                            _learningState.value = LearningState.Empty(learningType)
                        }
                        newWords.isNotEmpty() -> {
                            currentWords = newWords.shuffled()
                            currentPhase = LearningPhase.Flashcard
                            _learningState.value = LearningState.Flashcard(
                                word = currentWords[0],
                                progress = "${currentIndex + 1}/${currentWords.size}"
                            )
                        }
                        reviewWords.isNotEmpty() -> {
                            currentWords = reviewWords.shuffled()
                            currentPhase = LearningPhase.QuizReview
                            startQuiz()
                        }
                    }
                }
                LearningType.Review -> {
                    reviewWords = getReviewWordsUseCase(20)
                    if (reviewWords.isEmpty()) {
                        _learningState.value = LearningState.Empty(learningType)
                    } else {
                        currentWords = reviewWords.shuffled()
                        currentPhase = LearningPhase.QuizReview
                        startQuiz()
                    }
                }
                LearningType.WrongWords -> {
                    val wrongWords = getWrongWordsUseCase()
                    if (wrongWords.isEmpty()) {
                        _learningState.value = LearningState.Empty(learningType)
                    } else {
                        currentWords = wrongWords.shuffled()
                        currentPhase = LearningPhase.QuizReview
                        startQuiz()
                    }
                }
                LearningType.Favorites -> {
                    val favoriteWords = wordRepository.getFavoriteWords().first()
                    if (favoriteWords.isEmpty()) {
                        _learningState.value = LearningState.Empty(learningType)
                    } else {
                        currentWords = favoriteWords.shuffled()
                        currentPhase = LearningPhase.QuizReview
                        startQuiz()
                    }
                }
            }
        }
    }
    
    fun onFlashcardFlip() {
        val currentState = _learningState.value
        if (currentState is LearningState.Flashcard) {
            _learningState.value = currentState.copy(isFlipped = true)
        }
    }
    
    fun onNextFlashcard() {
        currentIndex++
        if (currentIndex >= currentWords.size) {
            // 闪卡完成，进入新词测试
            currentIndex = 0
            currentPhase = LearningPhase.QuizNewWords
            startQuiz()
        } else {
            _learningState.value = LearningState.Flashcard(
                word = currentWords[currentIndex],
                progress = "${currentIndex + 1}/${currentWords.size}"
            )
        }
    }
    
    private fun startQuiz() {
        _learningState.value = LearningState.Quiz(
            word = currentWords[0],
            options = emptyList(), // 异步加载
            progress = "${currentIndex + 1}/${currentWords.size}"
        )
        loadQuizOptions(currentWords[0])
    }
    
    private suspend fun loadQuizOptions(word: Word) {
        val options = generateOptions(word)
        val currentState = _learningState.value
        if (currentState is LearningState.Quiz) {
            _learningState.value = currentState.copy(options = options)
        }
    }
    
    fun onSelectAnswer(selectedIndex: Int) {
        val currentState = _learningState.value
        if (currentState !is LearningState.Quiz) return
        
        val correctIndex = currentState.options.indexOfFirst { it == currentState.word.meaning }
        val isCorrect = selectedIndex == correctIndex
        
        totalAnswered++
        if (isCorrect) correctCount++
        
        viewModelScope.launch {
            updateWordAfterAnswerUseCase(currentState.word, isCorrect)
        }
        
        _learningState.value = currentState.copy(
            selectedIndex = selectedIndex,
            correctIndex = correctIndex,
            isCorrect = isCorrect,
            showFeedback = true,
            encouragingMessage = if (!isCorrect) getRandomEncouragement() else null
        )
    }
    
    fun onNextQuestion() {
        when (currentPhase) {
            LearningPhase.QuizNewWords -> newWordsCompleted++
            LearningPhase.QuizReview -> reviewWordsCompleted++
            else -> {}
        }
        
        currentIndex++
        
        when {
            // 还有复习词，继续
            currentPhase == LearningPhase.QuizReview && currentIndex < currentWords.size -> {
                _learningState.value = LearningState.Quiz(
                    word = currentWords[currentIndex],
                    options = emptyList(),
                    progress = "${currentIndex + 1}/${currentWords.size}"
                )
                loadQuizOptions(currentWords[currentIndex])
            }
            // 新词测试完成，有复习词
            currentPhase == LearningPhase.QuizNewWords && reviewWords.isNotEmpty() -> {
                currentWords = reviewWords.shuffled()
                currentIndex = 0
                currentPhase = LearningPhase.QuizReview
                startQuiz()
            }
            // 所有测试完成
            else -> {
                finishLearning()
            }
        }
    }
    
    private fun finishLearning() {
        val duration = System.currentTimeMillis() - learningStartTime
        
        viewModelScope.launch {
            getDailyProgressUseCase.updateTodayProgress(
                newWordsLearned = newWordsCompleted,
                wordsReviewed = reviewWordsCompleted,
                correctCount = correctCount,
                totalCount = totalAnswered
            )
        }
        
        _learningState.value = LearningState.Completed(
            totalWords = totalAnswered,
            correctCount = correctCount,
            accuracy = if (totalAnswered > 0) correctCount.toFloat() / totalAnswered else 0f,
            durationSeconds = (duration / 1000).toInt()
        )
    }
    
    private suspend fun generateOptions(correctWord: Word): List<String> {
        val usedMeanings = mutableSetOf(correctWord.meaning)
        val options = mutableListOf(correctWord.meaning)
        
        val prefix = correctWord.word.first().uppercaseChar().toString()
        val sameGroupWords = wordRepository.getWordsByPrefix(prefix)
            .filter { it.id != correctWord.id && it.meaning !in usedMeanings }
            .shuffled()
            .take(3)
        
        for (word in sameGroupWords) {
            if (word.meaning !in usedMeanings) {
                usedMeanings.add(word.meaning)
                options.add(word.meaning)
            }
        }
        
        if (options.size < 4) {
            val allOtherWords = wordRepository.getAllWords()
                .first()
                .filter { 
                    it.word.first().uppercaseChar().toString() != prefix && 
                    it.meaning !in usedMeanings 
                }
                .shuffled()
            
            for (word in allOtherWords) {
                if (options.size >= 4) break
                if (word.meaning !in usedMeanings) {
                    usedMeanings.add(word.meaning)
                    options.add(word.meaning)
                }
            }
        }
        
        return options.shuffled()
    }
    
    private fun getRandomEncouragement(): String {
        val encouragements = listOf(
            "加油！再想想！",
            "别灰心，继续加油！",
            "这词有点难，记住了！",
            "继续保持，你能行！",
            "温故知新，再接再厉！",
            "差一点就对了！",
            "熟能生巧，多练几遍！",
            "记忆需要反复，加油！"
        )
        return encouragements.random()
    }
}

sealed class LearningState {
    object Loading : LearningState()
    data class Empty(val type: LearningType) : LearningState()
    data class Flashcard(
        val word: Word,
        val progress: String,
        val isFlipped: Boolean = false
    ) : LearningState()
    data class Quiz(
        val word: Word,
        val options: List<String>,
        val progress: String,
        val selectedIndex: Int? = null,
        val correctIndex: Int? = null,
        val isCorrect: Boolean? = null,
        val showFeedback: Boolean = false,
        val encouragingMessage: String? = null
    ) : LearningState()
    data class Completed(
        val totalWords: Int,
        val correctCount: Int,
        val accuracy: Float,
        val durationSeconds: Int
    ) : LearningState()
}

enum class LearningType {
    Today,
    Review,
    WrongWords,
    Favorites
}
```

---

## 8.5 MainActivity

```kotlin
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var userPreferences: UserPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val isDarkMode by userPreferences.getIsDarkMode().collectAsState(initial = false)
            
            EnglishFlashCardTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                
                MainApp(
                    navController = navController,
                    startDestination = Screen.Onboarding.route
                )
            }
        }
    }
}

@Composable
fun MainApp(
    navController: NavHostController,
    startDestination: String
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Library.route,
        Screen.Favorites.route
    )
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavGraph(
            navController = navController,
            startDestination = startDestination
        )
    }
}
```

---

## 8.6 统一错误处理 Resource

```kotlin
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}

inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> {
    return when (this) {
        is Resource.Success -> Resource.Success(transform(data))
        is Resource.Error -> Resource.Error(message, exception)
        is Resource.Loading -> Resource.Loading
    }
}

inline fun <T> Resource<T>.onSuccess(action: (T) -> Unit): Resource<T> {
    if (this is Resource.Success) {
        action(data)
    }
    return this
}

inline fun <T> Resource<T>.onError(action: (String, Throwable?) -> Unit): Resource<T> {
    if (this is Resource.Error) {
        action(message, exception)
    }
    return this
}

suspend fun <T> safeApiCall(apiCall: suspend () -> T): Resource<T> {
    return try {
        Resource.Success(apiCall())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown error", e)
    }
}
```

---

## 9. UI 组件设计

### 9.1 FlashCard 组件

```kotlin
@Composable
fun FlashCard(
    word: Word,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "cardFlip"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { onFlip() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // 正面
                FrontContent(word = word)
            } else {
                // 背面
                BackContent(
                    word = word,
                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                )
            }
        }
    }
}

@Composable
private fun FrontContent(word: Word) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = word.word,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = word.phonetic,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "点击翻转",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BackContent(word: Word, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = word.meaning,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (!word.example.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "例句：",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = word.example,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

### 9.2 QuizOption 组件

```kotlin
@Composable
fun QuizOption(
    text: String,
    index: Int,
    state: OptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (state) {
        OptionState.Normal -> MaterialTheme.colorScheme.surface
        OptionState.Selected -> MaterialTheme.colorScheme.primaryContainer
        OptionState.Correct -> Color(0xFF4CAF50)
        OptionState.Wrong -> Color(0xFFF44336)
    }
    
    val contentColor = when (state) {
        OptionState.Normal -> MaterialTheme.colorScheme.onSurface
        OptionState.Selected -> MaterialTheme.colorScheme.onPrimaryContainer
        OptionState.Correct -> Color.White
        OptionState.Wrong -> Color.White
    }
    
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = state == OptionState.Normal) { onClick() },
        colors = CardDefaults.outlinedCardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${'A' + index}. $text",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            when (state) {
                OptionState.Correct -> Text("✓", color = Color.White)
                OptionState.Wrong -> Text("✗", color = Color.White)
                else -> {}
            }
        }
    }
}

enum class OptionState {
    Normal,
    Selected,
    Correct,
    Wrong
}
```

### 9.3 WordBottomSheet 组件

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordBottomSheet(
    word: Word,
    onDismiss: () -> Unit,
    onToggleFavorite: (Boolean) -> Unit,
    onStartReview: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = word.word,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = word.phonetic,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = word.meaning,
                style = MaterialTheme.typography.titleMedium
            )
            
            if (!word.example.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "例句：",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = word.example,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { onToggleFavorite(!word.isFavorite) }
                ) {
                    Text(if (word.isFavorite) "取消收藏" else "收藏")
                }
                Button(onClick = onStartReview) {
                    Text("复习")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
```

### 9.4 SettingsScreen

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 学习设置
            Text(
                text = "学习设置",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "每日新词数",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "选择每天学习的新单词数量",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var expanded by remember { mutableStateOf(false) }
                    val options = listOf(5, 10, 15, 20, 30, 50)
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.dailyNewWordsLimit.toString(),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.toString()) },
                                    onClick = {
                                        viewModel.setDailyNewWordsLimit(option)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 外观设置
            Text(
                text = "外观设置",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "深色模式",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 关于
            Text(
                text = "关于",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "版本",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "1.0.0",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
```

### 9.5 OnboardingScreen

```kotlin
@Composable
fun OnboardingScreen(
    onStartLearning: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📚",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "欢迎使用词汇背诵",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "专为程序员设计的英语词汇学习工具",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        FeatureCard(
            icon = "📖",
            title = "闪卡学习",
            description = "先看单词，再看释义"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FeatureCard(
            icon = "✏️",
            title = "巩固记忆",
            description = "四选一测试，答对两次掌握"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FeatureCard(
            icon = "📊",
            title = "科学复习",
            description = "根据遗忘曲线安排复习"
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onStartLearning,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("开始学习", modifier = Modifier.padding(vertical = 8.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onSkip) {
            Text("跳过")
        }
    }
}

@Composable
private fun FeatureCard(
    icon: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

### 9.6 SettingsViewModel

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            userPreferences.getIsDarkMode().collect { isDark ->
                _uiState.update { it.copy(isDarkMode = isDark) }
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(dailyNewWordsLimit = userPreferences.getDailyNewWordsLimit()) }
        }
    }
    
    fun setDailyNewWordsLimit(limit: Int) {
        viewModelScope.launch {
            userPreferences.setDailyNewWordsLimit(limit)
            _uiState.update { it.copy(dailyNewWordsLimit = limit) }
        }
    }
    
    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            userPreferences.setIsDarkMode(isDark)
            _uiState.update { it.copy(isDarkMode = isDark) }
        }
    }
}

data class SettingsUiState(
    val dailyNewWordsLimit: Int = 20,
    val isDarkMode: Boolean = false
)
```

---

## 10. 导航设计

### 10.1 导航图

```kotlin
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onStartLearning = {
                    // 关闭 onboarding 并开始学习
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                    navController.navigate(Screen.Learning.createRoute(LearningType.Today))
                },
                onSkip = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onStartLearning = {
                    navController.navigate(Screen.Learning.createRoute(LearningType.Today))
                },
                onWrongWordsReview = {
                    navController.navigate(Screen.Learning.createRoute(LearningType.WrongWords))
                },
                onFavoritesReview = {
                    navController.navigate(Screen.Learning.createRoute(LearningType.Favorites))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.Library.route) {
            LibraryScreen(
                onWordClick = { word ->
                    // 显示底部弹窗
                }
            )
        }
        
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onWordClick = { word ->
                    // 显示底部弹窗
                },
                onStartReview = {
                    navController.navigate(Screen.Learning.createRoute(LearningType.Favorites))
                }
            )
        }
        
        composable(Screen.Me.route) {
            MeScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(
            route = Screen.Learning.route,
            arguments = listOf(
                navArgument("learningType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val learningType = LearningType.valueOf(
                backStackEntry.arguments?.getString("learningType") ?: LearningType.Today.name
            )
            LearningScreen(
                learningType = learningType,
                onExit = { navController.popBackStack() },
                onComplete = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Library : Screen("library")
    object Favorites : Screen("favorites")
    object Me : Screen("me")
    object Learning : Screen("learning/{learningType}") {
        fun createRoute(type: LearningType) = "learning/${type.name}"
    }
    object Settings : Screen("settings")
}
```

### 10.2 底部导航

```kotlin
@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, Icons.Home, "首页"),
    BottomNavItem(Screen.Library.route, Icons.Library, "单词库"),
    BottomNavItem(Screen.Favorites.route, Icons.Favorite, "收藏"),
    BottomNavItem(Screen.Me.route, Icons.Person, "我的")
)
```

---

## 11. 依赖注入配置

### 11.1 AppModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideUserPreferences(
        @ApplicationContext context: Context
    ): UserPreferences = UserPreferences(context)
    
    @Provides
    @Singleton
    fun provideCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
```

### 11.2 DatabaseModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "english_flashcard.db"
    ).build()
    
    @Provides
    @Singleton
    fun provideWordDao(database: AppDatabase): WordDao = database.wordDao()
    
    @Provides
    @Singleton
    fun provideDailyProgressDao(database: AppDatabase): DailyProgressDao = database.dailyProgressDao()
}
```

### 11.3 NetworkModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val BASE_URL = "https://api.example.com/"
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    @Provides
    @Singleton
    fun provideWordApi(retrofit: Retrofit): WordApi = retrofit.create(WordApi::class.java)
}
```

### 11.4 RepositoryModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideWordRepository(wordDao: WordDao): WordRepository =
        WordRepositoryImpl(wordDao)
    
    @Provides
    @Singleton
    fun provideSyncRepository(
        wordApi: WordApi,
        wordRepository: WordRepository,
        userPreferences: UserPreferences
    ): SyncRepository = SyncRepositoryImpl(wordApi, wordRepository, userPreferences)
}
```

---

## 12. 数据初始化

### 12.1 内置词库加载

```kotlin
suspend fun initializeDatabase(context: Context, wordDao: WordDao) {
    // 检查是否已有数据
    if (wordDao.getWordCount() > 0) return
    
    // 从 assets 加载内置词库
    val jsonString = context.assets.open("words.json")
        .bufferedReader()
        .use { it.readText() }
    
    val wordList = Gson().fromJson(jsonString, Array<WordJson>::class.java)
    
    val entities = wordList.map { wordJson ->
        WordEntity(
            word = wordJson.word,
            phonetic = wordJson.phonetic,
            meaning = wordJson.meaning,
            example = wordJson.example,
            isFavorite = false,
            isMastered = false,
            correctStreak = 0,
            wrongCount = 0,
            lastReviewAt = 0,
            nextReviewAt = 0,
            createdAt = System.currentTimeMillis()
        )
    }
    
    wordDao.insertWords(entities)
}

data class WordJson(
    val word: String,
    val phonetic: String,
    val meaning: String,
    val example: String?
)
```

### 12.2 UserPreferences

```kotlin
class UserPreferences(private val context: Context) {
    
    private val dataStore = context.dataStore
    
    companion object {
        private val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        private val DAILY_NEW_WORDS_LIMIT = intPreferencesKey("daily_new_words_limit")
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val IS_ONBOARDING_SHOWN = booleanPreferencesKey("is_onboarding_shown")
    }
    
    suspend fun getLastSyncTimestamp(): Long =
        dataStore.data.first()[LAST_SYNC_TIMESTAMP] ?: 0L
    
    suspend fun setLastSyncTimestamp(timestamp: Long) {
        dataStore.edit { it[LAST_SYNC_TIMESTAMP] = timestamp }
    }
    
    suspend fun getDailyNewWordsLimit(): Int =
        dataStore.data.first()[DAILY_NEW_WORDS_LIMIT] ?: 20
    
    suspend fun setDailyNewWordsLimit(limit: Int) {
        dataStore.edit { it[DAILY_NEW_WORDS_LIMIT] = limit }
    }
    
    fun getIsDarkMode(): Flow<Boolean> = dataStore.data.map { it[IS_DARK_MODE] ?: false }
    
    suspend fun setIsDarkMode(isDark: Boolean) {
        dataStore.edit { it[IS_DARK_MODE] = isDark }
    }
    
    fun isOnboardingShown(): Flow<Boolean> = dataStore.data.map { it[IS_ONBOARDING_SHOWN] ?: false }
    
    suspend fun setOnboardingShown(shown: Boolean) {
        dataStore.edit { it[IS_ONBOARDING_SHOWN] = shown }
    }
}
```

---

## 13. 学习算法实现

### 13.1 复习间隔计算

```kotlin
object ReviewIntervalCalculator {
    
    fun calculateNextReviewTime(word: Word, isCorrect: Boolean): Long {
        val now = System.currentTimeMillis()
        
        return when {
            !isCorrect -> {
                // 答错：明天复习
                now + TimeUnit.DAYS.toMillis(1)
            }
            word.correctStreak == 0 -> {
                // 首次答对：明天复习
                now + TimeUnit.DAYS.toMillis(1)
            }
            word.correctStreak == 1 -> {
                // 第二次答对：3天后复习
                now + TimeUnit.DAYS.toMillis(3)
            }
            else -> {
                // 已掌握：7天后复习
                now + TimeUnit.DAYS.toMillis(7)
            }
        }
    }
    
    fun shouldMarkAsMastered(correctStreak: Int): Boolean = correctStreak >= 2
    
    fun shouldRemoveFromWrongList(correctStreak: Int): Boolean = correctStreak >= 3
}
```

### 13.2 每日任务计算

```kotlin
class DailyTaskCalculator(
    private val dailyNewWordsLimit: Int = 20
) {
    suspend fun calculateTodayTasks(wordRepository: WordRepository): DailyTasks {
        val newWords = wordRepository.getNewWordsForToday(dailyNewWordsLimit)
        val reviewWords = wordRepository.getWordsForReview(50)
        
        return DailyTasks(
            newWordTasks = newWords,
            reviewTasks = reviewWords,
            totalNewWords = newWords.size,
            totalReviewWords = reviewWords.size
        )
    }
}

data class DailyTasks(
    val newWordTasks: List<Word>,
    val reviewTasks: List<Word>,
    val totalNewWords: Int,
    val totalReviewWords: Int
) {
    val hasNewWords: Boolean get() = newWordTasks.isNotEmpty()
    val hasReviewWords: Boolean get() = reviewTasks.isNotEmpty()
    val isEmpty: Boolean get() = !hasNewWords && !hasReviewWords
}
```

---

## 14. 主题配置

### 14.1 颜色

```kotlin
// Light Theme
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4A90D9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4F0),
    onPrimaryContainer = Color(0xFF1A3A5C),
    
    secondary = Color(0xFF6B7280),
    onSecondary = Color.White,
    
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF333333),
    
    surface = Color(0xFFF5F5F5),
    onSurface = Color(0xFF333333),
    surfaceVariant = Color(0xFFE8E8E8),
    onSurfaceVariant = Color(0xFF666666),
    
    error = Color(0xFFF44336),
    onError = Color.White
)

// Dark Theme
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4A90D9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2A4A6C),
    onPrimaryContainer = Color(0xFFD0E4F7),
    
    secondary = Color(0xFF9E9E9E),
    onSecondary = Color.Black,
    
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFF9E9E9E),
    
    error = Color(0xFFCF6679),
    onError = Color.Black
)
```

### 14.2 字体

```kotlin
val Typography = Typography(
    displayLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
)
```

---

## 15. 构建配置

### 15.1 项目级 build.gradle.kts

```kotlin
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}
```

### 15.2 APP 级 build.gradle.kts

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.english.flashcard"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.english.flashcard"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

---

## 16. 动画规范

| 动画 | 时长 | 类型 | 参数 |
|------|------|------|------|
| 闪卡翻转 | 300ms | 3D rotationY | cameraDistance = 12f * density |
| 页面切换 | 300ms | slideInOut | horizontal |
| 底部弹窗 | 250ms | slideUp | - |
| 答题反馈 | 1000ms | fadeIn + scale | delay 500ms |
| Tab 切换 | 200ms | crossfade | - |
| 按钮点击 | 100ms | scale | 0.95f |

---

## 17. 修订记录

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0 | 2026-04-05 | 初始技术设计文档 |
| 1.1 | 2026-04-05 | 新增 Quiz 选项从其他组补充、每日新词数可配置、闪卡→测试边界处理、OnboardingScreen、SettingsScreen、鼓励语池 |
| 1.2 | 2026-04-05 | 新增 Quiz 选项 meaning 去重逻辑、GetDailyProgressUseCase 实现、LearningViewModel 每日进度更新、首页任务卡片进度计算、首页日期显示 |
| 1.3 | 2026-04-05 | 架构优化：目录结构调整、添加 MainActivity、添加 Resource.kt 统一错误处理、UI State 统一到 model/ 子目录 |
| 1.4 | 2026-04-05 | 新增「我的」Tab页面（MeScreen、MeViewModel），更新底部导航为 4 项，更新导航图 |
