package com.example.vocanote.core.model

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class SpacedRepetitionTest {
    private val now = Instant.parse("2026-08-15T03:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `forgotten answer resets progress and returns in ten minutes`() {
        val schedule = word(intervalDays = 12, streak = 5).scheduleAfterReview(
            remembered = false,
            clock = clock
        )

        assertEquals(0, schedule.intervalDays)
        assertEquals(0, schedule.reviewStreak)
        assertEquals(now.plus(Duration.ofMinutes(10)), schedule.nextReviewAt)
    }

    @Test
    fun `remembered answers expand from one to three days and then double`() {
        val first = word(intervalDays = 0, streak = 0).scheduleAfterReview(true, clock)
        val second = word(intervalDays = 1, streak = 1).scheduleAfterReview(true, clock)
        val third = word(intervalDays = 3, streak = 2).scheduleAfterReview(true, clock)

        assertEquals(1, first.intervalDays)
        assertEquals(3, second.intervalDays)
        assertEquals(6, third.intervalDays)
        assertEquals(3, third.reviewStreak)
    }

    @Test
    fun `review interval is capped at one hundred eighty days`() {
        val schedule = word(intervalDays = 120, streak = 8).scheduleAfterReview(true, clock)

        assertEquals(180, schedule.intervalDays)
        assertEquals(now.plus(Duration.ofDays(180)), schedule.nextReviewAt)
    }

    private fun word(intervalDays: Int, streak: Int) = SavedWord(
        id = "word",
        word = "resilient",
        meaning = "회복력이 강한",
        reviewIntervalDays = intervalDays,
        reviewStreak = streak
    )
}
