package com.example.vocanote.core.model

import java.time.Clock
import java.time.Instant
import java.time.LocalDate

data class ReviewSessionSummary(
    val completedAt: Instant,
    val questionCount: Int,
    val correctCount: Int
)

data class StudyStats(
    val currentStreakDays: Int = 0,
    val reviewedTodayCount: Int = 0,
    val studyDaysLast7: Int = 0,
    val accuracyLast7Days: Float = 0f
)

fun buildStudyStats(
    sessions: List<ReviewSessionSummary>,
    clock: Clock = Clock.systemDefaultZone()
): StudyStats {
    val today = LocalDate.now(clock)
    val byDate = sessions.groupBy { it.completedAt.atZone(clock.zone).toLocalDate() }
    val streakStart = when {
        today in byDate -> today
        today.minusDays(1) in byDate -> today.minusDays(1)
        else -> null
    }
    var streak = 0
    var cursor = streakStart
    while (cursor != null && cursor in byDate) {
        streak += 1
        cursor = cursor.minusDays(1)
    }

    val weekStart = today.minusDays(6)
    val recent = sessions.filter {
        !it.completedAt.atZone(clock.zone).toLocalDate().isBefore(weekStart)
    }
    val questionCount = recent.sumOf { it.questionCount }

    return StudyStats(
        currentStreakDays = streak,
        reviewedTodayCount = byDate[today].orEmpty().sumOf { it.questionCount },
        studyDaysLast7 = byDate.keys.count { !it.isBefore(weekStart) && !it.isAfter(today) },
        accuracyLast7Days = if (questionCount == 0) 0f else {
            recent.sumOf { it.correctCount }.toFloat() / questionCount
        }
    )
}
