package com.example.vocanote.features.review.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.vocanote.features.words.data.SavedWord
import com.example.vocanote.ui.theme.AccentCoral
import com.example.vocanote.ui.theme.BorderBlue
import com.example.vocanote.ui.theme.Canvas
import com.example.vocanote.ui.theme.InkSoft
import com.example.vocanote.ui.theme.PrimaryBlue

private enum class ReviewMode(val title: String, val subtitle: String) {
    MultipleChoice("뜻 고르기", "보기에서 뜻을 선택"),
    Writing("직접 쓰기", "뜻을 보고 단어를 입력")
}

private data class ReviewQuestion(
    val word: SavedWord,
    val options: List<String>
)

@Composable
fun ReviewPage(
    words: List<SavedWord>,
    modifier: Modifier = Modifier
) {
    val reviewWords = remember(words) {
        words.filter { it.word.isNotBlank() && it.meaning.isNotBlank() }
    }
    val multipleChoiceQuestions = remember(reviewWords) {
        buildQuestions(reviewWords, reverse = false)
    }
    val writingQuestions = remember(reviewWords) {
        buildQuestions(reviewWords, reverse = true)
    }
    var selectedMode by rememberSaveable { mutableStateOf(ReviewMode.MultipleChoice) }
    var mcCurrentIndex by rememberSaveable(multipleChoiceQuestions) { mutableIntStateOf(0) }
    var mcScore by rememberSaveable(multipleChoiceQuestions) { mutableIntStateOf(0) }
    var mcSelectedOption by rememberSaveable(multipleChoiceQuestions, mcCurrentIndex) { mutableStateOf<String?>(null) }
    var mcIsAnswered by rememberSaveable(multipleChoiceQuestions, mcCurrentIndex) { mutableStateOf(false) }
    var writingCurrentIndex by rememberSaveable(writingQuestions) { mutableIntStateOf(0) }
    var writingScore by rememberSaveable(writingQuestions) { mutableIntStateOf(0) }
    var writingAnswer by rememberSaveable(writingQuestions, writingCurrentIndex) { mutableStateOf("") }
    var writingFeedback by rememberSaveable(writingQuestions, writingCurrentIndex) { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Canvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "복습",
                style = MaterialTheme.typography.headlineMedium
            )

            if (multipleChoiceQuestions.isEmpty() && writingQuestions.isEmpty()) {
                EmptyReviewCard()
                return@Column
            }

            ModePicker(
                selectedMode = selectedMode,
                onModeChange = { selectedMode = it }
            )

            when (selectedMode) {
                ReviewMode.MultipleChoice -> MultipleChoiceReview(
                    questions = multipleChoiceQuestions,
                    currentIndex = mcCurrentIndex,
                    score = mcScore,
                    selectedOption = mcSelectedOption,
                    isAnswered = mcIsAnswered,
                    onAnswer = { option, isCorrect ->
                        mcSelectedOption = option
                        mcIsAnswered = true
                        if (isCorrect) mcScore += 1
                    },
                    onNext = {
                        mcCurrentIndex += 1
                        mcSelectedOption = null
                        mcIsAnswered = false
                    },
                    onRestart = {
                        mcCurrentIndex = 0
                        mcScore = 0
                        mcSelectedOption = null
                        mcIsAnswered = false
                    }
                )
                ReviewMode.Writing -> WritingReview(
                    questions = writingQuestions,
                    currentIndex = writingCurrentIndex,
                    score = writingScore,
                    answer = writingAnswer,
                    feedback = writingFeedback,
                    onAnswerChange = { writingAnswer = it },
                    onCheck = { isCorrect, message ->
                        if (isCorrect) writingScore += 1
                        writingFeedback = message
                    },
                    onNext = {
                        writingCurrentIndex += 1
                        writingAnswer = ""
                        writingFeedback = null
                    },
                    onRestart = {
                        writingCurrentIndex = 0
                        writingScore = 0
                        writingAnswer = ""
                        writingFeedback = null
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyReviewCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "문제를 만들 단어가 아직 부족해요",
                style = MaterialTheme.typography.titleLarge,
                color = PrimaryBlue
            )
            Text(
                text = "뜻과 단어가 모두 들어간 항목을 더 추가하면 여기서 바로 문제를 풀 수 있어요.",
                style = MaterialTheme.typography.bodyLarge,
                color = InkSoft
            )
        }
    }
}

@Composable
private fun ModePicker(
    selectedMode: ReviewMode,
    onModeChange: (ReviewMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReviewMode.entries.forEach { mode ->
            if (selectedMode == mode) {
                Button(
                    onClick = { onModeChange(mode) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    ModeText(mode)
                }
            } else {
                OutlinedButton(
                    onClick = { onModeChange(mode) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    ModeText(mode)
                }
            }
        }
    }
}

@Composable
private fun ModeText(mode: ReviewMode) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = mode.title,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = mode.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = InkSoft
        )
    }
}

