package com.example.vocanote.features.review.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vocanote.core.designsystem.VocaSpacing
import com.example.vocanote.features.review.domain.*
import kotlinx.coroutines.launch

@Composable
internal fun CrosswordContent(
    state: ReviewSessionState,
    onFill: (String, String) -> Unit,
    onCheck: () -> Unit,
    onRetrySave: () -> Unit,
    onSummary: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val puzzle = state.crossword ?: return
    var selectedId by rememberSaveable(state.id) { mutableStateOf(puzzle.entries.first().wordId) }
    var draft by rememberSaveable(state.id) { mutableStateOf("") }
    var error by rememberSaveable(state.id) { mutableStateOf("") }
    val entry = puzzle.entries.find { it.wordId == selectedId } ?: puzzle.entries.first()
    val questions = remember(state.questions) { state.questions.associateBy { it.wordId } }
    val board = remember(puzzle) { puzzle.entries.flatMap(::entryCells).toMap() }
    val starts = remember(puzzle) { puzzle.entries.associate { cellKey(it.row, it.col) to it.number } }
    val selectedCells = entryCells(entry).map { it.first }.toSet()
    val graded = state.phase == ReviewPhase.Graded
    val results = state.answers.associate { it.wordId to it.isCorrect }
    val completed = puzzle.entries.count { item -> entryCells(item).all { state.crosswordCells[it.first] != null } }
    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    fun choose(id: String) { selectedId = id; draft = ""; error = "" }
    fun fill() {
        if (graded) return
        if (crosswordAnswer(draft).length != entry.answer.length) {
            error = "영문 ${entry.answer.length}글자를 입력해 주세요. 공백·하이픈·아포스트로피는 생략해요."
        } else {
            onFill(entry.wordId, draft)
            draft = ""
            error = ""
            keyboard?.hide()
        }
    }
    LazyColumn(state = listState, modifier = modifier.fillMaxSize().testTag("crossword_content"),
        contentPadding = PaddingValues(VocaSpacing.large), verticalArrangement = Arrangement.spacedBy(VocaSpacing.medium)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(VocaSpacing.small)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("가로세로 낱말", style = MaterialTheme.typography.headlineSmall)
                    TextButton(onClick = onExit) { Text("나가기") }
                }
                Text(if (graded) "${state.answers.size}문제 중 ${state.answers.count { it.isCorrect }}개 정답" else "$completed / ${puzzle.entries.size}개 낱말 입력")
                Text("칸이나 힌트를 선택해 영어 낱말을 채우세요. 교차하는 글자는 함께 바뀌어요. 큰 판은 상하좌우로 밀어 볼 수 있어요.", style = MaterialTheme.typography.bodySmall)
                if (graded) {
                    ReviewSaveStatus(state, onRetrySave)
                    Button(onClick = onSummary, modifier = Modifier.fillMaxWidth()) { Text("학습 요약 보기") }
                } else Button(onClick = {
                    if (!crosswordComplete(puzzle, state.crosswordCells)) error = "아직 비어 있는 칸이 있어요. 모든 낱말을 채워 주세요."
                    else { error = ""; onCheck() }
                }, modifier = Modifier.fillMaxWidth()) { Text("전체 정답 확인") }
                if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
            }
        }
        item {
            Box(Modifier.fillMaxWidth().heightIn(max = 380.dp).horizontalScroll(rememberScrollState()).verticalScroll(rememberScrollState()).testTag("crossword_board")) {
                Column {
                    repeat(puzzle.rows) { row ->
                        Row {
                            repeat(puzzle.cols) { col ->
                                val key = cellKey(row, col)
                                val answer = board[key]
                                val value = state.crosswordCells[key].orEmpty()
                                val active = key in selectedCells
                                val color = when {
                                    answer == null -> MaterialTheme.colorScheme.surfaceVariant
                                    graded && value != answer.toString() -> MaterialTheme.colorScheme.errorContainer
                                    active -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                }
                                val cellModifier = Modifier.size(36.dp).background(color).border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                Box(if (answer == null) cellModifier else cellModifier
                                    .semantics { contentDescription = "${row + 1}행 ${col + 1}열 ${value.ifEmpty { "빈칸" }}"; selected = active }
                                    .clickable {
                                        val matches = puzzle.entries.filter { item -> entryCells(item).any { it.first == key } }
                                        choose((matches.find { it.wordId != selectedId } ?: matches.first()).wordId)
                                    }, contentAlignment = Alignment.Center) {
                                    starts[key]?.let { Text(it.toString(), Modifier.align(Alignment.TopStart).padding(start = 2.dp), fontSize = 8.sp, lineHeight = 10.sp) }
                                    if (answer != null) Text(value, fontSize = 17.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                Column(Modifier.fillMaxWidth().padding(VocaSpacing.medium), verticalArrangement = Arrangement.spacedBy(VocaSpacing.small)) {
                    Text("${entry.number} ${entry.direction.label} · ${entry.answer.length}글자", style = MaterialTheme.typography.labelLarge)
                    Text(questions[entry.wordId]?.prompt.orEmpty(), style = MaterialTheme.typography.titleLarge)
                    Text(entryCells(entry).joinToString(" ") { state.crosswordCells[it.first] ?: "_" }, fontFamily = FontFamily.Monospace)
                    if (graded) Text("정답: ${questions[entry.wordId]?.answer}") else {
                        OutlinedTextField(value = draft, onValueChange = { draft = it }, label = { Text("선택한 낱말 입력") },
                            placeholder = { Text("${entry.answer.length}글자 영어 단어") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("crossword_answer"),
                            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { fill() }))
                        Button(onClick = { fill() }, enabled = draft.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("낱말 채우기") }
                    }
                }
            }
        }
        CrosswordDirection.entries.forEach { direction ->
            item { Text("${direction.label} 힌트", style = MaterialTheme.typography.titleLarge) }
            items(puzzle.entries.filter { it.direction == direction }, key = { it.wordId }) { clue ->
                Surface(shape = MaterialTheme.shapes.medium,
                    color = if (clue.wordId == selectedId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().testTag("crossword_clue_${clue.wordId}").clickable {
                        choose(clue.wordId)
                        scope.launch { listState.animateScrollToItem(2) }
                    }) {
                    Column(Modifier.padding(VocaSpacing.medium), verticalArrangement = Arrangement.spacedBy(VocaSpacing.small)) {
                        Text("${clue.number} ${direction.label} · ${questions[clue.wordId]?.prompt} (${clue.answer.length}글자)")
                        if (graded) {
                            Text(if (results[clue.wordId] == true) "정답" else "오답", color = if (results[clue.wordId] == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                            Text(questions[clue.wordId]?.answer.orEmpty(), style = MaterialTheme.typography.titleMedium)
                            MeaningList(questions[clue.wordId]?.meanings.orEmpty())
                        }
                    }
                }
            }
        }
    }
}
private val CrosswordDirection.label get() = if (this == CrosswordDirection.Across) "가로" else "세로"
