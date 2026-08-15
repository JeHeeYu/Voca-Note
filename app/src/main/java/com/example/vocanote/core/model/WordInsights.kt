package com.example.vocanote.core.model

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.util.Locale

data class WordInsights(
    val totalCount: Int,
    val reviewedTodayCount: Int,
    val mistakeCount: Int,
    val dueWords: List<SavedWord>
)

fun buildWordInsights(
    words: List<SavedWord>,
    clock: Clock = Clock.systemDefaultZone()
): WordInsights {
    val today = LocalDate.now(clock)
    val now = Instant.now(clock)
    val dueWords = words
        .filter { it.isDueForReview(now) }
        .sortedWith(
            compareBy<SavedWord> { it.nextReviewAt ?: Instant.EPOCH }
                .thenBy { it.accuracy }
                .thenBy { it.word.lowercase(Locale.ROOT) }
        )

    return WordInsights(
        totalCount = words.size,
        reviewedTodayCount = words.count { it.wasReviewedOn(today, clock.zone) },
        mistakeCount = words.count { it.needsMistakePractice() },
        dueWords = dueWords
    )
}

fun SavedWord.isDueForReview(now: Instant = Instant.now()): Boolean {
    val dueAt = nextReviewAt ?: return true
    return !dueAt.isAfter(now)
}

fun SavedWord.needsMistakePractice(): Boolean = incorrectCount > 0 && accuracy < 0.8f
