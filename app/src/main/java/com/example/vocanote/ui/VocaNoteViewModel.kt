package com.example.vocanote.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vocanote.core.data.FirestoreVocaRepository
import com.example.vocanote.core.data.Subscription
import com.example.vocanote.core.data.VocaRepository
import com.example.vocanote.core.model.ReviewAnswer
import com.example.vocanote.core.model.ReviewMode
import com.example.vocanote.core.model.ReviewSettings
import com.example.vocanote.core.model.WordDraft
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VocaNoteViewModel(
    private val auth: FirebaseAuth,
    private val repository: VocaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(VocaNoteUiState())
    val uiState: StateFlow<VocaNoteUiState> = _uiState.asStateFlow()

    private var wordsSubscription: Subscription? = null
    private var settingsSubscription: Subscription? = null
    private var statsSubscription: Subscription? = null

    init {
        auth.currentUser?.let(::onSignedIn)
    }

    fun setSigningIn(isSigningIn: Boolean) {
        _uiState.update { it.copy(isSigningIn = isSigningIn) }
    }

    fun onSignInFailed() {
        _uiState.update {
            it.copy(
                isSigningIn = false,
                snackbarMessage = "로그인을 완료하지 못했어요. 다시 시도해 주세요."
            )
        }
    }

    fun onSignedIn(user: FirebaseUser) {
        clearSubscriptions()
        _uiState.update {
            it.copy(
                isSignedIn = true,
                isSigningIn = false,
                isLoadingWords = true,
                user = user.toUserProfile(),
                snackbarMessage = null
            )
        }

        wordsSubscription = repository.observeWords(
            userId = user.uid,
            onSuccess = { words ->
                _uiState.update { it.copy(words = words, isLoadingWords = false) }
            },
            onError = {
                _uiState.update {
                    it.copy(
                        isLoadingWords = false,
                        snackbarMessage = "단어장을 불러오지 못했어요. 네트워크를 확인해 주세요."
                    )
                }
            }
        )
        settingsSubscription = repository.observeReviewSettings(
            userId = user.uid,
            onSuccess = { settings ->
                _uiState.update { it.copy(reviewSettings = settings) }
            },
            onError = {
                _uiState.update {
                    it.copy(snackbarMessage = "연습 설정을 불러오지 못했어요.")
                }
            }
        )
        statsSubscription = repository.observeStudyStats(
            userId = user.uid,
            onSuccess = { stats -> _uiState.update { it.copy(studyStats = stats) } },
            onError = {
                _uiState.update { it.copy(snackbarMessage = "학습 기록을 불러오지 못했어요.") }
            }
        )
    }

    fun onSignedOut() {
        clearSubscriptions()
        _uiState.value = VocaNoteUiState()
    }

    fun addWord(draft: WordDraft, onSuccess: () -> Unit) {
        mutate(
            successMessage = "단어장에 저장했어요.",
            onSuccess = onSuccess
        ) { userId -> repository.addWord(userId, draft) }
    }

    fun updateWord(wordId: String, draft: WordDraft, onSuccess: () -> Unit) {
        mutate(
            successMessage = "수정한 내용을 저장했어요.",
            onSuccess = onSuccess
        ) { userId -> repository.updateWord(userId, wordId, draft) }
    }

    fun deleteWord(wordId: String, onSuccess: () -> Unit) {
        mutate(
            successMessage = "단어를 삭제했어요.",
            onSuccess = onSuccess
        ) { userId -> repository.deleteWord(userId, wordId) }
    }

    fun updateReviewSettings(settings: ReviewSettings) {
        if (_uiState.value.isSaving) return
        val previous = _uiState.value.reviewSettings
        _uiState.update { it.copy(reviewSettings = settings.normalized()) }
        mutate(
            showBusy = true,
            onFailure = { _uiState.update { it.copy(reviewSettings = previous) } }
        ) { userId -> repository.updateReviewSettings(userId, settings) }
    }

    fun saveReviewSession(mode: ReviewMode, answers: List<ReviewAnswer>) {
        mutate(showBusy = false, successMessage = "오늘의 연습 기록을 저장했어요.") { userId ->
            repository.saveReviewSession(userId, mode, answers)
        }
    }

    fun consumeSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun mutate(
        showBusy: Boolean = true,
        successMessage: String? = null,
        failureMessage: String? = null,
        onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
        block: suspend (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        if (showBusy && _uiState.value.isSaving) return
        if (showBusy) _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            runCatching { block(userId) }
                .onSuccess {
                    _uiState.update { it.copy(snackbarMessage = successMessage) }
                    onSuccess()
                }
                .onFailure { error ->
                    onFailure(error)
                    _uiState.update {
                        it.copy(snackbarMessage = failureMessage ?: error.toFriendlyMessage())
                    }
                }
            if (showBusy) _uiState.update { it.copy(isSaving = false) }
        }
    }

    private fun clearSubscriptions() {
        wordsSubscription?.cancel()
        settingsSubscription?.cancel()
        statsSubscription?.cancel()
        wordsSubscription = null
        settingsSubscription = null
        statsSubscription = null
    }

    override fun onCleared() {
        clearSubscriptions()
        super.onCleared()
    }

    class Factory(
        private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
        private val repository: VocaRepository = FirestoreVocaRepository()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(VocaNoteViewModel::class.java))
            return VocaNoteViewModel(auth, repository) as T
        }
    }
}

private fun FirebaseUser.toUserProfile() = UserProfile(
    name = displayName?.trim().orEmpty().ifBlank { "Voca Note 학습자" },
    email = email?.trim().orEmpty(),
    photoUrl = photoUrl?.toString()
)

private fun Throwable.toFriendlyMessage(): String = when (this) {
    is TimeoutCancellationException -> "요청 시간이 초과됐어요. 다시 시도해 주세요."
    else -> "저장하지 못했어요. 잠시 후 다시 시도해 주세요."
}
