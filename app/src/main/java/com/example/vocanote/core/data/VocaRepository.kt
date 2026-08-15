package com.example.vocanote.core.data

import com.example.vocanote.core.model.ReviewAnswer
import com.example.vocanote.core.model.ReviewMode
import com.example.vocanote.core.model.ReviewSettings
import com.example.vocanote.core.model.SavedWord
import com.example.vocanote.core.model.StudyStats
import com.example.vocanote.core.model.WordDraft

fun interface Subscription {
    fun cancel()
}

interface VocaRepository {
    fun observeWords(
        userId: String,
        onSuccess: (List<SavedWord>) -> Unit,
        onError: (Throwable) -> Unit
    ): Subscription

    fun observeReviewSettings(
        userId: String,
        onSuccess: (ReviewSettings) -> Unit,
        onError: (Throwable) -> Unit
    ): Subscription

    fun observeStudyStats(
        userId: String,
        onSuccess: (StudyStats) -> Unit,
        onError: (Throwable) -> Unit
    ): Subscription

    suspend fun addWord(userId: String, draft: WordDraft)

    suspend fun updateWord(userId: String, wordId: String, draft: WordDraft)

    suspend fun deleteWord(userId: String, wordId: String)

    suspend fun updateReviewSettings(userId: String, settings: ReviewSettings)

    suspend fun saveReviewSession(
        userId: String,
        mode: ReviewMode,
        answers: List<ReviewAnswer>
    )
}
