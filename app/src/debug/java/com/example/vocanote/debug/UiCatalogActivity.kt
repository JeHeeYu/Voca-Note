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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.vocanote.core.model.ReviewSettings
import com.example.vocanote.core.model.SavedWord
import com.example.vocanote.core.model.StudyStats
import com.example.vocanote.features.editor.presentation.WordEditorScreen
import com.example.vocanote.features.home.presentation.HomeScreen
import com.example.vocanote.features.library.presentation.LibraryScreen
import com.example.vocanote.features.review.presentation.ReviewScreen
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
        "review" -> BottomNavDestination.Review
        "settings" -> BottomNavDestination.Settings
        "editor" -> null
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
                "review" -> ReviewScreen(
                    words = sampleWords,
                    settings = sampleSettings,
                    onSaveSession = { _, _ -> },
                    onAddWord = {},
                    modifier = modifier
                )
                "settings" -> SettingsScreen(
                    user = sampleUser,
                    settings = sampleSettings,
                    isSaving = false,
                    appVersion = "v1.0",
                    onUpdateSettings = {},
                    onSignOut = {},
                    modifier = modifier
                )
                "editor" -> WordEditorScreen(
                    existingWord = sampleWords.first(),
                    allWords = sampleWords,
                    isSaving = false,
                    onBack = {},
                    onSave = {},
                    onDelete = {},
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
    name = "유제희",
    email = "sees1234558@gmail.com",
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
    sampleWord("resilient", "회복력이 강한, 탄력 있는", correct = 6, incorrect = 1),
    sampleWord("subtle", "미묘한, 감지하기 어려운", correct = 3, incorrect = 2),
    sampleWord("elaborate", "정교한; 상세히 설명하다", correct = 1, incorrect = 2),
    sampleWord("concise", "간결한", correct = 4, incorrect = 1),
    sampleWord("vivid", "생생한, 선명한"),
    sampleWord("coherent", "일관성 있는, 논리적인", correct = 2, incorrect = 1),
    sampleWord("pragmatic", "실용적인, 현실적인", correct = 1, incorrect = 1),
    sampleWord("nuance", "미묘한 차이", correct = 5)
)

private fun sampleWord(
    word: String,
    meaning: String,
    correct: Int = 0,
    incorrect: Int = 0
) = SavedWord(
    id = word,
    word = word,
    meaning = meaning,
    example = "A clear example makes the meaning easier to remember.",
    note = "문장에서 쓰이는 느낌을 함께 기억하기",
    correctCount = correct,
    incorrectCount = incorrect,
    createdAt = now.minus((word.length % 5).toLong(), ChronoUnit.DAYS),
    lastReviewedAt = if (correct + incorrect == 0) null else now.minus(2, ChronoUnit.DAYS)
)
