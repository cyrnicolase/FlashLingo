# 初中词汇预填充数据库设计

**日期**: 2026-04-05  
**状态**: 已批准  
**类型**: 技术设计文档

---

## 1. 背景与目标

### 背景
- 现有 Android 闪卡应用中自带词汇量不足（约 100 条编程相关词汇）
- 需要导入 `data/1-初中-顺序.json` 作为初始词库（约 3000 条中文初中词汇）
- 用户希望打包时完成数据准备，安装后立即可用

### 目标
- 将 JSON 词汇库转换为预填充的 SQLite 数据库
- 打包进 APK，安装后数据库直接可用（零等待）
- 保留 phrases 信息供后续展示

---

## 2. 数据结构

### 2.1 源数据格式 (JSON)

```json
{
  "word": "able",
  "translations": [
    {"translation": "能力，能耐；才能", "type": "n"},
    {"translation": "能；[经管] 有能力的；能干的", "type": "adj"}
  ],
  "phrases": [
    {"phrase": "will be able to", "translation": "将能够"},
    {"phrase": "be able to do", "translation": "能够做"}
  ]
}
```

### 2.2 目标数据格式 (SQLite - words 表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER PRIMARY KEY | 自增主键 |
| word | TEXT UNIQUE | 英文单词 |
| phonetic | TEXT | 音标（留空） |
| meaning | TEXT | 中文含义（取第一个 translation） |
| example | TEXT | 例句（留空） |
| isFavorite | INTEGER | 是否收藏（默认 0） |
| isMastered | INTEGER | 是否掌握（默认 0） |
| correctStreak | INTEGER | 连续正确次数（默认 0） |
| wrongCount | INTEGER | 错误次数（默认 0） |
| lastReviewAt | INTEGER | 上次复习时间戳（NULL） |
| nextReviewAt | INTEGER | 下次复习时间戳（NULL） |
| createdAt | INTEGER | 创建时间戳 |
| phrases | TEXT | 短语 JSON 数组 |

### 2.3 phrases 字段格式

```json
[{"phrase":"will be able to","translation":"将能够"},{"phrase":"be able to do","translation":"能够做"}]
```

---

## 3. 实现方案

### 3.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│  构建流程 (CI / Local Build)                                │
│                                                             │
│  data/1-初中-顺序.json                                      │
│         ↓                                                   │
│  scripts/convert_to_sqlite.py                               │
│         ↓                                                   │
│  app/src/main/assets/database/words.db                       │
│         ↓                                                   │
│  Gradle Build → APK (含预填充数据库)                        │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 转换脚本逻辑

```python
# scripts/convert_to_sqlite.py

import json
import sqlite3
import os
from datetime import datetime
from pathlib import Path

def convert():
    # 1. 读取 JSON 文件（JSON 是一个数组）
    with open('data/1-初中-顺序.json', 'r', encoding='utf-8') as f:
        words_data = json.load(f)
    
    # 2. 创建/覆盖 SQLite 数据库
    db_path = 'app/src/main/assets/database/words.db'
    os.makedirs(os.path.dirname(db_path), exist_ok=True)
    
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    # 3. 创建 words 表
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS words (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            word TEXT UNIQUE NOT NULL,
            phonetic TEXT DEFAULT '',
            meaning TEXT NOT NULL,
            example TEXT DEFAULT '',
            isFavorite INTEGER DEFAULT 0,
            isMastered INTEGER DEFAULT 0,
            correctStreak INTEGER DEFAULT 0,
            wrongCount INTEGER DEFAULT 0,
            lastReviewAt INTEGER,
            nextReviewAt INTEGER,
            createdAt INTEGER NOT NULL,
            phrases TEXT DEFAULT '[]'
        )
    ''')
    
    # 4. 批量插入数据
    records = []
    for item in words_data:
        word = item['word']
        # 取第一个 translation
        meaning = item['translations'][0]['translation'] if item.get('translations') else ''
        # 保留所有 phrases
        phrases = json.dumps(item.get('phrases', []), ensure_ascii=False)
        # 使用秒（与现有 Room mapper 兼容）
        created_at = int(datetime.now().timestamp())
        
        records.append((
            word, '', meaning, '', 0, 0, 0, 0, None, None, created_at, phrases
        ))
    
    cursor.executemany('''
        INSERT OR IGNORE INTO words 
        (word, phonetic, meaning, example, isFavorite, isMastered, 
         correctStreak, wrongCount, lastReviewAt, nextReviewAt, createdAt, phrases)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ''', records)
    
    conn.commit()
    conn.close()
    print(f"转换完成: {len(records)} 条词汇")
```

### 3.3 数据库配置变更

修改 `AppDatabase.kt`:

```kotlin
@Database(
    entities = [WordEntity::class, DailyProgressEntity::class],
    version = 2,  // 版本号递增（1 → 2）
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun wordDao(): WordDao
    abstract fun dailyProgressDao(): DailyProgressDao
    
    companion object {
        const val DATABASE_NAME = "flashcard_database"
        
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .createFromAsset("database/words.db")
                .addMigrations(object : Migration(1, 2) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL("ALTER TABLE words ADD COLUMN phrases TEXT DEFAULT '[]'")
                    }
                })
                .build()
        }
    }
}
```

