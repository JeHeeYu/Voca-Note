package com.example.vocanote.features.editor.domain

import com.example.vocanote.core.model.SavedWord
import com.example.vocanote.core.model.WordDraft
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WordValidatorTest {
    @Test
    fun `blank word and meaning are rejected`() {
        val result = validateWordDraft(WordDraft(" ", ""), emptyList())

        assertFalse(result.isValid)
        assertTrue(result.wordError != null)
        assertTrue(result.meaningError != null)
    }

    @Test
    fun `duplicate check ignores case and surrounding spaces`() {
        val existing = listOf(savedWord(id = "one", word = "Resilient"))

        val result = validateWordDraft(
            draft = WordDraft(" resilient ", "회복력이 강한"),
            existingWords = existing
        )

        assertFalse(result.isValid)
        assertTrue(result.wordError?.contains("이미") == true)
    }

    @Test
    fun `editing word does not conflict with itself`() {
        val existing = listOf(savedWord(id = "one", word = "Resilient"))

        val result = validateWordDraft(
            draft = WordDraft("resilient", "회복력이 강한"),
            existingWords = existing,
            editingWordId = "one"
        )

        assertTrue(result.isValid)
        assertNull(result.wordError)
    }

    private fun savedWord(id: String, word: String) = SavedWord(
        id = id,
        word = word,
        meaning = "뜻"
    )
}
