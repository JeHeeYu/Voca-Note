package com.example.vocanote.ui

import android.content.Context
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.vocanote.auth.AuthScreen
import com.example.vocanote.features.add.presentation.AddWordPage
import com.example.vocanote.features.search.presentation.SearchPage
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
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Words) }
    var selectedDestination by remember { mutableStateOf(BottomNavDestination.Words) }
    var isSigningIn by remember { mutableStateOf(false) }
    var isSignedIn by remember { mutableStateOf(auth.currentUser != null) }

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
