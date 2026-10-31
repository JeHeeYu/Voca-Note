package com.example.vocanote.features.review.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vocanote.core.designsystem.ContentMaxWidth
import com.example.vocanote.core.model.*
import com.example.vocanote.features.review.domain.*

@Composable
fun ReviewScreen(
    words: List<SavedWord>,
    settings: ReviewSettings,
    onSaveSession: SaveReviewSession,
    onAddWord: () -> Unit,
    modifier: Modifier = Modifier,
    onSessionStatusChange: (Boolean, Boolean) -> Unit = { _, _ -> },
    session: ReviewSessionViewModel = viewModel()
) {
    var mode by rememberSaveable { mutableStateOf(session.state.mode) }
    var scope by rememberSaveable { mutableStateOf(ReviewScope.All) }
    var presentation by rememberSaveable { mutableStateOf(session.state.presentation) }
    var count by rememberSaveable { mutableStateOf(settings.sessionSize.toString()) }
    var confirmExit by remember { mutableStateOf(false) }
    val state = session.state
    LaunchedEffect(state.dirty, state.saving) { onSessionStatusChange(state.dirty, state.saving) }
    DisposableEffect(Unit) { onDispose { onSessionStatusChange(false, false) } }

    fun start(restriction: Set<String>? = null) {
        session.start(words, mode, presentation, scope, restriction?.size ?: (count.toIntOrNull() ?: 10), restriction)
    }
    fun exit() { if (state.dirty) confirmExit = true else session.reset() }
    BackHandler(state.phase != ReviewPhase.Setup) { exit() }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val content = Modifier.widthIn(max = ContentMaxWidth)
        when (state.phase) {
            ReviewPhase.Setup -> ReviewSetupContent(
                words = words, selectedMode = mode, selectedScope = scope,
                targetCount = count, presentation = presentation,
                onModeChange = { mode = it }, onScopeChange = { scope = it },
                onCountChange = { count = it },
                onPresentationChange = {
                    presentation = it
                    if (it == ReviewPresentation.Batch && mode == ReviewMode.Recall) mode = ReviewMode.Writing
                },
                onStart = { start() }, onAddWord = onAddWord, modifier = content
            )
            ReviewPhase.Question, ReviewPhase.Graded -> {
                if (state.presentation == ReviewPresentation.Batch) {
                    BatchReviewContent(
                        state = state, onInput = session::input,
                        onCheck = { if (session.checkBatch()) session.save(onSaveSession) },
                        onRetrySave = { session.save(onSaveSession) },
                        onSummary = session::showSummary, onExit = ::exit, modifier = content
                    )
                } else {
                    state.current?.let { question ->
                        ReviewQuestionContent(
                            modifier = content, mode = state.mode, question = question,
                            currentIndex = state.index, totalCount = state.questions.size,
                            selectedOption = state.inputs[question.wordId],
                            writingAnswer = state.inputs[question.wordId].orEmpty(),
                            isAnswerChecked = state.checked, isAnswerRevealed = state.revealed,
                            recallResult = state.answers.firstOrNull { it.wordId == question.wordId }?.isCorrect,
                            onExit = ::exit,
                            onSelectOption = { session.input(question.wordId, it); session.check() },
                            onRevealRecall = session::reveal, onRateRecall = session::check,
                            onWritingAnswerChange = { session.input(question.wordId, it) },
                            onCheckWriting = { session.check() },
                            onNext = {
                                session.advance()
                                if (session.state.phase == ReviewPhase.Finished) session.save(onSaveSession)
                            }
                        )
                    }
                }
            }
            ReviewPhase.Finished -> Column(modifier = content) {
                ReviewSaveStatus(state, onRetry = { session.save(onSaveSession) })
                if (state.presentation == ReviewPresentation.Batch) {
                    TextButton(onClick = session::showAnswers, modifier = Modifier.fillMaxWidth()) { Text("전체 답안 다시 보기") }
                }
                ReviewFinishedContent(
                    correctCount = state.answers.count { it.isCorrect }, totalCount = state.answers.size,
                    incorrectCount = state.answers.count { !it.isCorrect },
                    actionsEnabled = state.saved,
                    onRestart = { start(state.questions.map { it.wordId }.toSet()) }, onDone = session::reset,
                    onRetryMistakes = state.answers.filterNot { it.isCorrect }.map { it.wordId }.filter { id -> words.any { it.id == id } }.toSet()
                        .takeIf { it.isNotEmpty() }?.let { ids -> { start(ids) } },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    if (confirmExit) AlertDialog(
        onDismissRequest = { confirmExit = false },
        title = { Text(if (state.saving) "학습 기록을 저장하고 있어요" else "연습을 나갈까요?") },
        text = { Text(if (state.saving) "저장이 끝날 때까지 기다려 주세요." else "저장하지 않은 답안과 학습 기록이 사라져요.") },
        confirmButton = { TextButton(enabled = !state.saving, onClick = { session.reset(); confirmExit = false }) { Text("나가기") } },
        dismissButton = { TextButton(onClick = { confirmExit = false }) { Text("계속하기") } }
    )
}
