package com.example.vocanote.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.example.vocanote.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(
    context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val appContext = context.applicationContext
    private val credentialManager = CredentialManager.create(appContext)

    suspend fun signIn(context: Context): Result<FirebaseUser> = try {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(appContext.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val credential = credentialManager.getCredential(context, request).credential
        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)

        Result.success(
            requireNotNull(auth.signInWithCredential(firebaseCredential).await().user) {
                "Google sign-in completed without a Firebase user."
            }
        )
    } catch (error: NoCredentialException) {
        Result.failure(error)
    } catch (error: Exception) {
        Result.failure(error)
    }

    suspend fun signOut() {
        auth.signOut()
        runCatching {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }
    }
}
