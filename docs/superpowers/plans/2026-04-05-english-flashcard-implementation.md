# English Vocabulary Flashcard App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a complete Android app for programmers to learn English vocabulary through flashcards and quizzes.

**Architecture:** MVVM + Clean Architecture with Jetpack Compose, Hilt DI, Room database, Retrofit for API sync.

**Tech Stack:** Kotlin 1.9.x, Jetpack Compose BOM 2024.02.00, Hilt 2.50, Room 2.6.1, Retrofit 2.9.0, Material Design 3, Navigation 2.7.7

---

## Module Overview

The project is organized into these independent modules that can be built in parallel:

| Module | Description | Dependencies |
|--------|-------------|--------------|
| 1. Project Setup | Gradle configs, Application class, manifest | None |
| 2. Domain Layer | Models, Repository interfaces, UseCases | None |
| 3. Data Layer | Room DB, DAOs, Entities, API, DTOs, Repositories | Domain |
| 4. DI Layer | Hilt modules | Domain, Data |
| 5. UI Theme & Navigation | Theme, Colors, NavGraph, Bottom Nav | DI |
| 6. UI Screens | Home, Library, Favorites, Me, Learning, Settings, Onboarding, Completion | DI, Theme |
| 7. UI Components | FlashCard, QuizOption, WordCard, etc. | Theme |
| 8. Built-in Word Library | words.json with initial vocabulary | None |

---

## Module 1: Project Setup

**Files to Create:**
- `settings.gradle.kts`
- `build.gradle.kts` (project level)
- `gradle.properties`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/english/flashcard/FlashCardApp.kt`

- [ ] **Step 1: Create project-level build files**

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "EnglishFlashCard"
include(":app")
```

```kotlin
// build.gradle.kts (project level)
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}
```

```properties
// gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 2: Create app-level build.gradle.kts**

```kotlin
// app/build.gradle.kts
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
            isMinifyEnabled = false
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
    // Compose BOM
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
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
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
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    
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

- [ ] **Step 3: Create AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".FlashCardApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.EnglishFlashCard">
        <activity
            android:name=".ui.MainActivity"
            android:exported="true"
            android:theme="@style/Theme.EnglishFlashCard">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 4: Create Application class**

```kotlin
// app/src/main/java/com/english/flashcard/FlashCardApp.kt
package com.english.flashcard

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FlashCardApp : Application()
```

- [ ] **Step 5: Create basic resources**

```xml
<!-- app/src/main/res/values/strings.xml -->
<resources>
    <string name="app_name">词汇背诵</string>
</resources>
```

```xml
<!-- app/src/main/res/values/themes.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.EnglishFlashCard" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

- [ ] **Step 6: Create MainActivity**

```kotlin
// app/src/main/java/com/english/flashcard/ui/MainActivity.kt
package com.english.flashcard.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.english.flashcard.ui.theme.EnglishFlashCardTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EnglishFlashCardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }
}
```

- [ ] **Step 7: Create Theme files**

```kotlin
// app/src/main/java/com/english/flashcard/ui/theme/Color.kt
package com.english.flashcard.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme Colors
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF5F5F5)
val LightPrimary = Color(0xFF4A90D9)
val LightOnPrimary = Color.White
val LightOnBackground = Color(0xFF333333)
val LightOnSurface = Color(0xFF333333)
val LightSecondaryText = Color(0xFF666666)

// Dark Theme Colors
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkPrimary = Color(0xFF4A90D9)
val DarkOnPrimary = Color.White
val DarkOnBackground = Color(0xFFE0E0E0)
val DarkOnSurface = Color(0xFFE0E0E0)
val DarkSecondaryText = Color(0xFF9E9E9E)

// Semantic Colors
val SuccessGreen = Color(0xFF4CAF50)
val ErrorRed = Color(0xFFE53935)
val WarningOrange = Color(0xFFFF9800)
```

```kotlin
// app/src/main/java/com/english/flashcard/ui/theme/Type.kt
package com.english.flashcard.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)
```

```kotlin
// app/src/main/java/com/english/flashcard/ui/theme/Theme.kt
package com.english.flashcard.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightSecondaryText
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkSecondaryText
)

@Composable
fun EnglishFlashCardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

- [ ] **Step 8: Create basic MainApp composable stub**

```kotlin
// app/src/main/java/com/english/flashcard/ui/MainApp.kt (stub for now)
package com.english.flashcard.ui

import androidx.compose.runtime.Composable

@Composable
fun MainApp() {
    // Will be implemented in UI Screens module
}
```

---

## Module 2: Domain Layer

