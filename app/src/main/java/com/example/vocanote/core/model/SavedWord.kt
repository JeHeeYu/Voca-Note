package com.example.vocanote.core.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

data class SavedWord(
    val id: String,
    val word: String,
    val meaning: String,
    val partOfSpeech: PartOfSpeech? = null,
    val example: String = "",
    val note: String = "",
    val synonyms: List<String> = emptyList(),
    val derivatives: List<String> = emptyList(),
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val reviewStreak: Int = 0,
    val reviewIntervalDays: Int = 0,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val lastReviewedAt: Instant? = null,
    val nextReviewAt: Instant? = null
) {
    val attemptCount: Int
        get() = correctCount + incorrectCount

    val accuracy: Float
        get() = if (attemptCount == 0) 0f else correctCount.toFloat() / attemptCount

    fun wasCreatedOn(date: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): Boolean =
        createdAt?.atZone(zoneId)?.toLocalDate() == date

    fun wasReviewedOn(date: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): Boolean =
        lastReviewedAt?.atZone(zoneId)?.toLocalDate() == date
}

data class WordDraft(
    val word: String,
    val meaning: String,
    val partOfSpeech: PartOfSpeech? = null,
    val example: String = "",
    val note: String = "",
    val synonyms: List<String> = emptyList(),
    val derivatives: List<String> = emptyList()
) {
    fun normalized(): WordDraft = copy(
        word = word.trim(),
        meaning = meaning.trim(),
        example = example.trim(),
        note = note.trim(),
        synonyms = synonyms.normalizedTerms(),
        derivatives = derivatives.normalizedTerms()
    )
}

private fun List<String>.normalizedTerms(): List<String> = map(String::trim)
    .filter(String::isNotBlank)
    .distinctBy { it.lowercase(Locale.ROOT) }
