package com.example.vocanote.features.search.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vocanote.ui.theme.Canvas
import com.example.vocanote.ui.theme.InkSoft
import com.example.vocanote.ui.theme.PrimaryBlue

@Composable
fun SearchPage(
    wordCount: Int,
    modifier: Modifier = Modifier
) {
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
                        text = "등록한 단어 ${wordCount}개",
                        style = MaterialTheme.typography.titleLarge,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "여기에 나중에 뜻 맞추기나 스펠링 복습 기능을 붙이면 됩니다.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = InkSoft
                    )
                }
            }

            Text(
                text = "지금은 단어를 모으는 단계라서, 복습 화면은 단순하게 준비만 해두었습니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
