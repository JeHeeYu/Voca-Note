package com.example.vocanote.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PartOfSpeechTest {
    @Test
    fun `traditional English parts of speech are all available`() {
        assertEquals(
            listOf("명사", "대명사", "동사", "형용사", "부사", "전치사", "접속사", "감탄사"),
            PartOfSpeech.entries.map(PartOfSpeech::label)
        )
        assertEquals(
            listOf("n", "pron", "v", "adj", "adv", "prep", "conj", "interj"),
            PartOfSpeech.entries.map(PartOfSpeech::abbreviation)
        )
    }

    @Test
    fun `stored values round trip and unknown values stay unselected`() {
        PartOfSpeech.entries.forEach { partOfSpeech ->
            assertEquals(
                partOfSpeech,
                PartOfSpeech.fromStorageValue(partOfSpeech.storageValue)
            )
        }
        assertNull(PartOfSpeech.fromStorageValue(null))
        assertNull(PartOfSpeech.fromStorageValue("determiner"))
    }
}
