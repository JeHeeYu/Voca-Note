package com.example.vocanote.features.review.domain

import com.example.vocanote.core.model.*
import com.example.vocanote.features.review.presentation.*
import org.junit.Assert.*
import org.junit.Test
import java.text.Normalizer

class LearningGamesTest {
    private val meanings = listOf("기록, 음반", "기록하다", "잘 자라다; 번창하다", "관점/시각")
    private val record = SavedWord("record", "record", "기록, 음반", meanings = meanings.map { WordMeaning(it) })
    private val words = listOf("apple", "pear", "grape", "peach", "lemon", "melon", "lime", "plum", "in time").map { SavedWord(it, it, "뜻 $it") }

    @Test fun `any meaning accepts whitespace and normalized Hangul but not partial or unrelated answers`() {
        listOf("기 록", "음\t반", "기록 하다", "잘\u00a0자라다", "번창하다", "시각", "기록,음반", Normalizer.normalize("기록하다", Normalizer.Form.NFD)).forEach { assertTrue(it, meaningAnswersMatch(it, meanings)) }
        listOf("", " \n ", "기록하", "잘", "오답, 음반", "기록합니다").forEach { assertFalse(it, meaningAnswersMatch(it, meanings)) }
    }
    @Test fun `single and batch share meaning grading including legacy flat records`() {
        val q = buildReviewQuestions(listOf(record), ReviewMode.MeaningWriting, 1).single()
        assertEquals("record", q.prompt)
        assertEquals("", q.context)
        assertTrue(questionMatches("기 록 하 다", q, ReviewMode.MeaningWriting))
        assertEquals(mapOf("record" to true), gradeBatch(listOf(q), mapOf("record" to "음반"), ReviewMode.MeaningWriting))
        val legacy = buildReviewQuestions(listOf(record.copy(meanings = emptyList())), ReviewMode.MeaningWriting, 1).single()
        assertTrue(questionMatches("음반", legacy, ReviewMode.MeaningWriting))
        assertFalse(questionMatches("rec ord", q, ReviewMode.Writing))
    }
    @Test fun `crosswords are connected without adjacent undeclared runs`() {
        for (offset in words.indices) {
            val pool = words.drop(offset) + words.take(offset)
            val puzzle = buildCrossword(pool.map { it.id to it.word }, 8)
            assertTrue(puzzle.entries.size in 5..8)
            assertTrue(puzzle.rows <= 25 && puzzle.cols <= 25)
            val board = mutableMapOf<String, Char>()
            puzzle.entries.forEach { entry -> entryCells(entry).forEach { (key, letter) -> assertEquals(letter, board[key] ?: letter); board[key] = letter } }
            for (direction in CrosswordDirection.entries) for (row in 0 until puzzle.rows) for (col in 0 until puzzle.cols) {
                val dr = if (direction == CrosswordDirection.Down) 1 else 0
                val dc = if (direction == CrosswordDirection.Across) 1 else 0
                if (cellKey(row, col) !in board || cellKey(row - dr, col - dc) in board) continue
                var answer = ""
                var i = 0
                while (cellKey(row + dr*i, col + dc*i) in board) { answer += board[cellKey(row + dr*i, col + dc*i)]; i++ }
                if (answer.length > 1) assertTrue(puzzle.entries.any { it.row == row && it.col == col && it.direction == direction && it.answer == answer })
            }
            val seen = mutableSetOf(puzzle.entries.first().wordId)
            var changed = true
            while (changed) { changed = false; for (entry in puzzle.entries) if (entry.wordId !in seen && puzzle.entries.any { other -> other.wordId in seen && entryCells(entry).any { cell -> entryCells(other).any { it.first == cell.first } } }) { seen.add(entry.wordId); changed = true } }
            assertEquals(puzzle.entries.size, seen.size)
        }
    }
    @Test fun `unsupported and disconnected words never produce a broken puzzle`() {
        assertEquals("INTIME", crosswordAnswer(" in-time "))
        assertEquals("CANT", crosswordAnswer("can't"))
        listOf("a", "word123", "한글", "x".repeat(19)).forEach { assertEquals("", crosswordAnswer(it)) }
        assertTrue(buildCrossword(listOf("a" to "AAA", "b" to "BBB"), 5).entries.isEmpty())
        assertTrue(buildCrossword(words.map { it.id to it.word }, 0).entries.isEmpty())
        val puzzle = buildCrossword(words.map { it.id to it.word } + ("duplicate" to "APPLE"), 30)
        assertEquals(puzzle.entries.size, puzzle.entries.map { it.answer }.distinct().size)
    }
    @Test fun `crossword cells persist through selection grading and save retry`() {
        val session = ReviewSessionViewModel()
        session.start(words, ReviewMode.Crossword, ReviewPresentation.Single, ReviewScope.All, 5)
        assertEquals(ReviewPresentation.Batch, session.state.presentation)
        assertFalse(session.checkBatch())
        val puzzle = session.state.crossword!!
        puzzle.entries.forEach { session.fillCrossword(it.wordId, it.answer) }
        assertTrue(crosswordComplete(puzzle, session.state.crosswordCells))
        puzzle.entries.forEach { assertEquals(it.answer, crosswordInputs(puzzle, session.state.crosswordCells)[it.wordId]) }
        assertTrue(session.checkBatch())
        assertFalse(session.checkBatch())
        assertTrue(session.state.answers.all { it.isCorrect })
        val before = session.state.crosswordCells
        session.fillCrossword(puzzle.entries.first().wordId, "X".repeat(puzzle.entries.first().answer.length))
        assertEquals(before, session.state.crosswordCells)
        var calls = 0
        val ids = mutableSetOf<String>()
        val save: SaveReviewSession = { id, mode, _, complete -> ids.add(id); assertEquals(ReviewMode.Crossword, mode); calls++; complete(if(calls==1) "offline" else null) }
        session.save(save)
        assertFalse(session.state.saved)
        session.save(save)
        session.save(save)
        assertTrue(session.state.saved)
        assertEquals(1, ids.size)
        assertEquals(2, calls)
    }
}
