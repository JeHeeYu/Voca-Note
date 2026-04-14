package com.example.vocanote.features.words.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vocanote.features.words.data.SavedWord
import com.example.vocanote.ui.theme.BottomBarBorder
import com.example.vocanote.ui.theme.Canvas
import com.example.vocanote.ui.theme.InkSoft
import com.example.vocanote.ui.theme.PrimaryBlue

@Composable
fun WordListPage(
    words: List<SavedWord>,
    isLoading: Boolean,
    helperMessage: String?,
    onWordClick: (SavedWord) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    val expandedSections = remember {
        mutableStateMapOf<Char, Boolean>().apply {
            ('A'..'Z').forEach { put(it, true) }
        }
    }

    val filteredWords = words.filter { savedWord ->
        query.isBlank() ||
            savedWord.word.contains(query, ignoreCase = true) ||
            savedWord.meaning.contains(query, ignoreCase = true)
    }

    val sections = ('A'..'Z').map { letter ->
        letter to filteredWords.filter { it.word.firstOrNull()?.uppercaseChar() == letter }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Canvas
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "리스트",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = helperMessage ?: if (isLoading) {
                            "등록한 단어를 불러오는 중이에요."
                        } else {
                            "등록한 단어를 검색하고, 알파벳별로 접어서 볼 수 있어요."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    placeholder = { Text("단어 또는 뜻 검색") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    }
                )
            }

            items(sections, key = { it.first }) { (letter, letterWords) ->
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AlphabetSection(
                        letter = letter,
                        words = letterWords,
                        expanded = expandedSections[letter] == true,
                        onWordClick = onWordClick,
                        onToggle = {
                            expandedSections[letter] = expandedSections[letter] != true
                        }
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = BottomBarBorder.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlphabetSection(
    letter: Char,
    words: List<SavedWord>,
    expanded: Boolean,
    onWordClick: (SavedWord) -> Unit,
    onToggle: () -> Unit
) {
    Surface(shape = RoundedCornerShape(18.dp), tonalElevation = 1.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = letter.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "${words.size}개",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft
                    )
                }
                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.KeyboardArrowDown
                    } else {
                        Icons.AutoMirrored.Filled.KeyboardArrowRight
                    },
                    contentDescription = null,
                    tint = InkSoft
                )
            }

            if (expanded) {
                if (words.isEmpty()) {
                    Text(
                        text = "등록한 단어 없음",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSoft
                    )
                } else {
                    words.forEach { savedWord ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            tonalElevation = 0.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onWordClick(savedWord) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = savedWord.word,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = savedWord.meaning,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = InkSoft
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
