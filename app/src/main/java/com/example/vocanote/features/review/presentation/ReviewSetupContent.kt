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

@Composable
internal fun ReviewSetupContent(
    words: List<SavedWord>,
    selectedMode: ReviewMode,
    selectedScope: ReviewScope,
    targetCount: Int,
    onModeChange: (ReviewMode) -> Unit,
    onScopeChange: (ReviewScope) -> Unit,
    onStart: () -> Unit,
    onAddWord: () -> Unit,
    modifier: Modifier = Modifier
) {
    val insights = remember(words) { buildWordInsights(words) }
    val validWords = words.count { it.word.isNotBlank() && it.meaning.isNotBlank() }
    val mistakeCount = words.count { it.needsMistakePractice() }
    val selectedWordCount = when (selectedScope) {
        ReviewScope.Due -> insights.dueWords.size
        ReviewScope.Mistakes -> mistakeCount
        ReviewScope.All -> validWords
    }
    val canStart = when (selectedMode) {
        ReviewMode.MultipleChoice -> selectedWordCount >= 2
        ReviewMode.Recall,
        ReviewMode.Writing,
        ReviewMode.Listening -> selectedWordCount >= 1
    }

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
                Text(
                    text = "연습 방식",
                    modifier = Modifier.padding(bottom = VocaSpacing.xSmall),
                    style = MaterialTheme.typography.titleLarge
                )
                ModeOption(
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
                    ReviewSummaryRow(label = "이번 연습", value = "${minOf(targetCount, selectedWordCount)}개")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ReviewSummaryRow(label = "오늘 복습", value = "${insights.dueWords.size}개")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ReviewSummaryRow(label = "오답 기록", value = "${mistakeCount}개")
                }
            }
        }
        item {
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
                    text = if (selectedMode == ReviewMode.MultipleChoice && selectedWordCount == 1) {
                        "뜻 고르기는 서로 다른 단어가 2개 이상 필요해요."
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
