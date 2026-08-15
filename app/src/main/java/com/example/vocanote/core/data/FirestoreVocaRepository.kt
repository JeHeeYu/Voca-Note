package com.example.vocanote.core.data

import com.example.vocanote.core.model.ReviewAnswer
import com.example.vocanote.core.model.ReviewMode
import com.example.vocanote.core.model.ReviewSettings
import com.example.vocanote.core.model.ReviewSessionSummary
import com.example.vocanote.core.model.SavedWord
import com.example.vocanote.core.model.StudyStats
import com.example.vocanote.core.model.WordDraft
import com.example.vocanote.core.model.buildStudyStats
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.util.Locale

class FirestoreVocaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : VocaRepository {
    override fun observeWords(
        userId: String,
        onSuccess: (List<SavedWord>) -> Unit,
        onError: (Throwable) -> Unit
    ): Subscription {
        val registration = userWordsCollection(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val words = snapshot?.documents.orEmpty().mapNotNull { document ->
                    val word = document.getString("word")?.trim().orEmpty()
                    val meaning = document.getString("meaning")?.trim().orEmpty()
                    if (word.isBlank() || meaning.isBlank()) return@mapNotNull null

                    SavedWord(
                        id = document.id,
                        word = word,
                        meaning = meaning,
                        example = document.getString("example")?.trim().orEmpty(),
                        note = document.getString("note")?.trim().orEmpty(),
                        correctCount = document.getLong("correctCount")?.toInt() ?: 0,
                        incorrectCount = document.getLong("incorrectCount")?.toInt() ?: 0,
                        reviewStreak = document.getLong("reviewStreak")?.toInt() ?: 0,
                        reviewIntervalDays = document.getLong("reviewIntervalDays")?.toInt() ?: 0,
                        createdAt = document.getTimestamp("createdAt")?.toDate()?.toInstant(),
                        updatedAt = document.getTimestamp("updatedAt")?.toDate()?.toInstant(),
                        lastReviewedAt = document.getTimestamp("lastReviewedAt")?.toDate()?.toInstant(),
                        nextReviewAt = document.getTimestamp("nextReviewAt")?.toDate()?.toInstant()
                    )
                }.sortedBy { it.word.lowercase(Locale.ROOT) }

                onSuccess(words)
            }
        return Subscription(registration::remove)
    }

    override fun observeReviewSettings(
        userId: String,
        onSuccess: (ReviewSettings) -> Unit,
        onError: (Throwable) -> Unit
    ): Subscription {
        val registration = userReviewSettingsDocument(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                onSuccess(
                    ReviewSettings(
                        sessionSize = snapshot?.getLong("sessionSize")?.toInt()
                            ?: snapshot?.getLong("multipleChoiceCount")?.toInt()
                            ?: ReviewSettings.DEFAULT_SESSION_SIZE,
                        dailyGoal = snapshot?.getLong("dailyGoal")?.toInt()
                            ?: ReviewSettings.DEFAULT_DAILY_GOAL
                    ).normalized()
                )
            }
        return Subscription(registration::remove)
    }

    override fun observeStudyStats(
        userId: String,
        onSuccess: (StudyStats) -> Unit,
        onError: (Throwable) -> Unit
    ): Subscription {
        val registration = userReviewSessionsCollection(userId)
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(SESSION_HISTORY_LIMIT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val sessions = snapshot?.documents.orEmpty().mapNotNull { document ->
                    val completedAt = document.getTimestamp("completedAt")?.toDate()?.toInstant()
                        ?: return@mapNotNull null
                    ReviewSessionSummary(
                        completedAt = completedAt,
                        questionCount = document.getLong("questionCount")?.toInt() ?: 0,
                        correctCount = document.getLong("correctCount")?.toInt() ?: 0
                    )
                }
                onSuccess(buildStudyStats(sessions))
            }
        return Subscription(registration::remove)
    }

    override suspend fun addWord(userId: String, draft: WordDraft) {
        val clean = draft.normalized()
        withTimeout(REQUEST_TIMEOUT_MILLIS) {
            userWordsCollection(userId).add(
                mapOf(
                    "word" to clean.word,
                    "meaning" to clean.meaning,
                    "example" to clean.example,
                    "note" to clean.note,
                    "wordLowercase" to clean.word.lowercase(Locale.ROOT),
                    "correctCount" to 0,
                    "incorrectCount" to 0,
                    "reviewStreak" to 0,
                    "reviewIntervalDays" to 0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    override suspend fun updateWord(userId: String, wordId: String, draft: WordDraft) {
        val clean = draft.normalized()
        withTimeout(REQUEST_TIMEOUT_MILLIS) {
            userWordsCollection(userId).document(wordId).update(
                mapOf(
                    "word" to clean.word,
                    "meaning" to clean.meaning,
                    "example" to clean.example,
                    "note" to clean.note,
                    "wordLowercase" to clean.word.lowercase(Locale.ROOT),
                    "isFavorite" to FieldValue.delete(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    override suspend fun deleteWord(userId: String, wordId: String) {
        withTimeout(REQUEST_TIMEOUT_MILLIS) {
            userWordsCollection(userId).document(wordId).delete().await()
        }
    }

    override suspend fun updateReviewSettings(userId: String, settings: ReviewSettings) {
        val normalized = settings.normalized()
        withTimeout(REQUEST_TIMEOUT_MILLIS) {
            userReviewSettingsDocument(userId).set(
                mapOf(
                    "sessionSize" to normalized.sessionSize,
                    "dailyGoal" to normalized.dailyGoal,
                    "multipleChoiceCount" to FieldValue.delete(),
                    "writingCount" to FieldValue.delete(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
        }
    }

    override suspend fun saveReviewSession(
        userId: String,
        mode: ReviewMode,
        answers: List<ReviewAnswer>
    ) {
        if (answers.isEmpty()) return

        withTimeout(REQUEST_TIMEOUT_MILLIS) {
            val batch = firestore.batch()
            answers.distinctBy { it.wordId }.forEach { answer ->
                val wordDocument = userWordsCollection(userId).document(answer.wordId)
                batch.update(
                    wordDocument,
                    mapOf(
                        (if (answer.isCorrect) "correctCount" else "incorrectCount") to FieldValue.increment(1),
                        "reviewStreak" to answer.reviewStreak,
                        "reviewIntervalDays" to answer.reviewIntervalDays,
                        "nextReviewAt" to Timestamp(answer.nextReviewAt.epochSecond, answer.nextReviewAt.nano),
                        "isFavorite" to FieldValue.delete(),
                        "lastReviewedAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            val sessionDocument = userReviewSessionsCollection(userId).document()
            batch.set(
                sessionDocument,
                mapOf(
                    "mode" to mode.storageValue,
                    "questionCount" to answers.size,
                    "correctCount" to answers.count { it.isCorrect },
                    "wordIds" to answers.map { it.wordId },
                    "incorrectWordIds" to answers.filterNot { it.isCorrect }.map { it.wordId },
                    "completedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        }
    }

    private fun userWordsCollection(userId: String) = firestore
        .collection("users")
        .document(userId)
        .collection("words")

    private fun userReviewSettingsDocument(userId: String) = firestore
        .collection("users")
        .document(userId)
        .collection("settings")
        .document("review")

    private fun userReviewSessionsCollection(userId: String) = firestore
        .collection("users")
        .document(userId)
        .collection("reviewSessions")

    private companion object {
        const val REQUEST_TIMEOUT_MILLIS = 10_000L
        const val SESSION_HISTORY_LIMIT = 120L
    }
}
