package com.example.vocanote.features.review.domain

import com.example.vocanote.core.model.PartOfSpeech
import com.example.vocanote.core.model.ReviewMode
import com.example.vocanote.core.model.SavedWord
import com.example.vocanote.core.model.isDueForReview
import com.example.vocanote.core.model.WordMeaning
import com.example.vocanote.core.model.wordMeanings
import java.time.Instant
import java.text.Normalizer
import java.util.Locale
import kotlin.random.Random

enum class ReviewScope { All, Due, Mistakes }
enum class ReviewPresentation { Single, Batch }

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
    val antonyms: List<String>,
    val derivatives: List<String>,
    val confusableWords: List<String>,
    val collocations: List<String>,
    val spokenText: String?,
    val meanings: List<WordMeaning> = emptyList()
)

private fun distractorsFor(word: SavedWord, pool: List<SavedWord>): List<String> {
    val own = word.wordMeanings.map { it.meaning.trim().lowercase(Locale.ROOT) }.toSet()
    return pool.filter { it.id != word.id }.flatMap { it.wordMeanings }
        .map { it.meaning.trim() }.filter { it.isNotBlank() && it.lowercase(Locale.ROOT) !in own }
        .distinctBy { it.lowercase(Locale.ROOT) }
}

fun eligibleReviewWords(words: List<SavedWord>, targets: List<SavedWord>, mode: ReviewMode): List<SavedWord> =
    targets.filter { word -> word.word.isNotBlank() && word.wordMeanings.any { it.meaning.isNotBlank() } &&
        (mode != ReviewMode.Crossword || crosswordAnswer(word.word).isNotBlank()) &&
        (mode != ReviewMode.MultipleChoice || distractorsFor(word, words).isNotEmpty()) }

fun buildReviewQuestions(
    words: List<SavedWord>,
    mode: ReviewMode,
    targetCount: Int,
    random: Random = Random.Default,
    targets: List<SavedWord> = words,
    scope: ReviewScope = ReviewScope.All,
    now: Instant = Instant.now()
): List<ReviewQuestion> {
    val shuffled = eligibleReviewWords(words, targets, mode).shuffled(random)
    val ordered = if (scope == ReviewScope.All) shuffled else shuffled.sortedWith(
        compareBy<SavedWord> { !it.isDueForReview(now) }.thenBy { it.accuracy }.thenByDescending { it.incorrectCount }
    )
    val eligibleWords = ordered.take(targetCount.coerceIn(0, 30))

    return eligibleWords.map { current ->
        val meanings = current.wordMeanings.filter { it.meaning.isNotBlank() }
        val selected = meanings.random(random)
        val answer = when (mode) {
            ReviewMode.Recall, ReviewMode.MeaningWriting -> meanings.joinToString(" · ") { it.meaning.trim() }
            ReviewMode.MultipleChoice -> selected.meaning.trim()
            ReviewMode.Writing,
            ReviewMode.Listening, ReviewMode.Crossword -> current.word.trim()
        }
        val options = if (mode == ReviewMode.MultipleChoice) {
            val wrongAnswers = distractorsFor(current, words).shuffled(random).take(3)
            (wrongAnswers + answer).distinct().shuffled(random)
        } else {
            emptyList()
        }

        ReviewQuestion(
            wordId = current.id,
            prompt = when (mode) {
                ReviewMode.Recall,
                ReviewMode.MultipleChoice, ReviewMode.MeaningWriting -> current.word.trim()
                ReviewMode.Writing, ReviewMode.Crossword -> selected.meaning.trim()
                ReviewMode.Listening -> "소리를 듣고 단어를 입력하세요"
            },
            answer = answer,
            partOfSpeech = if (mode == ReviewMode.Writing || mode == ReviewMode.MultipleChoice) selected.partOfSpeech else null,
            meanings = meanings,
            options = options,
            context = when (mode) {
                ReviewMode.Recall,
                ReviewMode.Writing -> current.example.toCloze(current.word)
                ReviewMode.MultipleChoice -> current.example.ifBlank { current.note }.trim()
                ReviewMode.Listening -> "재생 버튼을 여러 번 눌러도 괜찮아요"
                ReviewMode.MeaningWriting, ReviewMode.Crossword -> ""
            },
            example = current.example.trim(),
            note = current.note.trim(),
            synonyms = current.synonyms,
            antonyms = current.antonyms,
            derivatives = current.derivatives,
            confusableWords = current.confusableWords,
            collocations = current.collocations,
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

fun gradeBatch(questions: List<ReviewQuestion>, inputs: Map<String, String>, mode: ReviewMode = ReviewMode.Writing): Map<String, Boolean>? {
    if (questions.isEmpty() || questions.any { inputs[it.wordId].isNullOrBlank() }) return null
    return questions.associate { it.wordId to questionMatches(inputs.getValue(it.wordId), it, mode) }
}

fun normalizeMeaningAnswer(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFKC)
    .filterNot { it.isWhitespace() }.lowercase(Locale.ROOT)
private fun withoutMeaningNotes(value: String): String {
    val text = Normalizer.normalize(value, Normalizer.Form.NFKC)
    val result = StringBuilder()
    var depth = 0
    var start = 0
    text.forEachIndexed { index, char ->
        when {
            char == '(' -> {
                if (depth == 0) start = index
                depth++
            }
            char == ')' && depth > 0 -> depth--
            depth == 0 -> result.append(char)
        }
    }
    // An unmatched opening parenthesis must not hide the rest of an answer.
    if (depth > 0) result.append(text.substring(start))
    return result.toString()
}
private fun meaningAlternatives(value: String): List<String> = Normalizer.normalize(value, Normalizer.Form.NFKC)
    .split(Regex("[,;/\\n]")).map(::normalizeMeaningAnswer).filter { it.isNotEmpty() }
fun meaningAnswersMatch(input: String, meanings: List<String>): Boolean {
    val answer = withoutMeaningNotes(input)
    val answers = meaningAlternatives(answer)
    val accepted = meanings.map(::withoutMeaningNotes).flatMap { listOf(normalizeMeaningAnswer(it)) + meaningAlternatives(it) }
        .filter { it.isNotEmpty() }.toSet()
    return normalizeMeaningAnswer(answer) in accepted ||
        (answers.isNotEmpty() && answers.all { it in accepted })
}
fun questionMatches(input: String, question: ReviewQuestion, mode: ReviewMode): Boolean = when (mode) {
    ReviewMode.MeaningWriting -> meaningAnswersMatch(input, question.meanings.map { it.meaning })
    ReviewMode.Crossword -> crosswordAnswer(input) == crosswordAnswer(question.answer)
    else -> answersMatch(input, question.answer)
}
