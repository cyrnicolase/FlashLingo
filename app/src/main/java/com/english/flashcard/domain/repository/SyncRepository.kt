package com.english.flashcard.domain.repository

interface SyncRepository {
    suspend fun syncWords(): Result<Unit>
    fun getLastSyncTimestamp(): Long
    suspend fun setLastSyncTimestamp(timestamp: Long)
}
