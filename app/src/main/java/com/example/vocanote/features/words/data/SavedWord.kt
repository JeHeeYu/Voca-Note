package com.example.vocanote.features.words.data

import java.time.Instant

data class SavedWord(
    val id: String,
    val word: String,
    val meaning: String,
    val note: String,
    val createdAt: Instant?
)
