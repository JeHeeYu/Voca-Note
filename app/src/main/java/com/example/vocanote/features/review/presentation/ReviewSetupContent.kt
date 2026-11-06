package com.example.vocanote.features.review.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vocanote.core.designsystem.VocaSpacing
import com.example.vocanote.core.designsystem.component.EmptyState
import com.example.vocanote.core.designsystem.component.PageHeader
import com.example.vocanote.core.model.ReviewMode
import com.example.vocanote.core.model.SavedWord
import com.example.vocanote.core.model.buildWordInsights
import com.example.vocanote.core.model.needsMistakePractice
import com.example.vocanote.core.model.isDueForReview
import com.example.vocanote.features.review.domain.ReviewScope
import com.example.vocanote.features.review.domain.ReviewPresentation
import com.example.vocanote.features.review.domain.eligibleReviewWords

@Composable
internal fun ReviewSetupContent(
    words: List<SavedWord>,
    selectedMode: ReviewMode,
    selectedScope: ReviewScope,
    targetCount: String,
    presentation: ReviewPresentation,
    onCountChange: (String) -> Unit,
    onPresentationChange: (ReviewPresentation) -> Unit,
    onModeChange: (ReviewMode) -> Unit,
    onScopeChange: (ReviewScope) -> Unit,
    onStart: () -> Unit,
    onAddWord: () -> Unit,
    modifier: Modifier = Modifier,
    startError: String? = null
) {
    val insights = remember(words) { buildWordInsights(words) }
    val validWords = eligibleReviewWords(words, words, ReviewMode.Recall).size
    val mistakeCount = words.count { it.needsMistakePractice() }
    val targets = words.filter { when (selectedScope) {
        ReviewScope.All -> true
        ReviewScope.Due -> it.isDueForReview()
        ReviewScope.Mistakes -> it.needsMistakePractice()
    } }
    val selectedWordCount = eligibleReviewWords(words, targets, selectedMode).size
    val requestedCount = targetCount.toIntOrNull()
    val validCount = requestedCount != null && requestedCount in 5..30
    val canStart = validCount && selectedWordCount >= if (selectedMode == ReviewMode.Crossword) 2 else 1

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = VocaSpacing.large,
            top = VocaSpacing.xLarge,
            end = VocaSpacing.large,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(VocaSpacing.xLarge)
    ) {
        item {
            PageHeader(
                title = "연습",
                subtitle = "기억을 꺼내는 방식으로 반복해야 오래 남아요"
            )
        }
        if (validWords == 0) {
            item {
                EmptyState(
                    icon = Icons.Default.School,
                    title = "복습할 단어가 아직 없어요",
                    description = "뜻이 입력된 단어를 추가하면 바로 문제를 만들 수 있어요.",
                    action = { Button(onClick = onAddWord) { Text("단어 추가") } }
                )
            }
            return@LazyColumn
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(VocaSpacing.medium)) {
                Text(text = "연습할 단어", style = MaterialTheme.typography.titleLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VocaSpacing.small)
                ) {
                    ScopeOption(
                        modifier = Modifier.weight(1f),
                        selected = selectedScope == ReviewScope.Due,
                        label = "오늘 복습",
                        onClick = { onScopeChange(ReviewScope.Due) }
                    )
                    ScopeOption(
                        modifier = Modifier.weight(1f),
                        selected = selectedScope == ReviewScope.Mistakes,
                        label = "오답 집중",
                        onClick = { onScopeChange(ReviewScope.Mistakes) }
                    )
                    ScopeOption(
                        modifier = Modifier.weight(1f),
                        selected = selectedScope == ReviewScope.All,
                        label = "전체",
                        onClick = { onScopeChange(ReviewScope.All) }
                    )
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(VocaSpacing.small)) {
                if (selectedMode != ReviewMode.Crossword) {
                Text("진행 방식", style = MaterialTheme.typography.titleLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(VocaSpacing.small)) {
                    FilterChip(selected = presentation == ReviewPresentation.Single, onClick = { onPresentationChange(ReviewPresentation.Single) }, label = { Text("한 문제씩") })
                    FilterChip(selected = presentation == ReviewPresentation.Batch, onClick = { onPresentationChange(ReviewPresentation.Batch) }, label = { Text("한 번에 풀기") })
                }
                }
                Text(
                    text = "연습 방식",
                    modifier = Modifier.padding(bottom = VocaSpacing.xSmall),
                    style = MaterialTheme.typography.titleLarge
                )
                if (presentation == ReviewPresentation.Single) ModeOption(
                    selected = selectedMode == ReviewMode.Recall,
                    icon = Icons.Default.Visibility,
                    title = "기억 카드",
                    description = "뜻을 먼저 떠올린 뒤 답을 확인하고 스스로 평가",
                    onClick = { onModeChange(ReviewMode.Recall) }
                )
                ModeOption(
                    selected = selectedMode == ReviewMode.MultipleChoice,
                    icon = Icons.Default.Quiz,
                    title = "뜻 고르기",
                    description = "영단어와 문맥을 보고 알맞은 뜻 선택",
                    onClick = { onModeChange(ReviewMode.MultipleChoice) }
                )
                ModeOption(
                    selected = selectedMode == ReviewMode.Writing,
                    icon = Icons.Default.Edit,
                    title = "직접 쓰기",
                    description = "뜻과 빈칸 예문을 보고 영어 단어 입력",
                    onClick = { onModeChange(ReviewMode.Writing) }
                )
                ModeOption(
                    selected = selectedMode == ReviewMode.MeaningWriting,
                    icon = Icons.Default.Edit,
                    title = "한글 뜻 맞히기",
                    description = "영어를 보고 뜻 하나 입력 · 띄어쓰기는 자유롭게",
                    onClick = { onModeChange(ReviewMode.MeaningWriting) }
                )
                ModeOption(
                    selected = selectedMode == ReviewMode.Crossword,
                    icon = Icons.Default.Quiz,
                    title = "가로세로 낱말",
                    description = "한글 힌트로 교차하는 영어 낱말 채우기",
                    onClick = { onModeChange(ReviewMode.Crossword) }
                )
                ModeOption(
                    selected = selectedMode == ReviewMode.Listening,
                    icon = Icons.Default.Headphones,
                    title = "듣고 쓰기",
                    description = "발음을 듣고 정확한 철자를 입력",
                    onClick = { onModeChange(ReviewMode.Listening) }
                )
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(VocaSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(VocaSpacing.medium)
                ) {
                    OutlinedTextField(
                        value = targetCount, onValueChange = onCountChange,
                        label = { Text("이번 문제 수") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = !validCount, supportingText = { Text("5~30 사이의 정수를 입력해 주세요.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(VocaSpacing.small)) {
                        listOf(5, 10, 20, 30).forEach { count ->
                            FilterChip(selected = requestedCount == count, onClick = { onCountChange(count.toString()) }, label = { Text("${count}개") })
                        }
                    }
                    Text(if (selectedScope == ReviewScope.All) "모든 단어를 같은 확률로 무작위 출제해요." else "선택한 범위에서 복습 우선순위에 따라 출제해요.", style = MaterialTheme.typography.bodySmall)
                    ReviewSummaryRow(label = "이번 연습", value = "${if (validCount) minOf(requestedCount!!, selectedWordCount) else 0}개")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ReviewSummaryRow(label = "오늘 복습", value = "${insights.dueWords.size}개")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ReviewSummaryRow(label = "오답 기록", value = "${mistakeCount}개")
                }
            }
        }
        item {
            if (selectedMode == ReviewMode.Crossword) Text("영문 2~18글자로 연결 가능한 단어만 출제해요. 공백·하이픈·아포스트로피는 칸에서 생략하며, 실제 문제 수는 줄어들 수 있어요.", modifier = Modifier.padding(bottom = VocaSpacing.medium), style = MaterialTheme.typography.bodySmall)
            if (startError != null) Text(startError, color = MaterialTheme.colorScheme.error)
            Button(
                onClick = onStart,
                enabled = canStart,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("연습 시작")
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(start = VocaSpacing.small)
                )
            }
            if (!canStart) {
                Text(
                    text = if (!validCount) {
                        "문제 수를 5~30 사이로 입력해 주세요."
                    } else if (selectedMode == ReviewMode.MultipleChoice && targets.isNotEmpty()) {
                        "뜻 고르기는 오답 선택지로 쓸 다른 뜻의 단어가 필요해요."
                    } else {
                        "선택한 범위에 연습할 단어가 없어요."
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = VocaSpacing.small),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ModeOption(
    selected: Boolean,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(VocaSpacing.large),
            horizontalArrangement = Arrangement.spacedBy(VocaSpacing.medium),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = "선택됨", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ScopeOption(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        modifier = modifier
    )
}

@Composable
private fun ReviewSummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.titleSmall)
    }
}
