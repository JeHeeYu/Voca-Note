package com.example.vocanote.features.review.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.vocanote.core.model.*
import com.example.vocanote.features.review.domain.*
import java.util.UUID

enum class ReviewPhase { Setup, Question, Graded, Finished }
typealias SaveReviewSession = (String, ReviewMode, List<ReviewAnswer>, (String?) -> Unit) -> Unit

data class ReviewSessionState(
    val id: String = "",
    val phase: ReviewPhase = ReviewPhase.Setup,
    val mode: ReviewMode = ReviewMode.Recall,
    val presentation: ReviewPresentation = ReviewPresentation.Single,
    val questions: List<ReviewQuestion> = emptyList(),
    val sourceWords: Map<String, SavedWord> = emptyMap(),
    val inputs: Map<String, String> = emptyMap(),
    val answers: List<ReviewAnswer> = emptyList(),
    val index: Int = 0,
    val revealed: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val saveError: String? = null
) {
    val current: ReviewQuestion? get() = questions.getOrNull(index)
    val checked: Boolean get() = answers.any { it.wordId == current?.wordId }
    val dirty: Boolean get() = phase != ReviewPhase.Setup && !saved
}

class ReviewSessionViewModel : ViewModel() {
    var state by mutableStateOf(ReviewSessionState())
        private set

    fun start(words: List<SavedWord>, mode: ReviewMode, presentation: ReviewPresentation,
              scope: ReviewScope, count: Int, restriction: Set<String>? = null) {
        val targets = words.filter {
            if (restriction != null) it.id in restriction else when (scope) {
                ReviewScope.All -> true
                ReviewScope.Due -> it.isDueForReview()
                ReviewScope.Mistakes -> it.needsMistakePractice()
            }
        }
        val questions = buildReviewQuestions(words, mode, count, targets = targets, scope = scope)
        if (questions.isEmpty()) return
        state = ReviewSessionState(
            id = UUID.randomUUID().toString(), phase = ReviewPhase.Question, mode = mode,
            presentation = if (mode == ReviewMode.Recall) ReviewPresentation.Single else presentation,
            questions = questions, sourceWords = targets.associateBy { it.id }
        )
    }

    fun input(wordId: String, value: String) {
        if (state.phase != ReviewPhase.Question || state.answers.any { it.wordId == wordId }) return
        state = state.copy(inputs = state.inputs + (wordId to value))
    }

    fun reveal() { state = state.copy(revealed = true) }

    fun check(remembered: Boolean? = null) {
        val current = state.current ?: return
        if (state.phase != ReviewPhase.Question || state.checked) return
        val input = state.inputs[current.wordId].orEmpty()
        if (remembered == null && input.isBlank()) return
        val correct = remembered ?: answersMatch(input, current.answer)
        val word = state.sourceWords.getValue(current.wordId)
        state = state.copy(answers = state.answers + word.toReviewAnswer(correct), revealed = true)
    }

    fun advance() {
        if (!state.checked) return
        state = if (state.index == state.questions.lastIndex) state.copy(phase = ReviewPhase.Finished)
        else state.copy(index = state.index + 1, revealed = false)
    }

    fun checkBatch(): Boolean {
        if (state.phase != ReviewPhase.Question || state.presentation != ReviewPresentation.Batch) return false
        val graded = gradeBatch(state.questions, state.inputs) ?: return false
        state = state.copy(
            phase = ReviewPhase.Graded,
            answers = graded.map { (id, correct) -> state.sourceWords.getValue(id).toReviewAnswer(correct) }
        )
        return true
    }

    fun save(onSave: SaveReviewSession) {
        if (state.saving || state.saved || state.answers.size != state.questions.size || state.answers.isEmpty()) return
        val id = state.id
        state = state.copy(saving = true, saveError = null)
        onSave(id, state.mode, state.answers) { error ->
            if (state.id == id) state = state.copy(saving = false, saved = error == null, saveError = error)
        }
    }

    fun showSummary() { state = state.copy(phase = ReviewPhase.Finished) }
    fun showAnswers() { state = state.copy(phase = ReviewPhase.Graded) }
    fun reset() { if (!state.saving) state = ReviewSessionState() }
}
