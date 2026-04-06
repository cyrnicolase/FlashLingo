#!/usr/bin/env python3
"""
Convert words JSON to SQLite database for Room pre-population.
Usage: python scripts/convert_to_sqlite.py
"""

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
    
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS daily_progress (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            date TEXT NOT NULL UNIQUE,
            newWordsLearned INTEGER NOT NULL DEFAULT 0,
            wordsReviewed INTEGER NOT NULL DEFAULT 0,
            correctCount INTEGER NOT NULL DEFAULT 0,
            totalCount INTEGER NOT NULL DEFAULT 0
        )
    ''')
    
    cursor.execute('CREATE UNIQUE INDEX IF NOT EXISTS index_daily_progress_date ON daily_progress(date)')
    
    cursor.execute('PRAGMA user_version = 5')
    
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
            word, phonetic, meaning, 0, 0, 0, 0, None, None, created_at, phrases_json, translations_json, sentences_json, part_of_speech
        ))
    
    cursor.executemany('''
        INSERT OR IGNORE INTO words 
        (word, phonetic, meaning, isFavorite, isMastered, 
         correctStreak, wrongCount, lastReviewAt, nextReviewAt, createdAt, 
         phrases, translations, sentences, partOfSpeech)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ''', records)
    
    conn.commit()
    
    cursor.execute('SELECT COUNT(*) FROM words')
    count = cursor.fetchone()[0]
    
    conn.close()
    
    print(f"Conversion complete: {count} words inserted")
    print(f"Database saved to: {db_path}")
    
    return count

if __name__ == "__main__":
    convert()