**Files to Create:**
- `app/src/main/java/com/english/flashcard/domain/model/Word.kt`
- `app/src/main/java/com/english/flashcard/domain/model/DailyProgress.kt`
- `app/src/main/java/com/english/flashcard/domain/repository/WordRepository.kt`
- `app/src/main/java/com/english/flashcard/domain/repository/SyncRepository.kt`
- `app/src/main/java/com/english/flashcard/domain/usecase/*UseCase.kt` (9 use cases)

- [ ] **Step 1: Create Word domain model**

```kotlin
// app/src/main/java/com/english/flashcard/domain/model/Word.kt
package com.english.flashcard.domain.model

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
```

- [ ] **Step 2: Create DailyProgress domain model**

```kotlin
// app/src/main/java/com/english/flashcard/domain/model/DailyProgress.kt
package com.english.flashcard.domain.model

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
```

- [ ] **Step 3: Create WordRepository interface**

```kotlin
// app/src/main/java/com/english/flashcard/domain/repository/WordRepository.kt
package com.english.flashcard.domain.repository

import com.english.flashcard.domain.model.Word
import kotlinx.coroutines.flow.Flow

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

- [ ] **Step 4: Create SyncRepository interface**

```kotlin
// app/src/main/java/com/english/flashcard/domain/repository/SyncRepository.kt
package com.english.flashcard.domain.repository

interface SyncRepository {
    suspend fun syncWords(): Result<Unit>
    suspend fun getLastSyncTimestamp(): Long
    suspend fun setLastSyncTimestamp(timestamp: Long)
}
```

- [ ] **Step 5: Create GetTodayLearningWordsUseCase**

```kotlin
// app/src/main/java/com/english/flashcard/domain/usecase/GetTodayLearningWordsUseCase.kt
package com.english.flashcard.domain.usecase

import com.english.flashcard.domain.model.Word
import com.english.flashcard.domain.repository.WordRepository
import javax.inject.Inject

class GetTodayLearningWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(dailyNewWordsLimit: Int): TodayLearningWords {
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

- [ ] **Step 6: Create UpdateWordAfterAnswerUseCase**

```kotlin
// app/src/main/java/com/english/flashcard/domain/usecase/UpdateWordAfterAnswerUseCase.kt
package com.english.flashcard.domain.usecase

import com.english.flashcard.domain.model.Word
import com.english.flashcard.domain.repository.WordRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

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

- [ ] **Step 7: Create remaining UseCases**

Create these files with proper implementations:
- `GetReviewWordsUseCase.kt` - Get words due for review
- `GetWrongWordsUseCase.kt` - Get words in wrong list (wrongCount > 0 && correctStreak < 3)
- `GetFavoriteWordsUseCase.kt` - Get favorite words
- `RemoveFromWrongWordsUseCase.kt` - Remove word from wrong list after 3 correct
- `ToggleFavoriteUseCase.kt` - Toggle favorite status
- `SearchWordsUseCase.kt` - Search words by query
- `SyncWordsUseCase.kt` - Sync words from remote
- `GetDailyProgressUseCase.kt` - Get and update daily progress

---

## Module 3: Data Layer

**Files to Create:**
- `app/src/main/java/com/english/flashcard/data/local/database/entity/WordEntity.kt`
- `app/src/main/java/com/english/flashcard/data/local/database/entity/DailyProgressEntity.kt`
- `app/src/main/java/com/english/flashcard/data/local/database/dao/WordDao.kt`
- `app/src/main/java/com/english/flashcard/data/local/database/dao/DailyProgressDao.kt`
- `app/src/main/java/com/english/flashcard/data/local/database/AppDatabase.kt`
- `app/src/main/java/com/english/flashcard/data/local/preferences/UserPreferences.kt`
- `app/src/main/java/com/english/flashcard/data/remote/dto/WordDto.kt`
- `app/src/main/java/com/english/flashcard/data/remote/dto/SyncResponse.kt`
- `app/src/main/java/com/english/flashcard/data/remote/api/WordApi.kt`
- `app/src/main/java/com/english/flashcard/data/repository/WordRepositoryImpl.kt`
- `app/src/main/java/com/english/flashcard/data/repository/SyncRepositoryImpl.kt`
- `app/src/main/java/com/english/flashcard/data/util/Resource.kt`

- [ ] **Step 1: Create WordEntity**

```kotlin
// app/src/main/java/com/english/flashcard/data/local/database/entity/WordEntity.kt
package com.english.flashcard.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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

- [ ] **Step 2: Create DailyProgressEntity**

```kotlin
// app/src/main/java/com/english/flashcard/data/local/database/entity/DailyProgressEntity.kt
package com.english.flashcard.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_progress",
    indices = [Index(value = ["date"], unique = true)]
)
data class DailyProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "date")
    val date: String,
    
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

- [ ] **Step 3: Create WordDao**

```kotlin
// app/src/main/java/com/english/flashcard/data/local/database/dao/WordDao.kt
package com.english.flashcard.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.english.flashcard.data.local.database.entity.WordEntity
import kotlinx.coroutines.flow.Flow

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
    
