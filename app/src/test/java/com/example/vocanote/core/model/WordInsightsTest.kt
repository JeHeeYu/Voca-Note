package com.example.vocanote.core.model

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordInsightsTest {
    private val now = Instant.parse("2026-08-15T03:00:00Z")
    private val clock = Clock.fixed(now, ZoneId.of("Asia/Seoul"))

    @Test
    fun `insights count reviews mistakes and due words`() {
        val words = listOf(
            word(
                id = "due",
                incorrectCount = 2,
                nextReviewAt = now.minusSeconds(1)
            ),
            word(
                id = "later",
                correctCount = 4,
                lastReviewedAt = Instant.parse("2026-08-15T02:00:00Z"),
                nextReviewAt = now.plusSeconds(60)
            )
        )

        val insights = buildWordInsights(words, clock)

        assertEquals(2, insights.totalCount)
        assertEquals(1, insights.reviewedTodayCount)
        assertEquals(1, insights.mistakeCount)
        assertEquals(listOf("due"), insights.dueWords.map { it.id })
    }

    @Test
    fun `word without schedule is due for its first review`() {
        assertTrue(word("unscheduled").isDueForReview(now))
    }

    @Test
    fun `scheduled word becomes due at its exact review time`() {
        val word = word(id = "scheduled", nextReviewAt = now)

        assertFalse(word.isDueForReview(now.minusNanos(1)))
        assertTrue(word.isDueForReview(now))
    }

    @Test
    fun `mistake practice excludes recovered high accuracy words`() {
        assertTrue(word("weak", correctCount = 2, incorrectCount = 1).needsMistakePractice())
        assertFalse(word("recovered", correctCount = 9, incorrectCount = 1).needsMistakePractice())
    }

    private fun word(
        id: String,
        correctCount: Int = 0,
        incorrectCount: Int = 0,
        lastReviewedAt: Instant? = null,
        nextReviewAt: Instant? = null
    ) = SavedWord(
        id = id,
        word = id,
        meaning = "뜻",
        correctCount = correctCount,
        incorrectCount = incorrectCount,
        lastReviewedAt = lastReviewedAt,
        nextReviewAt = nextReviewAt
    )
}
