package com.example.vocanote.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewSettingsTest {
    @Test
    fun `normalization keeps every setting inside supported bounds`() {
        val normalized = ReviewSettings(
            sessionSize = -1,
            dailyGoal = 999
        ).normalized()

        assertEquals(ReviewSettings.MIN_SESSION_SIZE, normalized.sessionSize)
        assertEquals(ReviewSettings.MAX_DAILY_GOAL, normalized.dailyGoal)
    }

    @Test
    fun `normalization preserves settings already in range`() {
        val settings = ReviewSettings(sessionSize = 12, dailyGoal = 30)

        assertEquals(settings, settings.normalized())
    }
}