@Composable
private fun MultipleChoiceReview(
    questions: List<ReviewQuestion>,
    currentIndex: Int,
    score: Int,
    selectedOption: String?,
    isAnswered: Boolean,
    onAnswer: (String, Boolean) -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit
) {
    if (currentIndex >= questions.size) {
        ReviewFinishedCard(
            totalCount = questions.size,
            score = score,
            onRestart = onRestart
        )
        return
    }

    val question = questions[currentIndex]
    val isCorrect = selectedOption == question.word.meaning

    ReviewPromptCard(
        progress = "${currentIndex + 1} / ${questions.size}",
        title = question.word.word,
        note = question.word.note
    )

    question.options.forEach { option ->
        val borderColor = when {
            !isAnswered -> BorderBlue
            option == question.word.meaning -> PrimaryBlue
            option == selectedOption && !isCorrect -> AccentCoral
            else -> BorderBlue
        }

        OutlinedButton(
            onClick = {
                if (isAnswered) return@OutlinedButton
                onAnswer(option, option == question.word.meaning)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, borderColor),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (option == selectedOption && !isCorrect) AccentCoral else PrimaryBlue
            )
        ) {
            Text(
                text = option,
                modifier = Modifier.padding(vertical = 6.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    if (isAnswered) {
        ReviewResultCard(
            message = if (isCorrect) "맞았어요" else "틀림",
            onNext = onNext,
            highlight = isCorrect
        )
    }
}

@Composable
private fun WritingReview(
    questions: List<ReviewQuestion>,
    currentIndex: Int,
    score: Int,
    answer: String,
    feedback: String?,
    onAnswerChange: (String) -> Unit,
    onCheck: (Boolean, String) -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit
) {
    if (currentIndex >= questions.size) {
        ReviewFinishedCard(
            totalCount = questions.size,
            score = score,
            onRestart = onRestart
        )
        return
    }

    val question = questions[currentIndex]

    ReviewPromptCard(
        progress = "${currentIndex + 1} / ${questions.size}",
        title = question.word.meaning,
        note = ""
    )

    OutlinedTextField(
        value = answer,
        onValueChange = onAnswerChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("정답 단어 입력") },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
    )

    Button(
        onClick = {
            val normalizedInput = answer.trim().lowercase()
            val normalizedAnswer = question.word.word.trim().lowercase()
            val isCorrect = normalizedInput.isNotBlank() && normalizedInput == normalizedAnswer
            onCheck(isCorrect, if (isCorrect) "맞았어요" else "정답은 ${question.word.word}")
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("정답 확인")
    }

    if (feedback != null) {
        ReviewResultCard(
            message = feedback.orEmpty(),
            onNext = onNext,
            highlight = feedback == "맞았어요"
        )
    }
}

@Composable
private fun ReviewPromptCard(
    progress: String,
    title: String,
    note: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = progress,
                style = MaterialTheme.typography.titleSmall,
                color = InkSoft
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = PrimaryBlue
            )
            if (note.isNotBlank()) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft
                )
            }
        }
    }
}

@Composable
private fun ReviewResultCard(
    message: String,
    onNext: () -> Unit,
    highlight: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = if (highlight) PrimaryBlue else InkSoft
            )
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("다음 문제")
            }
        }
    }
}

@Composable
private fun ReviewFinishedCard(
    totalCount: Int,
    score: Int,
    onRestart: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "복습 완료",
                style = MaterialTheme.typography.titleLarge,
                color = PrimaryBlue
            )
            Text(
                text = "${totalCount}문제 중 ${score}개 정답",
                style = MaterialTheme.typography.bodyLarge
            )
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("다시 풀기")
            }
        }
    }
}

private fun buildQuestions(
    words: List<SavedWord>,
    reverse: Boolean
): List<ReviewQuestion> {
    if (words.isEmpty()) return emptyList()

    val orderedWords = if (reverse) words.shuffled().reversed() else words.shuffled()

    return orderedWords.mapNotNull { current ->
        val cleanMeaning = current.meaning.trim()
        val cleanWord = current.word.trim()
        if (cleanMeaning.isBlank() || cleanWord.isBlank()) return@mapNotNull null

        val wrongOptions = orderedWords
            .asSequence()
            .filter { it.id != current.id }
            .map { it.meaning.trim() }
            .filter { it.isNotBlank() && it != cleanMeaning }
            .distinct()
            .shuffled()
            .take(3)
            .toList()

        ReviewQuestion(
            word = current,
            options = (wrongOptions + cleanMeaning).distinct().shuffled()
        )
    }
}
