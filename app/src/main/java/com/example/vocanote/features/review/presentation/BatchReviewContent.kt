package com.example.vocanote.features.review.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.vocanote.core.designsystem.VocaSpacing
import com.example.vocanote.core.model.ReviewMode
import com.example.vocanote.core.model.WordMeaning
import com.example.vocanote.core.speech.rememberWordSpeaker
import com.example.vocanote.features.review.domain.ReviewQuestion
import kotlinx.coroutines.launch

@Composable
internal fun BatchReviewContent(
    state: ReviewSessionState,
    onInput: (String, String) -> Unit,
    onCheck: () -> Unit,
    onRetrySave: () -> Unit,
    onSummary: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var attempted by remember(state.id) { mutableStateOf(false) }
    val graded = state.phase == ReviewPhase.Graded
    val missing = state.questions.filter { state.inputs[it.wordId].isNullOrBlank() }
    val results = state.answers.associateBy { it.wordId }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val speaker = rememberWordSpeaker()
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().testTag("batch_questions"),
        contentPadding = PaddingValues(VocaSpacing.large),
        verticalArrangement = Arrangement.spacedBy(VocaSpacing.medium)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(VocaSpacing.small)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("한 번에 풀기", style = MaterialTheme.typography.headlineSmall)
                    TextButton(onClick = onExit) { Text("나가기") }
                }
                Text(if (graded) "${state.questions.size}문제 중 ${state.answers.count { it.isCorrect }}개 정답"
                    else "${state.questions.size - missing.size}/${state.questions.size}문제 입력 · 모두 풀고 함께 확인하세요")
                if (state.mode == ReviewMode.MeaningWriting) Text("띄어쓰기는 무시해요. 등록된 뜻 중 하나만 맞히면 정답이에요.", style = MaterialTheme.typography.bodySmall)
                if (graded) {
                    ReviewSaveStatus(state, onRetrySave)
                    Button(onClick = onSummary, modifier = Modifier.fillMaxWidth()) { Text("학습 요약 보기") }
                } else {
                    Button(onClick = {
                        attempted = true
                        if (missing.isEmpty()) {
                            keyboard?.hide()
                            onCheck()
                            coroutineScope.launch { listState.animateScrollToItem(0) }
                        } else {
                            val index = state.questions.indexOfFirst { it.wordId == missing.first().wordId }
                            coroutineScope.launch { listState.animateScrollToItem(index + 1) }
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("전체 정답 확인") }
                    if (attempted && missing.isNotEmpty()) Text("아직 ${missing.size}문제의 답이 비어 있어요.", color = MaterialTheme.colorScheme.error)
                }
            }
        }
        itemsIndexed(state.questions, key = { _, question -> question.wordId }) { index, question ->
            BatchQuestionCard(
                index = index, question = question, mode = state.mode,
                value = state.inputs[question.wordId].orEmpty(),
                onValueChange = { onInput(question.wordId, it) }, graded = graded,
                correct = results[question.wordId]?.isCorrect == true,
                missing = attempted && state.inputs[question.wordId].isNullOrBlank(),
                canSpeak = speaker.isReady, onSpeak = { speaker.speak(question.answer) }
            )
        }
    }
}

@Composable
private fun BatchQuestionCard(
    index: Int, question: ReviewQuestion, mode: ReviewMode, value: String,
    onValueChange: (String) -> Unit, graded: Boolean, correct: Boolean, missing: Boolean,
    canSpeak: Boolean, onSpeak: () -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current
    Surface(
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, if (graded && !correct || !graded && missing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().testTag("batch_question_${question.wordId}")
    ) {
        Column(modifier = Modifier.padding(VocaSpacing.medium), verticalArrangement = Arrangement.spacedBy(VocaSpacing.small)) {
            Text("${index + 1}번 문제" + if (graded) if (correct) " · 정답" else " · 오답" else "", style = MaterialTheme.typography.labelLarge)
            Text(question.prompt, style = MaterialTheme.typography.titleLarge)
            question.partOfSpeech?.let { Text(it.displayLabel, style = MaterialTheme.typography.bodySmall) }
            if (mode == ReviewMode.Listening) {
                OutlinedButton(onClick = onSpeak, enabled = canSpeak) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                    Text("발음 듣기")
                }
            } else if (question.context.isNotBlank()) Text(question.context, style = MaterialTheme.typography.bodySmall)
            if (mode == ReviewMode.MultipleChoice) {
                question.options.forEach { option ->
                    FilterChip(selected = value == option, onClick = { onValueChange(option) }, enabled = !graded,
                        label = { Text(option) }, modifier = Modifier.fillMaxWidth())
                }
            } else {
                OutlinedTextField(
                    value = value, onValueChange = onValueChange, readOnly = graded,
                    label = { Text("${index + 1}번 답") }, singleLine = true,
                    placeholder = { Text(if (mode == ReviewMode.MeaningWriting) "한글 뜻 하나 입력" else "영어 단어 입력") },
                    isError = !graded && missing, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = if (mode == ReviewMode.MeaningWriting) KeyboardType.Text else KeyboardType.Ascii, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboard?.hide() })
                )
            }
            if (graded) {
                if (!correct) Text("정답: ${question.answer}", color = MaterialTheme.colorScheme.error)
                MeaningList(question.meanings)
            }
        }
    }
}

@Composable
internal fun MeaningList(meanings: List<WordMeaning>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        meanings.forEachIndexed { index, entry ->
            Text(listOfNotNull("${index + 1}.", entry.partOfSpeech?.displayLabel, entry.meaning).joinToString(" "), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun ReviewSaveStatus(state: ReviewSessionState, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(VocaSpacing.small)) {
        when {
            state.saving -> Text("학습 기록을 저장하고 있어요.")
            state.saved -> Text("학습 기록과 다음 복습 시간을 저장했어요.", color = MaterialTheme.colorScheme.primary)
            state.saveError != null -> {
                Text(state.saveError, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onRetry) { Text("저장 다시 시도") }
            }
        }
    }
}
