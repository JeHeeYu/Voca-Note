package com.example.vocanote.features.add.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.vocanote.ui.theme.Canvas
import com.example.vocanote.ui.theme.InkSoft

@Composable
fun AddWordPage(
    onBack: () -> Unit,
    isSaving: Boolean,
    helperMessage: String?,
    onSave: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var word by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    var meaning by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Canvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기"
                    )
                }
                Text(
                    text = "단어 추가",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Text(
                text = helperMessage ?: "단어와 뜻을 입력하면 단어장에 바로 추가됩니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft
            )

            Surface(shape = RoundedCornerShape(24.dp), tonalElevation = 1.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = word,
                        onValueChange = { word = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("단어") },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )

                    OutlinedTextField(
                        value = meaning,
                        onValueChange = { meaning = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("뜻") },
                        shape = RoundedCornerShape(16.dp),
                        minLines = 4,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )

                    Button(
                        onClick = {
                            if (word.isNotBlank() && meaning.isNotBlank()) {
                                onSave(word, meaning)
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (isSaving) "저장 중..." else "단어장에 저장",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
