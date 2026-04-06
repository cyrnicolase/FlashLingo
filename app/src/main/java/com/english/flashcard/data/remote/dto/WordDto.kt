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
    val example: String?,
    @SerializedName("partOfSpeech")
    val partOfSpeech: String = ""
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