**注意**：
- `createFromAsset` **仅在数据库首次创建时**从 assets 复制数据库
- 用户**升级 APK 时**（数据库已存在），`createFromAsset` 不会重新复制
- 新词只会添加到**全新安装**的用户，已安装用户不会自动获得新词
- 如需升级场景也添加新词，需额外实现 sync 逻辑

### 3.4 数据模型变更

**WordEntity.kt 新增字段**:

```kotlin
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val phonetic: String = "",
    val meaning: String,
    val example: String = "",
    val isFavorite: Boolean = false,
    val isMastered: Boolean = false,
    val correctStreak: Int = 0,
    val wrongCount: Int = 0,
    val lastReviewAt: Long? = null,
    val nextReviewAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val phrases: String = "[]"  // 新增：JSON 数组字符串
)
```

**Word.kt (Domain Model) 新增字段**:

```kotlin
data class Word(
    val id: Long = 0,
    val word: String,
    val phonetic: String = "",
    val meaning: String,
    val example: String = "",
    val isFavorite: Boolean = false,
    val isMastered: Boolean = false,
    val correctStreak: Int = 0,
    val wrongCount: Int = 0,
    val lastReviewAt: LocalDateTime? = null,
    val nextReviewAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val phrases: List<Phrase> = emptyList()  // 新增
)

data class Phrase(
    val phrase: String,
    val translation: String
)
```

**WordMapper.kt 更新**:

```kotlin
// 在 WordRepositoryImpl.kt 中修改

private val gson = Gson()

fun WordEntity.toDomain(): Word {
    val phraseList: List<Phrase> = try {
        gson.fromJson(phrases, object : TypeToken<List<Phrase>>() {}.type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    
    return Word(
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
            LocalDateTime.ofEpochSecond(lastReviewAt, 0, ZoneOffset.UTC)
        } else null,
        nextReviewAt = if (nextReviewAt != null && nextReviewAt > 0) {
            LocalDateTime.ofEpochSecond(nextReviewAt, 0, ZoneOffset.UTC)
        } else null,
        createdAt = if (createdAt > 0) {
            LocalDateTime.ofEpochSecond(createdAt, 0, ZoneOffset.UTC)
        } else LocalDateTime.now(),
        phrases = phraseList
    )
}
```

**注意**：
- Room 自动将 Boolean 与 INTEGER (0/1) 转换，无需手动处理
- timestamp 使用秒精度（与现有代码一致）
- `Phrase` 数据类放在 `domain/model/Phrase.kt`

---

## 4. 文件变更清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `scripts/convert_to_sqlite.py` | 新增 | JSON → SQLite 转换脚本 |
| `app/src/main/assets/database/words.db` | 新增（构建时生成） | 预填充数据库 |
| `app/src/main/java/.../domain/model/Phrase.kt` | 新增 | 短语数据类 |
| `app/src/main/java/.../data/local/database/entity/WordEntity.kt` | 修改 | 新增 phrases TEXT 字段 |
| `app/src/main/java/.../domain/model/Word.kt` | 修改 | 新增 phrases: List<Phrase> |
| `app/src/main/java/.../data/local/database/AppDatabase.kt` | 修改 | version→2，配置 createFromAsset |
| `app/src/main/java/.../data/repository/WordRepositoryImpl.kt` | 修改 | 更新 toDomain() mapper |
| `app/build.gradle.kts` | 修改 | 添加转换脚本任务 |

---

## 5. 构建流程集成

### 5.1 Gradle Task 配置

```kotlin
// app/build.gradle.kts
plugins {
    id("com.android.application")
    // ...
}

// 添加转换任务
val convertWordsTask = tasks.register<Exec>("convertWordsToSqlite") {
    group = "english"
    description = "Convert words JSON to SQLite database"
    
    commandLine("python", "scripts/convert_to_sqlite.py")
    workingDir = rootProject.projectDir
}

// APK 构建前先执行转换
tasks.whenTaskAdded {
    if (name.startsWith("assemble")) {
        dependsOn(convertWordsTask)
    }
}
```

### 5.2 构建命令

```bash
# 本地构建
./gradlew assembleDebug

# 或单独执行转换
python scripts/convert_to_sqlite.py
```

---

## 6. 数据验证

转换完成后需验证：

1. **记录数**: SQLite 中 `SELECT COUNT(*) FROM words` 应与 JSON 数组长度一致
2. **字段完整性**: 随机抽样检查 word, meaning, phrases 字段
3. **JSON 有效性**: phrases 字段可被 JSON.parse 正确解析
4. **唯一性**: `SELECT word, COUNT(*) FROM words GROUP BY word HAVING COUNT(*) > 1` 应无结果

---

## 7. 后续维护

### 7.1 更新词汇库
1. 修改 `data/1-初中-顺序.json`
2. 重新执行 `python scripts/convert_to_sqlite.py`
3. 重新构建 APK

### 7.2 版本管理
- JSON 文件纳入版本控制
- `words.db` 放入 `.gitignore`，由构建脚本生成

---

## 8. 风险与注意事项

| 风险 | 应对 |
|------|------|
| JSON 格式不一致导致转换失败 | 脚本添加 JSON 校验和错误日志 |
| phrases 数据过大影响 APK 大小 | 预估：3000 条 × 平均 200 字符 ≈ 600KB，影响可控 |
| Room 迁移问题 | 使用 `addMigrations` 或 `fallbackToDestructiveMigration` |
