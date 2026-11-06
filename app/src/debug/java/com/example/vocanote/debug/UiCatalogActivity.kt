package com.example.vocanote.debug

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.vocanote.core.model.PartOfSpeech
import com.example.vocanote.core.model.ReviewSettings
import com.example.vocanote.core.model.SavedWord
import com.example.vocanote.core.model.WordMeaning
import com.example.vocanote.core.model.StudyStats
import com.example.vocanote.features.editor.presentation.WordEditorScreen
import com.example.vocanote.features.home.presentation.HomeScreen
import com.example.vocanote.features.library.presentation.LibraryScreen
import com.example.vocanote.features.review.presentation.ReviewScreen
import com.example.vocanote.features.review.presentation.ReviewSessionViewModel
import com.example.vocanote.features.review.domain.ReviewPresentation
import com.example.vocanote.features.review.domain.ReviewScope
import com.example.vocanote.core.model.ReviewMode
import com.example.vocanote.features.settings.presentation.SettingsScreen
import com.example.vocanote.ui.UserProfile
import com.example.vocanote.ui.navigation.BottomNavDestination
import com.example.vocanote.ui.navigation.VocaBottomNavigationBar
import com.example.vocanote.ui.theme.VocaNoteTheme
import java.time.Instant
import java.time.temporal.ChronoUnit

class UiCatalogActivity : ComponentActivity() {
    private var requestedScreen by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        requestedScreen = intent.requestedScreen()
        setContent {
            VocaNoteTheme {
                CatalogScreen(screen = requestedScreen)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedScreen = intent.requestedScreen()
    }

}

private const val SCREEN_EXTRA = "screen"

private fun Intent.requestedScreen(): String = getStringExtra(SCREEN_EXTRA).orEmpty()

@Composable
private fun CatalogScreen(screen: String) {
    val destination = when (screen) {
        "library" -> BottomNavDestination.Library
        "review", "worksheet", "worksheet-results", "meaning-quiz", "crossword" -> BottomNavDestination.Review
        "settings" -> BottomNavDestination.Settings
        "editor", "new-editor" -> null
        else -> BottomNavDestination.Home
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                destination?.let {
                    VocaBottomNavigationBar(
                        selectedDestination = it,
                        onDestinationSelected = {}
                    )
                }
            },
            floatingActionButton = {
                if (destination == BottomNavDestination.Home || destination == BottomNavDestination.Library) {
                    ExtendedFloatingActionButton(
                        onClick = {},
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("단어 추가") }
                    )
                }
            }
        ) { padding ->
            val modifier = Modifier.fillMaxSize().padding(padding)
            when (screen) {
                "library" -> LibraryScreen(
                    words = sampleWords,
                    isLoading = false,
                    onOpenWord = {},
                    modifier = modifier
                )
                "review", "worksheet", "worksheet-results", "meaning-quiz", "crossword" -> ReviewScreen(
                    session = remember(screen) {
                        ReviewSessionViewModel().apply {
                            if (screen != "review") {
                                start(sampleWords, when (screen) {
                                    "meaning-quiz" -> ReviewMode.MeaningWriting
                                    "crossword" -> ReviewMode.Crossword
                                    else -> ReviewMode.Writing
                                }, if (screen == "meaning-quiz") ReviewPresentation.Single else ReviewPresentation.Batch, ReviewScope.All, if (screen == "crossword") 5 else 10)
                                if (screen == "crossword") state.crossword?.entries?.take(2)?.forEach { fillCrossword(it.wordId, it.answer) }
                                state.questions.forEachIndexed { index, question ->
                                    if (screen == "worksheet-results" || screen == "worksheet" && index < 3) input(question.wordId, if (index == 0) "recall" else question.answer)
                                }
                                if (screen == "worksheet-results") {
                                    checkBatch()
                                    save { _, _, _, complete -> complete(null) }
                                }
                            }
                        }
                    },
                    words = sampleWords,
                    settings = sampleSettings,
                    onSaveSession = { _, _, _, complete -> complete(null) },
                    onAddWord = {},
                    modifier = modifier
                )
                "settings" -> SettingsScreen(
                    user = sampleUser,
                    settings = sampleSettings,
                    isSaving = false,
                    appVersion = "v1.2.2",
                    onUpdateSettings = {},
                    onSignOut = {},
                    modifier = modifier
                )
                "editor" -> WordEditorScreen(
                    existingWord = sampleWords.first(),
                    allWords = sampleWords,
                    isSaving = false,
                    onBack = {},
                    onSave = { _, onSuccess -> onSuccess() },
                    onDelete = {},
                    modifier = modifier
                )
                "new-editor" -> WordEditorScreen(
                    existingWord = null,
                    allWords = sampleWords,
                    isSaving = false,
                    onBack = {},
                    onSave = { _, onSuccess -> onSuccess() },
                    onDelete = null,
                    modifier = modifier
                )
                else -> HomeScreen(
                    user = sampleUser,
                    words = sampleWords,
                    reviewSettings = sampleSettings,
                    studyStats = sampleStats,
                    isLoading = false,
                    onOpenReview = {},
                    onOpenLibrary = {},
                    onOpenWord = {},
                    modifier = modifier
                )
            }
        }
    }
}

