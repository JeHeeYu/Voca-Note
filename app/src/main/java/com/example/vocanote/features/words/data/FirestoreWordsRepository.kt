package com.example.vocanote.features.words.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

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
                    if (word.isBlank() || meaning.isBlank()) {
                        null
                    } else {
                        SavedWord(
                            id = document.id,
                            word = word,
                            meaning = meaning
                        )
                    }
                }

                onSuccess(words)
            }
    }

    suspend fun addWord(
        userId: String,
        word: String,
        meaning: String
    ) {
        val cleanWord = word.trim()
        val cleanMeaning = meaning.trim()

        userWordsCollection(userId)
            .add(
                mapOf(
                    "word" to cleanWord,
                    "meaning" to cleanMeaning,
                    "wordLowercase" to cleanWord.lowercase(),
                    "createdAt" to Timestamp.now()
                )
            )
            .await()
    }

    private fun userWordsCollection(userId: String) = firestore
        .collection("users")
        .document(userId)
        .collection("words")
}