    @Query("SELECT * FROM words WHERE is_favorite = 1 ORDER BY word ASC")
    fun getFavoriteWords(): Flow<List<WordEntity>>
    
    @Query("SELECT * FROM words WHERE correct_streak < 2 AND wrong_count > 0 ORDER BY last_review_at ASC")
    fun getWrongWords(): Flow<List<WordEntity>>
    
    @Query("SELECT * FROM words WHERE next_review_at <= :currentTime ORDER BY next_review_at ASC LIMIT :limit")
    suspend fun getWordsForReview(currentTime: Long, limit: Int): List<WordEntity>
    
    @Query("SELECT * FROM words WHERE created_at > 0 AND id NOT IN (SELECT id FROM words WHERE correct_streak > 0) ORDER BY created_at ASC LIMIT :limit")
    suspend fun getNewWords(limit: Int): List<WordEntity>
    
    @Query("SELECT * FROM words WHERE word LIKE '%' || :query || '%' OR meaning LIKE '%' || :query || '%' ORDER BY word ASC")
    fun searchWords(query: String): Flow<List<WordEntity>>
    
    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int
    
    @Query("SELECT COUNT(*) FROM words WHERE correct_streak > 0")
    suspend fun getMasteredWordCount(): Int
    
    @Query("SELECT * FROM words WHERE word LIKE :prefix || '%' ORDER BY word ASC")
    suspend fun getWordsByPrefix(prefix: String): List<WordEntity>
    
    @Query("DELETE FROM words WHERE word IN (:words)")
    suspend fun deleteWords(words: List<String>)
    
    @Query("UPDATE words SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)
}
```

- [ ] **Step 4: Create DailyProgressDao**

```kotlin
// app/src/main/java/com/english/flashcard/data/local/database/dao/DailyProgressDao.kt
package com.english.flashcard.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.english.flashcard.data.local.database.entity.DailyProgressEntity
import kotlinx.coroutines.flow.Flow

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
    
    @Query("SELECT COUNT(*) FROM daily_progress WHERE new_words_learned > 0 OR words_reviewed > 0")
    suspend fun getTotalStudyDays(): Int
}
```

- [ ] **Step 5: Create AppDatabase**

```kotlin
// app/src/main/java/com/english/flashcard/data/local/database/AppDatabase.kt
package com.english.flashcard.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.english.flashcard.data.local.database.dao.DailyProgressDao
import com.english.flashcard.data.local.database.dao.WordDao
import com.english.flashcard.data.local.database.entity.DailyProgressEntity
import com.english.flashcard.data.local.database.entity.WordEntity

@Database(
    entities = [WordEntity::class, DailyProgressEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun dailyProgressDao(): DailyProgressDao
}
```

- [ ] **Step 6: Create UserPreferences (DataStore)**

```kotlin
// app/src/main/java/com/english/flashcard/data/local/preferences/UserPreferences.kt
package com.english.flashcard.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    
    companion object {
        private val KEY_IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val KEY_DAILY_NEW_WORDS = intPreferencesKey("daily_new_words")
        private val KEY_ONBOARDING_SHOWN = booleanPreferencesKey("onboarding_shown")
        private val KEY_LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
    }
    
    val isDarkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_DARK_MODE] ?: false
    }
    
    val dailyNewWords: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_DAILY_NEW_WORDS] ?: 20
    }
    
    val isOnboardingShown: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_SHOWN] ?: false
    }
    
    suspend fun getLastSyncTimestamp(): Long {
        return dataStore.data.first()[KEY_LAST_SYNC_TIMESTAMP] ?: 0L
    }
    
    suspend fun setLastSyncTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_TIMESTAMP] = timestamp
        }
    }
    
    suspend fun setDarkMode(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_DARK_MODE] = isDark
        }
    }
    
    suspend fun setDailyNewWords(count: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_DAILY_NEW_WORDS] = count
        }
    }
    
    suspend fun setOnboardingShown(shown: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_SHOWN] = shown
        }
    }
}
```

- [ ] **Step 7: Create DTOs**

```kotlin
// app/src/main/java/com/english/flashcard/data/remote/dto/WordDto.kt
package com.english.flashcard.data.remote.dto

