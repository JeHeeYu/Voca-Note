package com.example.vocanote.core.model

data class ReviewSettings(
    val sessionSize: Int = DEFAULT_SESSION_SIZE,
    val dailyGoal: Int = DEFAULT_DAILY_GOAL
) {
    fun normalized(): ReviewSettings = copy(
        sessionSize = sessionSize.coerceIn(MIN_SESSION_SIZE, MAX_SESSION_SIZE),
        dailyGoal = dailyGoal.coerceIn(MIN_DAILY_GOAL, MAX_DAILY_GOAL)
    )

    companion object {
        const val MIN_SESSION_SIZE = 5
        const val MAX_SESSION_SIZE = 30
        const val DEFAULT_SESSION_SIZE = 10
        const val MIN_DAILY_GOAL = 5
        const val MAX_DAILY_GOAL = 100
        const val DEFAULT_DAILY_GOAL = 15
    }
}

enum class ReviewMode(val storageValue: String) {
    Recall("recall"),
    MultipleChoice("multiple_choice"),
    Writing("writing"),
    Listening("listening")
}

data class ReviewAnswer(
    val wordId: String,
    val isCorrect: Boolean,
    val nextReviewAt: java.time.Instant,
    val reviewIntervalDays: Int,
    val reviewStreak: Int
)
