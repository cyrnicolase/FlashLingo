package com.english.flashcard.data.remote.api

import com.english.flashcard.data.remote.dto.SyncResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WordApi {
    
    @GET("/api/v1/words/sync")
    suspend fun syncWords(
        @Query("since") since: Long? = null
    ): Response<SyncResponse>
}