import com.google.gson.annotations.SerializedName

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

data class ImportWordsRequest(
    @SerializedName("words")
    val words: List<WordDto>
)

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

- [ ] **Step 8: Create WordApi**

```kotlin
// app/src/main/java/com/english/flashcard/data/remote/api/WordApi.kt
package com.english.flashcard.data.remote.api

import com.english.flashcard.data.remote.dto.ImportWordsRequest
import com.english.flashcard.data.remote.dto.ImportWordsResponse
import com.english.flashcard.data.remote.dto.SyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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

- [ ] **Step 9: Create Resource utility**

```kotlin
// app/src/main/java/com/english/flashcard/data/util/Resource.kt
package com.english.flashcard.data.util

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

- [ ] **Step 10: Create WordRepositoryImpl**

```kotlin
// app/src/main/java/com/english/flashcard/data/repository/WordRepositoryImpl.kt
package com.english.flashcard.data.repository

import com.english.flashcard.data.local.database.dao.WordDao
import com.english.flashcard.data.local.database.entity.WordEntity
import com.english.flashcard.domain.model.Word
import com.english.flashcard.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
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

// Extension functions for mapping
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

- [ ] **Step 11: Create SyncRepositoryImpl**

```kotlin
// app/src/main/java/com/english/flashcard/data/repository/SyncRepositoryImpl.kt
package com.english.flashcard.data.repository

import com.english.flashcard.data.local.preferences.UserPreferences
import com.english.flashcard.data.remote.api.WordApi
import com.english.flashcard.data.remote.dto.WordDto
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
                    
                    if (data.deleted.isNotEmpty()) {
                        wordRepository.deleteWords(data.deleted)
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
    
    override suspend fun getLastSyncTimestamp(): Long =
        userPreferences.getLastSyncTimestamp()
    
    override suspend fun setLastSyncTimestamp(timestamp: Long) {
        userPreferences.setLastSyncTimestamp(timestamp)
    }
}
```

---

## Module 4: DI Layer (Hilt Modules)

**Files to Create:**
- `app/src/main/java/com/english/flashcard/di/DatabaseModule.kt`
- `app/src/main/java/com/english/flashcard/di/NetworkModule.kt`
- `app/src/main/java/com/english/flashcard/di/RepositoryModule.kt`

- [ ] **Step 1: Create DatabaseModule**

```kotlin
// app/src/main/java/com/english/flashcard/di/DatabaseModule.kt
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "flashcard_database"
        ).build()
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
```

- [ ] **Step 2: Create NetworkModule**

```kotlin
// app/src/main/java/com/english/flashcard/di/NetworkModule.kt
package com.english.flashcard.di

import com.english.flashcard.data.remote.api.WordApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val BASE_URL = "https://api.example.com/"
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideWordApi(retrofit: Retrofit): WordApi {
        return retrofit.create(WordApi::class.java)
    }
}
```

- [ ] **Step 3: Create RepositoryModule**

```kotlin
// app/src/main/java/com/english/flashcard/di/RepositoryModule.kt
package com.english.flashcard.di

import com.english.flashcard.data.repository.SyncRepositoryImpl
import com.english.flashcard.data.repository.WordRepositoryImpl
import com.english.flashcard.domain.repository.SyncRepository
import com.english.flashcard.domain.repository.WordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindWordRepository(
        wordRepositoryImpl: WordRepositoryImpl
    ): WordRepository
    
    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        syncRepositoryImpl: SyncRepositoryImpl
    ): SyncRepository
}
```

---

## Module 5: UI Navigation & Bottom Nav

**Files to Create:**
- `app/src/main/java/com/english/flashcard/ui/navigation/Screen.kt`
- `app/src/main/java/com/english/flashcard/ui/navigation/NavGraph.kt`
- `app/src/main/java/com/english/flashcard/ui/components/BottomNavBar.kt`

- [ ] **Step 1: Create Screen sealed class**

```kotlin
// app/src/main/java/com/english/flashcard/ui/navigation/Screen.kt
package com.english.flashcard.ui.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Library : Screen("library")
    data object Favorites : Screen("favorites")
    data object Me : Screen("me")
    data object Learning : Screen("learning/{learningType}") {
        fun createRoute(learningType: String) = "learning/$learningType"
    }
    data object Settings : Screen("settings")
    data object Completion : Screen("completion/{totalWords}/{correctCount}/{accuracy}/{duration}") {
        fun createRoute(totalWords: Int, correctCount: Int, accuracy: Float, duration: Int) = 
            "completion/$totalWords/$correctCount/$accuracy/$duration"
    }
}