private val sampleUser = UserProfile(
    name = "Voca 학습자",
    email = "learner@example.com",
    photoUrl = null
)

private val sampleSettings = ReviewSettings(
    sessionSize = 10,
    dailyGoal = 15
)

private val sampleStats = StudyStats(
    currentStreakDays = 6,
    reviewedTodayCount = 8,
    studyDaysLast7 = 5,
    accuracyLast7Days = 0.82f
)

private val now: Instant = Instant.now()

private val sampleWords = listOf(
    sampleWord("record", "기록", PartOfSpeech.Noun, correct = 6, incorrect = 1).copy(
        meanings = listOf(WordMeaning("기록, 음반", PartOfSpeech.Noun), WordMeaning("기록하다", PartOfSpeech.Verb)),
        example = "Please record your ideas.", note = "명사와 동사로 모두 사용할 수 있어요."
    ),
    sampleWord("resilient", "회복력이 강한, 탄력 있는", PartOfSpeech.Adjective, correct = 6, incorrect = 1),
    sampleWord("subtle", "미묘한, 감지하기 어려운", PartOfSpeech.Adjective, correct = 3, incorrect = 2),
    sampleWord("elaborate", "정교한; 상세히 설명하다", PartOfSpeech.Adjective, correct = 1, incorrect = 2),
    sampleWord("concise", "간결한", PartOfSpeech.Adjective, correct = 4, incorrect = 1),
    sampleWord("vivid", "생생한, 선명한", PartOfSpeech.Adjective),
    sampleWord("coherent", "일관성 있는, 논리적인", PartOfSpeech.Adjective, correct = 2, incorrect = 1),
    sampleWord("pragmatic", "실용적인, 현실적인", PartOfSpeech.Adjective, correct = 1, incorrect = 1),
    sampleWord("nuance", "미묘한 차이", PartOfSpeech.Noun, correct = 5),
    sampleWord("in time", "제시간에", PartOfSpeech.Phrase),
    sampleWord("give up", "포기하다", PartOfSpeech.PhrasalVerb),
    sampleWord("break the ice", "어색함을 깨다", PartOfSpeech.Idiom),
    sampleWord("ASAP", "가능한 한 빨리", PartOfSpeech.Abbreviation),
    sampleWord("embrace", "받아들이다", PartOfSpeech.Verb),
    sampleWord("curiosity", "호기심", PartOfSpeech.Noun),
    sampleWord("flourish", "번창하다", PartOfSpeech.Verb),
    sampleWord("tranquil", "고요한", PartOfSpeech.Adjective),
    sampleWord("endeavor", "노력", PartOfSpeech.Noun),
    sampleWord("appreciate", "감사하다", PartOfSpeech.Verb),
    sampleWord("perspective", "관점", PartOfSpeech.Noun)
)

private fun sampleWord(
    word: String,
    meaning: String,
    partOfSpeech: PartOfSpeech,
    correct: Int = 0,
    incorrect: Int = 0
) = SavedWord(
    id = word,
    word = word,
    meaning = meaning,
    partOfSpeech = partOfSpeech,
    example = "A clear example makes the meaning easier to remember.",
    note = "문장에서 쓰이는 느낌을 함께 기억하기",
    synonyms = if (word == "resilient") listOf("tough", "durable", "adaptable") else emptyList(),
    antonyms = if (word == "resilient") listOf("fragile", "vulnerable") else emptyList(),
    derivatives = if (word == "resilient") listOf("resilience", "resiliently") else emptyList(),
    confusableWords = if (word == "resilient") listOf("resistant", "resolute") else emptyList(),
    collocations = if (word == "resilient") listOf("highly resilient", "bounce back") else emptyList(),
    correctCount = correct,
    incorrectCount = incorrect,
    createdAt = now.minus((word.length % 5).toLong(), ChronoUnit.DAYS),
    lastReviewedAt = if (correct + incorrect == 0) null else now.minus(2, ChronoUnit.DAYS)
)
