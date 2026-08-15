package com.example.vocanote.ui

import com.example.vocanote.core.model.ReviewSettings
import com.example.vocanote.core.model.SavedWord
import com.example.vocanote.core.model.StudyStats

data class UserProfile(
    val name: String,
    val email: String,
    val photoUrl: String?
) {
    val firstName: String
        get() = name.trim().substringBefore(' ').ifBlank { "학습자" }

    val initial: String
        get() = firstName.firstOrNull()?.uppercase() ?: "V"
}

data class VocaNoteUiState(
    val isSignedIn: Boolean = false,
    val user: UserProfile? = null,
    val words: List<SavedWord> = emptyList(),
    val reviewSettings: ReviewSettings = ReviewSettings(),
    val studyStats: StudyStats = StudyStats(),
    val isLoadingWords: Boolean = false,
    val isSigningIn: Boolean = false,
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null
)