enum class LearningType(val value: String) {
    Today("today"),
    Review("review"),
    WrongWords("wrong"),
    Favorites("favorites")
}
```

- [ ] **Step 2: Create BottomNavBar**

```kotlin
// app/src/main/java/com/english/flashcard/ui/components/BottomNavBar.kt
package com.english.flashcard.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.english.flashcard.ui.navigation.Screen

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem(
            route = Screen.Home.route,
            label = "首页",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            route = Screen.Library.route,
            label = "单词库",
            selectedIcon = Icons.Filled.LibraryBooks,
            unselectedIcon = Icons.Outlined.LibraryBooks
        ),
        BottomNavItem(
            route = Screen.Favorites.route,
            label = "收藏",
            selectedIcon = Icons.Filled.Favorite,
            unselectedIcon = Icons.Outlined.FavoriteBorder
        ),
        BottomNavItem(
            route = Screen.Me.route,
            label = "我的",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person
        )
    )
    
    NavigationBar {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}
```

- [ ] **Step 3: Create NavGraph (stub screens for now)**

```kotlin
// app/src/main/java/com/english/flashcard/ui/navigation/NavGraph.kt
package com.english.flashcard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.english.flashcard.ui.screens.completion.CompletionScreen
import com.english.flashcard.ui.screens.home.HomeScreen
import com.english.flashcard.ui.screens.learning.LearningScreen
import com.english.flashcard.ui.screens.library.LibraryScreen
import com.english.flashcard.ui.screens.favorites.FavoritesScreen
import com.english.flashcard.ui.screens.me.MeScreen
import com.english.flashcard.ui.screens.onboarding.OnboardingScreen
import com.english.flashcard.ui.screens.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Onboarding.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onStartLearning = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
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
                onStartLearning = { type ->
                    navController.navigate(Screen.Learning.createRoute(type))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.Library.route) {
            LibraryScreen()
        }
        
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onStartReview = {
                    navController.navigate(Screen.Learning.createRoute(LearningType.Favorites.value))
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
            val learningType = backStackEntry.arguments?.getString("learningType") ?: LearningType.Today.value
            LearningScreen(
                learningType = learningType,
                onClose = { navController.popBackStack() },
                onComplete = { totalWords, correctCount, accuracy, duration ->
                    navController.navigate(Screen.Completion.createRoute(totalWords, correctCount, accuracy, duration)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.Completion.route,
            arguments = listOf(
                navArgument("totalWords") { type = NavType.IntType },
                navArgument("correctCount") { type = NavType.IntType },
                navArgument("accuracy") { type = NavType.FloatType },
                navArgument("duration") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val totalWords = backStackEntry.arguments?.getInt("totalWords") ?: 0
            val correctCount = backStackEntry.arguments?.getInt("correctCount") ?: 0
            val accuracy = backStackEntry.arguments?.getFloat("accuracy") ?: 0f
            val duration = backStackEntry.arguments?.getInt("duration") ?: 0
            CompletionScreen(
                totalWords = totalWords,
                correctCount = correctCount,
                accuracy = accuracy,
                durationSeconds = duration,
                onBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
```

---

## Module 6: UI Screens

**Files to Create:**
- `app/src/main/java/com/english/flashcard/ui/screens/onboarding/OnboardingScreen.kt`
- `app/src/main/java/com/english/flashcard/ui/screens/home/HomeScreen.kt, HomeViewModel.kt, HomeUiState.kt`
- `app/src/main/java/com/english/flashcard/ui/screens/library/LibraryScreen.kt, LibraryViewModel.kt, LibraryUiState.kt`
- `app/src/main/java/com/english/flashcard/ui/screens/favorites/FavoritesScreen.kt, FavoritesViewModel.kt, FavoritesUiState.kt`
- `app/src/main/java/com/english/flashcard/ui/screens/me/MeScreen.kt, MeViewModel.kt, MeUiState.kt`
- `app/src/main/java/com/english/flashcard/ui/screens/learning/LearningScreen.kt, LearningViewModel.kt, LearningState.kt, LearningType.kt`
- `app/src/main/java/com/english/flashcard/ui/screens/settings/SettingsScreen.kt, SettingsViewModel.kt, SettingsUiState.kt`
- `app/src/main/java/com/english/flashcard/ui/screens/completion/CompletionScreen.kt`

### 6.1 Onboarding Screen

- [ ] **Create OnboardingScreen.kt**

```kotlin
// app/src/main/java/com/english/flashcard/ui/screens/onboarding/OnboardingScreen.kt
package com.english.flashcard.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(
    onStartLearning: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📚",
            fontSize = 64.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "欢迎使用词汇背诵",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "专为程序员设计的英语词汇学习工具",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        FeatureCard(
            emoji = "📖",
            title = "闪卡学习",
            description = "先看单词，再看释义"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FeatureCard(
            emoji = "✏️",
            title = "巩固记忆",
            description = "四选一测试，答对两次掌握"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FeatureCard(
            emoji = "📊",
            title = "科学复习",
            description = "根据遗忘曲线安排复习"
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onStartLearning,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "开始学习",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onSkip) {
            Text(
                text = "跳过",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeatureCard(
    emoji: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
```

### 6.2 Home Screen

- [ ] **Create HomeUiState.kt**

```kotlin
// app/src/main/java/com/english/flashcard/ui/screens/home/HomeUiState.kt
package com.english.flashcard.ui.screens.home

import com.english.flashcard.domain.model.DailyProgress

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

- [ ] **Create HomeViewModel.kt**

```kotlin
// app/src/main/java/com/english/flashcard/ui/screens/home/HomeViewModel.kt
package com.english.flashcard.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.english.flashcard.data.local.preferences.UserPreferences
import com.english.flashcard.domain.repository.SyncRepository
import com.english.flashcard.domain.repository.WordRepository
import com.english.flashcard.domain.usecase.GetDailyProgressUseCase
import com.english.flashcard.domain.usecase.SyncWordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

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
            userPreferences.isOnboardingShown.collect { shown ->
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
            
            val dailyNewWordsLimit = userPreferences.dailyNewWords.first()
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
```

- [ ] **Create HomeScreen.kt**

```kotlin
// app/src/main/java/com/english/flashcard/ui/screens/home/HomeScreen.kt
package com.english.flashcard.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.english.flashcard.ui.navigation.LearningType
import com.english.flashcard.ui.screens.onboarding.OnboardingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onStartLearning: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.showOnboarding) {
        OnboardingScreen(
            onStartLearning = {
                viewModel.dismissOnboarding()
                onStartLearning(LearningType.Today.value)
            },
            onSkip = { viewModel.dismissOnboarding() }
        )
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("词汇背诵") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = uiState.todayDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                TodayTaskCard(
                    newWordsCount = uiState.todayNewWords,
                    reviewWordsCount = uiState.todayReviewWords,
                    progressPercent = uiState.todayProgressPercent,
                    onStartLearning = { onStartLearning(LearningType.Today.value) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickAccessCard(
                        modifier = Modifier.weight(1f),
                        emoji = "❌",
                        title = "错词",
                        count = uiState.wrongWordCount,
                        onClick = { onStartLearning(LearningType.WrongWords.value) }
                    )
                    QuickAccessCard(
                        modifier = Modifier.weight(1f),
                        emoji = "⭐",
                        title = "收藏",
                        count = uiState.favoriteCount,
                        onClick = { onStartLearning(LearningType.Favorites.value) }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                StatisticsCard(
                    todayLearned = uiState.todayProgress?.newWordsLearned ?: 0,
                    todayAccuracy = uiState.todayProgress?.accuracy ?: 0f,
                    weekStreak = 5,
                    totalMastered = uiState.masteredCount
                )
            }
        }
    }
}

@Composable
private fun TodayTaskCard(
    newWordsCount: Int,
    reviewWordsCount: Int,
    progressPercent: Int,
    onStartLearning: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📚", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "今日任务",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "新词 $newWordsCount · 待复习 $reviewWordsCount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            
            Text(
                text = "$progressPercent%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onStartLearning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = if (newWordsCount == 0 && reviewWordsCount == 0) "太棒了，今日任务已完成！" else "开始学习",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${count}个",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StatisticsCard(
    todayLearned: Int,
    todayAccuracy: Float,
    weekStreak: Int,
    totalMastered: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📊", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "进度统计",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            StatRow(label: "今日：学习", value = "$todayLearned 词", suffix = "正确率 ${(todayAccuracy * 100).toInt()}%")
            Spacer(modifier = Modifier.height(8.dp))
            StatRow(label = "本周：连续学习", value = "$weekStreak 天", suffix = "")
            Spacer(modifier = Modifier.height(8.dp))
            StatRow(label = "累计：已掌握", value = "$totalMastered 词", suffix = "")
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, suffix: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (suffix.isNotEmpty()) {
                Text(
                    text = " $suffix",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

### 6.3 Library Screen

- [ ] **Create LibraryUiState.kt, LibraryViewModel.kt, LibraryScreen.kt**

(Similar pattern - create State, ViewModel, and Composable for each screen)

### 6.4 Learning Screen (Flashcard + Quiz)

- [ ] **Create LearningState.kt, LearningViewModel.kt, LearningScreen.kt**

This is the most complex screen - implements flashcard flip animation and quiz functionality.

### 6.5 Other Screens

Create similar pattern for:
- FavoritesScreen
- MeScreen  
- SettingsScreen
- CompletionScreen

---

## Module 7: UI Components

**Files to Create:**
- `app/src/main/java/com/english/flashcard/ui/components/FlashCard.kt`
- `app/src/main/java/com/english/flashcard/ui/components/QuizOption.kt`
- `app/src/main/java/com/english/flashcard/ui/components/WordCard.kt`
- `app/src/main/java/com/english/flashcard/ui/components/WordBottomSheet.kt`
- `app/src/main/java/com/english/flashcard/ui/components/EmptyState.kt`

- [ ] **Create FlashCard.kt with 3D flip animation**

```kotlin
// app/src/main/java/com/english/flashcard/ui/components/FlashCard.kt
package com.english.flashcard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.english.flashcard.domain.model.Word

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
                FrontContent(word = word)
            } else {
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
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = word.word,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = word.phonetic,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "点击翻转",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

@Composable
private fun BackContent(word: Word, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = word.meaning,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (!word.example.isNullOrEmpty()) {
            Text(
                text = "例句：",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = word.example,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
```

- [ ] **Create QuizOption.kt**

```kotlin
// app/src/main/java/com/english/flashcard/ui/components/QuizOption.kt
package com.english.flashcard.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.english.flashcard.ui.theme.ErrorRed
import com.english.flashcard.ui.theme.SuccessGreen

@Composable
fun QuizOption(
    text: String,
    index: Int,
    isSelected: Boolean,
    isCorrect: Boolean?,
    showResult: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        showResult && isCorrect == true -> SuccessGreen.copy(alpha = 0.1f)
        showResult && isSelected && isCorrect == false -> ErrorRed.copy(alpha = 0.1f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val borderColor = when {
        showResult && isCorrect == true -> SuccessGreen
        showResult && isSelected && isCorrect == false -> ErrorRed
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    
    val textColor = when {
        showResult && isCorrect == true -> SuccessGreen
        showResult && isSelected && isCorrect == false -> ErrorRed
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val optionLabel = ('A' + index).toString()
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(2.dp, borderColor),
        onClick = onClick
    ) {
        Text(
            text = "$optionLabel. $text",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected || (showResult && isCorrect == true)) FontWeight.Medium else FontWeight.Normal,
            color = textColor,
            modifier = Modifier.padding(16.dp)
        )
    }
}
```

- [ ] **Create remaining components (WordCard, WordBottomSheet, EmptyState)**

---

## Module 8: Built-in Word Library

**Files to Create:**
- `app/src/main/res/raw/words.json`

- [ ] **Create words.json with initial vocabulary**

```json
{
  "words": [
    {
      "word": "abstract",
      "phonetic": "əbˈstrækt",
      "meaning": "抽象的",
      "example": "An abstract class cannot be instantiated directly."
    },
    {
      "word": "algorithm",
      "phonetic": "ˈælɡərɪðəm",
      "meaning": "算法",
      "example": "The quicksort algorithm has O(n log n) average complexity."
    },
    {
      "word": "array",
      "phonetic": "əˈreɪ",
      "meaning": "数组",
      "example": "You can iterate over an array using a for-each loop."
    },
    {
      "word": "boolean",
      "phonetic": "ˈbuːliən",
      "meaning": "布尔值",
      "example": "A boolean variable can only be true or false."
    },
    {
      "word": "buffer",
      "phonetic": "ˈbʌfər",
      "meaning": "缓冲区",
      "example": "Always clear the buffer before reading new data."
    },
    {
      "word": "cache",
      "phonetic": "kæʃ",
      "meaning": "缓存",
      "example": "The browser uses cache to load pages faster."
    },
    {
      "word": "callback",
      "phonetic": "ˈkɔːlbæk",
      "meaning": "回调函数",
      "example": "Pass a callback function to handle the async result."
    },
    {
      "word": "class",
      "phonetic": "klɑːs",
      "meaning": "类",
      "example": "A class defines the structure and behavior of objects."
    },
    {
      "word": "compiler",
      "phonetic": "kəmˈpaɪlər",
      "meaning": "编译器",
      "example": "The compiler transforms source code into machine code."
    },
    {
      "word": "concurrency",
      "phonetic": "kənˈkʌrənsi",
      "meaning": "并发",
      "example": "Concurrency allows multiple tasks to run simultaneously."
    },
    {
      "word": "constructor",
      "phonetic": "kənˈstrʌktər",
      "meaning": "构造函数",
      "example": "The constructor initializes the object's state."
    },
    {
      "word": "database",
      "phonetic": "ˈdeɪtəbeɪs",
      "meaning": "数据库",
      "example": "Always validate data before inserting into the database."
    },
    {
      "word": "debug",
      "phonetic": "diːˈbʌɡ",
      "meaning": "调试",
      "example": "Use breakpoints to debug your application."
    },
    {
      "word": "dependency",
      "phonetic": "dɪˈpendənsi",
      "meaning": "依赖",
      "example": "Manage project dependencies with a package manager."
    },
    {
      "word": "deprecated",
      "phonetic": "ˈdeprəkeɪtɪd",
      "meaning": "已弃用的",
      "example": "This method is deprecated, use the new one instead."
    },
    {
      "word": "dynamic",
      "phonetic": "daɪˈnæmɪk",
      "meaning": "动态的",
      "example": "Dynamic typing allows variables to change types at runtime."
    },
    {
      "word": "encapsulation",
      "phonetic": "ɪnˌkæpsjuˈleɪʃən",
      "meaning": "封装",
      "example": "Encapsulation hides internal implementation details."
    },
    {
      "word": "enum",
      "phonetic": "ˈiːnʌm",
      "meaning": "枚举",
      "example": "Use an enum to define a fixed set of constants."
    },
    {
      "word": "exception",
      "phonetic": "ɪkˈsepʃən",
      "meaning": "异常",
      "example": "Always catch exceptions to prevent app crashes."
    },
    {
      "word": "framework",
      "phonetic": "ˈfreɪmwɜːrk",
      "meaning": "框架",
      "example": "Spring is a popular Java framework for enterprise apps."
    },
    {
      "word": "function",
      "phonetic": "ˈfʌŋkʃən",
      "meaning": "函数",
      "example": "A pure function always returns the same result for the same input."
    },
    {
      "word": "garbage collection",
      "phonetic": "ˈɡɑːrbɪdʒ kəˈlekʃən",
      "meaning": "垃圾回收",
      "example": "Java uses garbage collection to automatically free memory."
    },
    {
      "word": "hash",
      "phonetic": "hæʃ",
      "meaning": "哈希",
      "example": "Use a hash table for O(1) average lookup time."
    },
    {
      "word": "immutable",
      "phonetic": "ɪˈmjuːtəbl",
      "meaning": "不可变的",
      "example": "Strings in Java are immutable objects."
    },
    {
      "word": "inheritance",
      "phonetic": "ɪnˈherɪtəns",
      "meaning": "继承",
      "example": "Inheritance allows a class to derive properties from another class."
    },
    {
      "word": "interface",
      "phonetic": "ˈɪntərfeɪs",
      "meaning": "接口",
      "example": "An interface defines a contract without implementation."
    },
    {
      "word": "iteration",
      "phonetic": "ˌɪtəˈreɪʃən",
      "meaning": "迭代",
      "example": "Each iteration processes one element of the collection."
    },
    {
      "word": "lambda",
      "phonetic": "ˈlæmdə",
      "meaning": "lambda表达式",
      "example": "Lambda expressions provide concise function syntax."
    },
    {
      "word": "library",
      "phonetic": "ˈlaɪbrəri",
      "meaning": "库",
      "example": "Leverage existing libraries to avoid reinventing the wheel."
    },
    {
      "word": "loop",
      "phonetic": "luːp",
      "meaning": "循环",
      "example": "Use a for loop to iterate over a range of values."
    }
  ]
}
```

---

## Implementation Order

Given independent modules, the recommended implementation order is:

1. **Module 1 (Project Setup)** - Foundation
2. **Module 2 (Domain Layer)** - Business logic interfaces
3. **Module 3 (Data Layer)** - Data access implementation
4. **Module 4 (DI Layer)** - Dependency injection wiring
5. **Module 5 (UI Navigation)** - App structure
6. **Module 6 (UI Screens)** - User interfaces
7. **Module 7 (UI Components)** - Reusable components
8. **Module 8 (Word Library)** - Initial data

---

## Verification

After implementation, verify:
- [ ] App compiles without errors
- [ ] All screens are navigable
- [ ] Flashcard flip animation works smoothly
- [ ] Quiz functionality works correctly
- [ ] Data persists between sessions
- [ ] Theme switching works
