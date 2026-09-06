package com.example.vocanote.features.editor.domain

import com.example.vocanote.core.model.SavedWord
import com.example.vocanote.core.model.WordDraft

data class WordValidationResult(
    val wordError: String? = null,
    val meaningError: String? = null,
    val exampleError: String? = null,
    val noteError: String? = null,
    val synonymsError: String? = null,
    val derivativesError: String? = null
) {
    val isValid: Boolean
        get() = listOf(
            wordError,
            meaningError,
            exampleError,
            noteError,
            synonymsError,
            derivativesError
        ).all { it == null }
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
        },
        exampleError = clean.example.takeIf { it.length > 500 }
            ?.let { "예문은 500자 이내로 입력해 주세요." },
        noteError = clean.note.takeIf { it.length > 1000 }
            ?.let { "설명은 1,000자 이내로 입력해 주세요." },
        synonymsError = validateTerms(clean.synonyms, "동의어"),
        derivativesError = validateTerms(clean.derivatives, "파생어")
    )
}

private fun validateTerms(terms: List<String>, label: String): String? = when {
    terms.size > 20 -> "${label}는 최대 20개까지 입력할 수 있어요."
    terms.any { it.length > 80 } -> "${label}는 항목마다 80자 이내로 입력해 주세요."
    else -> null
}
