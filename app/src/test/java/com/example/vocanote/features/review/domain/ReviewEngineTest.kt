package com.example.vocanote.features.review.domain

import com.example.vocanote.core.model.ReviewMode
import com.example.vocanote.core.model.SavedWord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ReviewEngineTest {
    private val words = listOf(
        word("1", "resilient", "회복력이 강한"),
        word("2", "concise", "간결한"),
        word("3", "elaborate", "정교한"),
        word("4", "subtle", "미묘한"),
        word("5", "vivid", "생생한")
    )

    @Test
    fun `multiple choice includes the answer once and up to three distractors`() {
        val questions = buildReviewQuestions(
            words = words,
            mode = ReviewMode.MultipleChoice,
            targetCount = 3,
            random = Random(7)
        )

        assertEquals(3, questions.size)
        questions.forEach { question ->
            assertEquals(1, question.options.count { it == question.answer })
            assertEquals(question.options.distinct().size, question.options.size)
            assertTrue(question.options.size <= 4)
        }
    }

    @Test
    fun `writing mode asks for the English word and has no options`() {
        val question = buildReviewQuestions(
            words = words,
            mode = ReviewMode.Writing,
            targetCount = 1,
            random = Random(3)
        ).single()
        val source = words.first { it.id == question.wordId }

        assertEquals(source.meaning, question.prompt)
        assertEquals(source.word, question.answer)
        assertTrue(question.options.isEmpty())
    }

    @Test
    fun `writing mode hides the answer inside the example`() {
        val source = word("1", "resilient", "회복력이 강한").copy(
            example = "She remained resilient under pressure."
        )

        val question = buildReviewQuestions(
            words = listOf(source),
            mode = ReviewMode.Writing,
            targetCount = 1,
            random = Random(3)
        ).single()

        assertEquals("She remained _____ under pressure.", question.context)
    }

    @Test
    fun `recall mode reveals meaning and keeps pronunciation text`() {
        val source = word("1", "resilient", "회복력이 강한").copy(note = "다시 일어서는 힘")

        val question = buildReviewQuestions(
            words = listOf(source),
            mode = ReviewMode.Recall,
            targetCount = 1
        ).single()

        assertEquals(source.word, question.prompt)
        assertEquals(source.meaning, question.answer)
        assertEquals(source.note, question.note)
        assertEquals(source.word, question.spokenText)
    }

    @Test
    fun `listening mode never exposes the answer before checking`() {
        val question = buildReviewQuestions(
            words = words,
            mode = ReviewMode.Listening,
            targetCount = 1,
            random = Random(4)
        ).single()

        assertEquals("소리를 듣고 단어를 입력하세요", question.prompt)
        assertEquals(question.answer, question.spokenText)
        assertFalse(question.context.contains(question.answer, ignoreCase = true))
        assertTrue(question.options.isEmpty())
    }

    @Test
    fun `answer comparison trims whitespace and ignores case`() {
        assertTrue(answersMatch("  Resilient ", "resilient"))
        assertFalse(answersMatch("resilience", "resilient"))
    }

    private fun word(id: String, value: String, meaning: String) = SavedWord(
        id = id,
        word = value,
        meaning = meaning
    )
}
