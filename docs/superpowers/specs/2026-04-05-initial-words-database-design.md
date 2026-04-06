# 词汇预填充数据库设计

**日期**: 2026-04-06  
**状态**: 已批准  
**类型**: 技术设计文档  
**版本**: 2.0

---

## 1. 背景与目标

### 背景
- 需要导入新词库 `data/BeiShiGaoZhong_1.json` 作为预填充词库
- 新词库包含更丰富的信息：美式音标、多词性翻译、例句、短语
- 用户希望打包时完成数据准备，安装后立即可用

### 目标
- 将 JSON 词汇库转换为预填充的 SQLite 数据库
- 打包进 APK，安装后数据库直接可用（零等待）
- 支持词性展示、例句展示

---

## 2. 数据结构

### 2.1 源数据格式 (JSON)

```json
{
  "word": "questionnaire",
  "us": "ˌkwɛstʃənˈɛr",
  "uk": "ˌkwestʃəˈneə(r)",
  "translations": [
    {"translation": "问卷；调查表", "type": "n"}
  ],
  "phrases": [
    {"phrase": "questionnaire survey", "translation": "问卷调查"}
  ],
  "sentences": [
    {
      "sentence": "Teachers will be asked to fill in a questionnaire.",
      "translation": "教师们将被要求填写一份调查问卷。"
    }
  ]
}
```

### 2.2 目标数据格式 (SQLite - words 表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER PRIMARY KEY | 自增主键 |
| word | TEXT UNIQUE | 英文单词 |
| phonetic | TEXT | 美式音标 |
| meaning | TEXT | 中文含义（取第一个翻译） |
| isFavorite | INTEGER | 是否收藏（默认 0） |
| isMastered | INTEGER | 是否掌握（默认 0） |
| correctStreak | INTEGER | 连续正确次数（默认 0） |
| wrongCount | INTEGER | 错误次数（默认 0） |
| lastReviewAt | INTEGER | 上次复习时间戳（NULL） |
| nextReviewAt | INTEGER | 下次复习时间戳（NULL） |
| createdAt | INTEGER | 创建时间戳 |
| phrases | TEXT | 短语 JSON 数组 |
| translations | TEXT | 翻译 JSON 数组（含词性） |
| sentences | TEXT | 例句 JSON 数组 |
| partOfSpeech | TEXT | 词性标签（如 "n"） |

### 2.3 translations 字段格式

```json
[
  {"type": "n", "translation": "问卷；调查表"}
]
```

### 2.4 phrases 字段格式

```json
[
  {"phrase": "questionnaire survey", "translation": "问卷调查"}
]
```

### 2.5 sentences 字段格式

```json
[
  {
    "sentence": "Teachers will be asked to fill in a questionnaire.",
    "translation": "教师们将被要求填写一份调查问卷。"
  }
]
```

---

## 3. 实现方案

### 3.1 转换脚本逻辑

```python
# scripts/convert_to_sqlite.py

import json
import sqlite3
import os
from datetime import datetime
from pathlib import Path

def convert():
    project_root = Path(__file__).parent.parent
    json_path = project_root / "data" / "BeiShiGaoZhong_1.json"
    db_dir = project_root / "app" / "src" / "main" / "assets" / "database"
    db_path = db_dir / "words.db"
    
    print(f"Reading JSON from: {json_path}")
    with open(json_path, 'r', encoding='utf-8') as f:
        words_data = json.load(f)
    
    os.makedirs(db_dir, exist_ok=True)
    if db_path.exists():
        db_path.unlink()
    
    print(f"Creating database at: {db_path}")
    conn = sqlite3.connect(str(db_path))
    cursor = conn.cursor()
    
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS words (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            word TEXT NOT NULL,
            phonetic TEXT NOT NULL,
            meaning TEXT NOT NULL,
            isFavorite INTEGER NOT NULL,
            isMastered INTEGER NOT NULL,
            correctStreak INTEGER NOT NULL,
            wrongCount INTEGER NOT NULL,
            lastReviewAt INTEGER,
            nextReviewAt INTEGER,
            createdAt INTEGER NOT NULL,
            phrases TEXT NOT NULL,
            translations TEXT NOT NULL,
            sentences TEXT NOT NULL,
            partOfSpeech TEXT NOT NULL DEFAULT ''
        )
    ''')
    
    cursor.execute('CREATE UNIQUE INDEX IF NOT EXISTS index_words_word ON words(word)')
    cursor.execute('CREATE INDEX IF NOT EXISTS index_words_isFavorite ON words(isFavorite)')
    cursor.execute('CREATE INDEX IF NOT EXISTS index_words_nextReviewAt ON words(nextReviewAt)')
    cursor.execute('CREATE INDEX IF NOT EXISTS index_words_isMastered ON words(isMastered)')
    
    records = []
    for item in words_data:
        word = item.get('word', '')
        if not word:
            continue
        
        # 音标：取 us (美式音标)
        phonetic = item.get('us', '') or item.get('uk', '') or ''
        
        # 翻译：取第一个 translation
        translations = item.get('translations', [])
        meaning = translations[0]['translation'] if translations else ''
        
        # 词性：取第一个 type
        part_of_speech = ''
        for t in translations:
            if 'type' in t and t['type']:
                part_of_speech = t['type']
                break
        
        # 短语
        phrases = item.get('phrases', [])
        phrases_json = json.dumps(phrases, ensure_ascii=False)
        
        # 翻译数组
        translations_json = json.dumps(translations, ensure_ascii=False)
        
        # 例句：取第一句
        sentences = item.get('sentences', [])
        first_sentence = sentences[0] if sentences else {}
        sentences_data = [first_sentence] if first_sentence else []
        sentences_json = json.dumps(sentences_data, ensure_ascii=False)
        
        created_at = int(datetime.now().timestamp())
        
        records.append((
            word, phonetic, meaning, 0, 0, 0, 0, None, None, created_at,
            phrases_json, translations_json, sentences_json, part_of_speech
        ))
    
    cursor.executemany('''
        INSERT OR IGNORE INTO words 
        (word, phonetic, meaning, isFavorite, isMastered, 
         correctStreak, wrongCount, lastReviewAt, nextReviewAt, createdAt, 
         phrases, translations, sentences, partOfSpeech)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ''', records)
    
    conn.commit()
    conn.close()
    print(f"Conversion complete: {len(records)} words inserted")
```

