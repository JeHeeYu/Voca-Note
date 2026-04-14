package com.example.vocanote.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.vocanote.features.add.presentation.AddWordPage
import com.example.vocanote.features.search.presentation.SearchPage
import com.example.vocanote.features.words.presentation.WordListPage
import com.example.vocanote.features.words.presentation.WordsPage
import com.example.vocanote.ui.navigation.BottomNavDestination
import com.example.vocanote.ui.navigation.VocaBottomNavigationBar
import com.example.vocanote.ui.theme.Canvas

private data class WordEntry(
    val word: String,
    val meaning: String
)

private sealed interface AppScreen {
    data object Words : AppScreen
    data object List : AppScreen
    data object Review : AppScreen
    data object AddWord : AppScreen
}

private val WordEntryListSaver = listSaver(
    save = { words -> words.flatMap { listOf(it.word, it.meaning) } },
    restore = { raw ->
        raw.chunked(2).mapNotNull { chunk ->
            if (chunk.size == 2) WordEntry(chunk[0], chunk[1]) else null
        }
    }
)

@Composable
fun VocaNoteApp() {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Words) }
    var selectedDestination by remember { mutableStateOf(BottomNavDestination.Words) }
    var words by rememberSaveable(stateSaver = WordEntryListSaver) {
        mutableStateOf(
            listOf(
                WordEntry("Apple", "사과"),
                WordEntry("Brisk", "활기찬"),
                WordEntry("Calm", "차분한"),
                WordEntry("Dream", "꿈"),
                WordEntry("Focus", "집중")
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Canvas,
        contentColor = Color.Unspecified,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (currentScreen != AppScreen.AddWord) {
                VocaBottomNavigationBar(
                    selectedDestination = selectedDestination,
                    onDestinationSelected = {
                        selectedDestination = it
                        currentScreen = when (it) {
                            BottomNavDestination.Words -> AppScreen.Words
                            BottomNavDestination.List -> AppScreen.List
                            BottomNavDestination.Search -> AppScreen.Review
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            AppScreen.Words -> WordsPage(
                modifier = Modifier.padding(innerPadding),
                words = words.map { it.word to it.meaning },
                onAddWord = { currentScreen = AppScreen.AddWord },
                onOpenReview = {
                    selectedDestination = BottomNavDestination.Search
                    currentScreen = AppScreen.Review
                }
            )

            AppScreen.List -> WordListPage(
                modifier = Modifier.padding(innerPadding),
                words = words.map { it.word to it.meaning }
            )

            AppScreen.Review -> SearchPage(
                modifier = Modifier.padding(innerPadding),
                wordCount = words.size
            )

            AppScreen.AddWord -> AddWordPage(
                modifier = Modifier.padding(innerPadding),
                onBack = { currentScreen = AppScreen.Words },
                onSave = { word, meaning ->
                    words = (words + WordEntry(word.trim(), meaning.trim()))
                        .sortedBy { it.word.lowercase() }
                    selectedDestination = BottomNavDestination.Words
                    currentScreen = AppScreen.Words
                }
            )
        }
    }
}
