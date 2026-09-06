package com.example.vocanote.features.editor.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.vocanote.core.designsystem.ContentMaxWidth
import com.example.vocanote.core.designsystem.VocaSpacing
import com.example.vocanote.core.model.PartOfSpeech
import com.example.vocanote.core.model.SavedWord
import com.example.vocanote.core.model.WordDraft
import com.example.vocanote.core.speech.rememberWordSpeaker
import com.example.vocanote.features.editor.domain.WordValidationResult
import com.example.vocanote.features.editor.domain.validateWordDraft

@Composable
fun WordEditorScreen(
    existingWord: SavedWord?,
    allWords: List<SavedWord>,
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: (WordDraft) -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var word by rememberSaveable(existingWord?.id) { mutableStateOf(existingWord?.word.orEmpty()) }
    var meaning by rememberSaveable(existingWord?.id) { mutableStateOf(existingWord?.meaning.orEmpty()) }
    var partOfSpeech by rememberSaveable(existingWord?.id) {
        mutableStateOf(existingWord?.partOfSpeech)
    }
    var example by rememberSaveable(existingWord?.id) { mutableStateOf(existingWord?.example.orEmpty()) }
    var note by rememberSaveable(existingWord?.id) { mutableStateOf(existingWord?.note.orEmpty()) }
    var synonyms by rememberSaveable(existingWord?.id) {
        mutableStateOf(existingWord?.synonyms.orEmpty().joinToString("\n"))
    }
    var derivatives by rememberSaveable(existingWord?.id) {
        mutableStateOf(existingWord?.derivatives.orEmpty().joinToString("\n"))
    }
    var validation by remember { mutableStateOf(WordValidationResult()) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val speaker = rememberWordSpeaker()
    val focusManager = LocalFocusManager.current

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = ContentMaxWidth)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = VocaSpacing.large, vertical = VocaSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(VocaSpacing.large)
        ) {
            EditorTopBar(
                title = if (existingWord == null) "단어 추가" else "단어 상세",
                onBack = onBack,
                onDelete = onDelete?.let { { showDeleteDialog = true } }
            )

            if (existingWord != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = existingWord.word, style = MaterialTheme.typography.headlineLarge)
                        Text(
                            text = listOfNotNull(
                                partOfSpeech?.label,
                                "정답률 ${(existingWord.accuracy * 100).toInt()}%",
                                "${existingWord.attemptCount}회 복습"
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { speaker.speak(word) }, enabled = speaker.isReady) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "$word 발음 듣기"
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            Column(verticalArrangement = Arrangement.spacedBy(VocaSpacing.medium)) {
                Text(text = "기본 정보", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = word,
                    onValueChange = {
                        word = it
                        validation = validation.copy(wordError = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("영어 단어") },
                    placeholder = { Text("예: resilient") },
                    supportingText = validation.wordError?.let { error -> { Text(error) } },
                    isError = validation.wordError != null,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                PartOfSpeechField(
                    selected = partOfSpeech,
                    onSelected = { partOfSpeech = it }
                )
                OutlinedTextField(
                    value = meaning,
                    onValueChange = {
                        meaning = it
                        validation = validation.copy(meaningError = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("뜻") },
                    placeholder = { Text("예: 회복력이 강한, 탄력 있는") },
                    supportingText = validation.meaningError?.let { error -> { Text(error) } },
                    isError = validation.meaningError != null,
                    minLines = 2,
                    maxLines = 4,
                    shape = MaterialTheme.shapes.medium
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(VocaSpacing.medium)) {
                Text(text = "연관 표현", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = synonyms,
                    onValueChange = {
                        synonyms = it
                        validation = validation.copy(synonymsError = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("동의어") },
                    placeholder = { Text("tough\ndurable") },
                    supportingText = {
                        Text(validation.synonymsError ?: "한 줄에 하나씩 입력")
                    },
                    isError = validation.synonymsError != null,
                    minLines = 2,
                    maxLines = 5,
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = derivatives,
                    onValueChange = {
                        derivatives = it
                        validation = validation.copy(derivativesError = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("파생어") },
                    placeholder = { Text("resilience\nresiliently") },
                    supportingText = {
                        Text(validation.derivativesError ?: "한 줄에 하나씩 입력")
                    },
                    isError = validation.derivativesError != null,
                    minLines = 2,
                    maxLines = 5,
                    shape = MaterialTheme.shapes.medium
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(VocaSpacing.medium)) {
                Text(text = "기억을 돕는 메모", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = example,
                    onValueChange = {
                        example = it
                        validation = validation.copy(exampleError = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("예문") },
                    placeholder = { Text("예: Children are often very resilient.") },
                    supportingText = validation.exampleError?.let { error -> { Text(error) } },
                    isError = validation.exampleError != null,
                    minLines = 2,
                    maxLines = 5,
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = {
                        note = it
                        validation = validation.copy(noteError = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("나만의 설명") },
                    placeholder = { Text("뉘앙스, 연상법, 사용 상황") },
                    supportingText = validation.noteError?.let { error -> { Text(error) } },
                    isError = validation.noteError != null,
                    minLines = 3,
                    maxLines = 7,
                    shape = MaterialTheme.shapes.medium
                )
            }

            speaker.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    val draft = WordDraft(
                        word = word,
                        meaning = meaning,
                        partOfSpeech = partOfSpeech,
                        example = example,
                        note = note,
                        synonyms = synonyms.lines(),
                        derivatives = derivatives.lines()
                    )
                    val result = validateWordDraft(draft, allWords, existingWord?.id)
                    validation = result
                    if (result.isValid) onSave(draft.normalized())
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Text(
                        text = if (existingWord == null) "단어장에 추가" else "변경사항 저장",
                        modifier = Modifier.padding(start = VocaSpacing.small)
                    )
                }
            }

            if (existingWord != null && onDelete != null) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null)
                    Text(text = "단어 삭제", modifier = Modifier.padding(start = VocaSpacing.small))
                }
            }
        }
    }

    if (showDeleteDialog && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("단어를 삭제할까요?") },
            text = { Text("복습 기록과 함께 삭제되며 되돌릴 수 없어요.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("취소") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PartOfSpeechField(
    selected: PartOfSpeech?,
    onSelected: (PartOfSpeech?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected?.label ?: "선택 안 함",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            label = { Text("품사") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = MaterialTheme.shapes.medium
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("선택 안 함") },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            PartOfSpeech.entries.forEach { part ->
                DropdownMenuItem(
                    text = { Text(part.label) },
                    onClick = {
                        onSelected(part)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EditorTopBar(
    title: String,
    onBack: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "단어 삭제",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
