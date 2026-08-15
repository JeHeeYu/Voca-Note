package com.example.vocanote.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vocanote.core.model.ReviewAnswer
import com.example.vocanote.core.model.ReviewMode
import com.example.vocanote.core.model.ReviewSettings
import com.example.vocanote.core.model.WordDraft
import com.example.vocanote.features.editor.presentation.WordEditorScreen
import com.example.vocanote.features.home.presentation.HomeScreen
import com.example.vocanote.features.library.presentation.LibraryScreen
import com.example.vocanote.features.review.presentation.ReviewScreen
import com.example.vocanote.features.settings.presentation.SettingsScreen
import com.example.vocanote.ui.VocaNoteUiState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VocaNoteNavigation(
    state: VocaNoteUiState,
    appVersion: String,
    snackbarHostState: SnackbarHostState,
    onAddWord: (WordDraft, () -> Unit) -> Unit,
    onUpdateWord: (String, WordDraft, () -> Unit) -> Unit,
    onDeleteWord: (String, () -> Unit) -> Unit,
    onUpdateSettings: (ReviewSettings) -> Unit,
    onSaveReviewSession: (ReviewMode, List<ReviewAnswer>) -> Unit,
    onSignOut: () -> Unit
) {
    val user = state.user ?: return
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedDestination = BottomNavDestination.entries.firstOrNull { it.route == currentRoute }
    val showMainNavigation = selectedDestination != null
    val showAddButton = currentRoute == AppRoute.Home || currentRoute == AppRoute.Library

    fun openMainDestination(destination: BottomNavDestination) {
        if (destination == BottomNavDestination.Home) {
            navController.popBackStack(AppRoute.Home, inclusive = false)
            return
        }
        navController.navigate(destination.route) {
            popUpTo(AppRoute.Home)
            launchSingleTop = true
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showMainNavigation) {
                VocaBottomNavigationBar(
                    selectedDestination = selectedDestination ?: BottomNavDestination.Home,
                    onDestinationSelected = ::openMainDestination
                )
            }
        },
        floatingActionButton = {
            if (showAddButton) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(AppRoute.editor()) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("단어 추가") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            composable(AppRoute.Home) {
                HomeScreen(
                    modifier = Modifier.testTag("screen_home"),
                    user = user,
                    words = state.words,
                    reviewSettings = state.reviewSettings,
                    isLoading = state.isLoadingWords,
                    studyStats = state.studyStats,
                    onOpenReview = { openMainDestination(BottomNavDestination.Review) },
                    onOpenLibrary = { openMainDestination(BottomNavDestination.Library) },
                    onOpenWord = { navController.navigate(AppRoute.editor(it.id)) }
                )
            }
            composable(AppRoute.Library) {
                LibraryScreen(
                    modifier = Modifier.testTag("screen_library"),
                    words = state.words,
                    isLoading = state.isLoadingWords,
                    onOpenWord = { navController.navigate(AppRoute.editor(it.id)) }
                )
            }
            composable(AppRoute.Review) {
                ReviewScreen(
                    modifier = Modifier.testTag("screen_review"),
                    words = state.words,
                    settings = state.reviewSettings,
                    onSaveSession = onSaveReviewSession,
                    onAddWord = { navController.navigate(AppRoute.editor()) }
                )
            }
            composable(AppRoute.Settings) {
                SettingsScreen(
                    modifier = Modifier.testTag("screen_settings"),
                    user = user,
                    settings = state.reviewSettings,
                    isSaving = state.isSaving,
                    appVersion = appVersion,
                    onUpdateSettings = onUpdateSettings,
                    onSignOut = onSignOut
                )
            }
            composable(
                route = AppRoute.EditorPattern,
                arguments = listOf(navArgument(AppRoute.WordIdArgument) { type = NavType.StringType })
            ) { entry ->
                val wordId = entry.arguments?.getString(AppRoute.WordIdArgument)
                val existingWord = state.words.firstOrNull { it.id == wordId }
                WordEditorScreen(
                    modifier = Modifier.testTag("screen_editor"),
                    existingWord = existingWord,
                    allWords = state.words,
                    isSaving = state.isSaving,
                    onBack = navController::popBackStack,
                    onSave = { draft ->
                        if (wordId == AppRoute.NewWordId) {
                            onAddWord(draft, navController::popBackStack)
                        } else if (wordId != null) {
                            onUpdateWord(wordId, draft, navController::popBackStack)
                        }
                    },
                    onDelete = if (existingWord == null) null else {
                        { onDeleteWord(existingWord.id, navController::popBackStack) }
                    }
                )
            }
        }
    }
}
