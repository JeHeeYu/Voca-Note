package com.example.vocanote.features.review.domain

import com.example.vocanote.core.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import kotlin.random.Random

class ReviewSelectionTest {
    private val now = Instant.parse("2026-10-31T00:00:00Z")
    private val pool = (0..39).map { i -> SavedWord(
        id = "$i", word = "word${'a' + i / 26}${'a' + i % 26}", meaning = "뜻 $i",
        correctCount = if (i < 20) 10 else 0,
        createdAt = if (i < 20) now.minusSeconds(900000) else now,
        nextReviewAt = if (i < 20) now.plusSeconds(900000) else null
    ) }

    @Test fun `all modes give old learned words the same chance as newly added words`() {
        for (mode in ReviewMode.entries) {
            val neutral = pool.map { it.copy(correctCount = 0, createdAt = now, nextReviewAt = null) }
            fun ids(words: List<SavedWord>) = buildReviewQuestions(words, mode, 10, Random(17), now = now).map { it.wordId }
            assertEquals(ids(neutral), ids(pool))
            assertTrue(ids(pool).any { it.toInt() < 20 })
            assertTrue(ids(pool).any { it.toInt() >= 20 })
            assertEquals(10, ids(pool).distinct().size)
        }
    }

    @Test fun `due and mistake scopes retain priority and question count is bounded`() {
        for (scope in listOf(ReviewScope.Due, ReviewScope.Mistakes)) {
            assertTrue(buildReviewQuestions(pool, ReviewMode.Writing, 10, Random(17), scope = scope, now = now).all { it.wordId.toInt() >= 20 })
        }
        assertEquals(30, buildReviewQuestions(pool, ReviewMode.Writing, 100).size)
        assertTrue(buildReviewQuestions(pool, ReviewMode.Writing, -1).isEmpty())
    }

    @Test fun `a scoped word uses distractors from the whole library and excludes all its own meanings`() {
        val target = SavedWord("record", "record", "old", meanings = listOf(WordMeaning("기록", PartOfSpeech.Noun), WordMeaning("기록하다", PartOfSpeech.Verb)))
        val others = listOf(SavedWord("other", "other", "기록하다"), SavedWord("different", "different", "다른"))
        val questions = buildReviewQuestions(others + target, ReviewMode.MultipleChoice, 10, Random(2), targets = listOf(target))
        val question = questions.single()
        assertEquals(setOf(question.answer, "다른"), question.options.toSet())
        assertEquals(target.meanings.first { it.meaning == question.answer }.partOfSpeech, question.partOfSpeech)
        assertTrue(buildReviewQuestions(listOf(target), ReviewMode.MultipleChoice, 10).isEmpty())
        val writing = buildReviewQuestions(listOf(target), ReviewMode.Writing, 1, Random(2)).single()
        assertEquals(target.meanings.first { it.meaning == writing.prompt }.partOfSpeech, writing.partOfSpeech)
        val listening = buildReviewQuestions(listOf(target), ReviewMode.Listening, 1).single()
        assertNull(listening.partOfSpeech)
    }

    @Test fun `batch grading rejects blank answers and grades a complete set once`() {
        val questions = buildReviewQuestions(pool, ReviewMode.Writing, 20, Random(3))
        val inputs = questions.associate { it.wordId to " ${it.answer.uppercase()} " }
        assertNull(gradeBatch(questions, inputs - questions.last().wordId))
        assertNull(gradeBatch(questions, inputs + (questions.last().wordId to " ")))
        assertEquals(20, gradeBatch(questions, inputs)!!.values.count { it })
        assertEquals(19, gradeBatch(questions, inputs + (questions.last().wordId to "wrong"))!!.values.count { it })
    }
}
