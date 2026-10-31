package com.example.vocanote.core.model

import com.example.vocanote.features.editor.domain.validateWordDraft
import org.junit.Assert.*
import org.junit.Test

class WordMeaningTest {
    private val entries = listOf(WordMeaning("기록", PartOfSpeech.Noun), WordMeaning("기록하다", PartOfSpeech.Verb))

    @Test fun `web meanings round trip with the first entry mirrored for old clients`() {
        val draft = WordDraft(" record ", "old", PartOfSpeech.Adjective, meanings = entries).normalized()
        assertEquals("record", draft.word)
        assertEquals("기록", draft.meaning)
        assertEquals(PartOfSpeech.Noun, draft.partOfSpeech)
        assertEquals(entries, decodeMeanings(draft.meanings.toStorageMeanings(), "old", "adjective"))
    }

    @Test fun `legacy commas remain intact until explicitly split`() {
        val legacy = decodeMeanings(null, "기록, 음반", "noun")
        assertEquals(1, legacy.size)
        assertEquals(listOf(WordMeaning("기록", PartOfSpeech.Noun), WordMeaning("음반", PartOfSpeech.Noun)), splitMeaningEntries(legacy, 0))
        val full = List(MAX_MEANINGS) { legacy.first() }
        assertEquals(full, splitMeaningEntries(full, 0))
    }

    @Test fun `every meaning is validated including blank later entries and limits`() {
        fun validate(values: List<WordMeaning>) = validateWordDraft(WordDraft("record", "old", meanings = values), emptyList())
        assertTrue(validate(entries).isValid)
        assertTrue(validate(entries + WordMeaning()).meaningErrors.containsKey(2))
        assertFalse(validate(entries + WordMeaning("x".repeat(301))).isValid)
        assertTrue(validate(List(10) { WordMeaning("뜻") }).isValid)
        assertFalse(validate(List(11) { WordMeaning("뜻") }).isValid)
    }

    @Test fun `summary includes every meaning and part of speech`() {
        val word = SavedWord("record", "record", "old", meanings = entries)
        assertEquals("명사 (n) 기록 · 동사 (v) 기록하다", word.meaningSummary)
        assertEquals(PartOfSpeech.Phrase, decodeMeanings(listOf(mapOf("meaning" to "제시간에", "partOfSpeech" to "phrase")), "", null).single().partOfSpeech)
    }
}
