package com.example.vocanote.core.data

import com.example.vocanote.core.model.toReviewAnswer
import com.example.vocanote.core.model.decodeMeanings
import com.example.vocanote.core.model.toStorageMeanings
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
                    val meanings = decodeMeanings(
                        document.get("meanings"), document.getString("meaning").orEmpty(),
                        document.getString("partOfSpeech")
                    )
                    val meaning = meanings.first().meaning
                    if (word.isBlank() || meaning.isBlank()) return@mapNotNull null

                    SavedWord(
                        id = document.id,
                        word = word,
                        meaning = meaning,
                        partOfSpeech = meanings.first().partOfSpeech,
                        meanings = meanings,
                        example = document.getString("example")?.trim().orEmpty(),
                        note = document.getString("note")?.trim().orEmpty(),
                        synonyms = document.get("synonyms").toTermList(),
                        antonyms = document.get("antonyms").toTermList(),
                        derivatives = document.get("derivatives").toTermList(),
                        confusableWords = document.get("confusableWords").toTermList(),
                        collocations = document.get("collocations").toTermList(),
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
                    "meanings" to clean.meanings.toStorageMeanings(),
                    "partOfSpeech" to clean.partOfSpeech?.storageValue,
                    "example" to clean.example,
                    "note" to clean.note,
                    "synonyms" to clean.synonyms,
                    "antonyms" to clean.antonyms,
                    "derivatives" to clean.derivatives,
                    "confusableWords" to clean.confusableWords,
                    "collocations" to clean.collocations,
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
                    "meanings" to clean.meanings.toStorageMeanings(),
                    "partOfSpeech" to (clean.partOfSpeech?.storageValue ?: FieldValue.delete()),
                    "example" to clean.example,
                    "note" to clean.note,
                    "synonyms" to clean.synonyms,
                    "antonyms" to clean.antonyms,
                    "derivatives" to clean.derivatives,
                    "confusableWords" to clean.confusableWords,
                    "collocations" to clean.collocations,
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
        sessionId: String,
        mode: ReviewMode,
        answers: List<ReviewAnswer>
    ) {
        require(answers.size in 1..30 && answers.map { it.wordId }.distinct().size == answers.size)
        val sessionDocument = userReviewSessionsCollection(userId).document(sessionId)
        withTimeout(REQUEST_TIMEOUT_MILLIS) {
            firestore.runTransaction { transaction ->
                if (transaction.get(sessionDocument).exists()) return@runTransaction null
                // Read all words before any write, and schedule from their current server state.
                val documents = answers.map { answer ->
                    val reference = userWordsCollection(userId).document(answer.wordId)
                    val document = transaction.get(reference)
                    check(document.exists()) { "A word was deleted during this session" }
                    reference to document
                }
                answers.zip(documents).forEach { (answer, pair) ->
                    val (reference, document) = pair
                    val latest = SavedWord(
                        id = answer.wordId, word = document.getString("word").orEmpty(), meaning = "",
                        reviewIntervalDays = document.getLong("reviewIntervalDays")?.toInt() ?: 0,
                        reviewStreak = document.getLong("reviewStreak")?.toInt() ?: 0
                    ).toReviewAnswer(answer.isCorrect)
                    transaction.update(reference, mapOf(
                        (if (answer.isCorrect) "correctCount" else "incorrectCount") to FieldValue.increment(1),
                        "reviewStreak" to latest.reviewStreak,
                        "reviewIntervalDays" to latest.reviewIntervalDays,
                        "nextReviewAt" to Timestamp(latest.nextReviewAt.epochSecond, latest.nextReviewAt.nano),
                        "lastReviewedAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ))
                }
                transaction.set(sessionDocument, mapOf(
                    "mode" to mode.storageValue,
                    "questionCount" to answers.size,
                    "correctCount" to answers.count { it.isCorrect },
                    "wordIds" to answers.map { it.wordId },
                    "incorrectWordIds" to answers.filterNot { it.isCorrect }.map { it.wordId },
                    "completedAt" to FieldValue.serverTimestamp()
                ))
                null
            }.await()
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

private fun Any?.toTermList(): List<String> = (this as? List<*>)
    .orEmpty()
    .filterIsInstance<String>()
    .map(String::trim)
    .filter(String::isNotBlank)
    .distinctBy { it.lowercase(Locale.ROOT) }
