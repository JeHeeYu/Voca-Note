package com.example.vocanote.core.model

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class StudyStatsTest {
    private val clock = Clock.fixed(
        Instant.parse("2026-08-15T12:00:00Z"),
        ZoneId.of("Asia/Seoul")
    )

    @Test
    fun `stats combine sessions and keep a streak through today`() {
        val sessions = listOf(
            session("2026-08-15T03:00:00Z", questions = 5, correct = 4),
            session("2026-08-15T05:00:00Z", questions = 3, correct = 2),
            session("2026-08-14T03:00:00Z", questions = 2, correct = 2),
            session("2026-08-13T03:00:00Z", questions = 4, correct = 2),
            session("2026-08-11T03:00:00Z", questions = 20, correct = 20)
        )

        val stats = buildStudyStats(sessions, clock)

        assertEquals(3, stats.currentStreakDays)
        assertEquals(8, stats.reviewedTodayCount)
        assertEquals(4, stats.studyDaysLast7)
        assertEquals(30f / 34f, stats.accuracyLast7Days, 0.0001f)
    }

    @Test
    fun `yesterday keeps the streak alive when today has no session`() {
        val sessions = listOf(
            session("2026-08-14T03:00:00Z", questions = 4, correct = 3),
            session("2026-08-13T03:00:00Z", questions = 4, correct = 3)
        )

        assertEquals(2, buildStudyStats(sessions, clock).currentStreakDays)
    }

    private fun session(timestamp: String, questions: Int, correct: Int) = ReviewSessionSummary(
        completedAt = Instant.parse(timestamp),
        questionCount = questions,
        correctCount = correct
    )
}
