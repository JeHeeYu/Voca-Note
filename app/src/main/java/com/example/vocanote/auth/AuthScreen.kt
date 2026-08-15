package com.example.vocanote.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vocanote.R
import com.example.vocanote.core.designsystem.VocaSpacing
import com.example.vocanote.core.designsystem.component.BrandMark

@Composable
fun AuthScreen(
    isLoading: Boolean,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = VocaSpacing.xLarge, vertical = VocaSpacing.xxLarge),
                verticalArrangement = Arrangement.spacedBy(VocaSpacing.xxLarge)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(VocaSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BrandMark(size = 48.dp)
                    Text(text = "Voca Note", style = MaterialTheme.typography.headlineMedium)
                }

                Column(verticalArrangement = Arrangement.spacedBy(VocaSpacing.medium)) {
                    Text(
                        text = "기록한 단어를\n내 것으로 만드는 시간",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "오늘 만난 영어를 기록하고, 잊기 전에 다시 꺼내보세요.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(VocaSpacing.xLarge),
                        verticalArrangement = Arrangement.spacedBy(VocaSpacing.large)
                    ) {
                        AuthPromise(text = "단어와 예문을 한곳에 기록")
                        AuthPromise(text = "고르기와 쓰기로 반복 학습")
                        AuthPromise(text = "학습 결과에 따라 복습 순서 정리")
                    }
                }

                OutlinedButton(
                    onClick = onGoogleSignIn,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_google_logo),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                        Text(
                            text = "Google로 계속하기",
                            modifier = Modifier.padding(start = VocaSpacing.medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Text(
                    text = "계속하면 단어와 학습 기록이 계정에 안전하게 동기화됩니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AuthPromise(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(VocaSpacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.padding(6.dp).size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}
