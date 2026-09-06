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

    @Test
    fun `related terms are trimmed deduplicated and keep their order`() {
        val normalized = WordDraft(
            word = "resilient",
            meaning = "회복력이 강한",
            synonyms = listOf(" tough ", "Tough", "durable", ""),
            antonyms = listOf(" fragile ", "Fragile", "vulnerable"),
            derivatives = listOf(" resilience ", "resiliently"),
            confusableWords = listOf(" resistant ", "resolute"),
            collocations = listOf(" highly resilient ", "bounce back")
        ).normalized()

        assertTrue(normalized.synonyms == listOf("tough", "durable"))
        assertTrue(normalized.antonyms == listOf("fragile", "vulnerable"))
        assertTrue(normalized.derivatives == listOf("resilience", "resiliently"))
        assertTrue(normalized.confusableWords == listOf("resistant", "resolute"))
        assertTrue(normalized.collocations == listOf("highly resilient", "bounce back"))
    }

    @Test
    fun `too many related terms are rejected before saving`() {
        val result = validateWordDraft(
            draft = WordDraft(
                word = "resilient",
                meaning = "회복력이 강한",
                synonyms = (1..21).map { "synonym-$it" }
            ),
            existingWords = emptyList()
        )

        assertFalse(result.isValid)
        assertTrue(result.synonymsError?.contains("20개") == true)
    }

    @Test
    fun `every related term category is validated`() {
        val longTerm = "a".repeat(81)
        val result = validateWordDraft(
            draft = WordDraft(
                word = "resilient",
                meaning = "회복력이 강한",
                antonyms = listOf(longTerm),
                derivatives = listOf(longTerm),
                confusableWords = listOf(longTerm),
                collocations = listOf(longTerm)
            ),
            existingWords = emptyList()
        )

        assertFalse(result.isValid)
        assertTrue(result.antonymsError != null)
        assertTrue(result.derivativesError != null)
        assertTrue(result.confusableWordsError != null)
        assertTrue(result.collocationsError != null)
    }

    private fun savedWord(id: String, word: String) = SavedWord(
        id = id,
        word = word,
        meaning = "뜻"
    )
}
