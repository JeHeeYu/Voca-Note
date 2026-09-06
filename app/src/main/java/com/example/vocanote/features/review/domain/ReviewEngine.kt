package com.example.vocanote.features.review.domain

import com.example.vocanote.core.model.PartOfSpeech
import com.example.vocanote.core.model.ReviewMode
import com.example.vocanote.core.model.SavedWord
import com.example.vocanote.core.model.isDueForReview
import kotlin.random.Random

data class ReviewQuestion(
    val wordId: String,
    val prompt: String,
    val answer: String,
    val partOfSpeech: PartOfSpeech?,
    val options: List<String>,
    val context: String,
    val example: String,
    val note: String,
    val synonyms: List<String>,
    val derivatives: List<String>,
    val spokenText: String?
)

fun buildReviewQuestions(
    words: List<SavedWord>,
    mode: ReviewMode,
    targetCount: Int,
    random: Random = Random.Default
): List<ReviewQuestion> {
    val eligibleWords = words
        .filter { it.word.isNotBlank() && it.meaning.isNotBlank() }
        .shuffled(random)
        .sortedWith(
            compareBy<SavedWord> { !it.isDueForReview() }
                .thenBy { it.accuracy }
                .thenByDescending { it.incorrectCount }
        )
        .take(targetCount.coerceAtLeast(0))

    return eligibleWords.map { current ->
        val answer = when (mode) {
            ReviewMode.Recall,
            ReviewMode.MultipleChoice -> current.meaning.trim()
            ReviewMode.Writing,
            ReviewMode.Listening -> current.word.trim()
        }
        val options = if (mode == ReviewMode.MultipleChoice) {
            val wrongAnswers = words
                .asSequence()
                .filter { it.id != current.id }
                .map { it.meaning.trim() }
                .filter { it.isNotBlank() && !it.equals(answer, ignoreCase = true) }
                .distinct()
                .toList()
                .shuffled(random)
                .take(3)
            (wrongAnswers + answer).distinct().shuffled(random)
        } else {
            emptyList()
        }

        ReviewQuestion(
            wordId = current.id,
            prompt = when (mode) {
                ReviewMode.Recall,
                ReviewMode.MultipleChoice -> current.word.trim()
                ReviewMode.Writing -> current.meaning.trim()
                ReviewMode.Listening -> "소리를 듣고 단어를 입력하세요"
            },
            answer = answer,
            partOfSpeech = current.partOfSpeech,
            options = options,
            context = when (mode) {
                ReviewMode.Recall,
                ReviewMode.Writing -> current.example.toCloze(current.word)
                ReviewMode.MultipleChoice -> current.example.ifBlank { current.note }.trim()
                ReviewMode.Listening -> "재생 버튼을 여러 번 눌러도 괜찮아요"
            },
            example = current.example.trim(),
            note = current.note.trim(),
            synonyms = current.synonyms,
            derivatives = current.derivatives,
            spokenText = if (mode == ReviewMode.Listening || mode == ReviewMode.Recall) {
                current.word.trim()
            } else {
                null
            }
        )
    }
}

fun answersMatch(input: String, answer: String): Boolean =
    input.trim().equals(answer.trim(), ignoreCase = true)

private fun String.toCloze(word: String): String {
    if (isBlank()) return ""
    return replace(Regex(Regex.escape(word), RegexOption.IGNORE_CASE), "_____")
}
