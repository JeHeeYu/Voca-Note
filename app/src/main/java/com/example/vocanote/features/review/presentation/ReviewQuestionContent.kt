package com.example.vocanote.features.review.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vocanote.core.designsystem.VocaSpacing
import com.example.vocanote.core.model.ReviewMode
import com.example.vocanote.core.speech.rememberWordSpeaker
import com.example.vocanote.features.review.domain.ReviewQuestion
import com.example.vocanote.features.review.domain.answersMatch
import com.example.vocanote.ui.theme.AccentCoralLight
import com.example.vocanote.ui.theme.BrandLight

@Composable
internal fun ReviewQuestionContent(
    mode: ReviewMode,
    question: ReviewQuestion,
    currentIndex: Int,
    totalCount: Int,
    selectedOption: String?,
    writingAnswer: String,
    isAnswerChecked: Boolean,
    isAnswerRevealed: Boolean,
    recallResult: Boolean?,
    onExit: () -> Unit,
    onSelectOption: (String) -> Unit,
    onRevealRecall: () -> Unit,
    onRateRecall: (Boolean) -> Unit,
    onWritingAnswerChange: (String) -> Unit,
    onCheckWriting: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val writingIsCorrect = answersMatch(writingAnswer, question.answer)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val speaker = rememberWordSpeaker()

    LaunchedEffect(question.wordId, mode, speaker.isReady) {
        if (mode == ReviewMode.Listening && speaker.isReady) {
            question.spokenText?.let { speaker.speak(it) }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = VocaSpacing.large,
            top = VocaSpacing.medium,
            end = VocaSpacing.large,
            bottom = VocaSpacing.xLarge
        ),
        verticalArrangement = Arrangement.spacedBy(VocaSpacing.large)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "${currentIndex + 1} / $totalCount", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = when (mode) {
                            ReviewMode.Recall -> "뜻을 먼저 떠올린 뒤 확인하세요"
                            ReviewMode.MultipleChoice -> "알맞은 뜻을 고르세요"
                            ReviewMode.Writing -> "영어 단어를 입력하세요"
                            ReviewMode.Listening -> "발음을 듣고 철자를 입력하세요"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onExit) {
                    Icon(Icons.Default.Close, contentDescription = "연습 종료")
                }
            }
        }
        item {
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / totalCount.coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            QuestionPrompt(
                mode = mode,
                question = question,
                canSpeak = speaker.isReady,
                onSpeak = { question.spokenText?.let { speaker.speak(it) } }
            )
        }

        if (mode == ReviewMode.Recall) {
            if (!isAnswerRevealed) {
                item {
                    Button(
                        onClick = onRevealRecall,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("뜻 확인")
                    }
                }
            } else {
                item { RecallAnswerPanel(question = question) }
                if (!isAnswerChecked) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(VocaSpacing.small)
                        ) {
                            OutlinedButton(
                                onClick = { onRateRecall(false) },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("모름")
                            }
                            Button(
                                onClick = { onRateRecall(true) },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("기억남")
                            }
                        }
                    }
                }
            }
        } else if (mode == ReviewMode.MultipleChoice) {
            itemsIndexed(question.options) { index, option ->
                AnswerOption(
                    index = index,
                    text = option,
                    selected = option == selectedOption,
                    isCorrectAnswer = option == question.answer,
                    isAnswerChecked = isAnswerChecked,
                    onClick = { onSelectOption(option) }
                )
            }
        } else {
            item {
                OutlinedTextField(
                    value = writingAnswer,
                    onValueChange = { if (!isAnswerChecked) onWritingAnswerChange(it) },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    label = { Text("정답") },
                        placeholder = { Text(if (mode == ReviewMode.Listening) "들은 단어 입력" else "영어 단어 입력") },
                    readOnly = isAnswerChecked,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        if (writingAnswer.isNotBlank()) onCheckWriting()
                    })
                )
            }
            if (!isAnswerChecked) {
                item {
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            onCheckWriting()
                        },
                        enabled = writingAnswer.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("정답 확인")
                    }
                }
            }
        }

        if (isAnswerChecked) {
            val isCorrect = when (mode) {
                ReviewMode.Recall -> recallResult == true
                ReviewMode.MultipleChoice -> selectedOption == question.answer
                ReviewMode.Writing,
                ReviewMode.Listening -> writingIsCorrect
            }
            item {
                FeedbackPanel(
                    isCorrect = isCorrect,
                    answer = question.answer,
                    showAnswer = mode != ReviewMode.Recall
                )
            }
            item {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(if (currentIndex == totalCount - 1) "결과 보기" else "다음 문제")
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.padding(start = VocaSpacing.small)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionPrompt(
    mode: ReviewMode,
    question: ReviewQuestion,
    canSpeak: Boolean,
    onSpeak: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().heightIn(min = 190.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(VocaSpacing.xLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            question.partOfSpeech?.let { partOfSpeech ->
                Text(
                    text = "품사 · ${partOfSpeech.label}",
                    modifier = Modifier.padding(bottom = VocaSpacing.small),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (mode == ReviewMode.Listening) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    IconButton(onClick = onSpeak, enabled = canSpeak, modifier = Modifier.size(72.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "발음 다시 듣기",
                            modifier = Modifier.size(34.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Text(
                    text = question.prompt,
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            if (question.context.isNotBlank()) {
                Text(
                    text = question.context,
                    modifier = Modifier.padding(top = VocaSpacing.medium),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun RecallAnswerPanel(question: ReviewQuestion) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(VocaSpacing.large),
            verticalArrangement = Arrangement.spacedBy(VocaSpacing.small)
        ) {
            Text(text = question.answer, style = MaterialTheme.typography.headlineSmall)
            if (question.example.isNotBlank()) {
                Text(
                    text = question.example,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (question.note.isNotBlank()) {
                Text(
                    text = question.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (question.synonyms.isNotEmpty()) {
                RelatedTerms(label = "유의어", terms = question.synonyms)
            }
            if (question.antonyms.isNotEmpty()) {
                RelatedTerms(label = "반의어", terms = question.antonyms)
            }
            if (question.derivatives.isNotEmpty()) {
                RelatedTerms(label = "파생어", terms = question.derivatives)
            }
            if (question.confusableWords.isNotEmpty()) {
                RelatedTerms(label = "혼동어", terms = question.confusableWords)
            }
            if (question.collocations.isNotEmpty()) {
                RelatedTerms(label = "숙어·연어", terms = question.collocations)
            }
        }
    }
}

@Composable
private fun RelatedTerms(label: String, terms: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = terms.joinToString(" · "),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AnswerOption(
    index: Int,
    text: String,
    selected: Boolean,
    isCorrectAnswer: Boolean,
    isAnswerChecked: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        isAnswerChecked && isCorrectAnswer -> BrandLight
        isAnswerChecked && selected -> AccentCoralLight
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        isAnswerChecked && isCorrectAnswer -> MaterialTheme.colorScheme.primary
        isAnswerChecked && selected -> MaterialTheme.colorScheme.error
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !isAnswerChecked, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        border = BorderStroke(if (selected || isCorrectAnswer && isAnswerChecked) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(VocaSpacing.large),
            horizontalArrangement = Arrangement.spacedBy(VocaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(30.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = ('A'.code + index).toChar().toString(), style = MaterialTheme.typography.labelLarge)
                }
            }
            Text(text = text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            if (isAnswerChecked && isCorrectAnswer) {
                Icon(Icons.Default.Check, contentDescription = "정답", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun FeedbackPanel(isCorrect: Boolean, answer: String, showAnswer: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isCorrect) BrandLight else AccentCoralLight,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(VocaSpacing.large),
            verticalArrangement = Arrangement.spacedBy(VocaSpacing.xSmall)
        ) {
            Text(
                text = if (isCorrect) "기억했어요" else "다시 보면 더 오래 남아요",
                style = MaterialTheme.typography.titleMedium,
                color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            if (!isCorrect && showAnswer) {
                Text(text = "정답: $answer", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
