package com.example.vocanote.features.words.data

import kotlinx.coroutines.withTimeout
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import java.time.Instant

class FirestoreWordsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeWords(
        userId: String,
        onSuccess: (List<SavedWord>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration {
        return userWordsCollection(userId)
            .orderBy("wordLowercase")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val words = snapshot?.documents.orEmpty().mapNotNull { document ->
                    val word = document.getString("word")?.trim().orEmpty()
                    val meaning = document.getString("meaning")?.trim().orEmpty()
                    val note = document.getString("note")?.trim().orEmpty()
                    val createdAt = document.getTimestamp("createdAt")?.toDate()?.toInstant()
                    if (word.isBlank() || meaning.isBlank()) {
                        null
                    } else {
                        SavedWord(
                            id = document.id,
                            word = word,
                            meaning = meaning,
                            note = note,
                            createdAt = createdAt
                        )
                    }
                }

                onSuccess(words)
            }
    }

    suspend fun addWord(
        userId: String,
        word: String,
        meaning: String,
        note: String
    ) {
        val cleanWord = word.trim()
        val cleanMeaning = meaning.trim()
        val cleanNote = note.trim()

        withTimeout(10_000) {
            userWordsCollection(userId)
                .add(
                    mapOf(
                        "word" to cleanWord,
                        "meaning" to cleanMeaning,
                        "note" to cleanNote,
                        "wordLowercase" to cleanWord.lowercase(),
                        "createdAt" to Timestamp.now()
                    )
                )
                .await()
        }
    }

    suspend fun updateWord(
        userId: String,
        wordId: String,
        word: String,
        meaning: String,
        note: String
    ) {
        val cleanWord = word.trim()
        val cleanMeaning = meaning.trim()
        val cleanNote = note.trim()

        withTimeout(10_000) {
            userWordsCollection(userId)
                .document(wordId)
                .update(
                    mapOf(
                        "word" to cleanWord,
                        "meaning" to cleanMeaning,
                        "note" to cleanNote,
                        "wordLowercase" to cleanWord.lowercase()
                    )
                )
                .await()
        }
    }

    private fun userWordsCollection(userId: String) = firestore
        .collection("users")
        .document(userId)
        .collection("words")
}
