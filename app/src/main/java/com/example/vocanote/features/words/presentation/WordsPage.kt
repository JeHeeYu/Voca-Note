package com.example.vocanote.features.words.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vocanote.ui.theme.AccentGold
import com.example.vocanote.ui.theme.Canvas
import com.example.vocanote.ui.theme.InkSoft
import com.example.vocanote.ui.theme.PrimaryBlue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class QuickAction(val title: String, val subtitle: String, val tint: Color)

@Composable
fun WordsPage(
    words: List<Pair<String, String>>,
    todayWordCount: Int,
    onAddWord: () -> Unit,
    onOpenReview: () -> Unit,
    isLoading: Boolean,
    helperMessage: String?,
    modifier: Modifier = Modifier
) {
    val today = rememberFormattedDate()
    val quickActions = listOf(
        QuickAction("단어 추가", "새 단어 적기", PrimaryBlue),
        QuickAction("바로 복습", "문제 풀러 가기", AccentGold)
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Canvas
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = today,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "내 단어장",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = helperMessage ?: if (isLoading) {
                            "저장된 단어를 불러오는 중이에요."
                        } else {
                            "추가한 단어를 보고, 필요할 때 바로 다시 꺼내보는 공간이에요."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft
                    )
                }
            }

            item {
                SummaryCard(wordCount = words.size, todayWordCount = todayWordCount)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    quickActions.forEach { action ->
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            action = action,
                            onClick = if (action.title.contains("추가")) onAddWord else onOpenReview
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(wordCount: Int, todayWordCount: Int) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "단어 ${wordCount}개 저장됨",
                    style = MaterialTheme.typography.titleLarge,
                    color = PrimaryBlue
                )
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.CheckCircleOutline,
                    contentDescription = null,
                    tint = PrimaryBlue
                )
            }
            Text(
                text = "오늘 추가한 단어 ${todayWordCount}개",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    action: QuickAction,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(action.tint.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = if (action.title.contains("추가")) Icons.Default.Add else Icons.Default.Quiz,
                    contentDescription = null,
                    tint = action.tint
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = action.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = action.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft
                )
            }
        }
    }
}

@Composable
private fun rememberFormattedDate(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREA)
    return LocalDate.now().format(formatter)
}
