package com.example.vocanote.ui

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vocanote.auth.AuthScreen
import com.example.vocanote.auth.GoogleAuthClient
import com.example.vocanote.ui.navigation.VocaNoteNavigation
import kotlinx.coroutines.launch

@Composable
fun VocaNoteApp(
    viewModel: VocaNoteViewModel = viewModel(
        factory = remember { VocaNoteViewModel.Factory() }
    )
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val authClient = remember(context) { GoogleAuthClient(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val appVersion = remember(context) { context.findAppVersionLabel() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeSnackbarMessage()
        }
    }

    if (!state.isSignedIn) {
        Box(modifier = Modifier.fillMaxSize()) {
            AuthScreen(
                isLoading = state.isSigningIn,
                onGoogleSignIn = {
                    if (state.isSigningIn) return@AuthScreen
                    viewModel.setSigningIn(true)
                    scope.launch {
                        authClient.signIn(context)
                            .onSuccess(viewModel::onSignedIn)
                            .onFailure { viewModel.onSignInFailed() }
                    }
                }
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
            )
        }
        return
    }

    VocaNoteNavigation(
        state = state,
        appVersion = appVersion,
        snackbarHostState = snackbarHostState,
        onAddWord = viewModel::addWord,
        onUpdateWord = viewModel::updateWord,
        onDeleteWord = viewModel::deleteWord,
        onUpdateSettings = viewModel::updateReviewSettings,
        onSaveReviewSession = viewModel::saveReviewSession,
        onSignOut = {
            scope.launch {
                authClient.signOut()
                viewModel.onSignedOut()
            }
        }
    )
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
