package com.example.vocanote.features.library.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vocanote.core.designsystem.ContentMaxWidth
import com.example.vocanote.core.designsystem.VocaSpacing
import com.example.vocanote.core.designsystem.component.EmptyState
import com.example.vocanote.core.designsystem.component.LoadingState
import com.example.vocanote.core.designsystem.component.PageHeader
import com.example.vocanote.core.model.SavedWord
import com.example.vocanote.core.model.isDueForReview
import com.example.vocanote.core.model.needsMistakePractice
import com.example.vocanote.core.speech.rememberWordSpeaker
import java.util.Locale

private enum class LibraryFilter(val label: String) {
    All("전체"),
    Due("오늘 복습"),
    Mistakes("자주 틀림")
}

private enum class LibrarySort(val label: String) {
    Alphabetical("알파벳순"),
    RecentlyAdded("최근 추가순"),
    NeedsReview("학습 우선순")
}

@Composable
fun LibraryScreen(
    words: List<SavedWord>,
    isLoading: Boolean,
    onOpenWord: (SavedWord) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(LibraryFilter.All) }
    var selectedSort by rememberSaveable { mutableStateOf(LibrarySort.Alphabetical) }
    var isSortMenuOpen by remember { mutableStateOf(false) }
    val speaker = rememberWordSpeaker()
    val visibleWords = remember(words, query, selectedFilter, selectedSort) {
        words.asSequence()
            .filter { word ->
                query.isBlank() || word.word.contains(query, ignoreCase = true) ||
                    word.meaning.contains(query, ignoreCase = true) ||
                    word.example.contains(query, ignoreCase = true) ||
                    word.partOfSpeech?.label?.contains(query, ignoreCase = true) == true ||
                    word.partOfSpeech?.abbreviation?.contains(query, ignoreCase = true) == true ||
                    word.partOfSpeech?.storageValue?.contains(query, ignoreCase = true) == true ||
                    word.synonyms.any { it.contains(query, ignoreCase = true) } ||
                    word.antonyms.any { it.contains(query, ignoreCase = true) } ||
                    word.derivatives.any { it.contains(query, ignoreCase = true) } ||
                    word.confusableWords.any { it.contains(query, ignoreCase = true) } ||
                    word.collocations.any { it.contains(query, ignoreCase = true) }
            }
            .filter { word ->
                when (selectedFilter) {
                    LibraryFilter.All -> true
                    LibraryFilter.Due -> word.isDueForReview()
                    LibraryFilter.Mistakes -> word.needsMistakePractice()
                }
            }
            .let { sequence ->
                when (selectedSort) {
                    LibrarySort.Alphabetical -> sequence.sortedBy { it.word.lowercase(Locale.ROOT) }
                    LibrarySort.RecentlyAdded -> sequence.sortedByDescending { it.createdAt }
                    LibrarySort.NeedsReview -> sequence.sortedWith(
                        compareByDescending<SavedWord> { it.isDueForReview() }
                            .thenBy { it.accuracy }
                            .thenByDescending { it.incorrectCount }
                    )
                }
            }
            .toList()
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().widthIn(max = ContentMaxWidth),
            contentPadding = PaddingValues(
                start = VocaSpacing.large,
                top = VocaSpacing.xLarge,
                end = VocaSpacing.large,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(VocaSpacing.medium)
        ) {
            item {
                PageHeader(
                    title = "내 단어장",
                    subtitle = if (words.isEmpty()) "처음 만난 단어를 모아보세요" else "${words.size}개의 단어를 학습하고 있어요",
                    action = {
                        Box {
                            IconButton(onClick = { isSortMenuOpen = true }) {
                                Icon(imageVector = Icons.Default.SortByAlpha, contentDescription = "정렬")
                            }
                            DropdownMenu(
                                expanded = isSortMenuOpen,
                                onDismissRequest = { isSortMenuOpen = false }
                            ) {
                                LibrarySort.entries.forEach { sort ->
                                    DropdownMenuItem(
                                        text = { Text(sort.label) },
                                        onClick = {
                                            selectedSort = sort
                                            isSortMenuOpen = false
                                        },
                                        leadingIcon = if (sort == selectedSort) {
                                            { Icon(Icons.Default.FilterList, contentDescription = null) }
                                        } else null
                                    )
                                }
                            }
                        }
                    }
                )
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("단어, 뜻, 품사, 예문, 연관어 검색") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(VocaSpacing.small)) {
                    items(LibraryFilter.entries) { filter ->
                        FilterChip(
                            selected = filter == selectedFilter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter.label) },
                        )
                    }
                }
            }

            when {
                isLoading -> item { LoadingState(label = "단어장을 불러오는 중이에요") }
                visibleWords.isEmpty() -> item {
                    EmptyState(
                        icon = Icons.Outlined.AutoStories,
                        title = if (query.isBlank() && selectedFilter == LibraryFilter.All) {
                            "아직 저장한 단어가 없어요"
                        } else {
                            "조건에 맞는 단어가 없어요"
                        },
                        description = if (query.isBlank() && selectedFilter == LibraryFilter.All) {
                            "아래 추가 버튼으로 첫 단어를 기록해 보세요."
                        } else {
                            "검색어나 필터를 바꿔서 다시 찾아보세요."
                        }
                    )
                }
                else -> items(visibleWords, key = { it.id }) { word ->
                    WordRow(
                        word = word,
                        onClick = { onOpenWord(word) },
                        canSpeak = speaker.isReady,
                        onSpeak = { speaker.speak(word.word) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WordRow(
    word: SavedWord,
    onClick: () -> Unit,
    canSpeak: Boolean,
    onSpeak: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(start = VocaSpacing.large, top = 14.dp, bottom = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(VocaSpacing.xSmall)
            ) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                word.partOfSpeech?.let { partOfSpeech ->
                    Text(
                        text = partOfSpeech.displayLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = word.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (word.example.isNotBlank()) {
                    Text(
                        text = word.example,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (word.attemptCount > 0) {
                    Text(
                        text = "${word.attemptCount}회 복습 · 정답률 ${(word.accuracy * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onSpeak, enabled = canSpeak) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "${word.word} 발음 듣기")
            }
        }
    }
}
