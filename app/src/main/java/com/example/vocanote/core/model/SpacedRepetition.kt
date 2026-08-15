package com.example.vocanote.core.model

import java.time.Clock
import java.time.Duration
import java.time.Instant

data class ReviewSchedule(
    val nextReviewAt: Instant,
    val intervalDays: Int,
    val reviewStreak: Int
)

fun SavedWord.scheduleAfterReview(
    remembered: Boolean,
    clock: Clock = Clock.systemUTC()
): ReviewSchedule {
    val now = Instant.now(clock)
    if (!remembered) {
        return ReviewSchedule(
            nextReviewAt = now.plus(RELEARNING_DELAY),
            intervalDays = 0,
            reviewStreak = 0
        )
    }

    val nextInterval = when {
        reviewIntervalDays <= 0 -> 1
        reviewIntervalDays == 1 -> 3
        else -> (reviewIntervalDays * 2).coerceAtMost(MAX_INTERVAL_DAYS)
    }
    return ReviewSchedule(
        nextReviewAt = now.plus(Duration.ofDays(nextInterval.toLong())),
        intervalDays = nextInterval,
        reviewStreak = reviewStreak + 1
    )
}

fun SavedWord.toReviewAnswer(
    remembered: Boolean,
    clock: Clock = Clock.systemUTC()
): ReviewAnswer {
    val schedule = scheduleAfterReview(remembered, clock)
    return ReviewAnswer(
        wordId = id,
        isCorrect = remembered,
        nextReviewAt = schedule.nextReviewAt,
        reviewIntervalDays = schedule.intervalDays,
        reviewStreak = schedule.reviewStreak
    )
}

private val RELEARNING_DELAY: Duration = Duration.ofMinutes(10)
private const val MAX_INTERVAL_DAYS = 180
