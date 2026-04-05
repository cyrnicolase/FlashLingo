package com.english.flashcard.domain.usecase

import com.english.flashcard.domain.repository.SyncRepository
import javax.inject.Inject

class SyncWordsUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return syncRepository.syncWords()
    }
}
