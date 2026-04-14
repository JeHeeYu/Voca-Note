package com.example.vocanote.ui

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.vocanote.auth.AuthScreen
import com.example.vocanote.features.add.presentation.AddWordPage
import com.example.vocanote.features.profile.presentation.ProfilePage
import com.example.vocanote.features.search.presentation.SearchPage
import com.example.vocanote.features.words.data.FirestoreWordsRepository
import com.example.vocanote.features.words.data.SavedWord
import com.example.vocanote.features.words.presentation.WordListPage
import com.example.vocanote.features.words.presentation.WordsPage
import com.example.vocanote.ui.navigation.BottomNavDestination
import com.example.vocanote.ui.navigation.VocaBottomNavigationBar
import com.example.vocanote.ui.theme.Canvas
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private sealed interface AppScreen {
    data object Words : AppScreen
    data object List : AppScreen
    data object Review : AppScreen
    data object Profile : AppScreen
    data object AddWord : AppScreen
}

@Composable
fun VocaNoteApp() {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val repository = remember { FirestoreWordsRepository() }
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val scope = rememberCoroutineScope()
    val appVersion = remember(context) { context.findAppVersionLabel() }

    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Words) }
    var selectedDestination by remember { mutableStateOf(BottomNavDestination.Words) }
    var isSigningIn by remember { mutableStateOf(false) }
    var isSignedIn by remember { mutableStateOf(auth.currentUser != null) }
    var isWordsLoading by remember { mutableStateOf(auth.currentUser != null) }
    var isSavingWord by remember { mutableStateOf(false) }
    var wordsErrorMessage by remember { mutableStateOf<String?>(null) }
    var savedWords by remember { mutableStateOf<List<SavedWord>>(emptyList()) }

    val currentUser = auth.currentUser
    val userId = currentUser?.uid

    DisposableEffect(userId, isSignedIn) {
        if (!isSignedIn || userId == null) {
            savedWords = emptyList()
            isWordsLoading = false
            wordsErrorMessage = null
            onDispose { }
        } else {
            isWordsLoading = true
            val registration = repository.observeWords(
                userId = userId,
                onSuccess = { words ->
                    savedWords = words
                    isWordsLoading = false
                    wordsErrorMessage = null
                },
                onError = {
                    isWordsLoading = false
                    wordsErrorMessage = "단어를 불러오지 못했어요."
                }
            )
            onDispose { registration.remove() }
        }
    }

    if (!isSignedIn) {
        AuthScreen(
            isLoading = isSigningIn,
            onGoogleSignIn = {
                if (isSigningIn) return@AuthScreen
                isSigningIn = true
                scope.launch {
                    isSignedIn = signInWithGoogle(
                        context = context,
                        credentialManager = credentialManager,
                        auth = auth
                    )
                    isWordsLoading = isSignedIn
                    isSigningIn = false
                }
            }
        )
        return
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
                            BottomNavDestination.Profile -> AppScreen.Profile
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            AppScreen.Words -> WordsPage(
                modifier = Modifier.padding(innerPadding),
                words = savedWords.map { it.word to it.meaning },
                onAddWord = { currentScreen = AppScreen.AddWord },
                onOpenReview = {
                    selectedDestination = BottomNavDestination.Search
                    currentScreen = AppScreen.Review
                },
                isLoading = isWordsLoading,
                helperMessage = wordsErrorMessage
            )

            AppScreen.List -> WordListPage(
                modifier = Modifier.padding(innerPadding),
                words = savedWords.map { it.word to it.meaning },
                isLoading = isWordsLoading,
                helperMessage = wordsErrorMessage
            )

            AppScreen.Review -> SearchPage(
                modifier = Modifier.padding(innerPadding),
                wordCount = savedWords.size
            )

            AppScreen.Profile -> ProfilePage(
                modifier = Modifier.padding(innerPadding),
                userName = currentUser?.displayName ?: "내 계정",
                userEmail = currentUser?.email ?: "로그인된 계정",
                appVersion = appVersion,
                onSignOut = {
                    if (isSigningIn) return@ProfilePage
                    isSigningIn = true
                    scope.launch {
                        signOut(
                            credentialManager = credentialManager,
                            auth = auth
                        )
                        selectedDestination = BottomNavDestination.Words
                        currentScreen = AppScreen.Words
                        savedWords = emptyList()
                        wordsErrorMessage = null
                        isWordsLoading = false
                        isSignedIn = false
                        isSigningIn = false
                    }
                }
            )

            AppScreen.AddWord -> AddWordPage(
                modifier = Modifier.padding(innerPadding),
                onBack = { currentScreen = AppScreen.Words },
                isSaving = isSavingWord,
                helperMessage = wordsErrorMessage,
                onSave = { word, meaning ->
                    if (userId == null || isSavingWord) return@AddWordPage
                    isSavingWord = true
                    wordsErrorMessage = null
                    scope.launch {
                        runCatching {
                            repository.addWord(
                                userId = userId,
                                word = word,
                                meaning = meaning
                            )
                        }.onSuccess {
                            selectedDestination = BottomNavDestination.Words
                            currentScreen = AppScreen.Words
                        }.onFailure {
                            wordsErrorMessage = "단어 저장에 실패했어요."
                        }
                        isSavingWord = false
                    }
                }
            )
        }
    }
}

private fun Context.findAppVersionLabel(): String {
    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0)
    }

    return "v${packageInfo.versionName ?: "1.0"}"
}

private suspend fun signOut(
    credentialManager: CredentialManager,
    auth: FirebaseAuth
) {
    auth.signOut()
    try {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    } catch (_: Exception) {
    }
}

private suspend fun signInWithGoogle(
    context: Context,
    credentialManager: CredentialManager,
    auth: FirebaseAuth
): Boolean {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId("822705486170-6pr84nm3tceeqie79ad2luhc1n1iuqcf.apps.googleusercontent.com")
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        val googleIdTokenCredential = GoogleIdTokenCredential
            .createFrom(credential.data)

        val firebaseCredential = GoogleAuthProvider.getCredential(
            googleIdTokenCredential.idToken,
            null
        )

        auth.signInWithCredential(firebaseCredential).await().user != null
    } catch (_: GetCredentialException) {
        false
    } catch (_: GoogleIdTokenParsingException) {
        false
    } catch (_: Exception) {
        false
    }
}
