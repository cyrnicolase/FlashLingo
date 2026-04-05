package com.english.flashcard.domain.repository

interface SyncRepository {
    suspend fun syncWords(): Result<Unit>
}