### 3.2 数据库配置

```kotlin
@Database(
    entities = [WordEntity::class, DailyProgressEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun wordDao(): WordDao
    abstract fun dailyProgressDao(): DailyProgressDao
    
    companion object {
        const val DATABASE_NAME = "flashcard_database"
        
        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE words ADD COLUMN sentences TEXT DEFAULT '[]'")
            }
        }
        
        fun getMigrations(): Array<Migration> = arrayOf(MIGRATION_4_5)
    }
}
```

**数据库迁移历史**：
- 2→3：添加 `partOfSpeech` 列
- 3→4：添加 `translations` 列
- 4→5：添加 `sentences` 列

---

## 4. 数据模型

### 4.1 WordEntity.kt

```kotlin
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val phonetic: String = "",
    val meaning: String,
    val isFavorite: Boolean = false,
    val isMastered: Boolean = false,
    val correctStreak: Int = 0,
    val wrongCount: Int = 0,
    val lastReviewAt: Long? = null,
    val nextReviewAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val phrases: String = "[]",
    val translations: String = "[]",
    val sentences: String = "[]",
    val partOfSpeech: String = ""
)
```

### 4.2 Word.kt (Domain Model)

```kotlin
data class Word(
    val id: Long = 0,
    val word: String,
    val phonetic: String = "",
    val meaning: String,
    val isFavorite: Boolean = false,
    val isMastered: Boolean = false,
    val correctStreak: Int = 0,
    val wrongCount: Int = 0,
    val lastReviewAt: LocalDateTime? = null,
    val nextReviewAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val phrases: List<Phrase> = emptyList(),
    val translations: List<Translation> = emptyList(),
    val sentences: List<Sentence> = emptyList(),
    val partOfSpeech: String = ""
)
```

### 4.3 数据类

```kotlin
data class Phrase(
    val phrase: String,
    val translation: String
)

data class Translation(
    val type: String,
    val translation: String
)

data class Sentence(
    val sentence: String,
    val translation: String
)
```

---

## 5. 文件变更清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `scripts/convert_to_sqlite.py` | 修改 | 新词库格式处理 |
| `app/src/main/assets/database/words.db` | 新增（构建时生成） | 预填充数据库 |
| `app/src/main/java/.../domain/model/Sentence.kt` | 新增 | 例句数据类 |
| `app/src/main/java/.../domain/model/Phrase.kt` | 新增 | 短语数据类 |
| `app/src/main/java/.../domain/model/Translation.kt` | 新增 | 翻译数据类（含词性） |
| `app/src/main/java/.../data/local/database/entity/WordEntity.kt` | 修改 | 新增 sentences 字段 |
| `app/src/main/java/.../domain/model/Word.kt` | 修改 | 新增 sentences 字段 |
| `app/src/main/java/.../data/local/database/AppDatabase.kt` | 修改 | version→5 |
| `app/src/main/java/.../data/repository/WordRepositoryImpl.kt` | 修改 | 更新 mapper |
| `app/src/main/java/.../ui/components/WordDetailSheet.kt` | 修改 | 显示例句 |
| `app/src/main/java/.../ui/components/FlashCard.kt` | 修改 | 显示例句 |

---

## 6. 构建流程

### 6.1 构建命令

```bash
# 本地构建
./gradlew assembleDebug

# 或单独执行转换
python scripts/convert_to_sqlite.py
```

---

## 7. 数据验证

转换完成后需验证：

1. **记录数**: SQLite 中 `SELECT COUNT(*) FROM words` 应与 JSON 数组长度一致
2. **字段完整性**: 随机抽样检查 word, meaning, phonetic, phrases, sentences 字段
3. **JSON 有效性**: phrases, translations, sentences 字段可被正确解析
4. **唯一性**: `SELECT word, COUNT(*) FROM words GROUP BY word HAVING COUNT(*) > 1` 应无结果
