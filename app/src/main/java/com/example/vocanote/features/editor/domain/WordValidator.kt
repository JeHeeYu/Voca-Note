package com.example.vocanote.features.editor.domain

import com.example.vocanote.core.model.SavedWord
import com.example.vocanote.core.model.WordDraft

data class WordValidationResult(
    val wordError: String? = null,
    val meaningError: String? = null
) {
    val isValid: Boolean
        get() = wordError == null && meaningError == null
}

fun validateWordDraft(
    draft: WordDraft,
    existingWords: List<SavedWord>,
    editingWordId: String? = null
): WordValidationResult {
    val clean = draft.normalized()
    val duplicate = existingWords.any {
        it.id != editingWordId && it.word.trim().equals(clean.word, ignoreCase = true)
    }

    return WordValidationResult(
        wordError = when {
            clean.word.isBlank() -> "영어 단어를 입력해 주세요."
            clean.word.length > 80 -> "단어는 80자 이내로 입력해 주세요."
            duplicate -> "이미 단어장에 있는 단어예요."
            else -> null
        },
        meaningError = when {
            clean.meaning.isBlank() -> "뜻을 입력해 주세요."
            clean.meaning.length > 300 -> "뜻은 300자 이내로 입력해 주세요."
            else -> null
        }
    )
}
