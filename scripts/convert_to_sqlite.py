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
    json_path = project_root / "data" / "1-初中-顺序.json"
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
            example TEXT NOT NULL,
            isFavorite INTEGER NOT NULL,
            isMastered INTEGER NOT NULL,
            correctStreak INTEGER NOT NULL,
            wrongCount INTEGER NOT NULL,
            lastReviewAt INTEGER,
            nextReviewAt INTEGER,
            createdAt INTEGER NOT NULL,
            phrases TEXT NOT NULL
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
        
        translations = item.get('translations', [])
        meaning = translations[0]['translation'] if translations else ''
        
        phrases = item.get('phrases', [])
        phrases_json = json.dumps(phrases, ensure_ascii=False)
        
        created_at = int(datetime.now().timestamp())
        
        records.append((
            word, '', meaning, '', 0, 0, 0, 0, None, None, created_at, phrases_json
        ))
    
    cursor.executemany('''
        INSERT OR IGNORE INTO words 
        (word, phonetic, meaning, example, isFavorite, isMastered, 
         correctStreak, wrongCount, lastReviewAt, nextReviewAt, createdAt, phrases)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
