package com.example.vocanote.features.home.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vocanote.core.designsystem.ContentMaxWidth
import com.example.vocanote.core.designsystem.VocaSpacing
import com.example.vocanote.core.designsystem.component.BrandMark
import com.example.vocanote.core.designsystem.component.InitialAvatar
import com.example.vocanote.core.designsystem.component.LoadingState
import com.example.vocanote.core.designsystem.component.SectionHeader
import com.example.vocanote.core.model.ReviewSettings
import com.example.vocanote.core.model.SavedWord
import com.example.vocanote.core.model.StudyStats
import com.example.vocanote.core.model.buildWordInsights
import com.example.vocanote.ui.UserProfile
import com.example.vocanote.ui.theme.AccentAmber
import com.example.vocanote.ui.theme.Brand
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    user: UserProfile,
    words: List<SavedWord>,
    reviewSettings: ReviewSettings,
    studyStats: StudyStats,
    isLoading: Boolean,
    onOpenReview: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenWord: (SavedWord) -> Unit,
    modifier: Modifier = Modifier
) {
    val insights = remember(words) { buildWordInsights(words) }
    val progress = if (reviewSettings.dailyGoal == 0) 0f else {
        (studyStats.reviewedTodayCount.toFloat() / reviewSettings.dailyGoal).coerceIn(0f, 1f)
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = ContentMaxWidth)
                .padding(horizontal = VocaSpacing.large),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = VocaSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(VocaSpacing.xLarge)
        ) {
            item {
                HomeTopBar(user = user)
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(VocaSpacing.xSmall)) {
                    Text(
                        text = "${user.firstName}님, 오늘도 한 걸음",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = formattedToday(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isLoading) {
                item { LoadingState(label = "학습 현황을 불러오는 중이에요") }
            } else {
                item {
                    DailyProgressPanel(
                        reviewedCount = studyStats.reviewedTodayCount,
                        dailyGoal = reviewSettings.dailyGoal,
                        progress = progress,
                        dueCount = insights.dueWords.size,
                        onOpenReview = onOpenReview
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(VocaSpacing.small)
                    ) {
                        MetricTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.LocalFireDepartment,
                            value = studyStats.currentStreakDays.toString(),
                            label = "연속 학습일",
                            tint = AccentAmber
                        )
                        MetricTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.CheckCircle,
                            value = studyStats.reviewedTodayCount.toString(),
                            label = "오늘 복습",
                            tint = Brand
                        )
                        MetricTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.TrackChanges,
                            value = "${(studyStats.accuracyLast7Days * 100).toInt()}%",
                            label = "7일 정확도",
                            tint = Brand
                        )
                    }
                }
                item {
                    SectionHeader(
                        title = "오늘 다시 볼 단어",
                        trailing = {
                            Text(
                                text = "전체 보기",
                                modifier = Modifier.clickable(onClick = onOpenLibrary),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
                if (insights.dueWords.isEmpty()) {
                    item { AllCaughtUpPanel() }
                } else {
                    items(insights.dueWords.take(4), key = { it.id }) { word ->
                        DueWordRow(word = word, onClick = { onOpenWord(word) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(user: UserProfile) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(VocaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandMark()
            Column {
                Text(text = "Voca Note", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "나만의 영어 단어장",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        InitialAvatar(initial = user.initial)
    }
}

@Composable
private fun DailyProgressPanel(
    reviewedCount: Int,
    dailyGoal: Int,
    progress: Float,
    dueCount: Int,
    onOpenReview: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(VocaSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(VocaSpacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(VocaSpacing.xSmall)) {
                    Text(text = "오늘의 학습", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "$reviewedCount / $dailyGoal 단어",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp).size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (dueCount == 0) "오늘 복습을 모두 마쳤어요" else "복습할 단어 ${dueCount}개",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = onOpenReview, enabled = dueCount > 0) {
                    Text(if (dueCount > 0) "오늘 연습" else "완료")
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 6.dp).size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(VocaSpacing.small)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DueWordRow(word: SavedWord, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(VocaSpacing.large),
            horizontalArrangement = Arrangement.spacedBy(VocaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOfNotNull(word.partOfSpeech?.label, word.meaning).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "단어 열기",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AllCaughtUpPanel() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(VocaSpacing.xLarge),
            verticalArrangement = Arrangement.spacedBy(VocaSpacing.xSmall)
        ) {
            Text(text = "복습을 모두 마쳤어요", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "연습 탭에서 오답 집중이나 듣고 쓰기를 이어갈 수 있어요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formattedToday(): String = LocalDate.now().format(
    DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREA)
)
