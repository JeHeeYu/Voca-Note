package com.example.vocanote.features.review.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vocanote.core.designsystem.VocaSpacing

@Composable
internal fun ReviewFinishedContent(
    correctCount: Int,
    totalCount: Int,
    incorrectCount: Int,
    onRestart: () -> Unit,
    onRetryMistakes: (() -> Unit)?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scorePercent = if (totalCount == 0) 0 else correctCount * 100 / totalCount
    Column(
        modifier = modifier.fillMaxSize().padding(VocaSpacing.xLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(88.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(
            text = "연습 완료",
            modifier = Modifier.padding(top = VocaSpacing.xLarge),
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "${totalCount}문제 중 ${correctCount}개 정답",
            modifier = Modifier.padding(top = VocaSpacing.small),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$scorePercent%",
            modifier = Modifier.padding(vertical = VocaSpacing.xLarge),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Text("완료")
        }
        if (onRetryMistakes != null) {
            OutlinedButton(
                onClick = onRetryMistakes,
                modifier = Modifier.fillMaxWidth().padding(top = VocaSpacing.small),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Text(
                    text = "틀린 단어 ${incorrectCount}개 다시 연습",
                    modifier = Modifier.padding(start = VocaSpacing.small)
                )
            }
        } else {
            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth().padding(top = VocaSpacing.small),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Text(text = "한 번 더 연습", modifier = Modifier.padding(start = VocaSpacing.small))
            }
        }
    }
}
