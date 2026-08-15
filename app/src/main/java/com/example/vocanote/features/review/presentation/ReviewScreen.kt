package com.example.vocanote.features.review.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.vocanote.core.designsystem.ContentMaxWidth
import com.example.vocanote.core.model.ReviewAnswer
import com.example.vocanote.core.model.ReviewMode
import com.example.vocanote.core.model.ReviewSettings
import com.example.vocanote.core.model.SavedWord
import com.example.vocanote.core.model.isDueForReview
import com.example.vocanote.core.model.needsMistakePractice
import com.example.vocanote.core.model.toReviewAnswer
import com.example.vocanote.features.review.domain.answersMatch
import com.example.vocanote.features.review.domain.buildReviewQuestions

private enum class ReviewPhase { Setup, Question, Finished }
internal enum class ReviewScope { Due, Mistakes, All }

@Composable
fun ReviewScreen(
    words: List<SavedWord>,
    settings: ReviewSettings,
    onSaveSession: (ReviewMode, List<ReviewAnswer>) -> Unit,
    onAddWord: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by rememberSaveable { mutableStateOf(ReviewMode.Recall) }
    var selectedScope by rememberSaveable { mutableStateOf(ReviewScope.Due) }
    var phase by rememberSaveable { mutableStateOf(ReviewPhase.Setup) }
    var sessionId by rememberSaveable { mutableIntStateOf(0) }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedOption by rememberSaveable { mutableStateOf<String?>(null) }
    var writingAnswer by rememberSaveable { mutableStateOf("") }
    var isAnswerChecked by rememberSaveable { mutableStateOf(false) }
    var isAnswerRevealed by rememberSaveable { mutableStateOf(false) }
    var recallResult by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var restrictedWordIds by remember { mutableStateOf<Set<String>?>(null) }
    val answers = remember { mutableStateListOf<ReviewAnswer>() }
    val scopeWords = remember(words, selectedScope, restrictedWordIds) {
        restrictedWordIds?.let { ids -> words.filter { it.id in ids } } ?: when (selectedScope) {
            ReviewScope.Due -> words.filter { it.isDueForReview() }
            ReviewScope.Mistakes -> words
                .filter { it.needsMistakePractice() }
                .sortedWith(compareBy<SavedWord> { it.accuracy }.thenByDescending { it.incorrectCount })
            ReviewScope.All -> words
        }
    }
    val questions = remember(scopeWords, words, selectedMode, settings.sessionSize, sessionId) {
        buildReviewQuestions(scopeWords, selectedMode, settings.sessionSize)
    }

    fun resetSession(restriction: Set<String>? = null) {
        restrictedWordIds = restriction
        sessionId += 1
        currentIndex = 0
        selectedOption = null
        writingAnswer = ""
        isAnswerChecked = false
        isAnswerRevealed = false
        recallResult = null
        answers.clear()
    }

    fun recordAnswer(wordId: String, remembered: Boolean) {
        if (answers.any { it.wordId == wordId }) return
        words.firstOrNull { it.id == wordId }?.let { word ->
            answers += word.toReviewAnswer(remembered)
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        when (phase) {
            ReviewPhase.Setup -> ReviewSetupContent(
                modifier = Modifier.widthIn(max = ContentMaxWidth),
                words = words,
                selectedMode = selectedMode,
                selectedScope = selectedScope,
                targetCount = settings.sessionSize,
                onModeChange = { selectedMode = it },
                onScopeChange = { selectedScope = it },
                onStart = {
                    resetSession(restriction = null)
                    phase = ReviewPhase.Question
                },
                onAddWord = onAddWord
            )

            ReviewPhase.Question -> {
                val question = questions.getOrNull(currentIndex)
                if (question == null) {
                    phase = ReviewPhase.Setup
                } else {
                    ReviewQuestionContent(
                        modifier = Modifier.widthIn(max = ContentMaxWidth),
                        mode = selectedMode,
                        question = question,
                        currentIndex = currentIndex,
                        totalCount = questions.size,
                        selectedOption = selectedOption,
                        writingAnswer = writingAnswer,
                        isAnswerChecked = isAnswerChecked,
                        isAnswerRevealed = isAnswerRevealed,
                        recallResult = recallResult,
                        onExit = { phase = ReviewPhase.Setup },
                        onSelectOption = { option ->
                            if (!isAnswerChecked) {
                                selectedOption = option
                                isAnswerChecked = true
                                recordAnswer(question.wordId, option == question.answer)
                            }
                        },
                        onRevealRecall = { isAnswerRevealed = true },
                        onRateRecall = { remembered ->
                            if (!isAnswerChecked) {
                                recallResult = remembered
                                isAnswerChecked = true
                                recordAnswer(question.wordId, remembered)
                            }
                        },
                        onWritingAnswerChange = { writingAnswer = it },
                        onCheckWriting = {
                            if (!isAnswerChecked) {
                                isAnswerChecked = true
                                recordAnswer(question.wordId, answersMatch(writingAnswer, question.answer))
                            }
                        },
                        onNext = {
                            if (currentIndex == questions.lastIndex) {
                                onSaveSession(selectedMode, answers.toList())
                                phase = ReviewPhase.Finished
                            } else {
                                currentIndex += 1
                                selectedOption = null
                                writingAnswer = ""
                                isAnswerChecked = false
                                isAnswerRevealed = false
                                recallResult = null
                            }
                        }
                    )
                }
            }

            ReviewPhase.Finished -> ReviewFinishedContent(
                modifier = Modifier.widthIn(max = ContentMaxWidth),
                correctCount = answers.count { it.isCorrect },
                totalCount = answers.size,
                incorrectCount = answers.count { !it.isCorrect },
                onRestart = {
                    resetSession(restrictedWordIds)
                    phase = ReviewPhase.Question
                },
                onRetryMistakes = answers.filterNot { it.isCorrect }
                    .map { it.wordId }
                    .toSet()
                    .takeIf { it.isNotEmpty() }
                    ?.let { missedIds ->
                        {
                            resetSession(missedIds)
                            phase = ReviewPhase.Question
                        }
                    },
                onDone = {
                    restrictedWordIds = null
                    phase = ReviewPhase.Setup
                }
            )
        }
    }
}
